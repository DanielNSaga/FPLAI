package com.fploptimizer.fplai.model;

import lombok.Data;

/**
 * Represents a transfer option in the Fantasy Premier League AI system.
 */
@Data
public class Transfer {
    private Player playerOut;
    private Player playerIn;

    public Transfer(Player playerOut, Player playerIn) {
        this.playerOut = playerOut;
        this.playerIn = playerIn;
    }

    /**
     * Calculates the difference in predicted points between the player coming into the team and the player going out.
     *
     * @return the difference in predicted points
     */
    public double getPointDifference() {
        return playerIn.getPrediction() - playerOut.getPrediction();
    }

    /**
     * Calculates the difference in cost between the player coming into the team and the player going out.
     *
     * @return the difference in cost
     */
    public double getCostDifference() {
        return playerIn.getNowCost() - playerOut.getNowCost();
    }
}
