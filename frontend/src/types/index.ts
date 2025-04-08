export interface Garage {
  id: number;
  garageName: string;
  garageCode: string;
  coordinate: string;
}

export interface Bus {
  doorNo: string;
  operator: string;
  garage: string;
  latitude: number;
  longitude: number;
  speed: number;
  licensePlate: string;
  time: Array<number>;
  nearestGarageCode?: string;
  nearestGarageName?: string;
  distanceToNearestGarage?: number;
}

export interface SearchResponse<T> {
  results: T[];
  count: number;
  totalCount: number;
  page: number;
  size: number;
  searchTerm: string;
  hasMatches: boolean;
} 