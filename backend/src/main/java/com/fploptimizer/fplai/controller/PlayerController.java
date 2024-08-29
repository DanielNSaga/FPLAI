package com.fploptimizer.fplai.controller;

import com.fploptimizer.fplai.model.Player;
import com.fploptimizer.fplai.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing player data.
 *
 * <p>This controller provides endpoints for retrieving and searching player data
 * from the Fantasy Premier League (FPL) API. It interacts with the {@link PlayerService}
 * to perform operations related to players.</p>
 */
@RestController
@CrossOrigin
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    /**
     * Constructor for PlayerController.
     *
     * <p>Initializes the controller with the required {@link PlayerService} to handle player-related operations.</p>
     *
     * @param playerService the {@link PlayerService} to be used for player operations
     */
    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Endpoint to fetch all players from the database.
     *
     * <p>This method retrieves all players currently stored in the database. It does not fetch new data from the FPL API
     * but rather returns the players that have already been saved.</p>
     *
     * @return a list of all {@link Player} entities stored in the database
     */
    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    /**
     * Searches for players by position and keyword.
     *
     * <p>This endpoint searches for players whose position matches the specified element type and whose first or last
     * name contains the specified keyword.</p>
     *
     * @param elementType the position (e.g., 1 = Goalkeeper, 2 = Defender, 3 = Midfielder, 4 = Forward) to filter players by
     * @param keyword     the search keyword to match against the first or last name of players
     * @return a list of {@link Player} entities that match the specified position and keyword
     */
    @GetMapping("/search")
    public List<Player> searchPlayers(@RequestParam Integer elementType, @RequestParam String keyword) {
        return playerService.searchPlayers(elementType, keyword);
    }
}
