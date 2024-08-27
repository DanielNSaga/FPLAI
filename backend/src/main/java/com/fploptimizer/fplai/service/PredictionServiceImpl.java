package com.fploptimizer.fplai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fploptimizer.fplai.model.Event;
import com.fploptimizer.fplai.model.Fixture;
import com.fploptimizer.fplai.model.Player;
import com.fploptimizer.fplai.model.PlayerData;
import com.fploptimizer.fplai.model.PlayerTeam;
import com.fploptimizer.fplai.repository.PlayerRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation for handling player prediction operations.
 * This service is responsible for generating player predictions using machine learning models,
 * retrieving these predictions from a Python-based API, and storing them in the database.
 */
@Service
public class PredictionServiceImpl implements PredictionService {

    private static final Logger logger = LoggerFactory.getLogger(PredictionServiceImpl.class);

    private final FixtureService fixtureService;
    private final PlayerRepository playerRepository;
    private final EventService eventService;
    private final PlayerTeamService playerTeamService;

    /**
     * Constructor-based dependency injection.
     *
     * @param fixtureService   the service for retrieving fixture data
     * @param playerRepository the repository for player data
     * @param eventService     the service for retrieving event data
     * @param playerTeamService the service for handling player team operations
     */
    public PredictionServiceImpl(FixtureService fixtureService, PlayerRepository playerRepository,
                                 EventService eventService, PlayerTeamService playerTeamService) {
        this.fixtureService = fixtureService;
        this.playerRepository = playerRepository;
        this.eventService = eventService;
        this.playerTeamService = playerTeamService;
    }

    /**
     * Converts an element type (position) code to its corresponding string representation.
     *
     * @param elementType the integer code representing the player's position
     * @return a string representing the player's position
     */
    private String convertElementTypeToPosition(int elementType) {
        return switch (elementType) {
            case 1 -> "GK";
            case 2 -> "DEF";
            case 3 -> "MID";
            case 4 -> "FWD";
            default -> throw new IllegalArgumentException("Invalid elementType: " + elementType);
        };
    }

    /**
     * Converts a Player entity into a PlayerData object, calculating various features for predictions.
     *
     * @param player       the player entity to convert
     * @param currentEvent the current event (gameweek)
     * @return a PlayerData object populated with features for prediction
     */
    private PlayerData convertToPlayerData(Player player, Event currentEvent) {
        PlayerData playerData = new PlayerData(player.getId());
        String position = convertElementTypeToPosition(player.getElementType());
        playerData.setPosition(position);

        playerData.addFeature("avg_points", player.getTotalPoints() / (double) currentEvent.getGameweek());
        playerData.addFeature("avg_bonus", player.getBonus() / (double) currentEvent.getGameweek());
        playerData.addFeature("avg_minutes_played", player.getMinutes() / (double) currentEvent.getGameweek());
        playerData.addFeature("avg_bps", player.getBps() / (double) currentEvent.getGameweek());

        switch (position) {
            case "GK" -> {
                playerData.addFeature("avg_saves", player.getSaves() / (double) currentEvent.getGameweek());
                playerData.addFeature("avg_goals_conceded", player.getGoalsConceded() / (double) currentEvent.getGameweek());
                playerData.addFeature("avg_penalties_saved", player.getPenaltiesSaved() / (double) currentEvent.getGameweek());
                playerData.addFeature("avg_clean_sheets", player.getCleanSheets() / (double) currentEvent.getGameweek());
            }
            case "DEF" -> {
                playerData.addFeature("avg_clean_sheets", player.getCleanSheets() / (double) currentEvent.getGameweek());
                playerData.addFeature("avg_goals_conceded", player.getGoalsConceded() / (double) currentEvent.getGameweek());
            }
            case "MID", "FWD" -> {
                playerData.addFeature("avg_goals_scored", player.getGoalsScored() / (double) currentEvent.getGameweek());
                playerData.addFeature("avg_assists", player.getAssists() / (double) currentEvent.getGameweek());

                if (isNumeric(player.getCreativity())) {
                    playerData.addFeature("creativity", parseDoubleOrDefault(player.getCreativity()));
                }
                if (isNumeric(player.getIctIndex())) {
                    playerData.addFeature("ict_index", parseDoubleOrDefault(player.getIctIndex()));
                }
                if (isNumeric(player.getThreat())) {
                    playerData.addFeature("threat", parseDoubleOrDefault(player.getThreat()));
                }
            }
        }

        if (isNumeric(player.getInfluence())) {
            playerData.addFeature("influence", parseDoubleOrDefault(player.getInfluence()));
        }
        playerData.addFeature("selected", parseDoubleOrDefault(player.getSelectedByPercent()) * 0.01 * currentEvent.getTotalPlayers());
        playerData.addFeature("transfers_balance", player.getTransfersIn() - player.getTransfersOut());
        playerData.addFeature("transfers_in", player.getTransfersIn());
        playerData.addFeature("transfers_out", player.getTransfersOut());
        playerData.addFeature("value", player.getNowCost());

        return playerData;
    }

    /**
     * Parses a string to a double, with a default value of 0.0 if parsing fails.
     *
     * @param value the string to parse
     * @return the parsed double value or 0.0 if parsing fails
     */
    private double parseDoubleOrDefault(String value) {
        if (value != null && !value.trim().isEmpty()) {
            value = value.trim();
            if (value.endsWith("%")) {
                value = value.substring(0, value.length() - 1);
            }
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                logger.error("Error parsing value: {}", value, e);
            }
        }
        return 0.0;
    }

    /**
     * Checks if a given string can be parsed as a numeric value.
     *
     * @param value the string to check
     * @return true if the string is numeric, false otherwise
     */
    private boolean isNumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Creates a list of PlayerData objects representing the features needed for predictions.
     *
     * @param player       the player for whom predictions are being generated
     * @param fixtures     the list of upcoming fixtures for the player
     * @param currentEvent the current event (gameweek)
     * @return a list of PlayerData objects containing features for predictions
     */
    private List<PlayerData> createPredictionInputs(Player player, List<Fixture> fixtures, Event currentEvent) {
        logger.debug("Creating prediction inputs for player: {} with {} fixtures", player.getId(), fixtures.size());
        List<PlayerData> predictionInputs = new ArrayList<>();
        PlayerData basePlayerData = convertToPlayerData(player, currentEvent);

        PlayerTeam playerTeam = player.getTeam();

        for (Fixture fixture : fixtures) {
            try {
                PlayerData playerData = new PlayerData(player.getId());
                playerData.setPosition(basePlayerData.getPosition());
                playerData.getFeatures().putAll(basePlayerData.getFeatures());

                PlayerTeam awayTeam = fixture.getAwayteam();
                PlayerTeam homeTeam = fixture.getHometeam();

                if (awayTeam == null || homeTeam == null) {
                    logger.warn("Fixture {} has null teams, skipping fixture for player ID: {}", fixture.getId(), player.getId());
                    continue;
                }

                PlayerTeam opponentTeam = awayTeam.equals(playerTeam) ? homeTeam : awayTeam;
                playerData.addFeature("opponent_difficulty", playerTeamService.calculateTeamStrength(opponentTeam, fixture.getGw()));
                playerData.addFeature("team_ranking", playerTeamService.calculateTeamStrength(playerTeam, fixture.getGw()));
                playerData.addFeature("was_home", playerTeamService.isHomeGame(playerTeam, fixture.getGw()) ? 1 : 0);
                playerData.addFeature("form_last_5", playerTeamService.calculateTeamForm(playerTeam, fixture.getGw()));

                predictionInputs.add(playerData);
            } catch (Exception e) {
                logger.error("Error while creating prediction inputs for fixture {} for player ID: {}", fixture.getId(), player.getId(), e);
            }
        }

        logger.debug("Created {} prediction inputs for player: {}", predictionInputs.size(), player.getId());
        return predictionInputs;
    }

    /**
     * Sends a request to the Python-based prediction service and retrieves predictions.
     *
     * @param playerDataList a list of PlayerData objects to be sent to the prediction service
     * @return a list of prediction values (doubles) corresponding to the input data
     * @throws Exception if an error occurs during the HTTP request or JSON parsing
     */
    public List<Double> getPredictionsFromPython(List<PlayerData> playerDataList) throws Exception {
        URL url = new URL("http://localhost:5000/predict");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);

        ObjectMapper objectMapper = new ObjectMapper();

        // Format playerDataList to match the expected structure for the Python service
        List<Map<String, Object>> formattedPlayerData = playerDataList.stream().map(playerData -> {
            Map<String, Object> playerMap = new HashMap<>();
            playerMap.put("position", playerData.getPosition());
            playerMap.put("features", playerData.getFeatures());

            return playerMap;
        }).collect(Collectors.toList());

        String jsonInputString = objectMapper.writeValueAsString(formattedPlayerData);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            return objectMapper.readValue(response.toString(), new TypeReference<>() {
            });
        } catch (Exception e) {
            logger.error("Error while getting predictions from Python service", e);
            throw e;
        }
    }

    /**
     * Generates and saves player predictions.
     * This method gathers player data, fetches predictions from a Python service,
     * and updates the player records with these predictions.
     */
    @Override
    public void generateAndSavePlayerPredictions() {
        List<PlayerData> allPlayerData = new ArrayList<>();
        Event currentEvent;
        int currentGw;

        try {
            currentEvent = eventService.getCurrentEvent();
            currentGw = currentEvent.getGameweek();

            List<Player> players = playerRepository.findAll();

            for (Player player : players) {
                MDC.put("playerId", String.valueOf(player.getId()));

                PlayerTeam team = player.getTeam();
                List<Fixture> nextFixtures = fixtureService.getNextFixtures(team, currentGw);

                List<PlayerData> playerData = createPredictionInputs(player, nextFixtures, currentEvent);
                allPlayerData.addAll(playerData);

                try {
                    List<Double> predictions = getPredictionsFromPython(playerData);
                    for (int i = 0; i < playerData.size(); i++) {
                        playerData.get(i).setPrediction(predictions.get(i));
                    }
                } catch (Exception e) {
                    logger.error("Failed to get predictions from Python service for player ID: {}", player.getId(), e);
                    return;
                }

                MDC.remove("playerId");
            }

            updatePlayersWithPredictions(allPlayerData);

        } catch (Exception e) {
            logger.error("An error occurred during prediction generation.", e);
        }
    }

    /**
     * Updates player entities in the database with their new prediction values.
     *
     * @param playerDataList a list of PlayerData objects containing the new predictions
     */
    private void updatePlayersWithPredictions(List<PlayerData> playerDataList) {
        playerDataList.stream()
                .collect(Collectors.groupingBy(PlayerData::getId))
                .forEach((playerId, playerDataGroup) -> {

                    double averagePrediction = playerDataGroup.stream()
                            .mapToDouble(PlayerData::getPrediction)
                            .average()
                            .orElse(0.0);

                    Player player = playerRepository.findById(playerId).orElse(null);
                    if (player != null) {
                        if ((player.getChanceOfPlayingThisRound() != null && player.getChanceOfPlayingThisRound() < 75) ||
                                (player.getChanceOfPlayingNextRound() != null && player.getChanceOfPlayingNextRound() < 75)) {
                            averagePrediction = 0.0;
                        }

                        player.setPrediction(averagePrediction);
                        playerRepository.save(player);
                    } else {
                        logger.warn("Player not found with ID: {}", playerId);
                    }
                });
    }

    /**
     * Triggers the training of machine learning models by sending a request to a Python service.
     */
    @Override
    public void trainModels() {
        try {
            URL url = new URL("http://localhost:5000/train");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write("{}".getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                logger.debug("Model training triggered successfully.");
            } else {
                logger.warn("Failed to trigger model training. Response code: {}", responseCode);
            }

        } catch (Exception e) {
            logger.error("An error occurred while trying to trigger model training.", e);
        }
    }

    /**
     * Initializes the prediction service by triggering model training.
     */
    public void init() {
        trainModels();
    }
}
