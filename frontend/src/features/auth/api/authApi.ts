import { apiRequest } from '../../../shared/api/apiClient';
import type { LoginResponse, LoginValues, SignupValues } from '../../../shared/types/user';

export function login(values: LoginValues): Promise<LoginResponse> {
  return apiRequest<LoginResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(values),
  });
}

export function signup(values: SignupValues): Promise<LoginResponse> {
  return apiRequest<LoginResponse>('/auth/signup', {
    method: 'POST',
    body: JSON.stringify(values),
  });
}
