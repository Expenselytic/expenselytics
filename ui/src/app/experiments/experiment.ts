import { Expense } from '../Expense/Expense';
export interface Experiment {
  id: number; name: string; category: string; startDate: string; durationDays: number; savingIds: number[];
}
export function experimentProgress(experiment: Experiment, expenses: Expense[], savings: Expense[], now = new Date()) {
  const start = new Date(`${experiment.startDate}T00:00:00`);
  const end = new Date(start); end.setDate(end.getDate() + experiment.durationDays);
  const baselineStart = new Date(start); baselineStart.setDate(baselineStart.getDate() - experiment.durationDays);
  const elapsed = Math.max(0, Math.min(1, (now.getTime() - start.getTime()) / (end.getTime() - start.getTime())));
  const matches = expenses.filter(item => item.expenseCategory === experiment.category);
  const baselineRows = matches.filter(item => item.expenseDate >= baselineStart && item.expenseDate < start);
  const baseline = baselineRows.reduce((sum, item) => sum + item.amount, 0);
  const actual = matches.filter(item => item.expenseDate >= start && item.expenseDate < end && item.expenseDate <= now)
    .reduce((sum, item) => sum + item.amount, 0);
  const expected = baseline * elapsed;
  return { baseline, baselineStart, end, actual, expected, reduction: Math.max(0, expected - actual),
    over: Math.max(0, actual - expected), hasBaseline: baselineRows.length > 0,
    deposited: savings.filter(item => experiment.savingIds.includes(item.id)).reduce((sum, item) => sum + item.amount, 0),
    percent: Math.round(elapsed * 100), status: now < start ? 'Planned' : now >= end ? 'Completed' : 'Active' };
}
