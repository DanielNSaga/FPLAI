import React from 'react';

/**
 * OptimizeTeamButton component that renders a button for triggering the team optimization process.
 *
 * @param {Object} props - The component's props.
 * @param {Function} props.onClick - The function to call when the button is clicked.
 * @returns {JSX.Element} The button element.
 */
const OptimizeTeamButton = ({ onClick }) => {
    return (
        <button
            onClick={onClick}
            className="bg-gray-900 text-white font-semibold py-2 px-4 rounded-lg hover:bg-gray-700 transition duration-200"
            style={{ marginTop: '20px', width: '100%', maxWidth: '200px' }}
        >
            Optimize Team
        </button>
    );
};

export default OptimizeTeamButton;
