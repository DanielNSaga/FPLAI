package com.fploptimizer.fplai.controller;

import com.fploptimizer.fplai.exception.ResourceNotFoundException;
import com.fploptimizer.fplai.model.Player;
import com.fploptimizer.fplai.model.Team;
import com.fploptimizer.fplai.model.TeamRequest;
import com.fploptimizer.fplai.repository.PlayerRepository;
import com.fploptimizer.fplai.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing team operations.
 *
 * <p>This controller provides endpoints for team-related operations, such as optimizing a team
 * based on player selections, budget, and available transfers.</p>
 */
@RestController
@CrossOrigin
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;
    private final PlayerRepository playerRepository;

    /**
     * Constructor for TeamController.
     *
     * <p>Initializes the controller with the required services for handling team operations and player data.</p>
     *
     * @param teamService the {@link TeamService} to be used for team operations
     * @param playerRepository the {@link PlayerRepository} to retrieve player data from the database
     */
    @Autowired
    public TeamController(TeamService teamService, PlayerRepository playerRepository) {
        this.teamService = teamService;
        this.playerRepository = playerRepository;
    }

    /**
     * Endpoint to optimize a fantasy football team based on the provided player IDs, budget, and transfers.
     *
     * <p>This method receives a {@link TeamRequest} containing player IDs, budget, and available transfers,
     * validates the input to ensure exactly 15 players are provided, and then optimizes the team using
     * the {@link TeamService}.</p>
     *
     * @param teamRequest the {@link TeamRequest} containing player IDs, budget, and available transfers
     * @return the optimized {@link Team} object
     * @throws ResourceNotFoundException if the number of players in the request is not exactly 15
     */
    @PostMapping("/optimize")
    public Team optimizeTeam(@RequestBody TeamRequest teamRequest) {
        List<Player> players = playerRepository.findAllById(teamRequest.getPlayerIds());
        Team team = new Team(players, teamRequest.getBudget(), teamRequest.getTransfers());

        if (players.size() != 15) {
            throw new ResourceNotFoundException("Team must have exactly 15 players.");
        }

        // Proceed with team optimization
        return teamService.optimizeTeam(team);
    }
}
