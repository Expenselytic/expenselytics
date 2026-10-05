import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { EMPTY, of, Subject } from 'rxjs';
import { FinancePage as App } from './finance-page';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { ResourceService } from './hal/resource.service';
import { Resource } from './hal/resource';
import { ApiRoot } from './api/api-root';
import { Expense } from './Expense/Expense';

const record = (id: number, amount: string, name = 'Lunch') =>
  new Expense({
    id,
    amount,
    name,
    category: 'Food',
    date: '2026-01-10T12:00:00',
  });
describe('App activity', () => {
  let root: jasmine.SpyObj<ApiRoot>;
  beforeEach(async () => {
    root = jasmine.createSpyObj('ApiRoot', [
      'getExpenses',
      'getSavings',
      'createExpense',
      'createSaving',
      'deleteExpense',
      'deleteSaving',
    ]);
    root.getExpenses.and.returnValue(
      of([record(1, '10'), record(2, '20')]),
    );
    root.getSavings.and.returnValue(of([record(1, '50', 'Fund')]));
    spyOn(Resource, 'fetchRoot').and.returnValue(of(root));
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        { provide: ResourceService, useValue: {} },
        { provide: HttpClient, useValue: { get: () => of([]) } },
        {
          provide: AuthService,
          useValue: {
            signedOut: EMPTY,
            current: () =>
              of({ name: 'Jane', email: 'jane@example.com' }),
          },
        },
      ],
    }).compileComponents();
    TestBed.inject(ActivatedRoute).snapshot.data = { workspace: true };
  });

  it(
    'shows newly saved expenses and savings ' +
      'and updates totals without another ' +
      'fetch',
    () => {
      const fixture = TestBed.createComponent(App);
      fixture.detectChanges();
      const app = fixture.componentInstance;
      root.createExpense.and.returnValue(of(record(3, '15', 'Dinner')));
      app.form = {
        name: 'Dinner',
        category: 'Food',
        amount: '15',
        date: '2026-01-10T12:00',
      };
      app.addExpense();
      root.createSaving.and.returnValue(of(record(2, '25', 'Vacation')));
      app.savingsForm = {
        name: 'Vacation',
        category: 'Savings',
        amount: '25',
        date: '2026-01-10T12:00',
      };
      app.addSaving();
      fixture.detectChanges();
      expect(app.expenses.map((row) => row.id)).toEqual([3, 2, 1]);
      expect(app.total).toBe(45);
      expect(app.savingsTotal).toBe(75);
      expect(app.savings[0].expenseName).toBe('Vacation');
      expect(root.getExpenses).toHaveBeenCalledTimes(1);
      expect(fixture.nativeElement.textContent).toContain('Dinner');
      expect(fixture.nativeElement.textContent).toContain('$75.00');
    },
  );

  it(
    'keeps failed saves out of totals and ' +
      'prevents duplicate submissions',
    () => {
      const fixture = TestBed.createComponent(App);
      fixture.detectChanges();
      const app = fixture.componentInstance;
      app.form = {
        name: 'Dinner',
        category: 'Food',
        amount: '15',
        date: '2026-01-10T12:00',
      };
      const pending = new Subject<Expense>();
      root.createExpense.and.returnValue(pending);
      app.addExpense();
      app.addExpense();
      expect(root.createExpense).toHaveBeenCalledTimes(1);
      pending.error(new Error('Offline'));
      expect(app.total).toBe(30);
      expect(app.form.name).toBe('Dinner');
      expect(app.saving).toBeFalse();
    },
  );

  it(
    'deletes a saving through the UI and ' +
      'updates totals and charts after ' +
      'success',
    () => {
      const fixture = TestBed.createComponent(App);
      fixture.detectChanges();
      const app = fixture.componentInstance;
      const now = new Date();
      app.savings = [
        new Expense({
          id: 5,
          name: 'Fund',
          category: 'Savings',
          amount: '50',
          date: now.toISOString(),
        }),
      ];
      fixture.detectChanges();
      const pending = new Subject<void>();
      root.deleteSaving.and.returnValue(pending);
      const button = fixture.nativeElement.querySelector(
        'button[title="Delete saving"]',
      );
      button.click();
      button.click();
      fixture.detectChanges();
      expect(root.deleteSaving).toHaveBeenCalledTimes(1);
      expect(button.disabled).toBeTrue();
      expect(app.savingsTotal).toBe(50);
      pending.next();
      pending.complete();
      fixture.detectChanges();
      expect(app.savings.length).toBe(0);
      expect(app.savingsTotal).toBe(0);
      expect(
        fixture.nativeElement.querySelector(
          'button[title="Delete saving"]',
        ),
      ).toBeNull();
      expect(
        fixture.nativeElement.querySelector('app-analytics').textContent,
      ).not.toContain('$50.00');
    },
  );

  it(
    'retains a saving and its total when ' +
      'deletion fails and allows retry',
    () => {
      const fixture = TestBed.createComponent(App);
      fixture.detectChanges();
      const app = fixture.componentInstance;
      const pending = new Subject<void>();
      root.deleteSaving.and.returnValue(pending);
      app.deleteSaving(app.savings[0]);
      pending.error(new Error('Offline'));
      expect(app.savingsTotal).toBe(50);
      expect(app.savings.length).toBe(1);
      expect(app.deletingSavings.size).toBe(0);
      expect(app.message).toContain('Could not remove');
      root.deleteSaving.and.returnValue(of(undefined));
      app.deleteSaving(app.savings[0]);
      expect(app.savingsTotal).toBe(0);
    },
  );

  it('renders the full workspace without the landing hero', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.hero')).toBeNull();
    expect(
      fixture.nativeElement.querySelector('app-analytics'),
    ).toBeTruthy();
    expect(
      fixture.nativeElement.querySelector('app-experiments'),
    ).toBeTruthy();
    expect(
      fixture.nativeElement.querySelector('.workspace-sidebar'),
    ).toBeTruthy();
  });
});
