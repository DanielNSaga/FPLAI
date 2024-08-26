package com.fploptimizer.fplai.model;

import lombok.Data;

/**
 * Represents the predicted performance of a player.
 */
@Data
public class PlayerPrediction {
    private int id;
    private double prediction;

    public PlayerPrediction(int id, double prediction) {
        this.id = id;
        this.prediction = prediction;
    }
}
