import { TestBed } from '@angular/core/testing';

import { OrdenesVentaService } from './orden-venta.service';

describe('OrdenVenta', () => {
  let service: OrdenesVentaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OrdenesVentaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
