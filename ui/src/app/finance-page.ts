import { AuthService } from './auth.service';
import { Profile } from './api/profile';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { Experiments } from './experiments/experiments';
import { Analytics } from './analytics/analytics';
import { FormsModule } from '@angular/forms';
import { ApiRoot, ExpenseRequest } from './api/api-root';
import { Expense } from './Expense/Expense';
import { Resource } from './hal/resource';
import { ResourceService } from './hal/resource.service';

@Component({
  selector: 'app-finance-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CurrencyPipe, Analytics, Experiments, RouterLink],
  templateUrl: './app.html',
  styleUrls: ['./app.css', './workspace.css'],
})
export class FinancePage implements OnInit {
  get isWorkspace(): boolean { return this.route.snapshot.data['workspace'] === true; }

  openWorkspace(): void {
    void this.router.navigateByUrl('/workspace');
  }

  private newest(items: Expense[]): Expense[] {
    return [...items].sort((a, b) => b.id - a.id);
  }

  get monthlyExpenses(): Expense[] {
    const now = new Date();
    return this.expenses.filter(item => item.expenseDate.getFullYear() === now.getFullYear()
      && item.expenseDate.getMonth() === now.getMonth());
  }

  get monthlyTotal(): number {
    return this.monthlyExpenses.reduce((sum, item) => sum + item.amount, 0);
  }

  get monthlySavings(): Expense[] {
    const now = new Date();
    return this.savings.filter(item => item.expenseDate.getFullYear() === now.getFullYear()
      && item.expenseDate.getMonth() === now.getMonth());
  }

  get monthlySaved(): number {
    return this.monthlySavings.reduce((sum, item) => sum + item.amount, 0);
  }

  isAuthenticated = false;
  authMode: 'login' | 'signup' = 'login';
  showAuth = false;
  profileName = '';
  email = '';
  password = '';
  authenticating = false;
  authError = '';
  loading = true;
  saving = false;
  message = '';
  root?: ApiRoot;
  expenses: Expense[] = [];
  savings: Expense[] = [];
  savingSavings = false;
  deletingSavings = new Set<number>();
  savingsLoading = false;
  savingsForm: ExpenseRequest = { ...this.emptyForm(), category: 'Savings' };

  get savingsTotal(): number {
    return this.savings.reduce((sum, item) => sum + item.amount, 0);
  }

  addSaving(): void {
    if (this.savingSavings || this.savingsLoading) return;
    if (!this.validForm(this.savingsForm)) {
      this.message = 'Enter a name, positive amount, category, and date.';
      return;
    }
    if (!this.root) { this.message = 'Connect the backend to save.'; return; }
    this.savingSavings = true;
    this.root.createSaving(this.resourceService, { ...this.savingsForm, amount: String(this.savingsForm.amount) }).subscribe({
      next: (saved) => {
        this.savings = this.newest([saved, ...this.savings.filter(item => item.id !== saved.id)]);
        this.savingSavings = false;
        this.savingsForm = { ...this.emptyForm(), category: 'Savings' };
        this.message = 'Saving added successfully.';
      },
      error: () => { this.savingSavings = false; this.message = 'Could not save. Check that you are logged in and try again.'; },
    });
  }

  deleteSaving(saving: Expense): void {
    if (!this.root || !saving.id) {
      this.message = 'This saving cannot be deleted yet.';
      return;
    }
    if (this.deletingSavings.has(saving.id)) return;
    this.deletingSavings.add(saving.id);
    this.root.deleteSaving(this.resourceService, saving.id).subscribe({
      next: () => {
        this.savings = this.savings.filter(item => item.id !== saving.id);
        this.deletingSavings.delete(saving.id);
        this.message = 'Saving removed.';
      },
      error: () => {
        this.deletingSavings.delete(saving.id);
        this.message = 'Could not remove this saving. Please try again.';
      },
    });
  }

  private loadSavings(): void {
    if (!this.root) return;
    this.savingsLoading = true;
    this.root.getSavings().subscribe({
      next: items => { this.savings = this.newest(items); this.savingsLoading = false; },
      error: () => { this.savingsLoading = false; this.message = 'Savings could not be loaded.'; },
    });
  }

  private validForm(form: ExpenseRequest): boolean {
    return !!form.name.trim() && !!form.category.trim() && Number(form.amount) > 0
      && /^\d+(\.\d{1,2})?$/.test(String(form.amount)) && !!form.date;
  }
  form: ExpenseRequest = this.emptyForm();

  constructor(
    private readonly resourceService: ResourceService,
    private readonly auth: AuthService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.loadApi();
    this.auth
      .current()
      .subscribe({
        next: (profile) => this.setProfile(profile),
        error: () => {},
      });
  }

  get total(): number {
    return this.expenses.reduce(
      (sum, expense) => sum + expense.amount,
      0,
    );
  }
  get recentTotal(): number {
    return this.expenses
      .slice(0, 3)
      .reduce((sum, expense) => sum + expense.amount, 0);
  }
  get categoryCount(): number {
    return new Set(
      this.expenses.map((expense) => expense.expenseCategory),
    ).size;
  }

  openAuth(mode: 'login' | 'signup'): void {
    this.authMode = mode;
    this.showAuth = true;
    this.authError = '';
    this.password = '';
  }
  authenticate(): void {
    if (this.authenticating) return;
    this.authenticating = true;
    this.authError = '';
    this.auth
      .authenticate(
        this.authMode,
        this.profileName,
        this.email,
        this.password,
      )
      .subscribe({
        next: (profile) => {
          this.setProfile(profile);
          this.showAuth = false;
          this.password = '';
          this.authenticating = false;
          this.message = `Welcome, ${profile.name}!`;
        },
        error: (error) => {
          this.authenticating = false;
          this.authError =
            error.status === 409
              ? 'An account with this email already exists.'
              : error.status === 401
                ? 'Invalid email or password.'
                : error.status === 400
                  ? 'Check your details. Signup requires a password of 12–256 characters.'
                  : 'Could not connect. Please try again.';
        },
      });
  }
  signOut(): void {
    this.auth.logout().subscribe({
      next: () => {
        void this.router.navigateByUrl('/');
        this.isAuthenticated = false;
        this.profileName = '';
        this.password = '';
        this.message = 'You have signed out.';
      },
      error: () => {
        this.message = 'Could not sign out. Please try again.';
      },
    });
  }
  private setProfile(profile: Profile): void {
    this.profileName = profile.name;
    this.email = profile.email;
    this.isAuthenticated = true;
  }

  addExpense(): void {
    if (this.saving || this.loading) return;
    if (!this.validForm(this.form)) {
      this.message = 'Add a name, amount, and category first.';
      return;
    }
    this.saving = true;
    const finish = (): void => {
      this.saving = false;
      this.form = this.emptyForm();
    };
    if (!this.root) {
      this.message =
        'The expense API is unavailable. Start the backend and try again.';
      this.saving = false;
      return;
    }
    this.root.createExpense(this.resourceService, { ...this.form, amount: String(this.form.amount) }).subscribe({
      next: (saved) => {
        this.expenses = this.newest([saved, ...this.expenses.filter(item => item.id !== saved.id)]);
        this.message = 'Expense saved successfully.';
        finish();
      },
      error: () => {
        this.message =
          'Could not save this expense. Please check the API and try again.';
        this.saving = false;
      },
    });
  }

  deleteExpense(expense: Expense): void {
    if (!this.root || !expense.id) {
      this.message = 'This expense cannot be deleted yet.';
      return;
    }
    this.root.deleteExpense(this.resourceService, expense.id).subscribe({
      next: () => {
        this.expenses = this.expenses.filter(
          (item) => item.id !== expense.id,
        );
        this.message = 'Expense removed.';
      },
      error: () =>
        (this.message =
          'Could not remove this expense. Please try again.'),
    });
  }

  private loadApi(): void {
    Resource.fetchRoot(ApiRoot).subscribe({
      next: (root) => {
        this.root = root;
        this.loadExpenses();
        this.loadSavings();
      },
      error: () => {
        this.loading = false;
        this.message = 'Connect the backend to see your saved expenses.';
      },
    });
  }

  private loadExpenses(): void {
    if (!this.root) {
      return;
    }
    this.loading = true;
    this.root.getExpenses().subscribe({
      next: (expenses) => {
        this.expenses = this.newest(expenses);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.message = 'Expenses could not be loaded.';
      },
    });
  }

  private emptyForm(): ExpenseRequest {
    return {
      name: '',
      category: 'Food & dining',
      amount: '',
      date: new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 16),
    };
  }
}
