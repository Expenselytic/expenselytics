import { Routes } from '@angular/router';
import { requireLogin } from './auth.guard';
import { LoginPage } from './login-page';
import { FinancePage } from './finance-page';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: FinancePage,
    title: 'Expenselytics · Your money, made clear',
  },
  {
    path: 'workspace',
    canActivate: [requireLogin],
    component: FinancePage,
    data: { workspace: true },
    title: 'Your workspace · Expenselytics',
  },
  {
    path: 'login',
    component: LoginPage,
    title: 'Log in · Expenselytics',
  },
  { path: 'chart', redirectTo: 'workspace', pathMatch: 'full' },
  { path: '**', redirectTo: '' },
];
