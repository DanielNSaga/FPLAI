import React from 'react';

/**
 * PredictionList component that displays a list of players sorted by their predicted points.
 *
 * @param {Object} props - The component's props.
 * @param {Array} props.players - The list of players to display, each with a prediction for the next gameweek.
 * @returns {JSX.Element} The rendered list of player predictions.
 */
const PredictionList = ({ players }) => {
    // Sort players by their predictions in descending order
    const sortedPlayers = [...players].sort((a, b) => b.prediction - a.prediction);

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <h1 className="text-3xl font-bold text-gray-900 mb-6 text-center">Player Predictions</h1>
            <div className="bg-white shadow overflow-hidden sm:rounded-lg">
                <ul className="divide-y divide-gray-200">
                    {sortedPlayers.map((player) => (
                        <li key={player.id} className="px-4 py-5 sm:px-6">
                            <div className="flex items-center justify-between">
                                <div className="flex-1 flex items-center">
                                    <div className="min-w-0 flex-1">
                                        <h2 className="text-xl font-bold text-custom-purple truncate">{player.web_name}</h2>
                                        <p className="mt-1 text-sm text-gray-500">
                                            Position: {player.element_type === 1 ? 'Goalkeeper' : player.element_type === 2 ? 'Defender' : player.element_type === 3 ? 'Midfielder' : 'Forward'}
                                        </p>
                                    </div>
                                </div>
                                <div className="flex items-center">
                                    <div className="text-right">
                                        <p className="text-sm text-gray-500">Prediction for next gameweek</p>
                                        <p className="text-2xl font-bold text-gray-900">{player.prediction.toFixed(2)}</p>
                                    </div>
                                </div>
                            </div>
                            <div className="mt-3 grid grid-cols-2 gap-4">
                                <div>
                                    <p className="text-sm font-semibold text-gray-900">Form</p>
                                    <p className="text-sm text-gray-700">{player.form}</p>
                                </div>
                                <div>
                                    <p className="text-sm font-semibold text-gray-900">Total Points</p>
                                    <p className="text-sm text-gray-700">{player.total_points}</p>
                                </div>
                            </div>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
};

export default PredictionList;
