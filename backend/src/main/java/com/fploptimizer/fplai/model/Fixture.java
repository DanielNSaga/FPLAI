package com.fploptimizer.fplai.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity class representing a Fixture in the Fantasy Premier League (FPL).
 *
 * <p>A Fixture represents a match between two teams (home and away) during a specific gameweek (gw).
 * It includes details such as the teams involved, whether the match is completed, and the result of the match.</p>
 */
@Data
@Entity
public class Fixture {

    /**
     * The unique identifier for the fixture.
     */
    @Id
    private String id;

    /**
     * The gameweek in which the fixture occurs.
     */
    private int gw;

    /**
     * The home team for the fixture.
     *
     * <p>This is a many-to-one relationship with the {@link PlayerTeam} entity,
     * and it is lazily fetched to avoid unnecessary data loading.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private PlayerTeam hometeam;

    /**
     * The away team for the fixture.
     *
     * <p>This is a many-to-one relationship with the {@link PlayerTeam} entity,
     * and it is lazily fetched to avoid unnecessary data loading.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private PlayerTeam awayteam;

    /**
     * Indicates whether the fixture has been completed.
     */
    private boolean isDone;

    /**
     * The result of the fixture, typically in the format "homeScore-awayScore".
     */
    private String result;

    /**
     * Parameterized constructor for the Fixture class.
     *
     * <p>This constructor initializes the Fixture object with the provided id, gameweek,
     * home team, away team, completion status, and result.</p>
     *
     * @param id the unique identifier for the fixture
     * @param gw the gameweek in which the fixture occurs
     * @param hometeam the home team participating in the fixture
     * @param awayteam the away team participating in the fixture
     * @param isDone whether the fixture has been completed
     * @param result the result of the fixture
     */
    public Fixture(String id, int gw, PlayerTeam hometeam, PlayerTeam awayteam, boolean isDone, String result) {
        this.id = id;
        this.gw = gw;
        this.hometeam = hometeam;
        this.awayteam = awayteam;
        this.isDone = isDone;
        this.result = result;
    }

    /**
     * Default constructor for the Fixture class.
     *
     * <p>This constructor is necessary for JPA and other frameworks that require a no-args constructor.</p>
     */
    public Fixture() {
    }
}
