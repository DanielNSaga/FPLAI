package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.Fixture;
import com.fploptimizer.fplai.model.PlayerTeam;
import com.fploptimizer.fplai.model.Event;
import com.fploptimizer.fplai.repository.FixtureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FixtureServiceImplTest {

    @Mock
    private EventService eventService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PlayerTeamService playerTeamService;

    @Mock
    private FixtureRepository fixtureRepository;

    @InjectMocks
    private FixtureServiceImpl fixtureService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Assuming the API key is set through PostConstruct method
        fixtureService.init();
    }

    @Test
    void fetchFixturesForCurrentSeason_savesFixturesToRepository() {
        // Arrange
        String jsonResponse = "{ \"matches\": [" +
                "{ \"id\": \"1\", \"matchday\": 1, \"homeTeam\": { \"name\": \"Team A\" }, \"awayTeam\": { \"name\": \"Team B\" }, \"status\": \"FINISHED\", \"score\": {} }," +
                "{ \"id\": \"2\", \"matchday\": 2, \"homeTeam\": { \"name\": \"Team C\" }, \"awayTeam\": { \"name\": \"Team D\" }, \"status\": \"SCHEDULED\", \"score\": {} }" +
                "] }";
        ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);

        when(restTemplate.exchange(eq(FixtureServiceImpl.API_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);
        when(playerTeamService.getPlayerTeamByName("Team A")).thenReturn(new PlayerTeam());
        when(playerTeamService.getPlayerTeamByName("Team B")).thenReturn(new PlayerTeam());
        when(playerTeamService.getPlayerTeamByName("Team C")).thenReturn(new PlayerTeam());
        when(playerTeamService.getPlayerTeamByName("Team D")).thenReturn(new PlayerTeam());

        // Act
        fixtureService.fetchFixturesForCurrentSeason();

        // Assert
        ArgumentCaptor<Fixture> fixtureCaptor = ArgumentCaptor.forClass(Fixture.class);
        verify(fixtureRepository, times(2)).save(fixtureCaptor.capture());

        List<Fixture> capturedFixtures = fixtureCaptor.getAllValues();
        assertEquals(2, capturedFixtures.size());

        assertEquals("1", capturedFixtures.get(0).getId());
        assertEquals(1, capturedFixtures.get(0).getGw());
        assertTrue(capturedFixtures.get(0).isDone());

        assertEquals("2", capturedFixtures.get(1).getId());
        assertEquals(2, capturedFixtures.get(1).getGw());
        assertFalse(capturedFixtures.get(1).isDone());
    }

    @Test
    void findNextFixture_returnsNextFixture() {
        // Arrange
        PlayerTeam team = new PlayerTeam();
        team.addHomeFixture(new Fixture("1", 1, team, new PlayerTeam(), true, "{}"));
        team.addHomeFixture(new Fixture("2", 2, team, new PlayerTeam(), false, "{}"));
        team.addAwayFixture(new Fixture("3", 3, new PlayerTeam(), team, false, "{}"));

        when(eventService.getCurrentEvent()).thenReturn(new Event("1", "2023/24", 1, 1000000));

        // Act
        Fixture nextFixture = fixtureService.findNextFixture(team);

        // Assert
        assertNotNull(nextFixture);
        assertEquals("2", nextFixture.getId());
        assertEquals(2, nextFixture.getGw());
    }

    @Test
    void getNextFixtures_returnsNextFiveFixtures() {
        // Arrange
        PlayerTeam team = new PlayerTeam();
        team.addHomeFixture(new Fixture("1", 1, team, new PlayerTeam(), true, "{}"));
        team.addHomeFixture(new Fixture("2", 2, team, new PlayerTeam(), false, "{}"));
        team.addHomeFixture(new Fixture("3", 3, team, new PlayerTeam(), false, "{}"));
        team.addAwayFixture(new Fixture("4", 4, new PlayerTeam(), team, false, "{}"));
        team.addAwayFixture(new Fixture("5", 5, new PlayerTeam(), team, false, "{}"));
        team.addAwayFixture(new Fixture("6", 6, new PlayerTeam(), team, false, "{}"));

        // Act
        List<Fixture> nextFixtures = fixtureService.getNextFixtures(team, 1);

        // Assert
        assertNotNull(nextFixtures);
        assertEquals(5, nextFixtures.size());
        assertEquals("2", nextFixtures.get(0).getId());
        assertEquals("6", nextFixtures.get(4).getId());
    }



    @Test
    void fetchFixturesForCurrentSeasonSavesFixturesToRepository() {
        // Arrange
        String jsonResponse = "{ \"matches\": [" +
                "{ \"id\": \"1\", \"matchday\": 1, \"homeTeam\": { \"shortName\": \"A\" }, \"awayTeam\": { \"shortName\": \"B\" }, \"status\": \"FINISHED\", \"score\": {} }," +
                "{ \"id\": \"2\", \"matchday\": 2, \"homeTeam\": { \"shortName\": \"C\" }, \"awayTeam\": { \"shortName\": \"D\" }, \"status\": \"SCHEDULED\", \"score\": {} }" +
                "] }";
        ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);

        when(restTemplate.exchange(eq(FixtureServiceImpl.API_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        when(playerTeamService.getPlayerTeamByName("A")).thenReturn(new PlayerTeam(1, "Team A", "A", 5, 3));
        when(playerTeamService.getPlayerTeamByName("B")).thenReturn(new PlayerTeam(2, "Team B", "B", 4, 2));
        when(playerTeamService.getPlayerTeamByName("C")).thenReturn(new PlayerTeam(3, "Team C", "C", 6, 4));
        when(playerTeamService.getPlayerTeamByName("D")).thenReturn(new PlayerTeam(4, "Team D", "D", 3, 5));

        // Act
        fixtureService.fetchFixturesForCurrentSeason();

        // Assert
        ArgumentCaptor<Fixture> fixtureCaptor = ArgumentCaptor.forClass(Fixture.class);
        verify(fixtureRepository, times(2)).save(fixtureCaptor.capture());

        List<Fixture> capturedFixtures = fixtureCaptor.getAllValues();
        assertEquals(2, capturedFixtures.size());

        Fixture fixture1 = capturedFixtures.get(0);
        assertEquals("1", fixture1.getId());
        assertEquals(1, fixture1.getGw());
        assertTrue(fixture1.isDone());
        assertNotNull(fixture1.getHometeam());
        assertNotNull(fixture1.getAwayteam());

        Fixture fixture2 = capturedFixtures.get(1);
        assertEquals("2", fixture2.getId());
        assertEquals(2, fixture2.getGw());
        assertFalse(fixture2.isDone());
        assertNotNull(fixture2.getHometeam());
        assertNotNull(fixture2.getAwayteam());
    }

}
