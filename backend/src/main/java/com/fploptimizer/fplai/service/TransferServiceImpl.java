package com.fploptimizer.fplai.service;

import com.fploptimizer.fplai.model.Player;
import com.fploptimizer.fplai.model.Team;
import com.fploptimizer.fplai.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service implementation for handling transfer operations in the Fantasy Premier League context.
 * This service manages the generation and application of player transfers for team optimization.
 */
@Service
public class TransferServiceImpl implements TransferService {

    private static final Logger logger = LoggerFactory.getLogger(TransferServiceImpl.class);

    private final PlayerService playerService;

    /**
     * Constructor for TransferServiceImpl.
     *
     * @param playerService the PlayerService used to fetch player data.
     */
    public TransferServiceImpl(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Finds the best transfer combination within the budget and transfer constraints.
     * The method filters and sorts potential transfers based on point difference (descending)
     * and cost difference (ascending) within the given budget and available transfers.
     *
     * @param potentialTransfers the list of potential transfers to evaluate.
     * @param budget             the current budget available for transfers.
     * @param transfers          the number of available transfers.
     * @param team               the user's team to ensure max three players per real team.
     * @return a list of the best transfers.
     */
    @Override
    public List<Transfer> findBestTransfers(List<Transfer> potentialTransfers, double budget, int transfers, Team team) {
        try {
            // Count current players per team in the user's team
            Map<String, Long> teamPlayerCount = team.getPlayers().stream()
                    .collect(Collectors.groupingBy(player -> player.getTeam().getName(), Collectors.counting()));

            return potentialTransfers.stream()
                    .filter(transfer -> transfer.getCostDifference() <= budget && transfers > 0)
                    .filter(transfer -> canAddPlayerFromTeam(transfer.getPlayerIn().getTeam().getName(), teamPlayerCount))
                    .sorted(Comparator.comparingDouble(Transfer::getPointDifference).reversed()
                            .thenComparingDouble(Transfer::getCostDifference))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error occurred while finding the best transfers", e);
            throw new RuntimeException("Failed to find the best transfers.", e);
        }
    }

    /**
     * Checks if a player from a given team can be added without exceeding the limit of 3 players from the same team.
     *
     * @param teamName        the name of the team to check.
     * @param teamPlayerCount the current count of players from each team in the user's team.
     * @return true if a player can be added, false otherwise.
     */
    private boolean canAddPlayerFromTeam(String teamName, Map<String, Long> teamPlayerCount) {
        return teamPlayerCount.getOrDefault(teamName, 0L) < 3;
    }

    /**
     * Generates a list of potential transfers for the given team.
     * The method fetches all players and groups them by their position (elementType),
     * then generates potential transfers for each position.
     *
     * @param team the team for which to generate potential transfers.
     * @return a list of potential transfers.
     */
    @Override
    public List<Transfer> generatePotentialTransfers(Team team) {
        try {
            List<Transfer> potentialTransfers = new ArrayList<>();

            // Fetch all players from the repository
            List<Player> allPlayers = playerService.getAllPlayers();

            // Generate potential transfers for each position
            potentialTransfers.addAll(generateTransfersForPosition(filterPlayersByPosition(team, 1), filterAvailablePlayersByPosition(allPlayers, 1)));
            potentialTransfers.addAll(generateTransfersForPosition(filterPlayersByPosition(team, 2), filterAvailablePlayersByPosition(allPlayers, 2)));
            potentialTransfers.addAll(generateTransfersForPosition(filterPlayersByPosition(team, 3), filterAvailablePlayersByPosition(allPlayers, 3)));
            potentialTransfers.addAll(generateTransfersForPosition(filterPlayersByPosition(team, 4), filterAvailablePlayersByPosition(allPlayers, 4)));

            logger.debug("Generated {} potential transfers for the team.", potentialTransfers.size());
            return potentialTransfers;

        } catch (Exception e) {
            logger.error("Error occurred while generating potential transfers", e);
            throw new RuntimeException("Failed to generate potential transfers.", e);
        }
    }

    /**
     * Filters players in the team by their position (elementType).
     *
     * @param team         the team containing the players.
     * @param positionType the position type to filter by (1: GK, 2: DEF, 3: MID, 4: FWD).
     * @return a list of players in the specified position.
     */
    private List<Player> filterPlayersByPosition(Team team, int positionType) {
        return team.getPlayers().stream()
                .filter(player -> player.getElementType() == positionType)
                .collect(Collectors.toList());
    }

    /**
     * Filters available players by their position (elementType).
     *
     * @param allPlayers   the list of all available players.
     * @param positionType the position type to filter by (1: GK, 2: DEF, 3: MID, 4: FWD).
     * @return a list of available players in the specified position.
     */
    private List<Player> filterAvailablePlayersByPosition(List<Player> allPlayers, int positionType) {
        return allPlayers.stream()
                .filter(player -> player.getElementType() == positionType)
                .collect(Collectors.toList());
    }

    /**
     * Generates potential transfers for a specific position (elementType).
     * This method compares players in the team with available players in the market for a given position
     * and creates a list of possible transfers.
     *
     * @param teamPlayers      the players in the user's team for this position.
     * @param availablePlayers the available players in the market for this position.
     * @return a list of potential transfers for this position.
     */
    private List<Transfer> generateTransfersForPosition(List<Player> teamPlayers, List<Player> availablePlayers) {
        List<Transfer> transfers = new ArrayList<>();

        try {
            for (Player playerOut : teamPlayers) {
                for (Player playerIn : availablePlayers) {
                    if (playerOut.getId() != playerIn.getId()) { // Avoid transferring the same player
                        Transfer transfer = new Transfer(playerOut, playerIn);
                        transfers.add(transfer);
                    }
                }
            }
            logger.debug("Generated {} transfers for position type.", transfers.size());
        } catch (Exception e) {
            logger.error("Error occurred while generating transfers for a position", e);
            throw new RuntimeException("Failed to generate transfers for position.", e);
        }

        return transfers;
    }

    /**
     * Applies the best transfers to the team.
     * This method adjusts the team's budget and transfer count, and updates the team's roster
     * by replacing players based on the given transfers.
     *
     * @param team          the user's team.
     * @param bestTransfers the list of best transfers to apply.
     */
    @Override
    public void applyTransfers(Team team, List<Transfer> bestTransfers) {
        try {
            Map<String, Long> teamPlayerCount = team.getPlayers().stream()
                    .collect(Collectors.groupingBy(player -> player.getTeam().getName(), Collectors.counting()));

            for (Transfer transfer : bestTransfers) {
                String playerInTeam = transfer.getPlayerIn().getTeam().getName();

                // Check if the player is already in the team
                boolean playerAlreadyInTeam = team.getPlayers().stream()
                        .anyMatch(player -> player.getId() == transfer.getPlayerIn().getId());

                if (!playerAlreadyInTeam &&
                        team.getBudget() >= transfer.getCostDifference() &&
                        team.getTransfers() > 0 &&
                        canAddPlayerFromTeam(playerInTeam, teamPlayerCount)) {

                    team.setBudget(team.getBudget() - transfer.getCostDifference());
                    team.setTransfers(team.getTransfers() - 1);
                    team.getPlayers().remove(transfer.getPlayerOut());
                    team.getPlayers().add(transfer.getPlayerIn());

                    // Update the count of players from the new player's team
                    teamPlayerCount.put(playerInTeam, teamPlayerCount.getOrDefault(playerInTeam, 0L) + 1);
                }
            }

            // Ensure the team has the correct positional distribution
            team.setPlayers(
                    new ArrayList<>(
                            Stream.of(
                                    filterPlayersByPosition(team, 1).stream().sorted(Comparator.comparingDouble(Player::getPrediction).reversed()).limit(2).toList(),
                                    filterPlayersByPosition(team, 2).stream().sorted(Comparator.comparingDouble(Player::getPrediction).reversed()).limit(5).toList(),
                                    filterPlayersByPosition(team, 3).stream().sorted(Comparator.comparingDouble(Player::getPrediction).reversed()).limit(5).toList(),
                                    filterPlayersByPosition(team, 4).stream().sorted(Comparator.comparingDouble(Player::getPrediction).reversed()).limit(3).toList()
                            ).flatMap(List::stream).toList()
                    )
            );

            logger.debug("Applied {} transfers to the team and ensured correct positional distribution.", bestTransfers.size());
        } catch (Exception e) {
            logger.error("Error occurred while applying transfers", e);
            throw new RuntimeException("Failed to apply transfers.", e);
        }
    }
}
