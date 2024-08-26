import React from 'react';

/**
 * Component to handle budget and transfer inputs for a fantasy football team.
 *
 * @param {Object} props - The component props.
 * @param {number|string} props.budget - The current budget value.
 * @param {number|string} props.transfers - The current transfers value.
 * @param {Function} props.onBudgetChange - Callback function to handle budget changes.
 * @param {Function} props.onTransfersChange - Callback function to handle transfers changes.
 */
const BudgetTransferBar = ({ budget, transfers, onBudgetChange, onTransfersChange }) => {

    /**
     * Handles the change in the budget input.
     * Ensures that the value is formatted correctly and within valid ranges.
     *
     * @param {Object} event - The input change event.
     */
    const handleBudgetChange = (event) => {
        let value = event.target.value;

        if (value === '') {
            // Set value to an empty string when the field is empty
            onBudgetChange({ target: { value: '' } });
        } else {
            value = value.replace(',', '.'); // Replace commas with dots for decimal

            const parsedValue = parseFloat(value);
            if (!isNaN(parsedValue)) {
                const roundedValue = Math.round(parsedValue * 10) / 10; // Round to nearest 0.1
                onBudgetChange({ target: { value: roundedValue } });
            }
        }
    };

    /**
     * Handles the change in the transfers input.
     * Ensures that the value is an integer and within the valid range.
     *
     * @param {Object} event - The input change event.
     */
    const handleTransfersChange = (event) => {
        let value = event.target.value;

        if (value === '') {
            // Set value to an empty string when the field is empty
            onTransfersChange({ target: { value: '' } });
        } else {
            value = parseInt(value, 10);

            if (!isNaN(value) && value >= 1 && value <= 15) {
                onTransfersChange({ target: { value } });
            }
        }
    };

    // Format the budget display
    const formattedBudget = budget !== '' && !isNaN(parseFloat(budget))
        ? `£${parseFloat(budget).toFixed(1).replace(/\.0$/, '')}m`
        : '';

    // Format the transfers display
    const formattedTransfers = transfers !== '' && !isNaN(parseInt(transfers, 10))
        ? `${transfers} Transfer${transfers > 1 ? 's' : ''}`
        : '';

    return (
        <div
            className="bg-custom-purple p-1 rounded-lg border border-gray-900 mx-auto mt-1"
            style={{ width: '60%', maxWidth: '400px' }}
        >
            <div className="flex flex-col md:flex-row justify-between items-center gap-1">
                <div className="flex flex-col items-center w-full">
                    <label className="text-white font-semibold mb-1 text-sm">Budget</label>
                    <input
                        type="number"
                        step="0.1"
                        value={budget}
                        onChange={handleBudgetChange}
                        className="border border-gray-400 p-1 rounded-lg w-full text-black text-sm"
                        placeholder="Enter budget"
                        min="0"
                        style={{ width: '100%' }}
                    />
                    {formattedBudget && (
                        <div className="text-white font-semibold mt-1 text-sm">
                            {formattedBudget}
                        </div>
                    )}
                </div>
                <div className="flex flex-col items-center w-full">
                    <label className="text-white font-semibold mb-1 text-sm">Transfers</label>
                    <input
                        type="number"
                        step="1"
                        value={transfers}
                        onChange={handleTransfersChange}
                        className="border border-gray-400 p-1 rounded-lg w-full text-black text-sm"
                        placeholder="Enter transfers"
                        min="1"
                        max="15"  // Maximum value set to 15
                        style={{ width: '100%' }}
                    />
                    {formattedTransfers && (
                        <div className="text-white font-semibold mt-1 text-sm">
                            {formattedTransfers}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default BudgetTransferBar;
