import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AgGridAngular } from 'ag-grid-angular';
import { ColDef, GridOptions } from 'ag-grid-community';

interface RowData {
  country: string;
  gold: number;
  silver: number;
  bronze: number;
}

@Component({
  selector: 'app-root',
  standalone: true,  // <-- needed for standalone bootstrap
  imports: [RouterOutlet, AgGridAngular],
  templateUrl: './app.html',
  styleUrls: ['./app.css']  // <-- corrected
})

export class App {
  columnDefs: ColDef<RowData>[] = [
    { field: 'country', chartDataType: 'category' },
    { field: 'gold', chartDataType: 'series' },
    { field: 'silver', chartDataType: 'series' },
    { field: 'bronze', chartDataType: 'series' }
  ];

  rowData: RowData[] = [
    { country: 'USA', gold: 50, silver: 30, bronze: 20 },
    { country: 'UK', gold: 40, silver: 25, bronze: 30 },
    { country: 'China', gold: 60, silver: 40, bronze: 35 },
    { country: 'India', gold: 20, silver: 25, bronze: 30 },
    { country: 'Germany', gold: 35, silver: 20, bronze: 25 }
  ];

  defaultColDef: ColDef<RowData> = {
    editable: true,
    sortable: true,
    filter: true,
    resizable: true
  };

gridOptions: GridOptions<RowData> = {
  enableCharts: true,
  enableRangeSelection: true,
  rowSelection: 'single', // or 'multiple'
  popupParent: document.body,
};
}