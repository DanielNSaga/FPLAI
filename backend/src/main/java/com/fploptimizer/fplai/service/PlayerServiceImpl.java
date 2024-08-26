package com.fploptimizer.fplai.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fploptimizer.fplai.model.Player;
import com.fploptimizer.fplai.model.PlayerTeam;
import com.fploptimizer.fplai.repository.PlayerRepository;
import com.fploptimizer.fplai.repository.PlayerTeamRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for handling player operations.
 * This service is responsible for fetching player data from the Fantasy Premier League API,
 * searching for players by keyword, and retrieving all players.
 */
@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);

    private final RestTemplate restTemplate;
    private final PlayerRepository playerRepository;
    private final PlayerTeamRepository playerTeamRepository;
    private final PlayerTeamService playerTeamService;
    private final FixtureService fixtureService;
    private final EventService eventService;
    private final PredictionService predictionService;
    private final ObjectMapper objectMapper;

    /**
     * Constructor for PlayerServiceImpl.
     *
     * @param restTemplate          the RestTemplate to be used for making HTTP requests.
     * @param playerRepository      the PlayerRepository to be used for interacting with the database.
     * @param playerTeamRepository  the PlayerTeamRepository to be used for interacting with team data.
     * @param playerTeamService     the PlayerTeamService to handle team-related operations.
     * @param fixtureService        the FixtureService to handle fixture-related operations.
     * @param eventService          the EventService to handle event-related operations.
     * @param predictionService     the PredictionService to handle player prediction-related operations.
     */
    @Autowired
    public PlayerServiceImpl(RestTemplate restTemplate, PlayerRepository playerRepository,
                             PlayerTeamRepository playerTeamRepository, PlayerTeamService playerTeamService,
                             FixtureService fixtureService, EventService eventService,
                             PredictionService predictionService) {
        this.restTemplate = restTemplate;
        this.playerRepository = playerRepository;
        this.playerTeamRepository = playerTeamRepository;
        this.playerTeamService = playerTeamService;
        this.fixtureService = fixtureService;
        this.predictionService = predictionService;
        this.eventService = eventService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Initializes player data at the application startup and schedules a task to run at 02:00 every day.
     * This method is responsible for fetching team data, updating fixtures, fetching player data,
     * fetching the current event, and generating player predictions.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    @PostConstruct
    public void init() {
        try {
            // Step 1: Initialize teams
            playerTeamService.initializeTeams();

            // Step 2: Fetch fixtures for the current season
            fixtureService.fetchFixturesForCurrentSeason();

            // Step 3: Update teams with fetched fixtures
            playerTeamService.updateTeamsWithFixtures();

            // Step 4: Fetch players after teams and fixtures are updated
            fetchPlayers();

            // Step 5: Fetch the current event
            eventService.fetchCurrentEvent();

            // Step 6: Generate and save player predictions
            predictionService.generateAndSavePlayerPredictions();

        } catch (Exception e) {
            logger.error("An error occurred during the initialization process.", e);
            throw e;  // Rethrow exception to ensure transaction rollback
        }
    }

    /**
     * Fetches all players from the Fantasy Premier League API and saves them to the database.
     * The method fetches player data from the external API, processes the data,
     * and updates the players in the local database.
     */
    @Transactional
    @Override
    public void fetchPlayers() {
        String url = "https://fantasy.premierleague.com/api/bootstrap-static/";
        String jsonResponse;

        try {
            jsonResponse = restTemplate.getForObject(url, String.class);
            if (jsonResponse == null) {
                logger.warn("Received null response from the Fantasy Premier League API.");
                return;
            }
        } catch (Exception e) {
            logger.error("Failed to fetch players from the Fantasy Premier League API", e);
            return;
        }

        List<PlayerTeam> allTeams = playerTeamRepository.findAllWithPlayers(); // Fetch teams with players initialized

        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            Player[] players = objectMapper.readValue(jsonObject.get("elements").toString(), Player[].class);

            // Create a map of teamCode to PlayerTeam for fast lookup
            Map<Integer, PlayerTeam> teamMap = allTeams.stream()
                    .collect(Collectors.toMap(PlayerTeam::getCode, team -> team));

            for (Player player : players) {
                // Get the team using teamCode
                PlayerTeam team = teamMap.get(player.getTeamCode());

                if (team != null) {
                    // Check if this player already exists in the team
                    Optional<Player> existingPlayer = team.getPlayers().stream()
                            .filter(p -> p.getId() == player.getId())
                            .findFirst();

                    if (existingPlayer.isPresent()) {
                        // Merge the existing player with the new data
                        Player managedPlayer = playerRepository.findById(player.getId()).orElse(null);
                        if (managedPlayer != null) {
                            managedPlayer.setTeam(team);
                            managedPlayer.setFirstName(player.getFirstName());
                            // Update other fields...
                            playerRepository.save(managedPlayer);
                        }
                    } else {
                        // Add new player to the team
                        player.setTeam(team);
                        team.getPlayers().add(player);
                    }
                } else {
                    logger.warn("No team found for team code: {}", player.getTeamCode());
                }
            }

            playerRepository.saveAll(List.of(players));
            playerTeamRepository.saveAll(allTeams);

        } catch (IOException e) {
            logger.error("Failed to process player data from the Fantasy Premier League API", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while processing player data", e);
        }
    }

    /**
     * Searches for players by their element type and a keyword.
     * The keyword is normalized in the query to remove diacritical marks, and both methods
     * are used to cover all possible search scenarios.
     *
     * @param elementType the position (e.g., Forward, Midfielder) to filter players by
     * @param keyword     the search keyword to match against the player's first or last name
     * @return a list of players with the specified element type that match the keyword
     */
    @Override
    public List<Player> searchPlayers(Integer elementType, String keyword) {
        List<Player> normalizedResults = playerRepository.searchPlayersWithNormalization(elementType, keyword);
        List<Player> originalResults = playerRepository.findByElementTypeAndFirstNameContainingIgnoreCaseOrElementTypeAndSecondNameContainingIgnoreCase(
                elementType, keyword, elementType, keyword);

        Set<Player> combinedResults = new HashSet<>(normalizedResults);
        combinedResults.addAll(originalResults);

        return List.copyOf(combinedResults);
    }

    /**
     * Retrieves all players from the database.
     *
     * @return a list of all players.
     */
    @Override
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }
}
