package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.Player;
import com.fploptimizer.fplai.model.PlayerTeam;
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

class TransferServiceImplTest {

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private TransferServiceImpl transferService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findBestTransfers_ShouldReturnFilteredAndSortedTransfers() {
        // Arrange
        PlayerTeam team = mock(PlayerTeam.class);
        when(team.getName()).thenReturn("TeamA");

        Player playerOut = new Player(1, team, 0, 100, 100, 3, "Player1", "1.5", 100, "Doe", "Doe", "25.0", 50, 100, 50, 300, 10, 5, 1, 0, 0, 20, 10, 5, "0.5", "1.0", "0.7", "3.0", 5, 6.0);
        Player playerIn = new Player(2, team, 0, 100, 100, 3, "Player2", "2.0", 105, "Smith", "Doe", "30.0", 60, 120, 40, 350, 15, 7, 2, 0, 0, 25, 12, 7, "0.6", "1.1", "0.8", "3.5", 6, 8.0);

        Team userTeam = mock(Team.class);
        when(userTeam.getPlayers()).thenReturn(List.of(playerOut));

        List<Transfer> potentialTransfers = new ArrayList<>();
        potentialTransfers.add(new Transfer(playerOut, playerIn));

        double budget = 20.0;
        int transfers = 1;

        // Act
        List<Transfer> bestTransfers = transferService.findBestTransfers(potentialTransfers, budget, transfers, userTeam);

        // Assert
        assertEquals(1, bestTransfers.size());
        assertEquals(2.0, bestTransfers.get(0).getPointDifference());
        assertEquals(5.0, bestTransfers.get(0).getCostDifference());
    }


    @Test
    void generatePotentialTransfers_ShouldReturnListOfPotentialTransfers() {
        // Arrange
        Player playerOut = new Player(1, null,0, 100, 100, 3, "Player1", "1.5", 100, "Doe","Doe", "25.0", 50, 100, 50, 300, 10, 5, 1, 0, 0, 20, 10, 5, "0.5", "1.0", "0.7", "3.0", 5, 6.0);
        Player playerIn = new Player(2, null,0, 100, 100, 3, "Player2", "2.0", 105, "Smith","Doe", "30.0", 60, 120, 40, 350, 15, 7, 2, 0, 0, 25, 12, 7, "0.6", "1.1", "0.8", "3.5", 6, 8.0);

        List<Player> teamPlayers = new ArrayList<>();
        teamPlayers.add(playerOut);

        List<Player> allPlayers = new ArrayList<>();
        allPlayers.add(playerIn);

        Team team = new Team(teamPlayers, 100.0, 2);

        when(playerService.getAllPlayers()).thenReturn(allPlayers);

        // Act
        List<Transfer> potentialTransfers = transferService.generatePotentialTransfers(team);

        // Assert
        assertEquals(1, potentialTransfers.size());
        assertEquals(1, potentialTransfers.get(0).getPlayerOut().getId());
        assertEquals(2, potentialTransfers.get(0).getPlayerIn().getId());
    }
    @Test
    void applyTransfers_ShouldUpdateTeamAccordingToBestTransfers() {
        // Arrange
        PlayerTeam teamOut = mock(PlayerTeam.class);
        when(teamOut.getName()).thenReturn("TeamA");

        PlayerTeam teamIn = mock(PlayerTeam.class);
        when(teamIn.getName()).thenReturn("TeamB");

        Player playerOut = new Player(1, teamOut, 0, 100, 100, 3, "Player1", "1.5", 100, "Doe", "Doe", "25.0", 50, 100, 50, 300, 10, 5, 1, 0, 0, 20, 10, 5, "0.5", "1.0", "0.7", "3.0", 5, 6.0);
        Player playerIn = new Player(2, teamIn, 0, 100, 100, 3, "Player2", "2.0", 105, "Smith", "Doe", "30.0", 60, 120, 40, 350, 15, 7, 2, 0, 0, 25, 12, 7, "0.6", "1.1", "0.8", "3.5", 6, 8.0);

        List<Player> teamPlayers = new ArrayList<>();
        teamPlayers.add(playerOut);

        Team team = new Team(teamPlayers, 100.0, 2);

        List<Transfer> bestTransfers = new ArrayList<>();
        bestTransfers.add(new Transfer(playerOut, playerIn));

        // Act
        transferService.applyTransfers(team, bestTransfers);

        // Assert
        assertEquals(1, team.getPlayers().size());
        assertEquals(2, team.getPlayers().get(0).getId());
        assertEquals(95.0, team.getBudget());  // 100.0 - 5.0 (cost difference)
        assertEquals(1, team.getTransfers());  // 2 - 1 (one transfer applied)
    }


}
