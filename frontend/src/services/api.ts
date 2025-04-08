import axios from 'axios';
import { Bus, Garage, SearchResponse } from '../types';

// Create axios instance with base URL
const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

// Garage API
export const fetchGarages = async (page = 0, size = 20): Promise<Garage[]> => {
  try {
    const response = await apiClient.get<Garage[]>(`/garages?page=${page}&size=${size}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching garages:', error);
    throw error;
  }
};

export const searchGarages = async (term: string, page = 0, size = 20): Promise<SearchResponse<Garage>> => {
  try {
    const response = await apiClient.get<SearchResponse<Garage>>(
      `/garages/search?term=${encodeURIComponent(term)}&page=${page}&size=${size}`
    );
    return response.data;
  } catch (error) {
    console.error('Error searching garages:', error);
    throw error;
  }
};

export const refreshGarages = async (): Promise<Garage[]> => {
  try {
    const response = await apiClient.get<Garage[]>('/garages/refresh');
    return response.data;
  } catch (error) {
    console.error('Error refreshing garages:', error);
    throw error;
  }
};

// Bus API
export const fetchBuses = async (page = 0, size = 20): Promise<Bus[]> => {
  try {
    const response = await apiClient.get<Bus[]>(`/buses?page=${page}&size=${size}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching buses:', error);
    throw error;
  }
};

export const searchBuses = async (term: string, page = 0, size = 20): Promise<SearchResponse<Bus>> => {
  try {
    const response = await apiClient.get<SearchResponse<Bus>>(
      `/buses/search?term=${encodeURIComponent(term)}&page=${page}&size=${size}`
    );
    return response.data;
  } catch (error) {
    console.error('Error searching buses:', error);
    throw error;
  }
};

export const refreshBuses = async (): Promise<Bus[]> => {
  try {
    const response = await apiClient.get<Bus[]>('/buses/refresh');
    return response.data;
  } catch (error) {
    console.error('Error refreshing buses:', error);
    throw error;
  }
}; 