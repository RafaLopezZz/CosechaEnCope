import { Component, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs/operators';

/**
 * Modal de autenticación para checkout
 * 
 * Ofrece dos opciones al usuario no autenticado:
 * 1. Login rápido (si ya tiene cuenta)
 * 2. Registro express con datos mínimos
 * 
 * FEATURES:
 * ✅ Tabs para alternar entre login y registro
 * ✅ Validación inline de formularios
 * ✅ Feedback visual de errores
 * ✅ Cierre automático tras login exitoso
 * ✅ Responsive mobile-first
 * 
 * @author rafalopezzz
 */
@Component({
  selector: 'app-checkout-auth-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './checkout-auth-modal.component.html',
  styleUrls: ['./checkout-auth-modal.component.scss']
})
export class CheckoutAuthModalComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  @Output() authSuccess = new EventEmitter<void>();
  @Output() closeModal = new EventEmitter<void>();

  activeTab: 'login' | 'registro' = 'login';
  loading = false;
  errorMessage: string | null = null;

  // Formulario de login
  loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  // Formulario de registro express
  registroForm = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmarPassword: ['', [Validators.required]],
    telefono: ['', [Validators.required, Validators.minLength(9)]],
    direccion: ['', [Validators.required, Validators.minLength(10)]],
    aceptaTerminos: [false, [Validators.requiredTrue]]
  });

  /**
   * Cambiar entre tabs de login y registro
   */
  setTab(tab: 'login' | 'registro'): void {
    this.activeTab = tab;
    this.errorMessage = null;
    this.loginForm.reset();
    this.registroForm.reset();
  }

  /**
   * Procesar login
   */
  onLogin(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = null;
    const credentials = this.loginForm.getRawValue();

    this.auth.login(credentials)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (response) => {
          // Verificar que sea un cliente
          if (response.tipoUsuario !== 'CLIENTE') {
            this.errorMessage = 'Esta cuenta no es de tipo cliente. Por favor, usa tu cuenta de cliente o regístrate.';
            sessionStorage.removeItem('authToken'); // Limpiar token
            return;
          }

          console.log('[CheckoutAuthModal] Login exitoso como cliente');
          this.authSuccess.emit();
        },
        error: (err: HttpErrorResponse) => {
          console.error('[CheckoutAuthModal] Error en login:', err);
          if (err.status === 401) {
            this.errorMessage = 'Email o contraseña incorrectos';
          } else {
            this.errorMessage = 'Error al iniciar sesión. Intenta de nuevo.';
          }
        }
      });
  }

  /**
   * Procesar registro express
   */
  onRegistro(): void {
    if (this.registroForm.invalid) {
      this.registroForm.markAllAsTouched();
      return;
    }

    const formValue = this.registroForm.getRawValue();

    // Validar que las contraseñas coincidan
    if (formValue.password !== formValue.confirmarPassword) {
      this.errorMessage = 'Las contraseñas no coinciden';
      return;
    }

    this.loading = true;
    this.errorMessage = null;

    const { nombre, email, password, telefono, direccion } = formValue;

    this.auth.registerCliente({ nombre, email, password, telefono, direccion })
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: () => {
          console.log('[CheckoutAuthModal] Registro exitoso, iniciando sesión automática');
          
          // Auto-login tras registro exitoso
          this.auth.login({ email, password }).subscribe({
            next: (response) => {
              if (response.tipoUsuario === 'CLIENTE') {
                this.authSuccess.emit();
              } else {
                this.errorMessage = 'Error en el tipo de cuenta';
              }
            },
            error: () => {
              // Si falla el auto-login, cambiar a tab de login
              this.errorMessage = 'Registro exitoso. Por favor, inicia sesión.';
              this.setTab('login');
              this.loginForm.patchValue({ email, password });
            }
          });
        },
        error: (err: HttpErrorResponse) => {
          console.error('[CheckoutAuthModal] Error en registro:', err);
          if (err.status === 409) {
            this.errorMessage = 'Ya existe una cuenta con este email';
          } else if (err.status === 400) {
            this.errorMessage = 'Datos inválidos. Verifica los campos.';
          } else {
            this.errorMessage = 'Error al registrarse. Intenta de nuevo.';
          }
        }
      });
  }

  /**
   * Cerrar modal
   */
  onClose(): void {
    this.closeModal.emit();
  }

  /**
   * Obtener mensaje de error para un campo
   */
  getErrorMessage(form: 'login' | 'registro', field: string): string {
    const control = form === 'login' 
      ? this.loginForm.get(field)
      : this.registroForm.get(field);

    if (!control || !control.touched || !control.errors) {
      return '';
    }

    if (control.errors['required']) {
      return 'Este campo es obligatorio';
    }
    if (control.errors['email']) {
      return 'Email inválido';
    }
    if (control.errors['minlength']) {
      const minLength = control.errors['minlength'].requiredLength;
      return `Mínimo ${minLength} caracteres`;
    }

    return 'Campo inválido';
  }
}
