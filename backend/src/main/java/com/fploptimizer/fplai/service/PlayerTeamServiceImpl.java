package com.fploptimizer.fplai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fploptimizer.fplai.model.Fixture;
import com.fploptimizer.fplai.model.PlayerTeam;
import com.fploptimizer.fplai.repository.FixtureRepository;
import com.fploptimizer.fplai.repository.PlayerTeamRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

/**
 * Service implementation for handling operations related to player teams.
 * This service is responsible for fetching team data from the Fantasy Premier League API,
 * assigning fixtures to teams, and calculating various team-related metrics.
 *
 * <p>This class interacts with the {@link FixtureRepository} and {@link PlayerTeamRepository}
 * to manage data persistence and retrieve the necessary information for computations.
 *
 * <p>Key functionalities include:
 * <ul>
 *     <li>Fetching and saving team data from an external API.</li>
 *     <li>Assigning fixtures to teams, with lazy loading handled for associated entities.</li>
 *     <li>Calculating team strength and form based on fixtures and results.</li>
 *     <li>Determining whether a team is playing a home game for a given gameweek.</li>
 * </ul>
 */
@Service
public class PlayerTeamServiceImpl implements PlayerTeamService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerTeamServiceImpl.class);
    private static final String API_URL = "https://fantasy.premierleague.com/api/bootstrap-static/";

    private final RestTemplate restTemplate;
    private final FixtureRepository fixtureRepository;
    private final PlayerTeamRepository playerTeamRepository;

    /**
     * Constructs a new {@code PlayerTeamServiceImpl} instance with the given parameters.
     *
     * @param restTemplate a {@link RestTemplate} instance used to fetch data from the API
     * @param fixtureRepository a {@link FixtureRepository} instance used to manage fixture data
     * @param playerTeamRepository a {@link PlayerTeamRepository} instance used to manage player team data
     */
    public PlayerTeamServiceImpl(RestTemplate restTemplate, FixtureRepository fixtureRepository, PlayerTeamRepository playerTeamRepository) {
        this.restTemplate = restTemplate;
        this.fixtureRepository = fixtureRepository;
        this.playerTeamRepository = playerTeamRepository;
    }

    /**
     * Fetches team data from the Fantasy Premier League API and updates the player teams in the repository.
     * <p>This method retrieves team information from the API and saves it into the database after clearing
     * any existing data. Fixtures are then assigned to the teams.
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
     * Retrieves a player team by its name and initializes its fixtures (lazy-loaded relations).
     *
     * @param name the name of the player team
     * @return the player team with the given name
     */
    @Override
    @Transactional
    public PlayerTeam getPlayerTeamByName(String name) {
        PlayerTeam playerTeam = playerTeamRepository.findByName(name);

        // Initialize lazy-loaded relations
        Hibernate.initialize(playerTeam.getHomeFixtures());
        Hibernate.initialize(playerTeam.getAwayFixtures());

        return playerTeam;
    }

    /**
     * Retrieves a player team by its code and initializes its fixtures (lazy-loaded relations).
     *
     * @param teamCode the code of the player team
     * @return the player team with the given code
     */
    @Override
    @Transactional
    public PlayerTeam getPlayerTeamByCode(int teamCode) {
        PlayerTeam playerTeam = playerTeamRepository.findByCode(teamCode);

        // Initialize lazy-loaded relations
        Hibernate.initialize(playerTeam.getHomeFixtures());
        Hibernate.initialize(playerTeam.getAwayFixtures());

        return playerTeam;
    }

    /**
     * Calculates the strength of a team for a given gameweek based on its fixtures.
     *
     * @param team the player team
     * @param gw the gameweek
     * @return a strength rating between 1 and 3, based on team strength
     */
    @Override
    @Transactional
    public int calculateTeamStrength(PlayerTeam team, int gw) {
        Hibernate.initialize(team.getHomeFixtures());
        Hibernate.initialize(team.getAwayFixtures());

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
     * Determines whether the team is playing a home game in the given gameweek.
     *
     * @param team the player team
     * @param gw the gameweek
     * @return true if the team is playing at home, false otherwise
     */
    @Override
    @Transactional
    public boolean isHomeGame(PlayerTeam team, int gw) {
        Hibernate.initialize(team.getHomeFixtures());
        Hibernate.initialize(team.getAwayFixtures());

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
     * Calculates the form of a team based on the results of its fixtures up to a given gameweek.
     * The form is determined by averaging the points (3 for win, 2 for draw, 1 for loss) over the past 5 fixtures.
     *
     * @param team the player team
     * @param gw the gameweek
     * @return the form of the team as a double value
     */
    @Override
    @Transactional
    public double calculateTeamForm(PlayerTeam team, int gw) {
        Hibernate.initialize(team.getHomeFixtures());
        Hibernate.initialize(team.getAwayFixtures());

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
     * Assigns fixtures to teams by updating the home and away fixtures for each team.
     * This method fetches all fixtures from the repository and adds them to the corresponding teams.
     * It ensures that all teams associated with the fixtures are saved in the repository.
     */
    @Transactional
    protected void assignFixturesToTeams() {
        List<Fixture> fixtures = fixtureRepository.findAll();

        for (Fixture fixture : fixtures) {
            PlayerTeam homeTeam = fixture.getHometeam();
            PlayerTeam awayTeam = fixture.getAwayteam();

            if (homeTeam != null) {
                Hibernate.initialize(homeTeam.getHomeFixtures());
                homeTeam.addHomeFixture(fixture);
            }

            if (awayTeam != null) {
                Hibernate.initialize(awayTeam.getAwayFixtures());
                awayTeam.addAwayFixture(fixture);
            }
        }

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
     * Initializes all player teams by retrieving them from the repository.
     * This method can be used to ensure that all player teams are loaded and ready for further operations.
     */
    @Transactional
    @Override
    public void initializeTeams() {
        List<PlayerTeam> teams = playerTeamRepository.findAll();
        logger.debug("Initialized {} teams.", teams.size());
    }

    /**
     * Updates teams with the latest fixtures by adding them to the respective teams.
     * This method ensures that all fixtures are correctly assigned to their respective home and away teams.
     */
    @Transactional
    @Override
    public void updateTeamsWithFixtures() {
        List<Fixture> fixtures = fixtureRepository.findAll();

        for (Fixture fixture : fixtures) {
            PlayerTeam homeTeam = fixture.getHometeam();
            PlayerTeam awayTeam = fixture.getAwayteam();

            if (homeTeam != null) {
                Hibernate.initialize(homeTeam.getHomeFixtures());
                homeTeam.addHomeFixture(fixture);
            }

            if (awayTeam != null) {
                Hibernate.initialize(awayTeam.getAwayFixtures());
                awayTeam.addAwayFixture(fixture);
            }
        }

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
