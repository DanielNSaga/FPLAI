package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.Fixture;
import com.fploptimizer.fplai.model.PlayerTeam;

import java.util.List;

/**
 * Service interface for managing fixtures in the Fantasy Premier League (FPL).
 *
 * <p>This interface defines methods for fetching fixtures for the current season,
 * finding the next fixture for a specific team, and retrieving upcoming fixtures for a team based on the current gameweek.</p>
 */
public interface FixtureService {

    /**
     * Fetches the fixtures for the current season from an external source (e.g., FPL API).
     *
     * <p>This method is typically called to update the fixtures data in the system, ensuring that the latest schedule is available.</p>
     */
    void fetchFixturesForCurrentSeason();

    /**
     * Finds the next fixture for a specific team.
     *
     * <p>This method returns the next upcoming fixture for the provided team, based on the current date or gameweek.</p>
     *
     * @param team the team for which to find the next fixture
     * @return the next Fixture for the specified team
     */
    Fixture findNextFixture(PlayerTeam team);

    /**
     * Retrieves the list of upcoming fixtures for a specific team, starting from the given current gameweek.
     *
     * <p>This method returns a list of fixtures that the team will participate in after the specified current gameweek.</p>
     *
     * @param team the team for which to retrieve upcoming fixtures
     * @param currentGw the current gameweek from which to start retrieving upcoming fixtures
     * @return a list of upcoming Fixtures for the specified team
     */
    List<Fixture> getNextFixtures(PlayerTeam team, int currentGw);
}
