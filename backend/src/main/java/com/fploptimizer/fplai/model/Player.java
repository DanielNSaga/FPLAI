package com.fploptimizer.fplai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Entity class representing a player in the Fantasy Premier League (FPL).
 *
 * <p>This class holds various attributes related to a player, including their team,
 * position, performance metrics, and predictive data. It is mapped to a database table using JPA annotations.</p>
 */
@Data
@NoArgsConstructor
@Entity
public class Player {

    @Id
    private int id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private PlayerTeam team;

    @JsonProperty("team_code")
    private int teamCode;

    @JsonProperty("chance_of_playing_next_round")
    private Integer chanceOfPlayingNextRound;

    @JsonProperty("chance_of_playing_this_round")
    private Integer chanceOfPlayingThisRound;

    @JsonProperty("element_type")
    private int elementType;

    @JsonProperty("first_name")
    private String firstName;

    private String form;

    @JsonProperty("now_cost")
    private int nowCost;

    @JsonProperty("second_name")
    private String secondName;

    @JsonProperty("web_name")
    private String webName;

    @JsonProperty("selected_by_percent")
    private String selectedByPercent;

    @JsonProperty("total_points")
    private int totalPoints;

    @JsonProperty("transfers_in")
    private int transfersIn;

    @JsonProperty("transfers_out")
    private int transfersOut;

    private int minutes;

    @JsonProperty("goals_scored")
    private int goalsScored;

    private int assists;

    @JsonProperty("clean_sheets")
    private int cleanSheets;

    @JsonProperty("goals_conceded")
    private int goalsConceded;

    @JsonProperty("penalties_saved")
    private int penaltiesSaved;

    private int saves;

    private int bonus;

    private int bps;

    private String influence;

    private String creativity;

    private String threat;

    @JsonProperty("ict_index")
    private String ictIndex;

    private int starts;

    private double prediction;

    public Player(int id, PlayerTeam team, int teamCode, Integer chanceOfPlayingNextRound,
                  Integer chanceOfPlayingThisRound, int elementType, String firstName,
                  String form, int nowCost, String secondName, String webName,
                  String selectedByPercent, int totalPoints, int transfersIn,
                  int transfersOut, int minutes, int goalsScored, int assists,
                  int cleanSheets, int goalsConceded, int penaltiesSaved, int saves,
                  int bonus, int bps, String influence, String creativity,
                  String threat, String ictIndex, int starts, double prediction) {
        this.id = id;
        this.team = team;
        this.teamCode = teamCode;
        this.chanceOfPlayingNextRound = chanceOfPlayingNextRound;
        this.chanceOfPlayingThisRound = chanceOfPlayingThisRound;
        this.elementType = elementType;
        this.firstName = firstName;
        this.form = form;
        this.nowCost = nowCost;
        this.secondName = secondName;
        this.webName = webName;
        this.selectedByPercent = selectedByPercent;
        this.totalPoints = totalPoints;
        this.transfersIn = transfersIn;
        this.transfersOut = transfersOut;
        this.minutes = minutes;
        this.goalsScored = goalsScored;
        this.assists = assists;
        this.cleanSheets = cleanSheets;
        this.goalsConceded = goalsConceded;
        this.penaltiesSaved = penaltiesSaved;
        this.saves = saves;
        this.bonus = bonus;
        this.bps = bps;
        this.influence = influence;
        this.creativity = creativity;
        this.threat = threat;
        this.ictIndex = ictIndex;
        this.starts = starts;
        this.prediction = prediction;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", teamCode=" + teamCode +
                ", firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", webName='" + webName + '\'' +
                ", elementType=" + elementType +
                ", nowCost=" + nowCost +
                ", totalPoints=" + totalPoints +
                ", prediction=" + prediction +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return id == player.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
