package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.Fixture;
import com.fploptimizer.fplai.model.PlayerTeam;
import com.fploptimizer.fplai.repository.FixtureRepository;
import com.fploptimizer.fplai.repository.PlayerTeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PlayerTeamServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private FixtureRepository fixtureRepository;

    @Mock
    private PlayerTeamRepository playerTeamRepository;

    @InjectMocks
    private PlayerTeamServiceImpl playerTeamService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void fetchTeams_savesTeamsAndAssignsFixtures() {
        // Arrange
        String jsonResponse = "{ \"teams\": [" +
                "{\"name\": \"Team A\", \"short_name\": \"TA\", \"strength_overall_home\": 1200, \"strength_overall_away\": 1100, \"code\": 1}," +
                "{\"name\": \"Team B\", \"short_name\": \"TB\", \"strength_overall_home\": 1300, \"strength_overall_away\": 1150, \"code\": 2}" +
                "] }";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(jsonResponse);

        PlayerTeam teamA = new PlayerTeam(1, "Team A", "TA", 1200, 1100);
        PlayerTeam teamB = new PlayerTeam(2, "Team B", "TB", 1300, 1150);

        List<Fixture> fixtures = Arrays.asList(
                new Fixture("1", 1, teamA, teamB, false, "W"),
                new Fixture("2", 2, teamA, teamB, false, "D")
        );
        when(fixtureRepository.findAll()).thenReturn(fixtures);

        // Act
        playerTeamService.fetchTeams();

        // Assert
        verify(playerTeamRepository, times(1)).deleteAll();
        verify(playerTeamRepository, times(2)).saveAll(anyList());


        assertTrue(teamA.getAllFixtures().contains(fixtures.get(0)));
        assertTrue(teamB.getAllFixtures().contains(fixtures.get(1)));
    }


    @Test
    void getPlayerTeamByName_returnsCorrectTeam() {
        // Arrange
        PlayerTeam team = new PlayerTeam(1, "Team A", "TA", 1200, 1100);
        when(playerTeamRepository.findByName("Team A")).thenReturn(team);

        // Act
        PlayerTeam result = playerTeamService.getPlayerTeamByName("Team A");

        // Assert
        assertNotNull(result);
        assertEquals("Team A", result.getName());
    }

    @Test
    void getPlayerTeamByCode_returnsCorrectTeam() {
        // Arrange
        PlayerTeam team = new PlayerTeam(1, "Team A", "TA", 1200, 1100);
        when(playerTeamRepository.findByCode(1)).thenReturn(team);

        // Act
        PlayerTeam result = playerTeamService.getPlayerTeamByCode(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCode());
    }

    @Test
    void calculateTeamStrength_returnsCorrectStrength() {
        // Arrange
        PlayerTeam team = new PlayerTeam(1, "Team A", "TA", 1300, 1200);

        Fixture fixture = new Fixture("1", 1, team, new PlayerTeam(), false, "W");
        team.addHomeFixture(fixture);

        // Act
        int strength = playerTeamService.calculateTeamStrength(team, 1);

        // Assert
        assertEquals(3, strength);
    }

    @Test
    void isHomeGame_returnsTrueIfHome() {
        // Arrange
        PlayerTeam team = new PlayerTeam(1, "Team A", "TA", 1300, 1200);
        Fixture fixture = new Fixture("1", 1, team, new PlayerTeam(), false, "W");
        team.addHomeFixture(fixture);

        // Act
        boolean isHome = playerTeamService.isHomeGame(team, 1);

        // Assert
        assertTrue(isHome);
    }

    @Test
    void calculateTeamForm_returnsCorrectForm() {
        // Arrange
        PlayerTeam team = new PlayerTeam(1, "Team A", "TA", 1300, 1200);
        Fixture fixture1 = new Fixture("1", 1, team, new PlayerTeam(), false, "W");
        Fixture fixture2 = new Fixture("2", 2, team, new PlayerTeam(), false, "D");
        Fixture fixture3 = new Fixture("3", 3, team, new PlayerTeam(), false, "L");
        team.addHomeFixture(fixture1);
        team.addHomeFixture(fixture2);
        team.addHomeFixture(fixture3);

        // Act
        double form = playerTeamService.calculateTeamForm(team, 4);

        // Assert
        assertEquals(2.0, form);
    }
}
