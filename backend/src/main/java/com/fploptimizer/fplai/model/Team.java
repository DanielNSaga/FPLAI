package com.fploptimizer.fplai.model;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Represents a user's team in the Fantasy Premier League AI system.
 */
@Data
public class Team {
    private List<Player> players;
    @NotNull
    private double budget;
    @NotNull
    private int transfers;
    public Team(List<Player> players, double budget, int transfers) {
        this.players = players;
        this.budget = budget;
        this.transfers = transfers;
    }
}
