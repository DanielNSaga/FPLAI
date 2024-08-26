package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.Fixture;
import com.fploptimizer.fplai.model.PlayerTeam;
import com.fploptimizer.fplai.repository.FixtureRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for handling fixture-related operations.
 * This service fetches fixtures for the current season from an external API
 * and processes them into {@link Fixture} objects.
 */
@Service
public class FixtureServiceImpl implements FixtureService {

    private static final Logger logger = LoggerFactory.getLogger(FixtureServiceImpl.class);
    public static final String API_URL = "http://api.football-data.org/v4/competitions/PL/matches";
    private static String apiKey;

    @Value("${api.key}")
    private String apiKeyFromProperties;

    private final EventService eventService;
    private final RestTemplate restTemplate;
    private final PlayerTeamService playerTeamService;
    private final FixtureRepository fixtureRepository;

    // Map for matching team names from the fixture API to FPL database names
    private static final Map<String, String> TEAM_NAME_MAPPING = new HashMap<>();

    static {
        TEAM_NAME_MAPPING.put("Wolverhampton", "Wolves");
        TEAM_NAME_MAPPING.put("Tottenham", "Spurs");
        TEAM_NAME_MAPPING.put("Brighton Hove", "Brighton");
        TEAM_NAME_MAPPING.put("Nottingham", "Nott'm Forest");
        TEAM_NAME_MAPPING.put("Man United", "Man Utd");
        TEAM_NAME_MAPPING.put("Ipswich Town", "Ipswich");
        TEAM_NAME_MAPPING.put("Leicester City", "Leicester");
        // Additional mappings can be added here if necessary
    }

    /**
     * Constructor for FixtureServiceImpl.
     *
     * @param eventService      the EventService for fetching the current event
     * @param restTemplate      the RestTemplate for making HTTP requests
     * @param playerTeamService the PlayerTeamService for retrieving team data
     * @param fixtureRepository the FixtureRepository for storing fixture data
     */
    public FixtureServiceImpl(EventService eventService, RestTemplate restTemplate,
                              PlayerTeamService playerTeamService, FixtureRepository fixtureRepository) {
        this.eventService = eventService;
        this.restTemplate = restTemplate;
        this.playerTeamService = playerTeamService;
        this.fixtureRepository = fixtureRepository;
    }

    /**
     * Initializes the API key from the application properties.
     */
    @PostConstruct
    void init() {
        apiKey = apiKeyFromProperties;
    }

    /**
     * Maps team names from the API response to the corresponding names in the FPL database.
     *
     * @param teamName the name of the team from the API response
     * @return the corresponding name in the FPL database
     */
    private String mapTeamName(String teamName) {
        return TEAM_NAME_MAPPING.getOrDefault(teamName, teamName);
    }

    /**
     * Fetches fixtures for the current season from an external API and saves them to the database.
     */
    @Override
    public void fetchFixturesForCurrentSeason() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(API_URL, HttpMethod.GET, entity, String.class);
        } catch (Exception e) {
            logger.error("Failed to fetch fixtures from the external API", e);
            return;
        }

        String jsonResponse = response.getBody();
        if (jsonResponse == null) {
            logger.warn("Received empty response from the external API");
            return;
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray matches = jsonObject.getAsJsonArray("matches");

            for (int i = 0; i < matches.size(); i++) {
                JsonObject match = matches.get(i).getAsJsonObject();
                String id = match.get("id").getAsString();
                int matchday = match.get("matchday").getAsInt();

                // Use the mapTeamName method to ensure correct name mapping
                String homeTeamName = mapTeamName(match.get("homeTeam").getAsJsonObject().get("shortName").getAsString());
                String awayTeamName = mapTeamName(match.get("awayTeam").getAsJsonObject().get("shortName").getAsString());

                PlayerTeam hometeam = playerTeamService.getPlayerTeamByName(homeTeamName);
                PlayerTeam awayteam = playerTeamService.getPlayerTeamByName(awayTeamName);

                if (hometeam == null || awayteam == null) {
                    logger.warn("Could not find teams for match: {} vs {}", homeTeamName, awayTeamName);
                    continue;
                }

                boolean isDone = match.get("status").getAsString().equals("FINISHED");
                String result = match.get("score").getAsJsonObject().toString();

                Fixture fixture = new Fixture(id, matchday, hometeam, awayteam, isDone, result);
                fixtureRepository.save(fixture);
            }
        } catch (Exception e) {
            logger.error("Failed to process the fixtures from the external API", e);
        }
    }

    /**
     * Finds the next upcoming fixture for the specified team.
     *
     * @param team the team for which to find the next fixture
     * @return the next Fixture for the specified team, or null if no upcoming fixture is found
     */
    @Override
    public Fixture findNextFixture(PlayerTeam team) {
        int currentGameweek = eventService.getCurrentEvent().getGameweek();
        List<Fixture> fixtures = team.getAllFixtures();
        for (Fixture fixture : fixtures) {
            if (fixture.getGw() >= currentGameweek && !fixture.isDone()) {
                return fixture;
            }
        }
        return null;
    }

    /**
     * Retrieves the list of upcoming fixtures for the specified team, starting from the current gameweek.
     *
     * @param team      the team for which to retrieve upcoming fixtures
     * @param currentGw the current gameweek from which to start retrieving upcoming fixtures
     * @return a list of upcoming Fixtures for the specified team, limited to the next five fixtures
     */
    @Override
    public List<Fixture> getNextFixtures(PlayerTeam team, int currentGw) {
        List<Fixture> nextFixtures = new ArrayList<>();
        List<Fixture> fixtures = team.getAllFixtures();

        for (Fixture fixture : fixtures) {
            if (fixture.getGw() >= currentGw && !fixture.isDone()) {
                nextFixtures.add(fixture);
                if (nextFixtures.size() == 5) {
                    break;
                }
            }
        }

        return nextFixtures;
    }
}
