package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.PlayerTeam;

/**
 * Service interface for handling operations related to PlayerTeams.
 * This interface defines methods for retrieving and processing team data,
 * including fetching team information, calculating team strength and form,
 * and determining home or away game status.
 */
public interface PlayerTeamService {

    /**
     * Retrieves a PlayerTeam by its name.
     *
     * @param name the name of the team
     * @return the PlayerTeam with the specified name, or null if not found
     */
    PlayerTeam getPlayerTeamByName(String name);

    /**
     * Retrieves a PlayerTeam by its team code.
     *
     * @param teamCode the code of the team
     * @return the PlayerTeam with the specified team code, or null if not found
     */
    PlayerTeam getPlayerTeamByCode(int teamCode);

    /**
     * Fetches all teams from the external source and updates the local database.
     * This method is responsible for pulling the latest team data and storing it locally.
     */
    void fetchTeams();

    /**
     * Calculates the strength of a team for a given gameweek.
     * The strength is determined based on various factors such as home/away performance
     * and other criteria that influence the team's expected performance.
     *
     * @param team the team for which to calculate the strength
     * @param gw   the gameweek for which to calculate the strength
     * @return the calculated strength of the team
     */
    int calculateTeamStrength(PlayerTeam team, int gw);

    /**
     * Determines if a game is a home game for a given team in a specific gameweek.
     *
     * @param team the team to check
     * @param gw   the gameweek to check
     * @return true if the game is a home game for the team, false otherwise
     */
    boolean isHomeGame(PlayerTeam team, int gw);

    /**
     * Calculates the form of a team for a given gameweek.
     * The form is based on the team's recent performances, typically over the last 5 matches.
     *
     * @param team the team for which to calculate the form
     * @param gw   the gameweek for which to calculate the form
     * @return the calculated form of the team as a double value
     */
    double calculateTeamForm(PlayerTeam team, int gw);

    /**
     * Initializes the teams by fetching their data from the external source
     * and preparing them for use in the application.
     * This method is typically called at application startup.
     */
    void initializeTeams();

    /**
     * Updates the teams with the latest fixture information.
     * This method ensures that each team has the most up-to-date fixture details,
     * including their home and away matches.
     */
    void updateTeamsWithFixtures();
}
