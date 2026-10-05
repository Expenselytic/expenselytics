import {
  fakeAsync,
  flushMicrotasks,
  TestBed,
  tick,
} from '@angular/core/testing';
import { Expense } from '../Expense/Expense';
import { NEVER, of, Subject, throwError } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { ResourceService } from '../hal/resource.service';
import { Experiments } from './experiments';

describe('Spending experiment workflow', () => {
  it(
    'loads persisted experiments, creates, ' +
      'links and unlinks deposits',
    () => {
      const existing = {
        id: 1,
        name: 'Cook',
        category: 'Food',
        startDate: '2026-01-01',
        durationDays: 14,
        savingIds: [] as number[],
      };
      const http = jasmine.createSpyObj<HttpClient>('http', ['get']);
      http.get.and.returnValue(of([existing]));
      const api = jasmine.createSpyObj<ResourceService>('api', [
        'post',
        'put',
        'delete',
      ]);
      api.post.and.returnValue(of({ ...existing, id: 2 }));
      api.put.and.returnValue(of({}));
      api.delete.and.returnValue(of(undefined));
      const component = new Experiments(http, api);
      component.ngOnInit();
      expect(component.experiments.length).toBe(1);
      component.create();
      expect(component.experiments.length).toBe(2);
      component.selected[1] = 10;
      component.link(existing);
      expect(existing.savingIds).toEqual([10]);
      component.unlink(existing, 10);
      expect(existing.savingIds).toEqual([]);
      api.put.and.returnValue(throwError(() => new Error('Offline')));
      component.selected[1] = 10;
      component.link(existing);
      expect(existing.savingIds).toEqual([]);
      expect(component.busy.size).toBe(0);
    },
  );
  it(
    'times out a stalled load and can retry ' + 'without remaining stuck',
    fakeAsync(() => {
      const http = jasmine.createSpyObj<HttpClient>('http', ['get']);
      http.get.and.returnValue(NEVER);
      const api = jasmine.createSpyObj<ResourceService>('api', [
        'post',
        'put',
        'delete',
      ]);
      const component = new Experiments(http, api);
      component.ngOnInit();
      tick(15000);
      expect(component.loading).toBeFalse();
      expect(component.loadFailed).toBeTrue();
      expect(component.message).toContain('too long');
      http.get.and.returnValue(of([]));
      component.load();
      expect(component.loadFailed).toBeFalse();
      expect(component.loading).toBeFalse();
      component.ngOnDestroy();
    }),
  );

  it(
    'keeps the deposit selector stable ' +
      'across change detection with a large ' +
      'history',
    fakeAsync(() => {
      const http = jasmine.createSpyObj<HttpClient>('http', ['get']);
      http.get.and.returnValue(
        of([
          {
            id: 1,
            name: 'Cook',
            category: 'Food',
            startDate: '2026-01-01',
            durationDays: 14,
            savingIds: [],
          },
        ]),
      );
      TestBed.configureTestingModule({
        imports: [Experiments],
        providers: [
          { provide: HttpClient, useValue: http },
          { provide: ResourceService, useValue: {} },
        ],
      });
      const fixture = TestBed.createComponent(Experiments);
      fixture.componentRef.setInput(
        'expenses',
        Array.from(
          { length: 720 },
          (_, id) =>
            new Expense({
              id,
              amount: '10',
              category: 'Food',
              date: '2026-01-02T12:00:00',
            }),
        ),
      );
      fixture.componentRef.setInput('savings', [
        new Expense({
          id: 1000,
          amount: '20',
          date: '2026-01-03T12:00:00',
        }),
      ]);
      fixture.detectChanges();
      flushMicrotasks();
      const select = fixture.nativeElement.querySelector('.link select');
      const cards = fixture.componentInstance.cards;
      for (let i = 0; i < 5; i++) {
        fixture.detectChanges();
        flushMicrotasks();
      }
      expect(fixture.componentInstance.cards).toBe(cards);
      expect(fixture.nativeElement.querySelector('.link select')).toBe(
        select,
      );
      fixture.componentRef.setInput('savings', []);
      fixture.detectChanges();
      flushMicrotasks();
      expect(fixture.componentInstance.cards).not.toBe(cards);
      expect(fixture.nativeElement.querySelector('.link select')).toBe(
        select,
      );
      fixture.destroy();
    }),
  );

  it(
    'requires confirmation and removes only ' +
      'after a successful response',
    () => {
      const experiment = {
        id: 1,
        name: 'Cook more',
        category: 'Food',
        startDate: '2026-01-01',
        durationDays: 14,
        savingIds: [10],
      };
      const http = jasmine.createSpyObj<HttpClient>('http', ['get']);
      const api = jasmine.createSpyObj<ResourceService>('api', [
        'delete',
      ]);
      const component = new Experiments(http, api);
      component.experiments = [experiment];
      component.savings = [
        new Expense({
          id: 10,
          amount: '25',
          date: '2026-01-03T12:00:00',
        }),
      ];
      const confirm = spyOn(window, 'confirm').and.returnValue(false);
      component.remove(experiment);
      expect(api.delete).not.toHaveBeenCalled();
      expect(component.experiments.length).toBe(1);
      confirm.and.returnValue(true);
      const pending = new Subject<void>();
      api.delete.and.returnValue(pending);
      component.remove(experiment);
      component.remove(experiment);
      expect(api.delete).toHaveBeenCalledOnceWith(
        '/api/v1/experiments/1',
      );
      expect(component.experiments.length).toBe(1);
      expect(confirm).toHaveBeenCalledTimes(2);
      pending.next();
      pending.complete();
      expect(component.experiments.length).toBe(0);
      expect(component.savings.length).toBe(1);
      expect(component.removing.size).toBe(0);
      component.ngOnDestroy();
    },
  );

  it('keeps an experiment and permits retry when removal fails', () => {
    const experiment = {
      id: 1,
      name: 'Cook',
      category: 'Food',
      startDate: '2026-01-01',
      durationDays: 14,
      savingIds: [],
    };
    const http = jasmine.createSpyObj<HttpClient>('http', ['get']);
    const api = jasmine.createSpyObj<ResourceService>('api', ['delete']);
    api.delete.and.returnValue(throwError(() => new Error('Offline')));
    const component = new Experiments(http, api);
    component.experiments = [experiment];
    spyOn(window, 'confirm').and.returnValue(true);
    component.remove(experiment);
    expect(component.experiments).toEqual([experiment]);
    expect(component.busy.size).toBe(0);
    expect(component.removing.size).toBe(0);
    expect(component.message).toContain('Could not remove');
    component.ngOnDestroy();
  });
});
