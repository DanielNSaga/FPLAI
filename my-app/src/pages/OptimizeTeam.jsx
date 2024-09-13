import React, { useState, useEffect } from 'react';
import FormationBar from '../components/FormationBar';
import TeamLayout from '../components/TeamLayout';
import BudgetTransferBar from '../components/BudgetTransferBar';
import OptimizeTeamButton from '../components/OptimizeTeamButton';
import axios from 'axios';

/**
 * The OptimizeTeam component allows users to optimize their Fantasy Premier League team
 * by selecting a formation, assigning players, setting a budget, and specifying transfers.
 * It communicates with a backend API to optimize the team based on the user's inputs.
 * Additionally, it displays the current gameweek for which the optimization is performed.
 */
const OptimizeTeam = () => {
    const [budget, setBudget] = useState('');
    const [transfers, setTransfers] = useState('');
    const [selectedFormation, setSelectedFormation] = useState('442');
    const [team, setTeam] = useState([]); // State to store the team
    const [error, setError] = useState('');
    const [apiPlayers, setApiPlayers] = useState([]); // State to store players from API response
    const [currentEvent, setCurrentEvent] = useState(null); // State to store the current event (gameweek)

    useEffect(() => {
        /**
         * Fetches the current event (gameweek) from the backend API and updates the state.
         */
        const fetchCurrentEvent = async () => {
            try {
                const response = await axios.get('https://fplai.onrender.com/api/events/current');
                setCurrentEvent(response.data);
            } catch (error) {
                console.error(
                    'Error fetching current event:',
                    error.response ? error.response.data : error.message
                );
            }
        };

        fetchCurrentEvent();
    }, []);

    const handleBudgetChange = (e) => setBudget(e.target.value);
    const handleTransfersChange = (e) => setTransfers(e.target.value);
    const handleFormationChange = (formation) => setSelectedFormation(formation);

    /**
     * Updates the team state when players are assigned or changed.
     * @param {Array} updatedTeam - The updated team array.
     */
    const handleTeamChange = (updatedTeam) => {
        console.log("Received updated team:", updatedTeam);
        setTeam(updatedTeam);
    };

    /**
     * Handles the optimization of the team by sending the current team setup, budget, and transfers
     * to the backend API, which returns the optimized team configuration.
     */
    const handleOptimizeTeam = async () => {
        console.log("Current team:", team);
        console.log("Team length:", team.length);

        // Validate that all necessary inputs are filled and all 15 players are assigned
        const isValid =
            budget &&
            transfers &&
            team.length === 15 &&
            !team.some((box) => !box.player);

        if (!isValid) {
            setError(
                'Please ensure all inputs are filled, including budget, transfers, and all 15 players.'
            );
            return;
        }

        setError('');

        // Prepare the request payload
        const playerIds = team.map((box) => box.player.id);
        const teamRequest = {
            budget: parseFloat(budget) * 10,
            transfers: parseInt(transfers, 10),
            playerIds: playerIds,
        };

        try {
            // Send a POST request to optimize the team
            const response = await axios.post(
                'https://fplai.onrender.com/api/teams/optimize',
                teamRequest
            );
            setApiPlayers(response.data.players); // Update the players with the API response
        } catch (error) {
            console.error(
                'Error optimizing team:',
                error.response ? error.response.data : error.message
            );
            setError('An error occurred while optimizing the team. Please try again.');
        }
    };

    return (
        <div className="flex flex-col items-center">
            <h1 className="text-5xl font-extrabold mt-8 mb-4 text-center">
                Optimize Your Fantasy Premier League Team
            </h1>
            {currentEvent && (
                <h2 className="text-2xl font-bold mt-4 mb-4 text-center">
                    Optimizing for {currentEvent.id}
                </h2>
            )}
            <div className="w-full max-w-7xl mt-4">
                {/* Desktop Layout */}
                <div className="hidden md:flex flex-row">
                    <div className="w-1/2 p-4 flex items-center">
                        <p className="text-gray-600 text-xl leading-loose">
                            <strong>Welcome to the team optimizer!</strong> Follow these steps to ensure your Fantasy Premier League team is in top shape:
                            <br /><br />
                            <strong>1. Select your team formation:</strong> Choose from formations like 4-4-2 or 3-5-2 to set up your team structure.
                            <br /><br />
                            <strong>2. Assign players:</strong> Use the search boxes to find and assign players to each position on your team. Ensure all 15 positions are filled.
                            <br /><br />
                            <strong>3. Enter your budget:</strong> Enter your remaining budget. Remember when a player has increased in value, you will not receive the full sale price when transferring him out. Adjust your available budget accordingly to reflect this.
                            <br /><br />
                            <strong>4. Set your transfers:</strong> Specify the number of free transfers you have available for this gameweek.
                            <br /><br />
                            <strong>5. Optimize your team:</strong> Once everything is set, click the "Optimize Team" button to see the best possible transfers and lineup based on your input.
                            <br /><br />
                            This tool analyzes your selections and suggests the optimal transfers to maximize your team's performance using AI.
                        </p>
                    </div>
                    <div className="w-1/2 flex flex-col items-center">
                        <FormationBar
                            selectedFormation={selectedFormation}
                            onFormationChange={handleFormationChange}
                        />
                        <TeamLayout
                            formation={selectedFormation}
                            onTeamChange={handleTeamChange}
                            apiPlayers={apiPlayers}
                        />
                        <BudgetTransferBar
                            budget={budget}
                            transfers={transfers}
                            onBudgetChange={handleBudgetChange}
                            onTransfersChange={handleTransfersChange}
                        />
                        <div className="mt-4">
                            <OptimizeTeamButton onClick={handleOptimizeTeam} />
                        </div>
                        {error && (
                            <div className="text-red-500 mt-4 text-center">
                                {error}
                            </div>
                        )}
                    </div>
                </div>
                {/* Mobile Layout */}
                <div className="flex flex-col md:hidden">
                    <div className="flex flex-col items-center">
                        {/* Instructions */}
                        <p className="text-gray-600 text-base leading-loose mb-4 px-4">
                            <strong>Welcome to the team optimizer!</strong> Follow these steps to ensure your Fantasy Premier League team is in top shape:
                            <br /><br />
                            <strong>1. Select your team formation:</strong> Choose from formations like 4-4-2 or 3-5-2 to set up your team structure.
                            <br /><br />
                            <strong>2. Assign players:</strong> Use the search boxes to find and assign players to each position on your team. Ensure all 15 positions are filled.
                            <br /><br />
                            <strong>3. Enter your budget:</strong> Enter your remaining budget. Remember when a player has increased in value, you will not receive the full sale price when transferring him out. Adjust your available budget accordingly to reflect this.
                            <br /><br />
                            <strong>4. Set your transfers:</strong> Specify the number of free transfers you have available for this gameweek.
                            <br /><br />
                            <strong>5. Optimize your team:</strong> Once everything is set, click the "Optimize Team" button to see the best possible transfers and lineup based on your input.
                            <br /><br />
                            This tool analyzes your selections and suggests the optimal transfers to maximize your team's performance using AI.
                        </p>
                        <FormationBar
                            selectedFormation={selectedFormation}
                            onFormationChange={handleFormationChange}
                        />
                        <div className="w-full flex justify-center">
                            <TeamLayout
                                formation={selectedFormation}
                                onTeamChange={handleTeamChange}
                                apiPlayers={apiPlayers}
                            />
                        </div>
                        <BudgetTransferBar
                            budget={budget}
                            transfers={transfers}
                            onBudgetChange={handleBudgetChange}
                            onTransfersChange={handleTransfersChange}
                        />
                        <div className="mt-4">
                            <OptimizeTeamButton onClick={handleOptimizeTeam} />
                        </div>
                        {error && (
                            <div className="text-red-500 mt-4 text-center">
                                {error}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default OptimizeTeam;
