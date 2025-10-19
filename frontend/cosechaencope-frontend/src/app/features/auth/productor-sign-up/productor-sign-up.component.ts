import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { finalize } from 'rxjs/operators';
import { NavbarComponent } from '../../home/navbar/navbar.component';
import { FooterComponent } from '../../home/footer/footer.component';

function match(field: string, other: string) {
  return (group: AbstractControl) => {
    const a = group.get(field)?.value; 
    const b = group.get(other)?.value;
    return a === b ? null : { mismatch: true };
  };
}

@Component({
  standalone: true,
  selector: 'app-productor-sign-up',
  imports: [CommonModule, ReactiveFormsModule, RouterModule, NavbarComponent, FooterComponent],
  templateUrl: './productor-sign-up.component.html',
  styleUrls: ['./productor-sign-up.component.scss'],
})
export class ProductorSignUpComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  loading = false;

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirm: ['', [Validators.required]],
    nombre: ['', [Validators.required]],
    direccion: ['', [Validators.required]],
    telefono: ['', [Validators.required]],
    terms: [false, Validators.requiredTrue],
  }, { validators: match('password', 'confirm') });

  onSubmit() {
    if (this.form.invalid) return;
    
    const { email, password, nombre, direccion, telefono } = this.form.getRawValue();
    this.loading = true;
    
    this.auth.registerProductor({ email, password, nombre, direccion, telefono })
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: () => this.router.navigate(['/productor/login'], { queryParams: { ok: 1 } }),
        error: (e) => this.router.navigate(['/error'], { 
          queryParams: { code: e?.status ?? 0, m: 'Error en el registro de productor' } 
        })
      });
  }
}