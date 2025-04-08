import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { HomePage } from './pages/HomePage';
import { GaragesPage } from './pages/GaragesPage';
import { BusesPage } from './pages/BusesPage';
import './App.css';

function App() {
  return (
    <Router>
      <div className="flex min-h-screen flex-col bg-indigo-50">
        <Navbar />
        <main className="flex-grow container mx-auto px-4 py-6">
          <div className="bg-white shadow-md rounded-lg p-6">
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/garages" element={<GaragesPage />} />
              <Route path="/buses" element={<BusesPage />} />
            </Routes>
          </div>
        </main>
        <footer className="bg-indigo-800 p-4 text-center text-white">
          <p>© {new Date().getFullYear()} IETT Bus Tracking System</p>
        </footer>
      </div>
    </Router>
  );
}

export default App;
