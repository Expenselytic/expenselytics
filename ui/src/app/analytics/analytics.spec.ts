import { Analytics } from './analytics';
import { Expense } from '../Expense/Expense';

describe('Workspace analytics', () => {
  it('aggregates only the selected months and recalculates categories and savings', () => {
    const charts = new Analytics(); charts.months = 3;
    const now = new Date();
    const item = (id: number, amount: string, monthsAgo: number, category: string) => new Expense({
      id, amount, category, date: new Date(now.getFullYear(), now.getMonth() - monthsAgo, 10).toISOString(),
    });
    charts.expenses = [item(1, '20', 0, 'Food'), item(2, '30', 1, 'Food'), item(3, '100', 4, 'Travel')];
    charts.savings = [item(1, '80', 0, 'Emergency')];
    expect(charts.spent).toBe(50);
    expect(charts.saved).toBe(80);
    expect(charts.categories[0].amount).toBe(50);
    expect(charts.trend[2].expenses).toBe(20);
    charts.breakdown = 'savings';
    expect(charts.categories[0].name).toBe('Emergency');
    charts.months = 6;
    expect(charts.spent).toBe(150);
  });
  it('handles empty periods without invalid chart coordinates', () => {
    const charts = new Analytics();
    expect(charts.spent).toBe(0);
    expect(charts.donut).toBe('#edf1ea');
    expect(charts.cumulativePoints).not.toContain('NaN');
  });
});
