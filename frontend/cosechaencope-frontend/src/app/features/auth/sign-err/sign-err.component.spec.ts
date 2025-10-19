import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SignErrComponent } from './sign-err.component';

describe('SignErrComponent', () => {
  let component: SignErrComponent;
  let fixture: ComponentFixture<SignErrComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SignErrComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SignErrComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
