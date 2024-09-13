import React, { useState } from 'react';
import FormationBar from '../components/FormationBar';
import TeamLayout from '../components/TeamLayout';
import BudgetTransferBar from '../components/BudgetTransferBar';
import OptimizeTeamButton from "../components/OptimizeTeamButton";
import axios from 'axios';

/**
 * OptimizeTeam komponent lar brukere optimalisere sitt Fantasy Premier League-lag
 * ved å velge formasjon, tildele spillere, sette budsjett og spesifisere overføringer.
 * Komponenten kommuniserer med en backend API for å optimalisere laget basert på brukerens input.
 */
const OptimizeTeam = () => {
    const [budget, setBudget] = useState('');
    const [transfers, setTransfers] = useState('');
    const [selectedFormation, setSelectedFormation] = useState('442');
    const [team, setTeam] = useState([]); // State for å lagre laget
    const [error, setError] = useState('');
    const [apiPlayers, setApiPlayers] = useState([]); // State for å lagre spillere fra API-respons

    const handleBudgetChange = (e) => setBudget(e.target.value);
    const handleTransfersChange = (e) => setTransfers(e.target.value);
    const handleFormationChange = (formation) => setSelectedFormation(formation);

    const handleTeamChange = (updatedTeam) => {
        setTeam(updatedTeam);
    };

    /**
     * Håndterer optimaliseringen av laget ved å sende gjeldende lagoppsett, budsjett og overføringer
     * til backend API, som returnerer det optimaliserte lagoppsettet.
     */
    const handleOptimizeTeam = async () => {
        // Sjekk om alle nødvendige input er fylt ut og alle 15 spillere er tildelt
        const isValid = budget && transfers && team.length === 15 && !team.some(box => !box.player);

        if (!isValid) {
            setError('Vennligst sørg for at alle felt er fylt ut, inkludert budsjett, overføringer og alle 15 spillere.');
            return;
        }

        setError('');

        // Forbered forespørselsdata
        const playerIds = team.map(box => box.player.id);
        const teamRequest = {
            budget: parseFloat(budget) * 10,
            transfers: parseInt(transfers, 10),
            playerIds: playerIds
        };

        try {
            // Send en POST-forespørsel for å optimalisere laget
            const response = await axios.post('https://fplai.onrender.com/api/teams/optimize', teamRequest);
            setApiPlayers(response.data.players); // Oppdater spillerne med API-responsen
        } catch (error) {
            console.error('Feil ved optimalisering av laget:', error.response ? error.response.data : error.message);
            setError('En feil oppstod under optimalisering av laget. Vennligst prøv igjen.');
        }
    };

    return (
        <div className="flex flex-col items-center">
            <h1 className="text-5xl font-extrabold mt-8 mb-4 text-center">
                Optimize Your Fantasy Premier League Team
            </h1>
            <div className="w-full max-w-7xl mt-4">
                {/* PC-layout */}
                <div className="hidden md:flex flex-row">
                    <div className="w-1/2 p-4 flex items-center">
                        <p className="text-gray-600 text-xl leading-loose">
                            <strong>Welcome to the team optimizer!</strong> Follow these steps to ensure your Fantasy Premier League team is in top shape:
                            <br /><br />
                            <strong>1. Select your team formation:</strong> Choose from the formations like 4-4-2 or 3-5-2 to set up your team structure.
                            <br /><br />
                            <strong>2. Assign players:</strong> Use the search boxes to find and assign players to each position on your team. Ensure all 15 positions are filled.
                            <br /><br />
                            <strong>3. Enter your budget:</strong> Enter your remaining budget. Remember when a player has increased in value, you will not receive the full sale price when transferring him out. Adjust your available budget accordingly to reflect this.
                            <br /><br />
                            <strong>4. Set your transfers:</strong> Specify the number of free transfers you have available for this gameweek.
                            <br /><br />
                            <strong>5. Optimize your team:</strong> Once everything is set, click the "Optimize Team" button to see the best possible transfers and lineup based on your input.
                            <br /><br />
                            This tool analyzes your selections and suggests the optimal transfers to maximize your team's performance by using AI.
                        </p>
                    </div>
                    <div className="w-1/2 p-4 flex flex-col items-center">
                        <FormationBar
                            selectedFormation={selectedFormation}
                            onFormationChange={handleFormationChange}
                        />
                        <TeamLayout
                            formation={selectedFormation}
                            onTeamChange={handleTeamChange}
                            apiPlayers={apiPlayers} // Send API-spillerne her
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
                {/* Mobil-layout */}
                <div className="flex flex-col md:hidden">
                    <div className="p-4 flex flex-col items-center">
                        <h1 className="text-3xl font-extrabold mt-4 mb-4 text-center">
                            Optimize Your Fantasy Premier League Team
                        </h1>
                        <p className="text-gray-600 text-base leading-loose mb-4">
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
                        <TeamLayout
                            formation={selectedFormation}
                            onTeamChange={handleTeamChange}
                            apiPlayers={apiPlayers} // Send API-spillerne her
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
        </div>
    );
};

export default OptimizeTeam;
