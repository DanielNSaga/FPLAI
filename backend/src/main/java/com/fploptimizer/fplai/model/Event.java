package com.fploptimizer.fplai.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * Entity class representing an Event in the Fantasy Premier League (FPL).
 *
 * <p>An Event typically corresponds to a specific gameweek within a season,
 * and contains information about the season, the current gameweek, and the total number of players participating.</p>
 */
@Data
@Entity
public class Event {

    /**
     * The unique identifier for the event.
     */
    @Id
    private String id;

    /**
     * The season to which the event belongs (e.g., "2023/24").
     */
    private String season;

    /**
     * The current gameweek of the season.
     */
    private int gameweek;

    /**
     * The total number of players participating in this event.
     */
    private int totalPlayers;

    /**
     * Default constructor for the Event class.
     *
     * <p>This constructor is necessary for JPA and other frameworks that require a no-args constructor.</p>
     */
    public Event() {
    }

    /**
     * Parameterized constructor for the Event class.
     *
     * <p>This constructor initializes the Event object with the provided id, season, gameweek, and totalPlayers.</p>
     *
     * @param id the unique identifier for the event
     * @param season the season to which the event belongs
     * @param gameweek the current gameweek of the season
     * @param totalPlayers the total number of players participating in this event
     */
    public Event(String id, String season, int gameweek, int totalPlayers) {
        this.id = id;
        this.season = season;
        this.gameweek = gameweek;
        this.totalPlayers = totalPlayers;
    }
}
