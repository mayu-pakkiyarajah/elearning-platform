import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
} from '../models/auth.model';
import { User } from '../models/user.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private tokenStorage = inject(TokenStorageService);

  private readonly baseUrl = `${environment.authApiUrl}/auth`;

  // Reactive current-user state — components/guards read this instead of
  // re-parsing localStorage everywhere.
  private readonly currentUserSignal = signal<User | null>(this.tokenStorage.getUser());
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isLoggedIn = computed(() => this.currentUserSignal() !== null);

  register(request: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/register`, request);
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request).pipe(
      tap((res) => this.persistSession(res)),
    );
  }

  refresh(): Observable<AuthResponse> {
    const refreshToken = this.tokenStorage.getRefreshToken();
    return this.http.post<AuthResponse>(`${this.baseUrl}/refresh`, { refreshToken }).pipe(
      tap((res) => this.persistSession(res)),
    );
  }

  logout(): void {
    const refreshToken = this.tokenStorage.getRefreshToken();
    if (refreshToken) {
      // fire-and-forget: even if this fails (e.g. offline), clear local state anyway
      this.http.post(`${this.baseUrl}/logout`, { refreshToken }).subscribe({
        error: () => {},
      });
    }
    this.tokenStorage.clear();
    this.currentUserSignal.set(null);
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/reset-password`, { token, newPassword });
  }

  getAccessToken(): string | null {
    return this.tokenStorage.getAccessToken();
  }

  getRefreshToken(): string | null {
    return this.tokenStorage.getRefreshToken();
  }

  hasRole(role: string): boolean {
    return this.currentUserSignal()?.roles.includes(role) ?? false;
  }

  /** Used by the auth interceptor after a successful silent token refresh. */
  updateAccessTokenOnly(accessToken: string): void {
    this.tokenStorage.updateAccessToken(accessToken);
  }

  private persistSession(res: AuthResponse): void {
    this.tokenStorage.setSession(res.accessToken, res.refreshToken, res.user);
    this.currentUserSignal.set(res.user);
  }
}
