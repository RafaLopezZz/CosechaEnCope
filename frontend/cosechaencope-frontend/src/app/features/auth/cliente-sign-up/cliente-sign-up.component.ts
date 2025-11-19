import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
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
  selector: 'app-cliente-sign-up',
  imports: [CommonModule, ReactiveFormsModule, RouterModule, NavbarComponent, FooterComponent],
  templateUrl: './cliente-sign-up.component.html',
  styleUrls: ['./cliente-sign-up.component.scss'],
})
export class ClienteSignUpComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  loading = false;
  returnUrl: string = '/articulos';

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirm: ['', [Validators.required]],
    nombre: ['', [Validators.required]],
    // Desglose de dirección
    calle: ['', [Validators.required]],
    cp: ['', [Validators.required, Validators.pattern(/^[0-9]{5}$/)]],
    localidad: ['', [Validators.required]],
    provincia: ['', [Validators.required]],
    telefono: [''],
    terms: [false, Validators.requiredTrue],
  }, { validators: match('password', 'confirm') });

  provincias = [
    'Álava', 'Albacete', 'Alicante', 'Almería', 'Asturias', 'Ávila', 'Badajoz', 'Barcelona', 'Burgos', 'Cáceres',
    'Cádiz', 'Cantabria', 'Castellón', 'Ciudad Real', 'Córdoba', 'La Coruña', 'Cuenca', 'Gerona', 'Granada', 'Guadalajara',
    'Guipúzcoa', 'Huelva', 'Huesca', 'Islas Baleares', 'Jaén', 'León', 'Lérida', 'Lugo', 'Madrid', 'Málaga', 'Murcia',
    'Navarra', 'Ourense', 'Palencia', 'Las Palmas', 'Pontevedra', 'La Rioja', 'Salamanca', 'Segovia', 'Sevilla', 'Soria',
    'Tarragona', 'Santa Cruz de Tenerife', 'Teruel', 'Toledo', 'Valencia', 'Valladolid', 'Vizcaya', 'Zamora', 'Zaragoza',
    'Ceuta', 'Melilla'
  ];

  ngOnInit() {
    // Obtener returnUrl de los query params
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '/articulos';
    });
  }

  onSubmit() {
    if (this.form.invalid) return;
    
    const { email, password, nombre, calle, cp, localidad, provincia, telefono } = this.form.getRawValue();
    
    // Concatenar dirección para el backend
    const direccionCompleta = `${calle}, ${cp}, ${localidad}, ${provincia}`;
    
    this.loading = true;
    
    this.auth.registerCliente({ email, password, nombre, direccion: direccionCompleta, telefono })
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: () => this.router.navigate(['/cliente/login'], { 
          queryParams: { ok: 1, returnUrl: this.returnUrl } 
        }),
        error: (e) => this.router.navigate(['/error'], { 
          queryParams: { code: e?.status ?? 0, m: 'Error en el registro de cliente' } 
        })
      });
  }
}