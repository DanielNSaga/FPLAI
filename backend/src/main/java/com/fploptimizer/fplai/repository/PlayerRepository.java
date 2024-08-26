package com.fploptimizer.fplai.repository;

import com.fploptimizer.fplai.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing Player entities.
 *
 * <p>This interface provides methods for performing CRUD operations on Player entities,
 * as well as custom queries to search for players by position and name, with or without normalization.</p>
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {

    /**
     * Finds players by their position (elementType) and a keyword that matches either their first name or second name.
     *
     * <p>This method performs a case-insensitive search and returns a list of players that match the specified position
     * and whose first or second name contains the provided keyword.</p>
     *
     * @param elementType the position (e.g., Forward, Midfielder) to filter players by
     * @param firstName the name keyword to filter players by first name
     * @param elementType2 the position to filter players by when searching by second name (typically the same as elementType)
     * @param secondName the name keyword to filter players by second name
     * @return a list of players with the specified position and matching the name criteria
     */
    List<Player> findByElementTypeAndFirstNameContainingIgnoreCaseOrElementTypeAndSecondNameContainingIgnoreCase(
            Integer elementType, String firstName, Integer elementType2, String secondName);

    /**
     * Searches for players by their position (elementType) and a keyword that matches either their first name or second name,
     * with normalization applied to the search.
     *
     * <p>This method uses the SQL `UNACCENT` function to perform a case-insensitive, accent-insensitive search
     * for players whose names match the provided keyword.</p>
     *
     * @param elementType the position (e.g., Forward, Midfielder) to filter players by
     * @param keyword the keyword to search for in the first name or second name, with normalization
     * @return a list of players with the specified position and matching the normalized name criteria
     */
    @Query("SELECT p FROM Player p WHERE p.elementType = :elementType " +
            "AND (LOWER(FUNCTION('unaccent', p.firstName)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :keyword, '%'))) " +
            "OR LOWER(FUNCTION('unaccent', p.secondName)) LIKE LOWER(FUNCTION('unaccent', CONCAT('%', :keyword, '%'))))")
    List<Player> searchPlayersWithNormalization(@Param("elementType") Integer elementType, @Param("keyword") String keyword);
}
