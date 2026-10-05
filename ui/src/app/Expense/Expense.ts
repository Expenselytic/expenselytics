import { Resource } from '../hal/resource';

export class Expense extends Resource {
  readonly id: number;
  readonly expenseName: string;
  readonly expenseAmount: string;
  readonly expenseDate: Date;
  readonly expenseCategory: string;

  constructor(obj: any) {
    // Pass only HAL metadata: the API's amount field conflicts with our
    // getter.
    super({ _links: obj._links ?? {}, _embedded: obj._embedded ?? {} });
    this.id = Number(obj.id ?? obj.uid ?? 0);
    this.expenseName = String(obj.name ?? 'Untitled expense');
    this.expenseAmount = String(obj.amount ?? '0');
    this.expenseDate = new Date(obj.date ?? Date.now());
    this.expenseCategory = String(obj.category ?? 'Other');
  }

  get amount(): number {
    return Number(this.expenseAmount) || 0;
  }
  get formattedDate(): string {
    return this.expenseDate.toLocaleDateString();
  }
}
