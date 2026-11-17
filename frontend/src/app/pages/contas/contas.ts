import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { extractErrorMessage, formatCurrencyBRL } from '../../core/format.utils';
import { ContaSaldo } from '../../models/financeiro.models';
import { FinanceiroService } from '../../services/financeiro.service';

@Component({
  selector: 'app-contas',
  imports: [FormsModule, RouterLink],
  templateUrl: './contas.html',
})
export class ContasPage implements OnInit {
  private readonly financeiroService = inject(FinanceiroService);

  readonly contas = signal<ContaSaldo[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly feedback = signal('');
  readonly editandoId = signal<number | null>(null);
  readonly formatBRL = formatCurrencyBRL;

  descricao = '';
  saldoInicial = 0;

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.financeiroService.listarContas().subscribe({
      next: (lista) => {
        this.contas.set(lista);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(extractErrorMessage(err, 'Erro ao carregar contas.'));
        this.loading.set(false);
      },
    });
  }

  salvar(): void {
    this.error.set('');
    this.feedback.set('');
    this.saving.set(true);

    const payload = {
      descricao: this.descricao,
      saldoInicial: Number(this.saldoInicial) || 0,
    };

    const id = this.editandoId();
    const request$ = id
      ? this.financeiroService.atualizarConta(id, payload)
      : this.financeiroService.criarConta(payload);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.feedback.set(id ? 'Conta atualizada com sucesso.' : 'Conta cadastrada com sucesso.');
        this.limparFormulario();
        this.carregar();
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(extractErrorMessage(err, 'Não foi possível salvar a conta.'));
      },
    });
  }

  editar(conta: ContaSaldo): void {
    this.editandoId.set(conta.id);
    this.descricao = conta.descricao;
    this.saldoInicial = conta.saldoInicial;
    this.error.set('');
    this.feedback.set('');
  }

  excluir(conta: ContaSaldo): void {
    if (!confirm(`Deseja excluir a conta "${conta.descricao}"?`)) return;
    this.error.set('');
    this.feedback.set('');
    this.financeiroService.excluirConta(conta.id).subscribe({
      next: () => {
        this.feedback.set('Conta excluída.');
        this.carregar();
      },
      error: (err) => {
        this.error.set(extractErrorMessage(err, 'Erro ao excluir conta.'));
      },
    });
  }

  limparFormulario(): void {
    this.editandoId.set(null);
    this.descricao = '';
    this.saldoInicial = 0;
  }
}
