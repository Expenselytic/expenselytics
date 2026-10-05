import { CommonModule } from '@angular/common';
import {
  Component,
  computed,
  Input,
  OnDestroy,
  OnInit,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription, takeUntil, timeout } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Expense } from '../Expense/Expense';
import { API_V1_ROOT } from '../api/api-paths';
import { ResourceService } from '../hal/resource.service';
import { Experiment, experimentProgress } from './experiment';

@Component({
  selector: 'app-experiments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './experiments.html',
  styleUrl: './experiments.css',
})
export class Experiments implements OnInit, OnDestroy {
  private readonly expenseData = signal<Expense[]>([]);
  private readonly savingData = signal<Expense[]>([]);
  private readonly experimentData = signal<Experiment[]>([]);
  private readonly destroyed = new Subject<void>();
  private loadSubscription?: Subscription;
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
  set experiments(value: Experiment[]) {
    this.experimentData.set(value);
  }
  get experiments(): Experiment[] {
    return this.experimentData();
  }
  loading = true;
  loadFailed = false;
  creating = false;
  busy = new Set<number>();
  removing = new Set<number>();
  selected: Record<number, number | null> = {};
  message = '';
  form = {
    name: '',
    category: 'Food & dining',
    startDate: this.today(),
    durationDays: 14,
  };
  private readonly url = `${API_V1_ROOT}/experiments`;
  constructor(
    private readonly http: HttpClient,
    private readonly resources: ResourceService,
  ) {}
  ngOnInit(): void {
    this.load();
  }
  today(): string {
    return new Date(Date.now() - new Date().getTimezoneOffset() * 60000)
      .toISOString()
      .slice(0, 10);
  }
  ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
  }
  trackExperiment(
    _index: number,
    card: { experiment: Experiment },
  ): number {
    return card.experiment.id;
  }
  trackSaving(_index: number, saving: Expense): number {
    return saving.id;
  }
  get categories(): string[] {
    return this.categoryData();
  }
  private readonly categoryData = computed(() => {
    return [
      ...new Set([
        'Food & dining',
        'Transport',
        'Shopping',
        'Health',
        'Home',
        'Entertainment',
        'Other',
        ...this.expenses.map((e) => e.expenseCategory),
      ]),
    ];
  });
  get cards() {
    return this.cardData();
  }
  private readonly cardData = computed(() => {
    const used = new Set(this.experiments.flatMap((e) => e.savingIds));
    const now = new Date();
    return this.experiments.map((experiment) => {
      const start = new Date(`${experiment.startDate}T00:00:00`);
      const ids = new Set(experiment.savingIds);
      return {
        experiment,
        progress: experimentProgress(
          experiment,
          this.expenses,
          this.savings,
          now,
        ),
        available: this.savings.filter(
          (s) => !used.has(s.id) && s.expenseDate >= start,
        ),
        linked: this.savings.filter((s) => ids.has(s.id)),
      };
    });
  });
  load(): void {
    this.loadSubscription?.unsubscribe();
    this.loading = true;
    this.loadFailed = false;
    this.message = '';
    this.loadSubscription = this.http
      .get<Experiment[]>(this.url)
      .pipe(timeout(15000), takeUntil(this.destroyed))
      .subscribe({
        next: (items) => {
          this.experiments = items;
          this.loading = false;
        },
        error: (error) => {
          this.loading = false;
          this.loadFailed = true;
          this.message =
            error.name === 'TimeoutError'
              ? 'Experiments took too long to load. You ' +
                'can keep using your workspace and ' +
                'retry.'
              : 'Experiments could not be loaded. Please retry.';
        },
      });
  }
  create(): void {
    if (this.creating || this.loading || this.loadFailed) return;
    this.creating = true;
    this.resources
      .post(this.url, this.form)
      .pipe(takeUntil(this.destroyed))
      .subscribe({
        next: (result) => {
          this.experiments = [result as Experiment, ...this.experiments];
          this.creating = false;
          this.form = { ...this.form, name: '' };
          this.message =
            'Experiment created. Keep recording ' +
            'your spending to measure progress.';
        },
        error: () => {
          this.creating = false;
          this.message =
            'Could not create experiment. Check ' +
            'your details and login, then retry.';
        },
      });
  }
  remove(experiment: Experiment): void {
    if (this.busy.has(experiment.id)) return;
    if (
      !window.confirm(
        `Remove "${experiment.name}"?\n\n` +
          'This permanently removes the experiment and its deposit ' +
          'links. Your expenses and savings deposits will be kept.',
      )
    )
      return;
    this.busy.add(experiment.id);
    this.removing.add(experiment.id);
    this.resources
      .delete(`${this.url}/${experiment.id}`)
      .pipe(takeUntil(this.destroyed))
      .subscribe({
        next: () => {
          this.experiments = this.experiments.filter(
            (item) => item.id !== experiment.id,
          );
          delete this.selected[experiment.id];
          this.busy.delete(experiment.id);
          this.removing.delete(experiment.id);
          this.message =
            'Experiment removed. Your expenses and ' +
            'savings deposits were kept.';
        },
        error: () => {
          this.busy.delete(experiment.id);
          this.removing.delete(experiment.id);
          this.message =
            'Could not remove the experiment. Please try again.';
        },
      });
  }

  link(experiment: Experiment): void {
    const id = this.selected[experiment.id];
    if (!id || this.busy.has(experiment.id)) return;
    this.busy.add(experiment.id);
    this.resources
      .put(`${this.url}/${experiment.id}/savings/${id}`, {})
      .pipe(takeUntil(this.destroyed))
      .subscribe({
        next: () => {
          experiment.savingIds = [...experiment.savingIds, id];
          this.experiments = [...this.experiments];
          this.selected[experiment.id] = null;
          this.busy.delete(experiment.id);
          this.message =
            'Deposit linked. No new money or deposit was created.';
        },
        error: () => {
          this.busy.delete(experiment.id);
          this.message =
            'Could not link deposit. It may already ' +
            'be linked elsewhere. Reload ' +
            'experiments and retry.';
        },
      });
  }
  unlink(experiment: Experiment, id: number): void {
    if (this.busy.has(experiment.id)) return;
    this.busy.add(experiment.id);
    this.resources
      .delete(`${this.url}/${experiment.id}/savings/${id}`)
      .pipe(takeUntil(this.destroyed))
      .subscribe({
        next: () => {
          experiment.savingIds = experiment.savingIds.filter(
            (value) => value !== id,
          );
          this.experiments = [...this.experiments];
          this.busy.delete(experiment.id);
          this.message =
            'Deposit unlinked; your saving is still recorded.';
        },
        error: () => {
          this.busy.delete(experiment.id);
          this.message = 'Could not unlink deposit. Please retry.';
        },
      });
  }
}
