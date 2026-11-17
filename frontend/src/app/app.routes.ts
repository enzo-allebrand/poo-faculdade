import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth.guard';
import { CategoriasPage } from './pages/categorias/categorias';
import { ContasPage } from './pages/contas/contas';
import { DashboardPage } from './pages/dashboard/dashboard';
import { LoginPage } from './pages/login/login';
import { TransacoesPage } from './pages/transacoes/transacoes';
import { TransferenciaPage } from './pages/transferencia/transferencia';

export const routes: Routes = [
  { path: 'login', component: LoginPage, canActivate: [guestGuard] },
  { path: '', component: DashboardPage, canActivate: [authGuard] },
  { path: 'contas', component: ContasPage, canActivate: [authGuard] },
  { path: 'categorias', component: CategoriasPage, canActivate: [authGuard] },
  { path: 'transacoes', component: TransacoesPage, canActivate: [authGuard] },
  { path: 'transferencia', component: TransferenciaPage, canActivate: [authGuard] },
  { path: '**', redirectTo: '' },
];
