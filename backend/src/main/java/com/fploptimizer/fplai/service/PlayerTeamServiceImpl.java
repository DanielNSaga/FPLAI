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

    public PlayerTeamServiceImpl(RestTemplate restTemplate, FixtureRepository fixtureRepository, PlayerTeamRepository playerTeamRepository) {
        this.restTemplate = restTemplate;
        this.fixtureRepository = fixtureRepository;
        this.playerTeamRepository = playerTeamRepository;
    }

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

    @Override
    @Transactional
    public PlayerTeam getPlayerTeamByName(String name) {
        PlayerTeam playerTeam = playerTeamRepository.findByName(name);

        // Initialize lazy-loaded relations
        Hibernate.initialize(playerTeam.getHomeFixtures());
        Hibernate.initialize(playerTeam.getAwayFixtures());

        return playerTeam;
    }

    @Override
    @Transactional
    public PlayerTeam getPlayerTeamByCode(int teamCode) {
        PlayerTeam playerTeam = playerTeamRepository.findByCode(teamCode);

        // Initialize lazy-loaded relations
        Hibernate.initialize(playerTeam.getHomeFixtures());
        Hibernate.initialize(playerTeam.getAwayFixtures());

        return playerTeam;
    }

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

    @Transactional
    @Override
    public void initializeTeams() {
        List<PlayerTeam> teams = playerTeamRepository.findAll();
        logger.debug("Initialized {} teams.", teams.size());
    }

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
