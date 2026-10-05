import { Experiment, experimentProgress } from './experiment';
import { Expense } from '../Expense/Expense';

describe('Spending experiment calculations', () => {
  const experiment: Experiment = { id: 1, name: 'Cook more', category: 'Food', startDate: '2026-01-15', durationDays: 14, savingIds: [10] };
  const expense = (id: number, amount: string, date: string, category = 'Food') => new Expense({ id, amount, date, category });
  it('prorates the previous period and keeps real deposits separate', () => {
    const rows = [expense(1, '140', '2026-01-01T12:00:00'), expense(2, '30', '2026-01-16T12:00:00'), expense(3, '900', '2026-01-16T12:00:00', 'Home')];
    const result = experimentProgress(experiment, rows, [expense(10, '20', '2026-01-17T12:00:00')], new Date('2026-01-22T00:00:00'));
    expect(result.expected).toBe(70);
    expect(result.actual).toBe(30);
    expect(result.reduction).toBe(40);
    expect(result.deposited).toBe(20);
    expect(result.percent).toBe(50);
    expect(experimentProgress(experiment, rows, [], new Date('2026-01-22T00:00:00')).deposited).toBe(0);
  });
  it('handles missing history, overspending, planned and completed periods', () => {
    expect(experimentProgress(experiment, [], []).hasBaseline).toBeFalse();
    const rows = [expense(1, '10', '2026-01-01T00:00:00'), expense(2, '30', '2026-01-15T00:00:00'), expense(3, '500', '2026-01-29T00:00:00')];
    const done = experimentProgress(experiment, rows, [], new Date('2026-02-01T00:00:00'));
    expect(done.status).toBe('Completed'); expect(done.actual).toBe(30); expect(done.over).toBe(20); expect(done.reduction).toBe(0);
    const planned = experimentProgress(experiment, rows, [], new Date('2026-01-14T00:00:00'));
    expect(planned.status).toBe('Planned'); expect(planned.expected).toBe(0); expect(planned.actual).toBe(0);
  });
});
