package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.Event;

/**
 * Service interface for managing events in the Fantasy Premier League (FPL).
 *
 * <p>This interface defines methods for fetching the current event and retrieving the most recent event from the database.</p>
 */
public interface EventService {

    /**
     * Fetches the current event from an external source (e.g., FPL API) and stores it in the database.
     *
     * <p>This method is typically called to update the current event information in the system,
     * ensuring that the latest gameweek and other event details are available.</p>
     */
    void fetchCurrentEvent();

    /**
     * Retrieves the most recent event stored in the database.
     *
     * <p>This method returns the event with the highest gameweek value, which represents the current or most recent event in the Fantasy Premier League.</p>
     *
     * @return the current or most recent `Event` object
     */
    Event getCurrentEvent();
}
