import React, { useState, useEffect } from 'react';
import SearchableBox from './SearchableBox';

/**
 * TeamLayout component handles the assignment of players to specific positions on the field
 * based on the chosen formation. It allows users to select players for each position
 * and automatically updates the team when the formation changes.
 */
const TeamLayout = ({ formation, onTeamChange, apiPlayers = [] }) => {
    const BOX_POSITIONS = [
        { id: 1, elementType: 1, positionLabel: 'GK', player: null },
        { id: 2, elementType: 1, positionLabel: 'GK', player: null },
        { id: 3, elementType: 2, positionLabel: 'DEF', player: null },
        { id: 4, elementType: 2, positionLabel: 'DEF', player: null },
        { id: 5, elementType: 2, positionLabel: 'DEF', player: null },
        { id: 6, elementType: 2, positionLabel: 'DEF', player: null },
        { id: 7, elementType: 2, positionLabel: 'DEF', player: null },
        { id: 8, elementType: 3, positionLabel: 'MID', player: null },
        { id: 9, elementType: 3, positionLabel: 'MID', player: null },
        { id: 10, elementType: 3, positionLabel: 'MID', player: null },
        { id: 11, elementType: 3, positionLabel: 'MID', player: null },
        { id: 12, elementType: 3, positionLabel: 'MID', player: null },
        { id: 13, elementType: 4, positionLabel: 'FWR', player: null },
        { id: 14, elementType: 4, positionLabel: 'FWR', player: null },
        { id: 15, elementType: 4, positionLabel: 'FWR', player: null },
    ];

    const FORMATION_LAYOUTS = {
        '343': { GK: 1, DEF: 3, MID: 4, FWR: 3 },
        '352': { GK: 1, DEF: 3, MID: 5, FWR: 2 },
        '433': { GK: 1, DEF: 4, MID: 3, FWR: 3 },
        '442': { GK: 1, DEF: 4, MID: 4, FWR: 2 },
        '451': { GK: 1, DEF: 4, MID: 5, FWR: 1 },
        '523': { GK: 1, DEF: 5, MID: 2, FWR: 3 },
        '532': { GK: 1, DEF: 5, MID: 3, FWR: 2 },
        '541': { GK: 1, DEF: 5, MID: 4, FWR: 1 },
    };

    const layout = FORMATION_LAYOUTS[formation] || FORMATION_LAYOUTS['442'];
    const [assignedPlayers, setAssignedPlayers] = useState(BOX_POSITIONS);

    useEffect(() => {
        clearPlayerPositions();
    }, [formation]);

    useEffect(() => {
        onTeamChange(assignedPlayers);
    }, [assignedPlayers, onTeamChange]);

    useEffect(() => {
        if (apiPlayers.length > 0) {
            updatePlayersFromApi(apiPlayers);
        }
    }, [apiPlayers]);

    /**
     * Clears all player positions when the formation changes.
     */
    const clearPlayerPositions = () => {
        const newAssignedPlayers = assignedPlayers.map(box => ({
            ...box,
            player: null,
        }));
        setAssignedPlayers(newAssignedPlayers);
    };

    /**
     * Handles the assignment of a player to a specific position on the field.
     * Ensures that a player cannot be assigned to more than one position.
     *
     * @param {Object} player - The player to be assigned.
     * @param {number} id - The ID of the box position.
     */
    const handlePlayerAssign = (player, id) => {
        const isDuplicate = assignedPlayers.some(box => box.player && box.player.id === player.id);

        if (isDuplicate) {
            alert(`Player ${player.web_name} is already assigned to another position.`);
            return;
        }

        const newAssignedPlayers = assignedPlayers.map(box =>
            box.id === id ? { ...box, player } : box
        );
        setAssignedPlayers(newAssignedPlayers);
    };

    /**
     * Updates the players on the field and bench based on data received from the API.
     *
     * @param {Array} apiPlayers - The list of players received from the API.
     */
    const updatePlayersFromApi = (apiPlayers) => {
        console.log("API Players: ", apiPlayers);

        let newAssignedPlayers = [...BOX_POSITIONS];

        // Sort players from the API based on their element type
        const sortedApiPlayers = {
            GK: apiPlayers.filter(player => player.element_type === 1),
            DEF: apiPlayers.filter(player => player.element_type === 2),
            MID: apiPlayers.filter(player => player.element_type === 3),
            FWR: apiPlayers.filter(player => player.element_type === 4),
        };

        console.log("Sorted API Players: ", sortedApiPlayers);

        // Assign players to the correct positions based on their position label
        newAssignedPlayers = newAssignedPlayers.map((box) => {
            const positionPlayers = sortedApiPlayers[box.positionLabel];
            if (positionPlayers && positionPlayers.length > 0) {
                const player = positionPlayers.shift();
                console.log(`Assigning ${player.web_name} to ${box.positionLabel} (Box ID: ${box.id})`);
                return {
                    ...box,
                    player: player,
                };
            }
            return box;
        });

        console.log("New Assigned Players: ", newAssignedPlayers);
        setAssignedPlayers(newAssignedPlayers);
    };

    /**
     * Renders a row of boxes for players of a specific element type.
     *
     * @param {number} elementType - The element type of the players (1: GK, 2: DEF, 3: MID, 4: FWR).
     * @param {number} count - The number of boxes to render in the row.
     * @returns {JSX.Element} A row of player boxes.
     */
    const renderRow = (elementType, count) => {
        const fieldBoxes = assignedPlayers.filter(box => box.elementType === elementType).slice(0, count);
        return (
            <div className="flex justify-center space-x-4">
                {fieldBoxes.map((box) => (
                    <div
                        key={box.id}
                        className="border border-gray-900 text-white flex items-center justify-center cursor-pointer rounded-lg"
                        style={{
                            width: '90px',
                            height: '90px',
                            fontSize: '8px',
                            borderRadius: '16px',
                            backgroundColor: '#1A202C',
                        }}
                    >
                        <SearchableBox
                            elementType={box.elementType}
                            positionLabel={box.positionLabel}
                            player={box.player}
                            onAssignPlayer={(player) => handlePlayerAssign(player, box.id)}
                        />
                    </div>
                ))}
            </div>
        );
    };

    /**
     * Renders the bench with the remaining players that are not on the field.
     *
     * @returns {JSX.Element} The bench of players.
     */
    const renderBench = () => {
        const benchBoxes = assignedPlayers.filter(box => {
            const onFieldCount = layout[box.positionLabel] || 0;
            return assignedPlayers.filter(b => b.elementType === box.elementType).indexOf(box) >= onFieldCount;
        });

        return (
            <div className="grid grid-cols-4 gap-4 justify-center mt-0">
                {benchBoxes.map((box) => (
                    <div
                        key={box.id}
                        className="border border-gray-900 text-white flex items-center justify-center cursor-pointer rounded-lg"
                        style={{
                            width: '90px',
                            height: '90px',
                            fontSize: '8px',
                            borderRadius: '16px',
                            backgroundColor: '#1A202C',
                        }}
                    >
                        <SearchableBox
                            elementType={box.elementType}
                            positionLabel={box.positionLabel}
                            player={box.player}
                            onAssignPlayer={(player) => handlePlayerAssign(player, box.id)}
                        />
                    </div>
                ))}
            </div>
        );
    };

    return (
        <div className="p-6">
            <div
                className="rounded-lg border border-gray-900"
                style={{
                    backgroundImage: `url('/half-rotate-2.png')`,
                    backgroundSize: 'cover',
                    backgroundPosition: 'center',
                    padding: '20px',
                }}
            >
                <div className="space-y-6">
                    {renderRow(1, layout.GK)}
                    {renderRow(2, layout.DEF)}
                    {renderRow(3, layout.MID)}
                    {renderRow(4, layout.FWR)}
                </div>
            </div>

            <div className="rounded-lg border border-gray-900 bg-custom-purple p-4 mt-0">
                <div className="text-center font-semibold mb-4 text-white">Bench</div>
                {renderBench()}
            </div>
        </div>
    );
};

export default TeamLayout;
