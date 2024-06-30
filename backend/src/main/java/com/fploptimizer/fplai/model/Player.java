package com.fploptimizer.fplai.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Represents a player in the Fantasy Premier League AI system.
 */
@Entity
public class Player {

    /**
     * The unique identifier for the player.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playerId;

    /**
     * The name of the player.
     */
    private String name;

    /**
     * The team the player belongs to.
     */
    private String team;

    /**
     * The position of the player.
     */
    private String position;

    /**
     * The cost of the player in the Fantasy Premier League.
     */
    private Double cost;

    /**
     * The predicted points for the player.
     */
    private Double predictedPoints;

    // Getters and Setters

    // Default constructor
    public Player() {}

    // Constructor with fields
    public Player(String name, String team, String position, Double cost, Double predictedPoints) {
        this.name = name;
        this.team = team;
        this.position = position;
        this.cost = cost;
        this.predictedPoints = predictedPoints;
    }

    // Getters and Setters


    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Double getPredictedPoints() {
        return predictedPoints;
    }

    public void setPredictedPoints(Double predictedPoints) {
        this.predictedPoints = predictedPoints;
    }
}

