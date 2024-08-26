package com.fploptimizer.fplai.repository;

import com.fploptimizer.fplai.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for the `Event` entity.
 *
 * <p>This interface provides methods to perform CRUD operations on the `Event` entity,
 * as well as custom queries to retrieve specific data from the database.</p>
 */
@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    /**
     * Finds the most recent `Event` based on the gameweek.
     *
     * <p>This method returns the `Event` with the highest gameweek value, which typically corresponds to the current or most recent event.</p>
     *
     * @return an `Optional` containing the most recent `Event` if found, otherwise empty.
     */
    Optional<Object> findTopByOrderByGameweekDesc();
}
