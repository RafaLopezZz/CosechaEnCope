import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { finalize } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';
import { NavbarComponent } from '../../home/navbar/navbar.component';
import { FooterComponent } from '../../home/footer/footer.component';

@Component({
  standalone: true,
  selector: 'app-productor-sign-in',
  imports: [CommonModule, ReactiveFormsModule, RouterModule, NavbarComponent, FooterComponent],
  templateUrl: './productor-sign-in.component.html',
  styleUrls: ['./productor-sign-in.component.scss'],
})
export class ProductorSignInComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  loading = false;
  registroExitoso = false;

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  ngOnInit() {
    // Verificar si viene de un registro exitoso
    this.route.queryParams.subscribe(params => {
      this.registroExitoso = params['ok'] === '1';
    });
  }

  onSubmit() {
    if (this.form.invalid) return;
    
    this.loading = true;
    const cred = this.form.getRawValue();
    
    this.auth.loginProductor(cred)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (response) => {
          // Verificar que sea un productor
          if (response.tipoUsuario !== 'PRODUCTOR') {
            this.router.navigate(['/error'], {
              queryParams: { 
                code: 403, 
                m: 'Acceso denegado. Usa el login de clientes.' 
              },
            });
            return;
          }
          
          // Redirigir al panel del productor
          this.router.navigateByUrl('/productor/dashboard');
        },
        error: (ex: HttpErrorResponse) => {
          this.router.navigate(['/error'], {
            queryParams: { 
              code: ex?.status ?? 0, 
              m: 'Error al iniciar sesión como productor' 
            },
          });
        },
      });
  }
}