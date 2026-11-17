import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { extractErrorMessage } from '../../core/format.utils';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
})
export class LoginPage {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly mode = signal<'login' | 'register'>('login');
  readonly loading = signal(false);
  readonly error = signal('');

  nome = '';
  email = '';
  senha = '';

  toggleMode(nextMode: 'login' | 'register'): void {
    this.mode.set(nextMode);
    this.error.set('');
  }

  submit(): void {
    this.error.set('');
    this.loading.set(true);

    const request$ =
      this.mode() === 'login'
        ? this.authService.login({ email: this.email, senha: this.senha })
        : this.authService.register({ nome: this.nome, email: this.email, senha: this.senha });

    request$.subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(extractErrorMessage(err, 'Não foi possível autenticar.'));
      },
    });
  }
}
