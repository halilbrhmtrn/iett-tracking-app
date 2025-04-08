import { Link } from 'react-router-dom';

export function HomePage() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center text-center">
      <h1 className="mb-6 text-4xl font-bold text-indigo-900">IETT Bus Tracking System</h1>
      <p className="mb-8 max-w-2xl text-xl text-indigo-700">
        Welcome to the Istanbul Electric Tramway and Tunnel Operations (IETT) Bus Tracking System.
        This system allows you to explore garages and buses operated by IETT.
      </p>
      <div className="flex flex-col space-y-4 sm:flex-row sm:space-y-0 sm:space-x-4">
        <Link
          to="/garages"
          className="rounded-md bg-indigo-600 px-8 py-3 text-white shadow-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
        >
          Explore Garages
        </Link>
        <Link
          to="/buses"
          className="rounded-md bg-indigo-600 px-8 py-3 text-white shadow-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
        >
          Explore Buses
        </Link>
      </div>
    </div>
  );
} 