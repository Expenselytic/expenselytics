import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Expense } from '../Expense/Expense';
import { API_V1_ROOT } from '../api/api-paths';
import { ResourceService } from '../hal/resource.service';
import { Experiment, experimentProgress } from './experiment';

@Component({ selector: 'app-experiments', standalone: true, imports: [CommonModule, FormsModule],
  templateUrl: './experiments.html', styleUrl: './experiments.css' })
export class Experiments implements OnInit {
  @Input() expenses: Expense[] = [];
  @Input() savings: Expense[] = [];
  experiments: Experiment[] = [];
  loading = true;
  loadFailed = false;
  creating = false;
  busy = new Set<number>();
  selected: Record<number, number | null> = {};
  message = '';
  form = { name: '', category: 'Food & dining', startDate: this.today(), durationDays: 14 };
  private readonly url = `${API_V1_ROOT}/experiments`;
  constructor(private readonly http: HttpClient, private readonly resources: ResourceService) {}
  ngOnInit(): void { this.load(); }
  today(): string { return new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10); }
  get categories(): string[] {
    return [...new Set(['Food & dining', 'Transport', 'Shopping', 'Health', 'Home', 'Entertainment', 'Other', ...this.expenses.map(e => e.expenseCategory)])];
  }
  get cards() { return this.experiments.map(experiment => ({ experiment, progress: experimentProgress(experiment, this.expenses, this.savings) })); }
  load(): void {
    this.loading = true; this.loadFailed = false;
    this.http.get<Experiment[]>(this.url).subscribe({
      next: items => { this.experiments = items; this.loading = false; },
      error: () => { this.loading = false; this.loadFailed = true; this.message = 'Experiments could not be loaded. Please retry.'; },
    });
  }
  create(): void {
    if (this.creating || this.loading || this.loadFailed) return;
    this.creating = true;
    this.resources.post(this.url, this.form).subscribe({
      next: result => { this.experiments = [result as Experiment, ...this.experiments]; this.creating = false; this.form = { ...this.form, name: '' }; this.message = 'Experiment created. Keep recording your spending to measure progress.'; },
      error: () => { this.creating = false; this.message = 'Could not create experiment. Check your details and login, then retry.'; },
    });
  }
  available(experiment: Experiment): Expense[] {
    const used = new Set(this.experiments.flatMap(e => e.savingIds));
    return this.savings.filter(s => !used.has(s.id) && s.expenseDate >= new Date(`${experiment.startDate}T00:00:00`));
  }
  linked(experiment: Experiment): Expense[] { return this.savings.filter(s => experiment.savingIds.includes(s.id)); }
  link(experiment: Experiment): void {
    const id = this.selected[experiment.id];
    if (!id || this.busy.has(experiment.id)) return;
    this.busy.add(experiment.id);
    this.resources.put(`${this.url}/${experiment.id}/savings/${id}`, {}).subscribe({
      next: () => { experiment.savingIds = [...experiment.savingIds, id]; this.selected[experiment.id] = null; this.busy.delete(experiment.id); this.message = 'Deposit linked. No new money or deposit was created.'; },
      error: () => { this.busy.delete(experiment.id); this.message = 'Could not link deposit. It may already be linked elsewhere. Reload experiments and retry.'; },
    });
  }
  unlink(experiment: Experiment, id: number): void {
    if (this.busy.has(experiment.id)) return;
    this.busy.add(experiment.id);
    this.resources.delete(`${this.url}/${experiment.id}/savings/${id}`).subscribe({
      next: () => { experiment.savingIds = experiment.savingIds.filter(value => value !== id); this.busy.delete(experiment.id); this.message = 'Deposit unlinked; your saving is still recorded.'; },
      error: () => { this.busy.delete(experiment.id); this.message = 'Could not unlink deposit. Please retry.'; },
    });
  }
}
