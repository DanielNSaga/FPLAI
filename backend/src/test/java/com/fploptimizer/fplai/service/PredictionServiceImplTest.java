package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.*;
import com.fploptimizer.fplai.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class PredictionServiceImplTest {

    @Mock
    private FixtureService fixtureService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private EventService eventService;

    @Mock
    private PlayerTeamService playerTeamService;

    @InjectMocks
    private PredictionServiceImpl predictionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGenerateAndSavePlayerPredictions() throws Exception {
        // Arrange
        PlayerTeam team = new PlayerTeam(1, "Team A", "TA", 100, 100);
        Player player = new Player(
                1,                    // id
                team,                 // team
                1,                     //teamCode
                100,                  // chanceOfPlayingNextRound
                100,                  // chanceOfPlayingThisRound
                3,                    // elementType
                "John",               // firstName
                "1.5",                // form
                100,                  // nowCost
                "Doe",                // secondName
                "Doe",                // webname
                "25.0",               // selectedByPercent
                50,                   // totalPoints
                100,                  // transfersIn
                50,                   // transfersOut
                300,                  // minutes
                10,                   // goalsScored
                5,                    // assists
                1,                    // cleanSheets
                0,                    // goalsConceded
                0,                    // penaltiesSaved
                20,                   // saves
                10,                   // bonus
                5,                    // bps
                "0.5",                // influence
                "1.0",                // creativity
                "0.7",                // threat
                "3.0",                // ictIndex
                5,                    // starts
                0.0                   // prediction
        );

        Event currentEvent = new Event("1", "2023/2024", 5, 5000000);
        Fixture fixture = new Fixture("1", 5, team, team, false, "0-0");

        when(playerRepository.findAll()).thenReturn(List.of(player));
        when(playerRepository.findById(1)).thenReturn(java.util.Optional.of(player)); // Mock findById for å returnere spilleren

        when(eventService.getCurrentEvent()).thenReturn(currentEvent);
        when(fixtureService.getNextFixtures(team, currentEvent.getGameweek())).thenReturn(List.of(fixture));
        when(playerTeamService.getPlayerTeamByCode(1)).thenReturn(team);
        when(playerTeamService.calculateTeamStrength(any(), anyInt())).thenReturn(50);
        when(playerTeamService.isHomeGame(any(), anyInt())).thenReturn(true);
        when(playerTeamService.calculateTeamForm(any(), anyInt())).thenReturn(60.0);

        PredictionServiceImpl spyPredictionService = spy(predictionService);

        doAnswer(invocation -> List.of(5.0)).when(spyPredictionService).getPredictionsFromPython(anyList());

        // Act
        spyPredictionService.generateAndSavePlayerPredictions();

        // Assert
        verify(playerRepository, times(1)).save(player);

        assertEquals(5.0, player.getPrediction());
    }

    @Test
    public void testGenerateAndSavePlayerPredictions_WhenPythonServiceFails() throws Exception {
        // Arrange
        PlayerTeam team = new PlayerTeam(1, "Team A", "TA", 100, 100);
        Player player = new Player(
                1, team,1, 100, 100, 3, "John", "1.5", 100, "Doe", "Doe", "25.0", 50, 100, 50, 300,
                10, 5, 1, 0, 0, 20, 10, 5, "0.5", "1.0", "0.7", "3.0", 5, 0.0
        );

        Event currentEvent = new Event("1", "2023/2024", 5, 5000000);
        Fixture fixture = new Fixture("1", 5, team, team, false, "0-0");

        when(playerRepository.findAll()).thenReturn(List.of(player));
        when(playerRepository.findById(1)).thenReturn(java.util.Optional.of(player));
        when(eventService.getCurrentEvent()).thenReturn(currentEvent);
        when(fixtureService.getNextFixtures(team, currentEvent.getGameweek())).thenReturn(List.of(fixture));
        when(playerTeamService.getPlayerTeamByCode(1)).thenReturn(team);

        PredictionServiceImpl spyPredictionService = spy(predictionService);

        // Mock Python prediction response to throw an exception
        doThrow(new RuntimeException("Python service failed")).when(spyPredictionService).getPredictionsFromPython(anyList());

        // Act & Assert
        try {
            spyPredictionService.generateAndSavePlayerPredictions();
        } catch (RuntimeException e) {
            assertEquals("Python service failed", e.getMessage());
        }

        verify(playerRepository, never()).save(any(Player.class));
    }

}
