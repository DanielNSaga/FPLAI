import React, { useState } from 'react';
import FormationBar from '../components/FormationBar';
import TeamLayout from '../components/TeamLayout';
import BudgetTransferBar from '../components/BudgetTransferBar';
import OptimizeTeamButton from "../components/OptimizeTeamButton";
import axios from 'axios';

/**
 * OptimizeTeam component allows users to optimize their Fantasy Premier League team
 * by selecting a formation, assigning players, setting a budget, and specifying transfers.
 * The component communicates with a backend API to optimize the team based on the user's inputs.
 */
const OptimizeTeam = () => {
    const [budget, setBudget] = useState('');
    const [transfers, setTransfers] = useState('');
    const [selectedFormation, setSelectedFormation] = useState('442');
    const [team, setTeam] = useState([]); // State to store the team
    const [error, setError] = useState('');
    const [apiPlayers, setApiPlayers] = useState([]); // State to store players from API response

    const handleBudgetChange = (e) => setBudget(e.target.value);
    const handleTransfersChange = (e) => setTransfers(e.target.value);
    const handleFormationChange = (formation) => setSelectedFormation(formation);

    const handleTeamChange = (updatedTeam) => {
        setTeam(updatedTeam);
    };

    /**
     * Handles the optimization of the team by sending the current team setup, budget, and transfers
     * to the backend API, which returns the optimized team configuration.
     */
    const handleOptimizeTeam = async () => {
        // Check if all necessary inputs are filled and all 15 players are assigned
        const isValid = budget && transfers && team.length === 15 && !team.some(box => !box.player);

        if (!isValid) {
            setError('Please make sure all inputs are filled, including budget, transfers, and all 15 players.');
            return;
        }

        setError('');

        // Prepare the request payload
        const playerIds = team.map(box => box.player.id);
        const teamRequest = {
            budget: parseFloat(budget) * 10,
            transfers: parseInt(transfers, 10),
            playerIds: playerIds
        };

        try {
            // Send a POST request to optimize the team
            const response = await axios.post('https://fplai.onrender.com/api/teams/optimize', teamRequest);
            setApiPlayers(response.data.players); // Update the players with the API response
        } catch (error) {
            console.error('Error optimizing team:', error.response ? error.response.data : error.message);
            setError('An error occurred while optimizing the team. Please try again.');
        }
    };

    return (
        <div className="flex flex-col items-center">
            <h1 className="text-3xl md:text-5xl font-extrabold mt-8 mb-4 text-center">
                Optimize Your Fantasy Premier League Team
            </h1>
            <div className="flex flex-col md:flex-row w-full max-w-7xl mt-4">
                <div className="w-full md:w-1/2 p-4 flex items-center">
                    <p className="text-gray-600 text-base md:text-xl leading-loose">
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

                <div className="w-full md:w-1/2 p-4 flex flex-col items-center">
                    <FormationBar
                        selectedFormation={selectedFormation}
                        onFormationChange={handleFormationChange}
                    />
                    <TeamLayout
                        formation={selectedFormation}
                        onTeamChange={handleTeamChange}
                        apiPlayers={apiPlayers} // Send the API players here
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
        </div>
    );
};

export default OptimizeTeam;
