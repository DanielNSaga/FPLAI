package com.fploptimizer.fplai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.SerializedName;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity class representing a football team in the Fantasy Premier League (FPL).
 *
 * <p>This class contains information about a team, including its players, fixtures,
 * and strength both at home and away. It is mapped to a database table using JPA annotations.</p>
 */
@Data
@Entity
public class PlayerTeam {

    @Id
    private int code;
    private String name;

    @SerializedName("short_name")
    private String shortName;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Player> players = new ArrayList<>();

    @OneToMany(mappedBy = "hometeam", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Fixture> homeFixtures = new ArrayList<>();

    @OneToMany(mappedBy = "awayteam", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Fixture> awayFixtures = new ArrayList<>();

    @SerializedName("strength_overall_home")
    private int homeStrength;

    @SerializedName("strength_overall_away")
    private int awayStrength;

    /**
     * Parameterized constructor for creating a PlayerTeam object with all attributes.
     *
     * @param code The unique code identifying the team.
     * @param name The name of the team.
     * @param shortName The short name of the team.
     * @param homeStrength The team's strength when playing at home.
     * @param awayStrength The team's strength when playing away.
     */
    public PlayerTeam(int code, String name, String shortName, int homeStrength, int awayStrength) {
        this.code = code;
        this.name = name;
        this.shortName = shortName;
        this.homeStrength = homeStrength;
        this.awayStrength = awayStrength;
    }

    /**
     * Default constructor for the PlayerTeam class.
     *
     * <p>This constructor is necessary for JPA and other frameworks that require a no-args constructor.</p>
     */
    public PlayerTeam() {
    }

    /**
     * Gets all fixtures (home and away) for the team.
     *
     * @return a combined list of all home and away fixtures.
     */
    public List<Fixture> getAllFixtures() {
        List<Fixture> allFixtures = new ArrayList<>(homeFixtures);
        allFixtures.addAll(awayFixtures);
        return allFixtures;
    }

    /**
     * Adds a home fixture to the team.
     *
     * <p>This method also ensures that the fixture is added to the away team's list of away fixtures
     * to maintain consistency.</p>
     *
     * @param fixture The home fixture to add.
     */
    public void addHomeFixture(Fixture fixture) {
        if (!this.homeFixtures.contains(fixture)) {
            this.homeFixtures.add(fixture);
            // Ensure the fixture is added to the away team's list of away fixtures
            if (fixture.getAwayteam() != null && !fixture.getAwayteam().getAwayFixtures().contains(fixture)) {
                fixture.getAwayteam().getAwayFixtures().add(fixture);
            }
        }
    }

    /**
     * Adds an away fixture to the team.
     *
     * <p>This method also ensures that the fixture is added to the home team's list of home fixtures
     * to maintain consistency.</p>
     *
     * @param fixture The away fixture to add.
     */
    public void addAwayFixture(Fixture fixture) {
        if (!this.awayFixtures.contains(fixture)) {
            this.awayFixtures.add(fixture);
            // Ensure the fixture is added to the home team's list of home fixtures
            if (fixture.getHometeam() != null && !fixture.getHometeam().getHomeFixtures().contains(fixture)) {
                fixture.getHometeam().getHomeFixtures().add(fixture);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerTeam that = (PlayerTeam) o;
        return code == that.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "PlayerTeam{" +
                "code=" + code +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", homeStrength=" + homeStrength +
                ", awayStrength=" + awayStrength +
                '}';
    }
}
