import { Expense } from '../Expense/Expense';
import { Budget, budgetProgress } from './budget';

describe('Monthly budget progress', () => {
  const budget: Budget = {
    id: 1, month: '2026-10', category: '', amount: 100,
  };
  const expense = (amount: number, category = 'Home', month = 9) =>
    new Expense({ amount, category, date: new Date(2026, month, 4) });
  const today = new Date(2026, 9, 22);

  it('counts only the selected month and category', () => {
    const result = budgetProgress(
      { ...budget, category: 'Home' },
      [expense(10), expense(20, 'Other'), expense(30, 'Home', 8)],
      today,
    );
    expect(result.spent).toBe(10);
    expect(result.remaining).toBe(90);
    expect(result.daily).toBe(9);
  });

  it('warns at 80 percent and caps the over-budget meter', () => {
    expect(budgetProgress(budget, [expense(79)], today).status)
      .toBe('good');
    expect(budgetProgress(budget, [expense(80)], today).status)
      .toBe('near');
    expect(budgetProgress(budget, [expense(100)], today).label)
      .toBe('Limit reached');
    const over = budgetProgress(budget, [expense(120)], today);
    expect(over.status).toBe('over');
    expect(over.remaining).toBe(-20);
    expect(over.bar).toBe(100);
    expect(over.daily).toBe(0);
  });

  it('uses cents and handles ended months and leap years', () => {
    expect(budgetProgress(budget, [expense(.1), expense(.2)], today)
      .spent).toBe(.3);
    expect(budgetProgress(budget, [], new Date(2026, 10, 1)).daily)
      .toBeNull();
    expect(budgetProgress(
      { ...budget, month: '2028-02', amount: 290 },
      [], new Date(2028, 0, 1),
    ).daily).toBe(10);
  });
});
