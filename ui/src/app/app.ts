import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiRoot, ExpenseRequest } from './api/api-root';
import { Expense } from './Expense/Expense';
import { Resource } from './hal/resource';
import { ResourceService } from './hal/resource.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, CurrencyPipe, DatePipe],
  templateUrl: './app.html',
  styleUrls: ['./app.css']  // <-- corrected
})

export class App implements OnInit {
  isAuthenticated = false;
  authMode: 'login' | 'signup' = 'login';
  showAuth = false;
  profileName = 'Alex';
  loading = true;
  saving = false;
  message = '';
  root?: ApiRoot;
  expenses: Expense[] = [];
  form: ExpenseRequest = this.emptyForm();

  constructor(private readonly resourceService: ResourceService) {}

  ngOnInit(): void { this.loadApi(); }

  get total(): number { return this.expenses.reduce((sum, expense) => sum + expense.amount, 0); }
  get recentTotal(): number { return this.expenses.slice(0, 3).reduce((sum, expense) => sum + expense.amount, 0); }
  get categoryCount(): number { return new Set(this.expenses.map(expense => expense.expenseCategory)).size; }

  openAuth(mode: 'login' | 'signup'): void { this.authMode = mode; this.showAuth = true; }
  authenticate(): void { this.isAuthenticated = true; this.showAuth = false; this.message = `Welcome${this.profileName ? `, ${this.profileName}` : ''}!`; }
  signOut(): void { this.isAuthenticated = false; this.message = 'You have signed out.'; }

  addExpense(): void {
    if (!this.form.name || !this.form.amount || !this.form.category) { this.message = 'Add a name, amount, and category first.'; return; }
    this.saving = true;
    const finish = (): void => { this.saving = false; this.form = this.emptyForm(); this.loadExpenses(); };
    if (!this.root) { this.message = 'The expense API is unavailable. Start the backend and try again.'; this.saving = false; return; }
    this.root.createExpense(this.resourceService, this.form).subscribe({
      next: () => { this.message = 'Expense saved successfully.'; finish(); },
      error: () => { this.message = 'Could not save this expense. Please check the API and try again.'; this.saving = false; }
    });
  }

  deleteExpense(expense: Expense): void {
    if (!this.root || !expense.id) { this.message = 'This expense cannot be deleted yet.'; return; }
    this.root.deleteExpense(this.resourceService, expense.id).subscribe({
      next: () => { this.expenses = this.expenses.filter(item => item.id !== expense.id); this.message = 'Expense removed.'; },
      error: () => this.message = 'Could not remove this expense. Please try again.'
    });
  }

  private loadApi(): void {
    Resource.fetchRoot(ApiRoot).subscribe({
      next: root => { this.root = root; this.loadExpenses(); },
      error: () => { this.loading = false; this.message = 'Connect the backend to see your saved expenses.'; }
    });
  }

  private loadExpenses(): void {
    if (!this.root) { return; }
    this.loading = true;
    this.root.getExpenses().subscribe({
      next: expenses => { this.expenses = expenses; this.loading = false; },
      error: () => { this.loading = false; this.message = 'Expenses could not be loaded.'; }
    });
  }

  private emptyForm(): ExpenseRequest {
    return { name: '', category: 'Food & dining', amount: '', date: new Date().toISOString().slice(0, 16) };
  }
}
