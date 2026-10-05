import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExpenseHeader } from './expense-header';

describe('ExpenseHeader', () => {
  let component: ExpenseHeader;
  let fixture: ComponentFixture<ExpenseHeader>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExpenseHeader],
    }).compileComponents();

    fixture = TestBed.createComponent(ExpenseHeader);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
