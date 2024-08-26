import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar';
import Footer from './components/Footer'; // Importer Footer
import OptimizeTeam from './pages/OptimizeTeam';
import Predictions from './pages/Predictions';
import About from './pages/About';

function AppRoutes() {
    return (
        <Router>
            <div className="flex flex-col min-h-screen">
                <Navbar />
                <div className="flex-grow p-4">
                    <Routes>
                        <Route path="/" element={<OptimizeTeam />} />
                        <Route path="/predictions" element={<Predictions />} />
                        <Route path="/about" element={<About />} />
                    </Routes>
                </div>
                <Footer /> {/* Legg til Footer her */}
            </div>
        </Router>
    );
}

export default AppRoutes;
