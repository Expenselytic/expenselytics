import { Analytics } from './analytics';
import { Expense } from '../Expense/Expense';

describe('Workspace analytics', () => {
  it(
    'aggregates only the selected months ' +
      'and recalculates categories and ' +
      'savings',
    () => {
      const charts = new Analytics();
      charts.months = 3;
      const now = new Date();
      const item = (
        id: number,
        amount: string,
        monthsAgo: number,
        category: string,
      ) =>
        new Expense({
          id,
          amount,
          category,
          date: new Date(
            now.getFullYear(),
            now.getMonth() - monthsAgo,
            10,
          ).toISOString(),
        });
      charts.expenses = [
        item(1, '20', 0, 'Food'),
        item(2, '30', 1, 'Food'),
        item(3, '100', 4, 'Travel'),
      ];
      charts.savings = [item(1, '80', 0, 'Emergency')];
      expect(charts.spent).toBe(50);
      expect(charts.saved).toBe(80);
      expect(charts.categories[0].amount).toBe(50);
      expect(charts.trend[2].expenses).toBe(20);
      charts.breakdown = 'savings';
      expect(charts.categories[0].name).toBe('Emergency');
      charts.months = 6;
      expect(charts.spent).toBe(150);
    },
  );
  it('handles empty periods without invalid chart coordinates', () => {
    const charts = new Analytics();
    expect(charts.spent).toBe(0);
    expect(charts.donut).toBe('#edf1ea');
    expect(charts.cumulativePoints).not.toContain('NaN');
  });
  it(
    'reuses chart calculations across ' +
      'screen updates and invalidates when ' +
      'data changes',
    () => {
      const charts = new Analytics();
      charts.expenses = Array.from(
        { length: 720 },
        (_, id) =>
          new Expense({
            id,
            amount: '10',
            category: 'Food',
            date: new Date().toISOString(),
          }),
      );
      const trend = charts.trend;
      const categories = charts.categories;
      for (let i = 0; i < 20; i++) {
        expect(charts.trend).toBe(trend);
        expect(charts.categories).toBe(categories);
        expect(charts.spent).toBe(7200);
      }
      charts.expenses = [];
      expect(charts.spent).toBe(0);
      expect(charts.trend).not.toBe(trend);
      charts.months = 3;
      expect(charts.trend.length).toBe(3);
    },
  );
});
