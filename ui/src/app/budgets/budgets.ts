import { CommonModule } from '@angular/common';
import {
  Component, DestroyRef, inject, Input, OnInit,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { timeout } from 'rxjs';
import { Expense } from '../Expense/Expense';
import { API_V1_ROOT } from '../api/api-paths';
import { Budget, budgetProgress, monthKey } from './budget';

@Component({
  selector: 'app-budgets',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './budgets.html',
  styleUrl: './budgets.css',
})
export class Budgets implements OnInit {
  @Input() expenses: Expense[] = [];
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly url = `${API_V1_ROOT}/budgets`;
  month = monthKey();
  budgets: Budget[] = [];
  category = '';
  amount: number | null = null;
  loading = false;
  busy = false;
  failed = false;
  message = '';

  get cards() {
    return this.budgets.map(item => budgetProgress(item, this.expenses))
      .sort((a, b) => a.budget.category.localeCompare(b.budget.category));
  }

  get categories() {
    return [...new Set([
      'Food & dining', 'Transport', 'Shopping', 'Health', 'Home',
      'Entertainment', 'Other',
      ...this.expenses.map(item => item.expenseCategory),
      ...this.budgets.map(item => item.category).filter(Boolean),
    ])].sort();
  }

  ngOnInit(): void { this.load(); }

  load(): void {
    if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(this.month)) return;
    this.loading = true;
    this.failed = false;
    this.message = '';
    this.budgets = [];
    this.http.get<Budget[]>(`${this.url}/${this.month}`)
      .pipe(timeout(15000), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: items => {
          this.budgets = items;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this.failed = true;
          this.message = 'Could not load budgets. Please retry.';
        },
      });
  }

  edit(budget: Budget): void {
    this.category = budget.category;
    this.amount = Number(budget.amount);
    this.message = 'Limit ready to edit below.';
  }

  save(): void {
    if (this.busy || this.loading || this.failed ||
      !this.amount || this.amount <= 0 ||
      this.amount > 9999999999.99 ||
      !/^\d+(\.\d{1,2})?$/.test(String(this.amount))) return;
    this.busy = true;
    this.message = '';
    this.http.put<Budget>(`${this.url}/${this.month}`, {
      category: this.category, amount: this.amount,
    }).pipe(timeout(15000), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: budget => {
          this.budgets = [...this.budgets.filter(
            item => item.category !== budget.category,
          ), budget];
          this.busy = false;
          this.amount = null;
          this.message = 'Budget saved.';
        },
        error: () => {
          this.busy = false;
          this.message = 'Could not save budget. Please try again.';
        },
      });
  }

  remove(budget: Budget): void {
    if (this.busy) return;
    this.busy = true;
    this.http.delete(`${this.url}/${budget.id}`)
      .pipe(timeout(15000), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.budgets = this.budgets.filter(b => b.id !== budget.id);
          this.busy = false;
          this.message = 'Budget removed.';
        },
        error: () => {
          this.busy = false;
          this.message = 'Could not remove budget. Please try again.';
        },
      });
  }
}
