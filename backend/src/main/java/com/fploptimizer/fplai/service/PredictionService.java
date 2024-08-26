package com.fploptimizer.fplai.service;

/**
 * Service interface for handling player prediction operations.
 * This interface defines methods for generating and saving player predictions
 * as well as for training predictive models.
 */
public interface PredictionService {

    /**
     * Generates and saves player predictions based on current data.
     * This method typically uses machine learning models to predict player performance,
     * and then saves these predictions to the database or another persistence layer.
     */
    void generateAndSavePlayerPredictions();

    /**
     * Trains the predictive models used for player predictions.
     * This method is responsible for training the models based on historical data,
     * optimizing them for accuracy, and saving the trained models for future use.
     */
    void trainModels();
}
