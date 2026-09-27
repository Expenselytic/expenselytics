import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShowExpense } from './show-expense';

describe('ShowExpense', () => {
  let component: ShowExpense;
  let fixture: ComponentFixture<ShowExpense>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShowExpense]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ShowExpense);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
