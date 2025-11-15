import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CarritoViewComponent } from './carrito-view.component';

describe('CarritoViewComponent', () => {
  let component: CarritoViewComponent;
  let fixture: ComponentFixture<CarritoViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CarritoViewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CarritoViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
