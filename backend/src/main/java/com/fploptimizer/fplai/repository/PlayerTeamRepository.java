package com.fploptimizer.fplai.repository;

import com.fploptimizer.fplai.model.PlayerTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing PlayerTeam entities.
 *
 * <p>This interface provides methods for performing CRUD operations on PlayerTeam entities,
 * as well as custom queries to fetch teams by name, code, and with their players.</p>
 */
@Repository
public interface PlayerTeamRepository extends JpaRepository<PlayerTeam, Integer> {

    /**
     * Finds a PlayerTeam by its name.
     *
     * @param name the name of the team to search for
     * @return the PlayerTeam with the specified name, or null if not found
     */
    PlayerTeam findByName(String name);

    /**
     * Finds a PlayerTeam by its unique code.
     *
     * @param code the unique code of the team to search for
     * @return the PlayerTeam with the specified code, or null if not found
     */
    PlayerTeam findByCode(int code);

    /**
     * Finds all PlayerTeams and fetches their associated players using a join fetch.
     *
     * <p>This method performs a LEFT JOIN FETCH to eagerly load the players associated with each team,
     * minimizing the number of queries and improving performance when accessing the players of each team.</p>
     *
     * @return a list of PlayerTeams with their players fetched
     */
    @Query("SELECT DISTINCT t FROM PlayerTeam t LEFT JOIN FETCH t.players")
    List<PlayerTeam> findAllWithPlayers();
}
