package com.fploptimizer.fplai.model;

import jakarta.persistence.*;
import java.util.List;

    /**
     * Represents a team in the Fantasy Premier League AI system.
     */
    @Entity
    public class Team {

        /**
         * The unique identifier for the team.
         */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long teamId;

        /**
         * The budget available for the team.
         */
        private Double budget;

        /**
         * The total points accumulated by the team.
         */
        private Double totalPoints;

        /**
         * The players in the team.
         */
        @OneToMany
        private List<Player> players;

        // Default constructor
        public Team() {}

        // Constructor with fields
        public Team(Double budget, Double totalPoints, List<Player> players) {
            this.budget = budget;
            this.totalPoints = totalPoints;
            this.players = players;
        }

        // Getters and Setters

        public Long getTeamId() {
            return teamId;
        }

        public void setTeamId(Long teamId) {
            this.teamId = teamId;
        }

        public Double getBudget() {
            return budget;
        }

        public void setBudget(Double budget) {
            this.budget = budget;
        }

        public Double getTotalPoints() {
            return totalPoints;
        }

        public void setTotalPoints(Double totalPoints) {
            this.totalPoints = totalPoints;
        }

        public List<Player> getPlayers() {
            return players;
        }

        public void setPlayers(List<Player> players) {
            this.players = players;
        }
    }


