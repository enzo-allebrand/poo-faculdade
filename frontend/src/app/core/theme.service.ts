import { Injectable, signal } from '@angular/core';

export type ThemeMode = 'dark' | 'light';

const STORAGE_KEY = 'financas_poo_theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly theme = signal<ThemeMode>(this.resolveInitialTheme());

  constructor() {
    this.applyTheme(this.theme());
  }

  toggle(): void {
    const next: ThemeMode = this.theme() === 'dark' ? 'light' : 'dark';
    this.setTheme(next);
  }

  setTheme(mode: ThemeMode): void {
    this.theme.set(mode);
    this.applyTheme(mode);
    try {
      localStorage.setItem(STORAGE_KEY, mode);
    } catch {
      // Ignore storage restrictions
    }
  }

  private resolveInitialTheme(): ThemeMode {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved === 'light' || saved === 'dark') {
        return saved;
      }
    } catch {
      // Ignore storage restrictions
    }
    return 'dark';
  }

  private applyTheme(mode: ThemeMode): void {
    if (typeof document !== 'undefined') {
      document.documentElement.setAttribute('data-theme', mode);
    }
  }
}
