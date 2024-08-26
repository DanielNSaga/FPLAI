package com.fploptimizer.fplai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fploptimizer.fplai.model.Fixture;
import com.fploptimizer.fplai.model.PlayerTeam;
import com.fploptimizer.fplai.repository.FixtureRepository;
import com.fploptimizer.fplai.repository.PlayerTeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

/**
 * Service implementation for handling operations related to player teams.
 * This service is responsible for fetching team data, assigning fixtures to teams,
 * and calculating various team-related metrics.
 */
@Service
public class PlayerTeamServiceImpl implements PlayerTeamService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerTeamServiceImpl.class);
    private static final String API_URL = "https://fantasy.premierleague.com/api/bootstrap-static/";

    private final RestTemplate restTemplate;
    private final FixtureRepository fixtureRepository;
    private final PlayerTeamRepository playerTeamRepository;

    /**
     * Constructor for PlayerTeamServiceImpl.
     *
     * @param restTemplate        the RestTemplate to be used for making HTTP requests.
     * @param fixtureRepository   the FixtureRepository to be used for interacting with fixture data.
     * @param playerTeamRepository the PlayerTeamRepository to be used for interacting with player team data.
     */
    public PlayerTeamServiceImpl(RestTemplate restTemplate, FixtureRepository fixtureRepository, PlayerTeamRepository playerTeamRepository) {
        this.restTemplate = restTemplate;
        this.fixtureRepository = fixtureRepository;
        this.playerTeamRepository = playerTeamRepository;
    }

    /**
     * Fetches player teams from the Fantasy Premier League API.
     * This method retrieves team data in JSON format, parses it, and updates the player team repository
     * by first clearing the existing data and then saving the newly fetched teams.
     */
    @Transactional
    @Override
    public void fetchTeams() {
        String jsonResponse;
        try {
            jsonResponse = restTemplate.getForObject(API_URL, String.class);
            if (jsonResponse == null) {
                logger.warn("Received null response from the Fantasy Premier League API.");
                return;
            }
        } catch (Exception e) {
            logger.error("Failed to fetch team data from the Fantasy Premier League API", e);
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<PlayerTeam> teams = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode teamsArray = rootNode.get("teams");

            Iterator<JsonNode> elements = teamsArray.elements();
            while (elements.hasNext()) {
                JsonNode teamNode = elements.next();
                PlayerTeam team = new PlayerTeam();
                team.setName(teamNode.get("name").asText());
                team.setShortName(teamNode.get("short_name").asText());
                team.setHomeStrength(teamNode.get("strength_overall_home").asInt());
                team.setAwayStrength(teamNode.get("strength_overall_away").asInt());
                team.setCode(teamNode.get("code").asInt());
                teams.add(team);
            }

            // Update the repository by first clearing the existing data
            playerTeamRepository.deleteAll();
            playerTeamRepository.saveAll(teams);

            // Assign fixtures to the teams
            assignFixturesToTeams();

            logger.info("Successfully fetched and saved {} teams.", teams.size());

        } catch (IOException e) {
            logger.error("Failed to process team data from the Fantasy Premier League API", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while processing team data", e);
        }
    }

    /**
     * Retrieves a PlayerTeam object by its name.
     *
     * @param name the name of the team.
     * @return the PlayerTeam object matching the given name.
     */
    @Override
    public PlayerTeam getPlayerTeamByName(String name) {
        return playerTeamRepository.findByName(name);
    }

    /**
     * Retrieves a PlayerTeam object by its code.
     *
     * @param teamCode the code of the team.
     * @return the PlayerTeam object matching the given code.
     */
    @Override
    public PlayerTeam getPlayerTeamByCode(int teamCode) {
        return playerTeamRepository.findByCode(teamCode);
    }

    /**
     * Calculates the strength of a team for a given gameweek.
     * The method evaluates the team's strength based on whether they are playing at home or away,
     * and categorizes the strength into three levels.
     *
     * @param team the PlayerTeam object for which the strength is being calculated.
     * @param gw   the gameweek number.
     * @return the strength level (1, 2, or 3) based on the team's overall strength.
     */
    @Override
    public int calculateTeamStrength(PlayerTeam team, int gw) {
        List<Fixture> fixtures = team.getAllFixtures();
        for (Fixture fixture : fixtures) {
            if (fixture.getGw() == gw) {
                int strength;
                if (fixture.getHometeam().equals(team)) {
                    strength = team.getHomeStrength();
                } else {
                    strength = team.getAwayStrength();
                }

                if (strength >= 1270) {
                    return 3;
                } else if (strength >= 1160) {
                    return 2;
                } else {
                    return 1;
                }
            }
        }
        logger.debug("No fixture found for team: {} in gameweek: {}", team.getName(), gw);
        return 0; // Return 0 or an appropriate value if no fixture is found for the given gameweek
    }

    /**
     * Determines if a team is playing at home in a given gameweek.
     *
     * @param team the PlayerTeam object to check.
     * @param gw   the gameweek number.
     * @return true if the team is playing at home, false otherwise.
     */
    @Override
    public boolean isHomeGame(PlayerTeam team, int gw) {
        List<Fixture> fixtures = team.getAllFixtures();
        for (Fixture fixture : fixtures) {
            if (fixture.getGw() == gw) {
                return fixture.getHometeam().equals(team);
            }
        }
        logger.debug("No fixture found for team: {} in gameweek: {}", team.getName(), gw);
        return false; // Return false if no fixture is found for the given gameweek
    }

    /**
     * Calculates the form of a team based on their results from the last five gameweeks prior to the given gameweek.
     *
     * @param team the PlayerTeam object for which the form is being calculated.
     * @param gw   the gameweek number.
     * @return the average form score based on the last five results.
     */
    @Override
    public double calculateTeamForm(PlayerTeam team, int gw) {
        List<Fixture> fixtures = team.getAllFixtures();
        List<Integer> results = new ArrayList<>();

        for (Fixture fixture : fixtures) {
            if (fixture.getGw() < gw) {
                if (results.size() >= 5) {
                    results.remove(0);
                }
                switch (fixture.getResult()) {
                    case "W" -> results.add(3);
                    case "D" -> results.add(2);
                    case "L" -> results.add(1);
                }
            }
        }

        if (results.isEmpty()) {
            logger.debug("No past results found for team: {} before gameweek: {}", team.getName(), gw);
            return 0; // Return 0 if no results are found before the given gameweek
        }

        double sum = results.stream().mapToInt(Integer::intValue).sum();

        return sum / results.size(); // Return the average of the results
    }

    /**
     * Assigns fixtures to the corresponding teams.
     * This method retrieves all fixtures from the repository and assigns them to the appropriate home and away teams.
     */
    private void assignFixturesToTeams() {
        List<Fixture> fixtures = fixtureRepository.findAll();

        // Assign fixtures to homeTeam and awayTeam without saving immediately
        for (Fixture fixture : fixtures) {
            PlayerTeam homeTeam = fixture.getHometeam();
            PlayerTeam awayTeam = fixture.getAwayteam();

            if (homeTeam != null) {
                homeTeam.addHomeFixture(fixture);
            }

            if (awayTeam != null) {
                awayTeam.addAwayFixture(fixture);
            }
        }

        // Save each team only once after all fixtures are assigned
        Set<PlayerTeam> uniqueTeams = new HashSet<>();
        for (Fixture fixture : fixtures) {
            if (fixture.getHometeam() != null) {
                uniqueTeams.add(fixture.getHometeam());
            }
            if (fixture.getAwayteam() != null) {
                uniqueTeams.add(fixture.getAwayteam());
            }
        }

        playerTeamRepository.saveAll(uniqueTeams);
        logger.info("Assigned fixtures to teams.");
    }

    /**
     * Initializes the teams by fetching their data from the external source and preparing them for use.
     */
    @Override
    @Transactional
    public void initializeTeams() {
        // Fetch basic team information without fixtures
        List<PlayerTeam> teams = playerTeamRepository.findAll();
        logger.debug("Initialized {} teams.", teams.size());
        // Logic to fetch and save basic team information
    }

    /**
     * Updates the teams with the latest fixture information.
     * This method ensures that each team has the most up-to-date fixture details, including home and away matches.
     */
    @Override
    @Transactional
    public void updateTeamsWithFixtures() {
        // Fetch all fixtures
        List<Fixture> fixtures = fixtureRepository.findAll();

        // Assign fixtures to home and away teams without triggering recursive saves
        for (Fixture fixture : fixtures) {
            PlayerTeam homeTeam = fixture.getHometeam();
            PlayerTeam awayTeam = fixture.getAwayteam();

            if (homeTeam != null) {
                homeTeam.addHomeFixture(fixture);
            }

            if (awayTeam != null) {
                awayTeam.addAwayFixture(fixture);
            }
        }

        // Save each team only once after all fixtures are assigned
        Set<PlayerTeam> uniqueTeams = new HashSet<>();
        for (Fixture fixture : fixtures) {
            if (fixture.getHometeam() != null) {
                uniqueTeams.add(fixture.getHometeam());
            }
            if (fixture.getAwayteam() != null) {
                uniqueTeams.add(fixture.getAwayteam());
            }
        }

        playerTeamRepository.saveAll(uniqueTeams);
        logger.info("Updated teams with the latest fixtures.");
    }
}
