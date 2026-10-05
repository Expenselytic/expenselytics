import { Expense } from '../Expense/Expense';

export interface Budget {
  id: number;
  month: string;
  category: string;
  amount: number;
}

export function monthKey(date = new Date()): string {
  return `${date.getFullYear()}-${String(date.getMonth() + 1)
    .padStart(2, '0')}`;
}

export function budgetProgress(
  budget: Budget, expenses: Expense[], now = new Date(),
) {
  const spentCents = expenses.filter(item =>
    monthKey(item.expenseDate) === budget.month &&
    (!budget.category || item.expenseCategory === budget.category),
  ).reduce((sum, item) => sum + Math.round(item.amount * 100), 0);
  const limitCents = Math.round(Number(budget.amount) * 100);
  const remaining = (limitCents - spentCents) / 100;
  const percent = limitCents > 0 ? spentCents / limitCents * 100 : 0;
  const [year, month] = budget.month.split('-').map(Number);
  const days = new Date(year, month, 0).getDate();
  const current = monthKey(now);
  const daysLeft = budget.month < current ? 0
    : budget.month === current ? days - now.getDate() + 1 : days;
  return {
    budget, spent: spentCents / 100, remaining, percent,
    bar: Math.min(100, Math.max(0, percent)),
    daily: daysLeft ? Math.max(0, remaining) / daysLeft : null,
    status: percent >= 100 ? 'over' : percent >= 80 ? 'near' : 'good',
    label: percent > 100 ? 'Over budget'
      : percent === 100 ? 'Limit reached'
      : percent >= 80 ? 'Approaching limit' : 'On track',
  };
}
