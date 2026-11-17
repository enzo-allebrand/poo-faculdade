import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom, tap } from 'rxjs';
import { API_BASE_URL } from './api.config';
import { LoginPayload, RegisterPayload, User } from '../models/financeiro.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly user = signal<User | null>(null);
  private sessionPromise: Promise<boolean> | null = null;

  /**
   * Verifica no backend se o cookie HttpOnly contém uma sessão JWT válida.
   */
  async ensureSession(): Promise<boolean> {
    if (this.user() !== null) {
      return true;
    }
    if (!this.sessionPromise) {
      this.sessionPromise = firstValueFrom(
        this.http.get<User>(`${API_BASE_URL}/auth/me`),
      )
        .then((user) => {
          this.user.set(user);
          return true;
        })
        .catch(() => {
          this.user.set(null);
          return false;
        })
        .finally(() => {
          this.sessionPromise = null;
        });
    }
    return this.sessionPromise;
  }

  login(payload: LoginPayload) {
    return this.http
      .post<User>(`${API_BASE_URL}/auth/login`, payload)
      .pipe(tap((user) => this.user.set(user)));
  }

  register(payload: RegisterPayload) {
    return this.http
      .post<User>(`${API_BASE_URL}/auth/register`, payload)
      .pipe(tap((user) => this.user.set(user)));
  }

  logout(): void {
    this.http.post(`${API_BASE_URL}/auth/logout`, {}).subscribe({
      next: () => this.clearAndRedirect(),
      error: () => this.clearAndRedirect(),
    });
  }

  clearSession(): void {
    this.user.set(null);
  }

  private clearAndRedirect(): void {
    this.user.set(null);
    this.router.navigate(['/login']);
  }
}
