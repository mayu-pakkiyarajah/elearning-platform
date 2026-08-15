import { User } from './user.model';

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  requestedRole: 'STUDENT' | 'INSTRUCTOR';
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInMs: number;
  user: User;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: string[];
}
