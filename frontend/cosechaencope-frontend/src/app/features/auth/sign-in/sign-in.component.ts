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
  selector: 'app-sign-in',
  imports: [CommonModule, ReactiveFormsModule, RouterModule, NavbarComponent, FooterComponent],
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.scss'],
})
export class SignInComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  loading = false;
  esProductor = false;

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  ngOnInit() {
    this.route.data.subscribe(
      (data) => (this.esProductor = !!data['productor'])
    );
  }

  onSubmit() {
    if (this.form.invalid) return;
    this.loading = true;

    const cred = this.form.getRawValue();
    const req$ = this.esProductor
      ? this.auth.loginProductor(cred)
      : this.auth.login(cred);

    req$.pipe(finalize(() => (this.loading = false))).subscribe({
      next: (response) => {
        // Redirigir según el tipo de usuario
        const redirectUrl = response.tipoUsuario === 'PRODUCTOR' 
          ? '/productor/dashboard' 
          : '/articulos';
        this.router.navigateByUrl(redirectUrl);
      },
      error: (ex:HttpErrorResponse) =>
        this.router.navigate(['/error'], {
          queryParams: { code: ex?.status ?? 0, m: 'No se pudo iniciar sesión' },
        }),
    });
  }
}
