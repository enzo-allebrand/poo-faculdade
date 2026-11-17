import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { extractErrorMessage } from '../../core/format.utils';
import { Categoria, TipoCategoria } from '../../models/financeiro.models';
import { FinanceiroService } from '../../services/financeiro.service';

@Component({
  selector: 'app-categorias',
  imports: [FormsModule, RouterLink],
  templateUrl: './categorias.html',
})
export class CategoriasPage implements OnInit {
  private readonly financeiroService = inject(FinanceiroService);

  readonly categorias = signal<Categoria[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly feedback = signal('');
  readonly editandoId = signal<number | null>(null);

  descricao = '';
  tipo: TipoCategoria = 'A';

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.financeiroService.listarCategorias().subscribe({
      next: (lista) => {
        this.categorias.set(lista);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(extractErrorMessage(err, 'Erro ao carregar categorias.'));
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
      tipo: this.tipo,
    };

    const id = this.editandoId();
    const request$ = id
      ? this.financeiroService.atualizarCategoria(id, payload)
      : this.financeiroService.criarCategoria(payload);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.feedback.set(id ? 'Categoria atualizada.' : 'Categoria criada.');
        this.limparFormulario();
        this.carregar();
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(extractErrorMessage(err, 'Não foi possível salvar a categoria.'));
      },
    });
  }

  editar(cat: Categoria): void {
    this.editandoId.set(cat.id);
    this.descricao = cat.descricao;
    this.tipo = cat.tipo || 'A';
    this.error.set('');
    this.feedback.set('');
  }

  excluir(cat: Categoria): void {
    if (!confirm(`Deseja excluir a categoria "${cat.descricao}"?`)) return;
    this.error.set('');
    this.feedback.set('');
    this.financeiroService.excluirCategoria(cat.id).subscribe({
      next: () => {
        this.feedback.set('Categoria excluída.');
        this.carregar();
      },
      error: (err) => {
        this.error.set(extractErrorMessage(err, 'Erro ao excluir categoria.'));
      },
    });
  }

  limparFormulario(): void {
    this.editandoId.set(null);
    this.descricao = '';
    this.tipo = 'A';
  }

  rotuloTipo(tipo: string): string {
    if (tipo === 'R') return 'Receita';
    if (tipo === 'D') return 'Despesa';
    return 'Ambos';
  }
}
