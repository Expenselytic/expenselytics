import { Resource } from '../hal/resource';
import { map, Observable } from 'rxjs';
import { ResourceService } from '../hal/resource.service';
import { Expense } from '../Expense/Expense';
import { API_V1_ROOT } from './api-paths';

export class ApiRoot extends Resource {
  apiVersion: string = 'unknown';

  status: string = 'unknown';

  constructor(obj: any) {
    super(obj);
    Object.assign(this, obj);
  }

  getSavings(): Observable<Expense[]> {
    return this.fetch(ExpenseList, 'savings').pipe(map(collection => collection.items));
  }

  createSaving(service: ResourceService, saving: ExpenseRequest): Observable<unknown> {
    return service.post(this.hrefFor('savings') ?? `${API_V1_ROOT}/savings`, saving);
  }

  getExpenses(): Observable<Expense[]> {
    return this.fetch(ExpenseList, 'getExpense').pipe(
      map((collection) => collection.items),
    );
  }

  createExpense(
    service: ResourceService,
    expense: ExpenseRequest,
  ): Observable<unknown> {
    const href = this.hrefFor('addExpense');
    if (!href) {
      throw new Error('The API does not advertise an addExpense link.');
    }
    return service.post(href, expense);
  }

  deleteExpense(service: ResourceService, id: number): Observable<void> {
    const href =
      this.hrefFor('deleteExpense', { id }) ??
      `${this.hrefFor('self') ?? API_V1_ROOT}/deleteExpense/${id}`;
    return service.delete(href);
  }

  updateExpense(
    service: ResourceService,
    id: number,
    expense: ExpenseRequest,
  ): Observable<Expense> {
    const href = this.hrefFor('editExpense', { id });
    if (!href) {
      throw new Error('The API does not advertise an editExpense link.');
    }
    return service
      .put(href, expense)
      .pipe(map((value) => new Expense(value)));
  }
}

export interface ExpenseRequest {
  name: string;
  category: string;
  amount: string;
  date: string;
}

/** Adapts the API's array response into a HAL Resource-compatible collection. */
export class ExpenseList extends Resource {
  readonly items: Expense[];

  constructor(obj: unknown) {
    super({});
    const values = Array.isArray(obj)
      ? obj
      : ((obj as { _embedded?: { expenses?: unknown[] } })?._embedded
          ?.expenses ?? []);
    this.items = values.map((value) => new Expense(value));
  }
}
