package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.Team;
import com.fploptimizer.fplai.model.Transfer;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service interface for handling transfer operations.
 */
@Service
public interface TransferService {

    List<Transfer> generatePotentialTransfers(Team team);

    /**
     * Finds the best transfer combination within the budget and transfer constraints.
     *
     * @param potentialTransfers The list of potential transfers.
     * @param budget The current budget.
     * @param transfers The number of available transfers.
     * @return A list of the best transfers.
     */
    List<Transfer> findBestTransfers(List<Transfer> potentialTransfers, double budget, int transfers, Team team);

    /**
     * Applies the best transfers to the team.
     *
     * @param team The user's team.
     * @param bestTransfers The list of best transfers to apply.
     */
    void applyTransfers(Team team, List<Transfer> bestTransfers);
}
