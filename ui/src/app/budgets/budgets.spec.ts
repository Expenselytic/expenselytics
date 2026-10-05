import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController, provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Budgets } from './budgets';

describe('Budget editor', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [Budgets],
    providers: [provideHttpClient(), provideHttpClientTesting()],
  }));

  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('loads, saves and edits a persisted monthly limit', async () => {
    const fixture = TestBed.createComponent(Budgets);
    const page = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    const url = `/api/v1/budgets/${page.month}`;
    http.expectOne(url).flush([]);
    page.amount = 100;
    page.save();
    const save = http.expectOne(url);
    expect(save.request.method).toBe('PUT');
    expect(save.request.body).toEqual({ category: '', amount: 100 });
    const budget = {
      id: 1, month: page.month, category: '', amount: 100,
    };
    save.flush(budget);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent)
      .toContain('Overall monthly budget');
    page.edit(budget);
    page.amount = 200;
    page.save();
    http.expectOne(url).flush({ ...budget, amount: 200 });
    expect(page.budgets.length).toBe(1);
    expect(page.cards[0].remaining).toBe(200);
    fixture.destroy();
  });

  it('preserves the limit on a failed delete and allows retry', () => {
    const fixture = TestBed.createComponent(Budgets);
    const page = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    const budget = {
      id: 1, month: page.month, category: 'Home', amount: 100,
    };
    http.expectOne(`/api/v1/budgets/${page.month}`).flush([budget]);
    page.remove(budget);
    http.expectOne('/api/v1/budgets/1').flush('Unavailable', {
      status: 503, statusText: 'Unavailable',
    });
    expect(page.budgets.length).toBe(1);
    expect(page.busy).toBeFalse();
    page.remove(budget);
    http.expectOne('/api/v1/budgets/1').flush(null);
    expect(page.budgets).toEqual([]);
    fixture.destroy();
  });

  it('blocks saving when initial loading fails', () => {
    const fixture = TestBed.createComponent(Budgets);
    const page = fixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(`/api/v1/budgets/${page.month}`).flush(null, {
      status: 500, statusText: 'Unavailable',
    });
    page.amount = 100;
    page.save();
    http.expectNone(`/api/v1/budgets/${page.month}`);
    expect(page.failed).toBeTrue();
    fixture.destroy();
  });
});
