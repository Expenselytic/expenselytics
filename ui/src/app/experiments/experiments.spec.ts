import { of, throwError } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { ResourceService } from '../hal/resource.service';
import { Experiments } from './experiments';

describe('Spending experiment workflow', () => {
  it('loads persisted experiments, creates, links and unlinks deposits', () => {
    const existing = { id: 1, name: 'Cook', category: 'Food', startDate: '2026-01-01', durationDays: 14, savingIds: [] as number[] };
    const http = jasmine.createSpyObj<HttpClient>('http', ['get']); http.get.and.returnValue(of([existing]));
    const api = jasmine.createSpyObj<ResourceService>('api', ['post', 'put', 'delete']);
    api.post.and.returnValue(of({ ...existing, id: 2 })); api.put.and.returnValue(of({})); api.delete.and.returnValue(of(undefined));
    const component = new Experiments(http, api); component.ngOnInit();
    expect(component.experiments.length).toBe(1);
    component.create(); expect(component.experiments.length).toBe(2);
    component.selected[1] = 10; component.link(existing); expect(existing.savingIds).toEqual([10]);
    component.unlink(existing, 10); expect(existing.savingIds).toEqual([]);
    api.put.and.returnValue(throwError(() => new Error('Offline')));
    component.selected[1] = 10; component.link(existing);
    expect(existing.savingIds).toEqual([]); expect(component.busy.size).toBe(0);
  });
});
