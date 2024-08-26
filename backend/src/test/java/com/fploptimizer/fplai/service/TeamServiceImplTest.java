package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.Player;
import com.fploptimizer.fplai.model.Team;
import com.fploptimizer.fplai.model.Transfer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TeamServiceImplTest {

    @Mock
    private PredictionService predictionService;

    @Mock
    private PlayerService playerService;

    @Mock
    private TransferService transferService;

    @Mock
    private FixtureService fixtureService;

    @Mock
    private PlayerTeamService playerTeamService;

    @InjectMocks
    private TeamServiceImpl teamService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void optimizeTeam_ShouldReturnOptimizedTeam() {
        // Arrange
        List<Player> players = new ArrayList<>();
        double budget = 100.0;
        int transfers = 2;

        Team team = new Team(players, budget, transfers);

        List<Transfer> potentialTransfers = new ArrayList<>();
        List<Transfer> bestTransfers = new ArrayList<>();

        // Mocking the TransferService methods
        when(transferService.generatePotentialTransfers(team)).thenReturn(potentialTransfers);
        when(transferService.findBestTransfers(potentialTransfers, team.getBudget(), team.getTransfers(), team)).thenReturn(bestTransfers);

        // Act
        Team optimizedTeam = teamService.optimizeTeam(team);

        // Assert
        verify(fixtureService, times(1)).fetchFixturesForCurrentSeason();
        verify(playerTeamService, times(1)).fetchTeams();
        verify(playerService, times(1)).fetchPlayers();
        verify(predictionService, times(1)).generateAndSavePlayerPredictions();
        verify(transferService, times(1)).generatePotentialTransfers(team);
        verify(transferService, times(1)).findBestTransfers(potentialTransfers, team.getBudget(), team.getTransfers(), team);
        verify(transferService, times(1)).applyTransfers(team, bestTransfers);

        assertEquals(team, optimizedTeam);
    }
}
