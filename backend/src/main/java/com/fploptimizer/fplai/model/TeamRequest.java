package com.fploptimizer.fplai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO (Data Transfer Object) class representing a request to optimize a fantasy football team.
 *
 * <p>This class contains the necessary data to process a team optimization request,
 * including the list of player IDs, the available budget, and the number of transfers.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamRequest {
    private List<Integer> playerIds;
    private double budget;
    private int transfers;
}
