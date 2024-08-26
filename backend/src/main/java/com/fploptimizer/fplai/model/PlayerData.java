package com.fploptimizer.fplai.model;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the data of a player, including their unique ID and a collection of features.
 */
@Data
public class PlayerData {
    private int id;
    private Map<String, Object> features;
    private double prediction;
    private String position;
    public PlayerData(int id) {
        this.id = id;
        this.features = new HashMap<>();
    }
    /**
     * Adds a feature to the player's data.
     *
     * @param key the name of the feature
     * @param value the value of the feature
     */
    public void addFeature(String key, Object value) {
        this.features.put(key, value);
    }
}

