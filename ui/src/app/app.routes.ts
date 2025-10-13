import { Routes } from '@angular/router';
import { ShowExpense } from './show-expense/show-expense';

export const routes: Routes = [
  {
    path: 'chart',
    component: ShowExpense
  },
  {
    path: '',
    redirectTo: 'chart',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'chart'
  }
];
