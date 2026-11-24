import { HttpErrorResponse } from '@angular/common/http';

export function formatCurrencyBRL(value: number | null | undefined): string {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value ?? 0);
}

export function extractErrorMessage(err: unknown, fallback = 'Ocorreu um erro inesperado.'): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'Não foi possível conectar ao backend (http://localhost:8080). Verifique se o servidor Quarkus está rodando.';
    }
    const body = err.error;
    if (body && typeof body === 'object') {
      if (typeof body.message === 'string' && body.message.trim()) {
        return body.message;
      }
      if (Array.isArray(body.violations) && body.violations.length > 0) {
        return body.violations.map((v: { message?: string }) => v.message).filter(Boolean).join(' ');
      }
    }
    if (typeof body === 'string' && body.trim()) {
      return body;
    }
  }
  return fallback;
}
