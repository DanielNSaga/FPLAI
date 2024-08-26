package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.Team;
import com.fploptimizer.fplai.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for handling team operations.
 * This service optimizes the user's team by generating potential transfers based on
 * player predictions and applying the best transfers within the budget and other constraints.
 */
@Service
public class TeamServiceImpl implements TeamService {

    private static final Logger logger = LoggerFactory.getLogger(TeamServiceImpl.class);

    private final TransferService transferService;

    /**
     * Constructor for TeamServiceImpl.
     *
     * @param transferService the TransferService to be used for generating and applying transfers.
     */
    @Autowired
    public TeamServiceImpl(TransferService transferService) {
        this.transferService = transferService;
    }

    /**
     * Optimizes the user's team based on player predictions and various constraints.
     * The method generates potential transfers, identifies the best transfers within
     * the provided budget and transfer limits, and applies these transfers to the team.
     *
     * @param team The user's current team to be optimized.
     * @return The optimized team after applying the best transfers.
     */
    @Override
    public Team optimizeTeam(Team team) {
        try {
            // Generate potential transfers based on the current team.
            logger.debug("Generating potential transfers for the team.");
            List<Transfer> potentialTransfers = transferService.generatePotentialTransfers(team);

            // Identify the best transfers that fit within the budget and transfer constraints.
            logger.debug("Finding the best transfers within budget and constraints.");
            List<Transfer> bestTransfers = transferService.findBestTransfers(potentialTransfers, team.getBudget(), team.getTransfers(), team);

            // Apply the identified best transfers to the team.
            logger.debug("Applying the best transfers to the team.");
            transferService.applyTransfers(team, bestTransfers);

            logger.debug("Team optimization complete.");
            return team;

        } catch (Exception e) {
            logger.error("An error occurred during team optimization.", e);
            throw new RuntimeException("Failed to optimize the team.", e);
        }
    }
}
