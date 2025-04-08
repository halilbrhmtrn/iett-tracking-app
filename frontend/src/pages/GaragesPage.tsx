import { useEffect, useState } from 'react';
import { Garage, SearchResponse } from '../types';
import { fetchGarages, searchGarages } from '../services/api';
import { EmptyState } from '../components/EmptyState';
import { Loading } from '../components/Loading';
import { SearchBar } from '../components/SearchBar';

export function GaragesPage() {
  const [garages, setGarages] = useState<Garage[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResponse, setSearchResponse] = useState<SearchResponse<Garage> | null>(null);

  const loadGarages = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchGarages();
      setGarages(data);
      setSearchResponse(null);
    } catch (err) {
      setError('Failed to load garages. Please try again later.');
      console.error('Error loading garages:', err);
    } finally {
      setLoading(false);
    }
  };

  // Load garages on mount
  useEffect(() => {
    loadGarages();
  }, []);

  const handleSearch = async (term: string) => {
    setSearchTerm(term);
    
    if (!term.trim()) {
      // If search term is empty, reset to showing all garages
      loadGarages();
      return;
    }
    
    try {
      setLoading(true);
      setError(null);
      const response = await searchGarages(term);
      setSearchResponse(response);
      setGarages(response.results);
    } catch (err) {
      setError('Failed to search garages. Please try again later.');
      console.error('Error searching garages:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="mb-2 text-3xl font-bold text-indigo-900">Garages</h1>
        <p className="text-indigo-700">Search and explore IETT garages</p>
      </div>

      <SearchBar 
        onSearch={handleSearch} 
        placeholder="Search by ID, garage name, or garage code..." 
      />

      {error && (
        <div className="mb-4 rounded-md bg-red-50 p-4 text-red-800">
          {error}
        </div>
      )}

      {loading ? (
        <Loading />
      ) : garages.length === 0 && !error ? (
        <EmptyState message={searchTerm ? `No garages found matching "${searchTerm}"` : "No garages available."} />
      ) : !error && (
        <div className="overflow-hidden rounded-xl border border-indigo-100 shadow-md">
          <table className="min-w-full divide-y divide-indigo-200">
            <thead className="bg-indigo-50">
              <tr>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-indigo-700">
                  ID
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-indigo-700">
                  Garage Name
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-indigo-700">
                  Garage Code
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-indigo-700">
                  Coordinates
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-indigo-100 bg-white">
              {garages.map((garage) => (
                <tr key={garage.id} className="hover:bg-indigo-50">
                  <td className="whitespace-nowrap px-6 py-4">{garage.id}</td>
                  <td className="px-6 py-4">{garage.garageName}</td>
                  <td className="px-6 py-4">{garage.garageCode}</td>
                  <td className="px-6 py-4">{garage.coordinate}</td>
                </tr>
              ))}
            </tbody>
          </table>
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