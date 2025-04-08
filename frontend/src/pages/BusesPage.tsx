import { useEffect, useState } from 'react';
import { Bus, SearchResponse } from '../types';
import { fetchBuses, searchBuses } from '../services/api';
import { EmptyState } from '../components/EmptyState';
import { Loading } from '../components/Loading';
import { SearchBar } from '../components/SearchBar';

export function BusesPage() {
  const [buses, setBuses] = useState<Bus[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResponse, setSearchResponse] = useState<SearchResponse<Bus> | null>(null);

  const loadBuses = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchBuses();
      setBuses(data);
      setSearchResponse(null);
    } catch (err) {
      setError('Failed to load buses. Please try again later.');
      console.error('Error loading buses:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBuses();
  }, []);

  const handleSearch = async (term: string) => {
    setSearchTerm(term);
    
    if (!term.trim()) {
      // If search term is empty, reset to showing all buses
      loadBuses();
      return;
    }
    
    try {
      setLoading(true);
      setError(null);
      const response = await searchBuses(term);
      setSearchResponse(response);
      setBuses(response.results);
    } catch (err) {
      setError('Failed to search buses. Please try again later.');
      console.error('Error searching buses:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = (dateTimeStr: Array<number>) => {
    /**
     * const dateArray = [
  2025,
  4,
  8,
  14,
  4,
  32,
  634082000
];
const date = new Date(
  dateArray[0], // year
  dateArray[1] - 1, // month (0-based)
  dateArray[2], // day
  dateArray[3], // hours
  dateArray[4], // minutes
  dateArray[5], // seconds
  Math.floor(dateArray[6] / 1e6) // milliseconds from nanoseconds
)
undefined
date.toLocaleDateString()
'4/8/2025'
date.toTimeString()
'14:04:32 GMT+0300 (GMT+03:00)'
     */
    try {
      const date = new Date(
        dateTimeStr[0], // year
        dateTimeStr[1] - 1, // month (0-based)
        dateTimeStr[2], // day
        dateTimeStr[3], // hours
        dateTimeStr[4], // minutes
        dateTimeStr[5], // seconds
        Math.floor(dateTimeStr[6] / 1e6) // milliseconds from nanoseconds
      );
      return `${date.toLocaleDateString()} ${date.toLocaleTimeString()}`;
    } catch {
      return dateTimeStr;
    }
  };

  const formatDistance = (distance: number | undefined) => {
    if (distance === undefined) return 'N/A';
    return `${distance.toFixed(2)} km`;
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="mb-2 text-3xl font-bold text-indigo-900">Buses</h1>
        <p className="text-indigo-700">Search and explore IETT buses</p>
      </div>

      <SearchBar 
        onSearch={handleSearch} 
        placeholder="Search by door number, operator, garage, or license plate..." 
      />

      {error && (
        <div className="mb-4 rounded-md bg-red-50 p-4 text-red-800">
          {error}
        </div>
      )}

      {loading ? (
        <Loading />
      ) : buses.length === 0 && !error ? (
        <EmptyState message={searchTerm ? `No buses found matching "${searchTerm}"` : "No buses available."} />
      ) : !error && (
        <div className="overflow-x-auto">
          <div className="overflow-hidden rounded-xl border border-indigo-100 shadow-md">
            <table className="min-w-full divide-y divide-indigo-200">
              <thead className="bg-indigo-50">
                <tr>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-indigo-700">
                    Door No
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-indigo-700">
                    Operator
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-indigo-700">
                    Garage
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-indigo-700">
                    License Plate
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-indigo-700">
                    Speed
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-indigo-700">
                    Last Updated
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-indigo-700">
                    Nearest Garage
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-indigo-100 bg-white">
                {buses.map((bus, index) => (
                  <tr key={`${bus.doorNo}-${index}`} className="hover:bg-indigo-50">
                    <td className="whitespace-nowrap px-6 py-4">{bus.doorNo ? `${bus.doorNo}`: 'N/A'}</td>
                    <td className="px-6 py-4">{bus.operator}</td>
                    <td className="px-6 py-4">{bus.garage}</td>
                    <td className="px-6 py-4">{bus.licensePlate}</td>
                    <td className="px-6 py-4">{bus.speed ? `${bus.speed} km/h` : 'N/A'}</td>
                    <td className="px-6 py-4">{bus.time ? formatDateTime(bus.time) : 'N/A'}</td>
                    <td className="px-6 py-4">
                      {bus.nearestGarageName ? (
                        <div>
                          <div>{bus.nearestGarageName}</div>
                          <div className="text-sm text-indigo-500">
                            {formatDistance(bus.distanceToNearestGarage)}
                          </div>
                        </div>
                      ) : (
                        'N/A'
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {searchResponse && !error && (
        <div className="mt-4 text-sm text-indigo-700">
          Showing {searchResponse.count} of {searchResponse.totalCount} results 
          {searchResponse.searchTerm && ` for "${searchResponse.searchTerm}"`}
        </div>
      )}
    </div>
  );
} 