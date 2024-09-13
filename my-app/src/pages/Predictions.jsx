import React, { useEffect, useState } from 'react';
import PredictionList from '../components/PredictionList';

/**
 * The Predictions component fetches player data from the backend API
 * and displays a list of player predictions. It handles loading states
 * and potential errors during data fetching. Additionally, it displays
 * the current gameweek for which the predictions are made.
 */
const Predictions = () => {
    const [players, setPlayers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentEvent, setCurrentEvent] = useState(null); // State to store the current event

    useEffect(() => {
        /**
         * Fetches the current event and player data from the backend API.
         * Updates the state with the fetched data or handles errors if any occur.
         */
        const fetchData = async () => {
            try {
                // Fetch the current event from the API
                const eventResponse = await fetch('https://fplai.onrender.com/api/events/current');
                if (!eventResponse.ok) {
                    throw new Error(`Network response was not ok: ${eventResponse.statusText}`);
                }
                const eventData = await eventResponse.json();
                setCurrentEvent(eventData);

                // Fetch players from the API
                const playersResponse = await fetch('https://fplai.onrender.com/api/players');
                if (!playersResponse.ok) {
                    throw new Error(`Network response was not ok: ${playersResponse.statusText}`);
                }
                const playersData = await playersResponse.json();
                setPlayers(playersData);
                setLoading(false);
            } catch (error) {
                console.error('There was a problem with the fetch operation:', error);
                setError(error);
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error.message}</div>;
    }

    return (
        <div>
            {currentEvent && (
                <h2 className="text-2xl font-bold mt-4 mb-4 text-center">
                    Predictions for {currentEvent.id}
                </h2>
            )}
            <PredictionList players={players} />
        </div>
    );
};

export default Predictions;
