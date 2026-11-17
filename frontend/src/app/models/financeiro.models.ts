export interface User {
  id: number;
  nome: string;
  email: string;
}

export interface LoginPayload {
  email: string;
  senha: string;
}

export interface RegisterPayload {
  nome: string;
  email: string;
  senha: string;
}

export interface ContaSaldo {
  id: number;
  descricao: string;
  saldoInicial: number;
  totalReceitas: number;
  totalDespesas: number;
  saldoAtual: number;
}

export interface ContaPayload {
  descricao: string;
  saldoInicial: number;
}

export type TipoCategoria = 'R' | 'D' | 'A';

export interface Categoria {
  id: number;
  descricao: string;
  tipo: TipoCategoria;
}

export interface CategoriaPayload {
  descricao: string;
  tipo: TipoCategoria;
}

export type TipoTransacao = 'R' | 'D';

export interface Transacao {
  id: number;
  descricao: string;
  valor: number;
  tipo: TipoTransacao;
  datatransacao: string;
  transferenciaId: string | null;
  contaId: number;
  contaDescricao: string;
  categoriaId: number;
  categoriaDescricao: string;
}

export interface TransacaoPayload {
  descricao: string;
  valor: number;
  tipo: TipoTransacao;
  datatransacao?: string;
  contaId: number;
  categoriaId: number;
}

export interface TransacaoFiltro {
  contaId?: number | null;
  categoriaId?: number | null;
  tipo?: '' | TipoTransacao;
  dataInicio?: string;
  dataFim?: string;
  busca?: string;
}

export interface TransferenciaPayload {
  contaOrigemId: number;
  contaDestinoId: number;
  categoriaId?: number | null;
  valor: number;
  descricao?: string;
  datatransacao?: string;
}

export interface TransferenciaResponse {
  transferenciaId: string;
  saida: Transacao;
  entrada: Transacao;
}

export interface EventoEnfileirado {
  status: string;
  correlationId: string;
  message: string;
}

export interface DashboardResumo {
  saldoGeral: number;
  totalReceitas: number;
  totalDespesas: number;
  contas: ContaSaldo[];
  ultimasTransacoes: Transacao[];
}
