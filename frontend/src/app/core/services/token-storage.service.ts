import { Injectable } from '@angular/core';
import { User } from '../models/user.model';

const ACCESS_TOKEN_KEY = 'elp_access_token';
const REFRESH_TOKEN_KEY = 'elp_refresh_token';
const USER_KEY = 'elp_user';

/**
 * Centralizes all localStorage access for auth state so nothing else in the app
 * touches `window.localStorage` directly.
 *
 * Security note: storing tokens in localStorage is convenient for a portfolio
 * project but is readable by any JS on the page (XSS risk). A production
 * hardening step would be to move the refresh token into an httpOnly cookie
 * set by the backend, and keep only the short-lived access token in memory.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  getUser(): User | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as User) : null;
  }

  setSession(accessToken: string, refreshToken: string, user: User): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  updateAccessToken(accessToken: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  }

  clear(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }
}
