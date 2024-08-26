import React from 'react';

/**
 * FormationBar component allows the user to select a football formation.
 *
 * @param {Object} props - The component props.
 * @param {string} props.selectedFormation - The currently selected formation.
 * @param {Function} props.onFormationChange - Callback function to handle formation changes.
 * @param {string|number} props.layoutWidth - The width of the layout container to set the width of the FormationBar.
 */
const FormationBar = ({ selectedFormation, onFormationChange, layoutWidth }) => {
    // Array of possible formations
    const formations = ['343', '352', '433', '442', '451', '523', '532', '541'];

    return (
        <div
            className="bg-custom-purple p-4 rounded-lg border border-gray-900 mx-auto"
            style={{ width: layoutWidth, marginTop: '20px' }} // Set fixed width to match the layout container and add top margin
        >
            <div className="text-white font-semibold text-xl mb-2">Select Formation</div>
            <div className="flex flex-wrap gap-2 justify-center">
                {formations.map((formation) => (
                    <button
                        key={formation}
                        onClick={() => onFormationChange(formation)}
                        className={`px-4 py-2 rounded-lg text-white border border-gray-900 ${
                            selectedFormation === formation ? 'bg-gray-700' : 'bg-gray-900'
                        }`}
                    >
                        {formation}
                    </button>
                ))}
            </div>
        </div>
    );
};

export default FormationBar;
