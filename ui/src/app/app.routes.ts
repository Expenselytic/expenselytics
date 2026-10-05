import { Routes } from '@angular/router';
import { FinancePage } from './finance-page';

export const routes: Routes = [
  { path: '', pathMatch: 'full', component: FinancePage, title: 'Expenselytics · Your money, made clear' },
  { path: 'workspace', component: FinancePage, data: { workspace: true }, title: 'Your workspace · Expenselytics' },
  { path: 'chart', redirectTo: 'workspace', pathMatch: 'full' },
  { path: '**', redirectTo: '' },
];
