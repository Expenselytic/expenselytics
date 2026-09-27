import { Routes } from '@angular/router';
import { ShowExpense } from './Expense/show-expense/show-expense.component';

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
