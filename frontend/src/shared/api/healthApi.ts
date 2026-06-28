import { apiRequest } from './apiClient';

export function getHealth(): Promise<void> {
  return apiRequest<void>('/health');
}
