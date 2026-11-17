import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { extractErrorMessage, formatCurrencyBRL } from '../../core/format.utils';
import {
  Categoria,
  ContaSaldo,
  TipoTransacao,
  Transacao,
  TransacaoFiltro,
} from '../../models/financeiro.models';
import { FinanceiroService } from '../../services/financeiro.service';

@Component({
  selector: 'app-transacoes',
  imports: [FormsModule, DatePipe, RouterLink],
  templateUrl: './transacoes.html',
})
export class TransacoesPage implements OnInit {
  private readonly financeiroService = inject(FinanceiroService);
  private readonly route = inject(ActivatedRoute);

  readonly transacoes = signal<Transacao[]>([]);
  readonly contas = signal<ContaSaldo[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly feedback = signal('');
  readonly editandoId = signal<number | null>(null);
  readonly formatBRL = formatCurrencyBRL;

  readonly totalReceitasFiltradas = computed(() =>
    this.transacoes()
      .filter((t) => t.tipo === 'R')
      .reduce((acc, t) => acc + Number(t.valor), 0),
  );

  readonly totalDespesasFiltradas = computed(() =>
    this.transacoes()
      .filter((t) => t.tipo === 'D')
      .reduce((acc, t) => acc + Number(t.valor), 0),
  );

  readonly saldoFiltrado = computed(
    () => this.totalReceitasFiltradas() - this.totalDespesasFiltradas(),
  );

  // Filtros
  filtroContaId: number | null = null;
  filtroCategoriaId: number | null = null;
  filtroTipo: '' | TipoTransacao = '';
  filtroDataInicio = '';
  filtroDataFim = '';
  filtroBusca = '';

  // Formulário de lançamento/edição
  descricao = '';
  valor: number | null = null;
  tipo: TipoTransacao = 'D';
  contaId: number | null = null;
  categoriaId: number | null = null;
  datatransacao = this.agoraParaInput();

  ngOnInit(): void {
    const qp = this.route.snapshot.queryParamMap;
    const cId = qp.get('contaId');
    const catId = qp.get('categoriaId');
    if (cId) this.filtroContaId = Number(cId);
    if (catId) this.filtroCategoriaId = Number(catId);

    this.carregarBaseETransacoes();
  }

  carregarBaseETransacoes(): void {
    this.loading.set(true);
    forkJoin({
      contas: this.financeiroService.listarContas(),
      categorias: this.financeiroService.listarCategorias(),
    }).subscribe({
      next: ({ contas, categorias }) => {
        this.contas.set(contas);
        this.categorias.set(categorias);
        if (!this.contaId && contas.length > 0) {
          this.contaId = this.filtroContaId ?? contas[0].id;
        }
        if (!this.categoriaId && categorias.length > 0) {
          this.categoriaId = this.filtroCategoriaId ?? categorias[0].id;
        }
        this.buscarTransacoes();
      },
      error: (err) => {
        this.error.set(extractErrorMessage(err, 'Erro ao carregar dados iniciais.'));
        this.loading.set(false);
      },
    });
  }

  buscarTransacoes(): void {
    this.loading.set(true);
    const filtro: TransacaoFiltro = {
      contaId: this.filtroContaId,
      categoriaId: this.filtroCategoriaId,
      tipo: this.filtroTipo,
      dataInicio: this.filtroDataInicio,
      dataFim: this.filtroDataFim,
      busca: this.filtroBusca,
    };

    this.financeiroService.listarTransacoes(filtro).subscribe({
      next: (lista) => {
        this.transacoes.set(lista);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(extractErrorMessage(err, 'Erro ao buscar transações.'));
        this.loading.set(false);
      },
    });
  }

  limparFiltros(): void {
    this.filtroContaId = null;
    this.filtroCategoriaId = null;
    this.filtroTipo = '';
    this.filtroDataInicio = '';
    this.filtroDataFim = '';
    this.filtroBusca = '';
    this.buscarTransacoes();
  }

  salvar(): void {
    if (!this.contaId || !this.categoriaId || !this.valor) {
      this.error.set('Preencha conta, categoria e valor.');
      return;
    }

    this.error.set('');
    this.feedback.set('');
    this.saving.set(true);

    const payload = {
      descricao: this.descricao,
      valor: Number(this.valor),
      tipo: this.tipo,
      datatransacao: this.datatransacao ? `${this.datatransacao}:00`.slice(0, 19) : undefined,
      contaId: Number(this.contaId),
      categoriaId: Number(this.categoriaId),
    };

    const id = this.editandoId();
    if (id) {
      this.financeiroService.atualizarTransacao(id, payload).subscribe({
        next: () => {
          this.saving.set(false);
          this.feedback.set('Transação atualizada.');
          this.limparFormulario();
          this.buscarTransacoes();
        },
        error: (err) => {
          this.saving.set(false);
          this.error.set(extractErrorMessage(err, 'Erro ao salvar transação.'));
        },
      });
      return;
    }

    this.financeiroService.criarTransacao(payload).subscribe({
      next: (res) => {
        this.saving.set(false);
        this.feedback.set(res.message || 'Transação enviada para fila Kafka!');
        this.limparFormulario();
        setTimeout(() => this.buscarTransacoes(), 350);
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(extractErrorMessage(err, 'Erro ao salvar transação.'));
      },
    });
  }

  editar(t: Transacao): void {
    this.editandoId.set(t.id);
    this.descricao = t.descricao;
    this.valor = t.valor;
    this.tipo = t.tipo;
    this.contaId = t.contaId;
    this.categoriaId = t.categoriaId;
    this.datatransacao = t.datatransacao ? t.datatransacao.slice(0, 16) : this.agoraParaInput();
    this.error.set('');
    this.feedback.set('');
  }

  excluir(t: Transacao): void {
    const msg = t.transferenciaId
      ? `Esta transação faz parte de uma transferência. Excluir "${t.descricao}" removerá ambos os lançamentos da transferência. Continuar?`
      : `Deseja excluir a transação "${t.descricao}"?`;
    if (!confirm(msg)) return;

    this.error.set('');
    this.feedback.set('');
    this.financeiroService.excluirTransacao(t.id).subscribe({
      next: () => {
        this.feedback.set('Transação excluída.');
        this.buscarTransacoes();
      },
      error: (err) => {
        this.error.set(extractErrorMessage(err, 'Erro ao excluir transação.'));
      },
    });
  }

  limparFormulario(): void {
    this.editandoId.set(null);
    this.descricao = '';
    this.valor = null;
    this.tipo = 'D';
    this.datatransacao = this.agoraParaInput();
  }

  private agoraParaInput(): string {
    const now = new Date();
    const offset = now.getTimezoneOffset() * 60000;
    return new Date(now.getTime() - offset).toISOString().slice(0, 16);
  }
}
