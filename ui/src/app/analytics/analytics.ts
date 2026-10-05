import { CommonModule } from '@angular/common';
import { Component, computed, Input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Expense } from '../Expense/Expense';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './analytics.html',
  styleUrl: './analytics.css',
})
export class Analytics {
  private readonly expenseData = signal<Expense[]>([]);
  private readonly savingData = signal<Expense[]>([]);
  private readonly monthCount = signal(6);
  private readonly categoryType = signal<'expenses' | 'savings'>(
    'expenses',
  );
  @Input() set expenses(value: Expense[]) {
    this.expenseData.set(value);
  }
  get expenses(): Expense[] {
    return this.expenseData();
  }
  @Input() set savings(value: Expense[]) {
    this.savingData.set(value);
  }
  get savings(): Expense[] {
    return this.savingData();
  }
  set months(value: number) {
    this.monthCount.set(Number(value));
  }
  get months(): number {
    return this.monthCount();
  }
  set breakdown(value: 'expenses' | 'savings') {
    this.categoryType.set(value);
  }
  get breakdown(): 'expenses' | 'savings' {
    return this.categoryType();
  }
  trackMonth(_index: number, row: { label: string }): string {
    return row.label;
  }
  trackCategory(_index: number, row: { name: string }): string {
    return row.name;
  }
  readonly colors = [
    '#315c50',
    '#8ab04c',
    '#cb9454',
    '#628cb0',
    '#a579a6',
    '#61716a',
  ];

  get trend() {
    return this.trendData();
  }
  private readonly trendData = computed(() => {
    const now = new Date();
    return Array.from({ length: Number(this.months) }, (_, i) => {
      const start = new Date(
        now.getFullYear(),
        now.getMonth() - Number(this.months) + 1 + i,
        1,
      );
      const end = new Date(start.getFullYear(), start.getMonth() + 1, 1);
      const sum = (items: Expense[]) =>
        items
          .filter(
            (item) => item.expenseDate >= start && item.expenseDate < end,
          )
          .reduce((value, item) => value + item.amount, 0);
      return {
        label: start.toLocaleDateString(undefined, {
          month: 'short',
          year: '2-digit',
        }),
        expenses: sum(this.expenses),
        savings: sum(this.savings),
      };
    });
  });
  get max(): number {
    return Math.max(
      1,
      ...this.trend.flatMap((row) => [row.expenses, row.savings]),
    );
  }
  get spent(): number {
    return this.trend.reduce((sum, row) => sum + row.expenses, 0);
  }
  get saved(): number {
    return this.trend.reduce((sum, row) => sum + row.savings, 0);
  }
  get cumulativePoints(): string {
    let total = 0;
    return this.trend
      .map((row, index, rows) => {
        total += row.savings;
        const x = 20 + (index * 560) / Math.max(1, rows.length - 1);
        const y = 180 - (total / Math.max(1, this.saved)) * 160;
        return `${x},${y}`;
      })
      .join(' ');
  }
  get categories() {
    return this.categoryData();
  }
  private readonly categoryData = computed(() => {
    const now = new Date();
    const start = new Date(
      now.getFullYear(),
      now.getMonth() - Number(this.months) + 1,
      1,
    );
    const end = new Date(now.getFullYear(), now.getMonth() + 1, 1);
    const totals = new Map<string, number>();
    for (const item of this[this.breakdown]) {
      if (item.expenseDate >= start && item.expenseDate < end) {
        totals.set(
          item.expenseCategory,
          (totals.get(item.expenseCategory) ?? 0) + item.amount,
        );
      }
    }
    return [...totals]
      .sort((a, b) => b[1] - a[1])
      .map(([name, amount], index) => ({
        name,
        amount,
        color: this.colors[index % this.colors.length],
      }));
  });
  get donut(): string {
    const total = this.categories.reduce(
      (sum, row) => sum + row.amount,
      0,
    );
    if (!total) return '#edf1ea';
    let angle = 0;
    return `conic-gradient(${this.categories
      .map((row) => {
        const start = angle;
        angle += (row.amount / total) * 360;
        return `${row.color} ${start}deg ${angle}deg`;
      })
      .join(',')})`;
  }
}
