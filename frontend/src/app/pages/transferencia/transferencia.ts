import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { extractErrorMessage, formatCurrencyBRL } from '../../core/format.utils';
import { Categoria, ContaSaldo } from '../../models/financeiro.models';
import { FinanceiroService } from '../../services/financeiro.service';

@Component({
  selector: 'app-transferencia',
  imports: [FormsModule, RouterLink],
  templateUrl: './transferencia.html',
})
export class TransferenciaPage implements OnInit {
  private readonly financeiroService = inject(FinanceiroService);
  private readonly router = inject(Router);

  readonly contas = signal<ContaSaldo[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly feedback = signal('');
  readonly formatBRL = formatCurrencyBRL;

  contaOrigemId: number | null = null;
  contaDestinoId: number | null = null;
  categoriaId: number | null = null;
  valor: number | null = null;
  descricao = 'Transferência entre contas';
  datatransacao = this.agoraParaInput();

  ngOnInit(): void {
    this.carregarDados();
  }

  carregarDados(): void {
    this.loading.set(true);
    forkJoin({
      contas: this.financeiroService.listarContas(),
      categorias: this.financeiroService.listarCategorias(),
    }).subscribe({
      next: ({ contas, categorias }) => {
        this.contas.set(contas);
        this.categorias.set(categorias);
        if (contas.length >= 1) this.contaOrigemId = contas[0].id;
        if (contas.length >= 2) this.contaDestinoId = contas[1].id;
        const catTransf = categorias.find((c) => c.descricao.toLowerCase().startsWith('transfer'));
        if (catTransf) this.categoriaId = catTransf.id;
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(extractErrorMessage(err, 'Erro ao carregar contas e categorias.'));
        this.loading.set(false);
      },
    });
  }

  transferir(): void {
    if (!this.contaOrigemId || !this.contaDestinoId || !this.valor) {
      this.error.set('Selecione as contas de origem e destino e informe o valor.');
      return;
    }
    if (this.contaOrigemId === this.contaDestinoId) {
      this.error.set('Conta de origem e destino devem ser diferentes.');
      return;
    }

    this.error.set('');
    this.feedback.set('');
    this.saving.set(true);

    this.financeiroService
      .transferir({
        contaOrigemId: Number(this.contaOrigemId),
        contaDestinoId: Number(this.contaDestinoId),
        categoriaId: this.categoriaId ? Number(this.categoriaId) : null,
        valor: Number(this.valor),
        descricao: this.descricao,
        datatransacao: this.datatransacao ? `${this.datatransacao}:00`.slice(0, 19) : undefined,
      })
      .subscribe({
        next: (res) => {
          this.saving.set(false);
          this.feedback.set(res.message || 'Transferência enviada para processamento via Kafka!');
          this.valor = null;
          setTimeout(() => this.carregarDados(), 350);
        },
        error: (err) => {
          this.saving.set(false);
          this.error.set(extractErrorMessage(err, 'Não foi possível realizar a transferência.'));
        },
      });
  }

  irParaTransacoes(): void {
    this.router.navigate(['/transacoes']);
  }

  private agoraParaInput(): string {
    const now = new Date();
    const offset = now.getTimezoneOffset() * 60000;
    return new Date(now.getTime() - offset).toISOString().slice(0, 16);
  }
}
