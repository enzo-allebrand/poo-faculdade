import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API_BASE_URL } from '../core/api.config';
import {
  Categoria,
  CategoriaPayload,
  ContaPayload,
  ContaSaldo,
  DashboardResumo,
  EventoEnfileirado,
  Transacao,
  TransacaoFiltro,
  TransacaoPayload,
  TransferenciaPayload,
} from '../models/financeiro.models';

@Injectable({ providedIn: 'root' })
export class FinanceiroService {
  private readonly http = inject(HttpClient);

  getDashboard() {
    return this.http.get<DashboardResumo>(`${API_BASE_URL}/Dashboard`);
  }

  listarContas() {
    return this.http.get<ContaSaldo[]>(`${API_BASE_URL}/Conta/findAll`);
  }

  criarConta(payload: ContaPayload) {
    return this.http.post<ContaSaldo>(`${API_BASE_URL}/Conta`, payload);
  }

  atualizarConta(id: number, payload: ContaPayload) {
    return this.http.put<ContaSaldo>(`${API_BASE_URL}/Conta/${id}`, payload);
  }

  excluirConta(id: number) {
    return this.http.delete<void>(`${API_BASE_URL}/Conta/${id}`);
  }

  listarCategorias() {
    return this.http.get<Categoria[]>(`${API_BASE_URL}/Categoria/findAll`);
  }

  criarCategoria(payload: CategoriaPayload) {
    return this.http.post<Categoria>(`${API_BASE_URL}/Categoria`, payload);
  }

  atualizarCategoria(id: number, payload: CategoriaPayload) {
    return this.http.put<Categoria>(`${API_BASE_URL}/Categoria/${id}`, payload);
  }

  excluirCategoria(id: number) {
    return this.http.delete<void>(`${API_BASE_URL}/Categoria/${id}`);
  }

  listarTransacoes(filtro: TransacaoFiltro = {}) {
    let params = new HttpParams();
    if (filtro.contaId) params = params.set('contaId', filtro.contaId);
    if (filtro.categoriaId) params = params.set('categoriaId', filtro.categoriaId);
    if (filtro.tipo) params = params.set('tipo', filtro.tipo);
    if (filtro.dataInicio) params = params.set('dataInicio', filtro.dataInicio);
    if (filtro.dataFim) params = params.set('dataFim', filtro.dataFim);
    if (filtro.busca?.trim()) params = params.set('busca', filtro.busca.trim());

    return this.http.get<Transacao[]>(`${API_BASE_URL}/Transacao/findAll`, { params });
  }

  criarTransacao(payload: TransacaoPayload) {
    return this.http.post<EventoEnfileirado>(`${API_BASE_URL}/Transacao`, payload);
  }

  atualizarTransacao(id: number, payload: TransacaoPayload) {
    return this.http.put<Transacao>(`${API_BASE_URL}/Transacao/${id}`, payload);
  }

  excluirTransacao(id: number) {
    return this.http.delete<void>(`${API_BASE_URL}/Transacao/${id}`);
  }

  transferir(payload: TransferenciaPayload) {
    return this.http.post<EventoEnfileirado>(`${API_BASE_URL}/Transacao/transferencia`, payload);
  }
}
