import React, { useEffect, useState } from 'react';
import PredictionList from '../components/PredictionList';  // Updated import path

/**
 * Predictions component fetches player data from the backend API
 * and displays a list of player predictions. Handles loading states
 * and potential errors during data fetching.
 */
const Predictions = () => {
    const [players, setPlayers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        // Fetch players from the API
        fetch('http://localhost:8080/api/players')
            .then(response => {
                console.log(response); // Log the entire response for debugging
                if (!response.ok) {
                    throw new Error(`Network response was not ok: ${response.statusText}`);
                }
                return response.text(); // Temporarily use .text() to inspect the content
            })
            .then(text => {
                console.log(text); // Log the text to see if it's JSON or HTML
                try {
                    const data = JSON.parse(text);
                    setPlayers(data);
                } catch (e) {
                    throw new Error("Failed to parse JSON: " + e.message);
                }
                setLoading(false);
            })
            .catch(error => {
                console.error('There was a problem with the fetch operation:', error);
                setError(error);
                setLoading(false);
            });
    }, []);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error.message}</div>;
    }

    return (
        <div>
            <PredictionList players={players} />
        </div>
    );
};

export default Predictions;
