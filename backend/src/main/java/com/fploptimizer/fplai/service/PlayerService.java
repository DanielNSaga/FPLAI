package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.Player;
import java.util.List;

/**
 * Service interface for handling player-related operations.
 *
 * <p>This interface defines methods for initializing player data, fetching players from a data source,
 * searching for players by a keyword, and retrieving all players.</p>
 */
public interface PlayerService {

     /**
      * Initializes player-related data or configurations.
      *
      * <p>This method is typically called during the application startup or at scheduled intervals to ensure
      * that the player data is up-to-date.</p>
      */
     void init();

     /**
      * Fetches all players from the data source.
      *
      * <p>This method retrieves player data from an external API or database and stores it in the application's repository.</p>
      */
     void fetchPlayers();

     /**
      * Searches for players by keyword.
      *
      * <p>This method searches for players based on a keyword, which can be part of the player's name or other attributes.</p>
      *
      * @param elementType the position type (e.g., 1 for GK, 2 for DEF, 3 for MID, 4 for FWD)
      * @param keyword the search keyword used to filter players by name or other criteria
      * @return a list of players matching the specified keyword
      */
     List<Player> searchPlayers(Integer elementType, String keyword);

     /**
      * Retrieves all players from the repository.
      *
      * <p>This method returns a list of all players currently stored in the application's repository.</p>
      *
      * @return a list of all players
      */
     List<Player> getAllPlayers();
}
