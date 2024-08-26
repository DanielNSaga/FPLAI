import React from 'react';

/**
 * The About component provides an overview of the Fantasy Premier League Optimizer,
 * explaining the AI model, backend system, and API integration.
 * It is structured with different sections, each detailing a specific aspect of the application.
 */
const About = () => {
    return (
        <div className="bg-gray-100 p-8">
            <div className="max-w-7xl mx-auto">
                <header className="text-center mb-12">
                    <h1 className="text-4xl font-extrabold text-gray-900 mb-4">About Our Fantasy Premier League Optimizer</h1>
                    <p className="text-lg text-gray-600">
                        Discover how our advanced AI-driven tool helps you optimize your Fantasy Premier League (FPL) team with precision and ease.
                    </p>
                </header>

                {/* Section explaining how the AI model works */}
                <section className="mb-12">
                    <h2 className="text-2xl font-bold text-gray-800 mb-4">How the AI Model Works</h2>
                    <p className="text-gray-700 leading-relaxed mb-4">
                        Our AI model, developed using Python, leverages machine learning to analyze vast amounts of historical FPL data. By examining metrics such as player performance, opponent difficulty, and team rankings, the AI provides accurate predictions on future player performances.
                    </p>
                    <p className="text-gray-700 leading-relaxed mb-4">
                        The model utilizes multiple advanced algorithms, including Random Forest, XGBoost, LightGBM, and CatBoost, combined in a stacking model. This approach maximizes prediction accuracy by harnessing the strengths of each algorithm. The predictions are processed by a Flask server and returned to the application to help you make informed decisions.
                    </p>
                </section>

                {/* Section providing an overview of the backend system */}
                <section className="mb-12">
                    <h2 className="text-2xl font-bold text-gray-800 mb-4">Backend System Overview</h2>
                    <p className="text-gray-700 leading-relaxed mb-4">
                        The backend of our application is built using Spring Boot, a powerful Java framework that handles the business logic, data management, and integration with the AI model. Here’s how it works:
                    </p>
                    <ul className="list-disc list-inside text-gray-700 leading-relaxed mb-4">
                        <li><strong>Data Management:</strong> Fetches and processes player, team, and fixture data from the official Fantasy Premier League API.</li>
                        <li><strong>Prediction Service:</strong> Interacts with the AI model to retrieve predictions and integrates them into the application.</li>
                        <li><strong>Transfer Optimization:</strong> Analyzes your team, suggests optimal transfers, and ensures compliance with FPL rules.</li>
                        <li><strong>Team Optimization:</strong> Automatically selects the best players within budget constraints to maximize points for upcoming gameweeks.</li>
                    </ul>
                </section>

                {/* Section detailing API integration */}
                <section className="mb-12">
                    <h2 className="text-2xl font-bold text-gray-800 mb-4">API Integration</h2>
                    <p className="text-gray-700 leading-relaxed mb-4">
                        The backend exposes a RESTful API to handle various operations, including:
                    </p>
                    <ul className="list-disc list-inside text-gray-700 leading-relaxed mb-4">
                        <li><strong>Fetching Data:</strong> Retrieves up-to-date player, team, and fixture data from external sources.</li>
                        <li><strong>Interacting with the AI Model:</strong> Sends data to the Flask server and receives predictions.</li>
                        <li><strong>Optimizing Teams:</strong> Processes team data, applies predictions, and suggests optimal transfers.</li>
                    </ul>
                </section>

                {/* Closing section summarizing the capabilities of the application */}
                <section>
                    <p className="text-gray-700 leading-relaxed">
                        By combining the capabilities of Spring Boot for backend management and a Python-based AI model for predictions, our application offers a comprehensive toolset for serious Fantasy Premier League players. Whether you're looking to fine-tune your team or make strategic transfers, this system is designed to help you succeed.
                    </p>
                </section>
            </div>
        </div>
    );
};

export default About;
