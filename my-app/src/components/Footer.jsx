import React from 'react';

/**
 * Footer component that renders a footer section at the bottom of the page.
 * The footer includes the current year and a copyright notice.
 *
 * @returns {JSX.Element} The footer element.
 */
function Footer() {
    return (
        <footer className="bg-custom-purple text-white py-10 text-center w-full mt-auto">
            <div className="container mx-auto">
                <p>&copy; {new Date().getFullYear()} Fantasy Premier League Optimizer. All rights reserved.</p>
            </div>
        </footer>
    );
}

export default Footer;
