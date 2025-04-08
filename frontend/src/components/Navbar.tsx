import { Link, useLocation } from 'react-router-dom';

export function Navbar() {
  const location = useLocation();

  const isActive = (path: string) => {
    return location.pathname === path;
  };

  return (
    <nav className="bg-indigo-800 p-4 text-white mb-4 shadow-md">
      <div className="container mx-auto flex items-center justify-between">
        <Link to="/" className="text-xl font-bold">
          IETT Bus Tracking System
        </Link>
        <div className="flex space-x-4">
          <Link
            to="/garages"
            className={`px-3 py-2 rounded-md text-sm font-medium ${
              isActive('/garages')
                ? 'bg-indigo-600 text-white'
                : 'text-indigo-200 hover:bg-indigo-700 hover:text-white'
            }`}
          >
            Garages
          </Link>
          <Link
            to="/buses"
            className={`px-3 py-2 rounded-md text-sm font-medium ${
              isActive('/buses')
                ? 'bg-indigo-600 text-white'
                : 'text-indigo-200 hover:bg-indigo-700 hover:text-white'
            }`}
          >
            Buses
          </Link>
        </div>
      </div>
    </nav>
  );
} 