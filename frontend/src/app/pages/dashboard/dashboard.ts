import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { extractErrorMessage, formatCurrencyBRL } from '../../core/format.utils';
import { DashboardResumo } from '../../models/financeiro.models';
import { FinanceiroService } from '../../services/financeiro.service';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DatePipe],
  templateUrl: './dashboard.html',
})
export class DashboardPage implements OnInit {
  private readonly financeiroService = inject(FinanceiroService);

  readonly resumo = signal<DashboardResumo | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly formatBRL = formatCurrencyBRL;

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.error.set('');
    this.financeiroService.getDashboard().subscribe({
      next: (data) => {
        this.resumo.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(extractErrorMessage(err, 'Erro ao carregar o resumo financeiro.'));
        this.loading.set(false);
      },
    });
  }
}
