package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.Team;

/**
 * Service interface for handling team operations.
 */
public interface TeamService {

    /**
     * Optimizes the user's team based on player predictions and constraints.
     *
     * @param team The user's team.
     * @return The optimized team.
     */
    Team optimizeTeam(Team team);
}
