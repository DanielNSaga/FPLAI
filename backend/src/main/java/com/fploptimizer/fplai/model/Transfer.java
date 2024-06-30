package com.fploptimizer.fplai.model;

import jakarta.persistence.*;

/**
 * Represents a transfer in the Fantasy Premier League AI system.
 */
@Entity
public class Transfer {

    /**
     * The unique identifier for the transfer.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transferId;

    /**
     * The player being transferred out.
     */
    @ManyToOne
    private Player fromPlayer;

    /**
     * The player being transferred in.
     */
    @ManyToOne
    private Player toPlayer;

    /**
     * The cost associated with the transfer.
     */
    private Double cost;

    /**
     * Predicted Points gained for the transfer.
     */
    private Double predictedPointChange;

    // Default constructor
    public Transfer() {}

    // Constructor with fields
    public Transfer(Player fromPlayer, Player toPlayer, Double cost, Double predictedPointChange) {
        this.fromPlayer = fromPlayer;
        this.toPlayer = toPlayer;
        this.cost = cost;
        this.predictedPointChange = predictedPointChange;
    }

    // Getters and Setters

    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    public Player getFromPlayer() {
        return fromPlayer;
    }

    public void setFromPlayer(Player fromPlayer) {
        this.fromPlayer = fromPlayer;
    }

    public Player getToPlayer() {
        return toPlayer;
    }

    public void setToPlayer(Player toPlayer) {
        this.toPlayer = toPlayer;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Double getPredictedPointChange() {
        return predictedPointChange;
    }

    public void setPredictedPointChange(Double predictedPointChange) {
        this.predictedPointChange = predictedPointChange;
    }
}

