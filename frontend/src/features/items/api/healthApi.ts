import { apiRequest } from '../../../shared/api/apiClient';

export type HealthResponse = {
  status: string;
  timestamp: string;
};

export function getHealth(): Promise<HealthResponse> {
  return apiRequest<HealthResponse>('/health');
}
