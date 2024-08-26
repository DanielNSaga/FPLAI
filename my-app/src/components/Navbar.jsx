import React from 'react';
import { Link } from 'react-router-dom';

/**
 * Navbar component that renders a navigation bar with links to different pages.
 *
 * @returns {JSX.Element} The navbar element.
 */
const Navbar = () => {
    return (
        <nav className="bg-custom-purple p-9">
            <div className="max-w-96xl mx-auto flex justify-between items-center">
                {/* Link to the home page */}
                <Link to="/" className="text-white text-xl font-bold font-poppins hover:text-gray-300">
                    Home
                </Link>
                <div className="space-x-4">
                    {/* Link to the predictions page */}
                    <Link to="/predictions" className="text-white text-lg font-poppins hover:text-gray-300">
                        Predictions
                    </Link>
                    {/* Link to the about page */}
                    <Link to="/about" className="text-white text-lg font-poppins hover:text-gray-300">
                        About
                    </Link>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
