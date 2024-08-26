package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.Player;
import com.fploptimizer.fplai.model.PlayerTeam;
import com.fploptimizer.fplai.repository.PlayerRepository;
import com.fploptimizer.fplai.repository.PlayerTeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerTeamRepository playerTeamRepository;

    @InjectMocks
    private PlayerServiceImpl playerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void fetchPlayers_savesPlayersAndUpdatesTeams() {
        // Arrange
        String jsonResponse = "{ \"elements\": [{ \"id\": 1, \"team_code\": 101, \"first_name\": \"Player1\", \"second_name\": \"Test\", \"element_type\": 2, \"now_cost\": 50, \"total_points\": 100 }] }"; // Sørg for at "team_code" matcher en eksisterende PlayerTeam
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(jsonResponse);

        List<PlayerTeam> mockTeams = new ArrayList<>();
        PlayerTeam mockTeam = new PlayerTeam();
        mockTeam.setCode(101);  // Sørg for at denne verdien matcher "team_code" i jsonResponse
        mockTeams.add(mockTeam);
        when(playerTeamRepository.findAll()).thenReturn(mockTeams);

        // Act
        playerService.fetchPlayers();

        // Assert
        verify(playerRepository, times(1)).saveAll(anyList());
        verify(playerTeamRepository, times(1)).saveAll(mockTeams);
        assertEquals(1, mockTeam.getPlayers().size());
    }


    @Test
    void searchPlayers_returnsMatchingPlayers() {
        // Arrange
        Integer elementType = 4;
        String keyword = "Player";
        List<Player> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(new Player());
        when(playerRepository.findByElementTypeAndFirstNameContainingIgnoreCaseOrElementTypeAndSecondNameContainingIgnoreCase(elementType, keyword,elementType, keyword))
                .thenReturn(expectedPlayers);

        // Act
        List<Player> actualPlayers = playerService.searchPlayers(elementType, keyword);

        // Assert
        assertEquals(expectedPlayers.size(), actualPlayers.size());
        verify(playerRepository, times(1))
                .findByElementTypeAndFirstNameContainingIgnoreCaseOrElementTypeAndSecondNameContainingIgnoreCase(elementType, keyword,elementType, keyword);
    }
    @Test
    void getAllPlayers_returnsAllPlayers() {
        // Arrange
        List<Player> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(new Player());
        when(playerRepository.findAll()).thenReturn(expectedPlayers);

        // Act
        List<Player> actualPlayers = playerService.getAllPlayers();

        // Assert
        assertEquals(expectedPlayers.size(), actualPlayers.size());
        verify(playerRepository, times(1)).findAll();
    }
}
