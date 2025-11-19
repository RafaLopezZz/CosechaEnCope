import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { UserStoreService } from '../../../core/services/user-store.service';
import { ClienteService, ClienteResponse, ClienteRequest } from '../../../core/services/cliente.service';

@Component({
  selector: 'app-perfil-cliente',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.scss',
})
export class PerfilClienteComponent implements OnInit {
  private fb = inject(FormBuilder);
  private userStore = inject(UserStoreService);
  private clienteService = inject(ClienteService);
  private router = inject(Router);

  cliente: ClienteResponse | null = null;
  loading = true;
  saving = false;
  successMessage = '';
  errorMessage = '';

  currentUser = this.userStore.snapshot();

  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(3)]],
    // Desglose de dirección
    calle: ['', [Validators.required]],
    cp: ['', [Validators.required, Validators.pattern(/^[0-9]{5}$/)]],
    localidad: ['', [Validators.required]],
    provincia: ['', [Validators.required]],
    telefono: ['', [Validators.required, Validators.pattern(/^[0-9]{9}$/)]],
  });

  provincias = [
    'Álava', 'Albacete', 'Alicante', 'Almería', 'Asturias', 'Ávila', 'Badajoz', 'Barcelona', 'Burgos', 'Cáceres',
    'Cádiz', 'Cantabria', 'Castellón', 'Ciudad Real', 'Córdoba', 'La Coruña', 'Cuenca', 'Gerona', 'Granada', 'Guadalajara',
    'Guipúzcoa', 'Huelva', 'Huesca', 'Islas Baleares', 'Jaén', 'León', 'Lérida', 'Lugo', 'Madrid', 'Málaga', 'Murcia',
    'Navarra', 'Ourense', 'Palencia', 'Las Palmas', 'Pontevedra', 'La Rioja', 'Salamanca', 'Segovia', 'Sevilla', 'Soria',
    'Tarragona', 'Santa Cruz de Tenerife', 'Teruel', 'Toledo', 'Valencia', 'Valladolid', 'Vizcaya', 'Zamora', 'Zaragoza',
    'Ceuta', 'Melilla'
  ];

  ngOnInit() {
    if (!this.currentUser || this.currentUser.tipoUsuario !== 'CLIENTE') {
      this.router.navigateByUrl('/cliente/login');
      return;
    }

    this.loadCliente();
  }

  loadCliente() {
    if (!this.currentUser) return;

    this.clienteService.getClientePorUsuario(this.currentUser.idUsuario).subscribe({
      next: (cliente) => {
        this.cliente = cliente;
        
        // Parsear dirección
        let calle = '', cp = '', localidad = '', provincia = '';
        if (cliente.direccion) {
          const parts = cliente.direccion.split(', ');
          if (parts.length === 4) {
            [calle, cp, localidad, provincia] = parts;
          } else {
            // Fallback para direcciones antiguas
            calle = cliente.direccion;
          }
        }

        this.form.patchValue({
          nombre: cliente.nombre || '',
          calle,
          cp,
          localidad,
          provincia,
          telefono: cliente.telefono,
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar cliente:', error);
        this.errorMessage = 'Error al cargar tus datos. Inténtalo de nuevo.';
        this.loading = false;
      },
    });
  }

  onSubmit() {
    if (this.form.invalid || !this.cliente) return;

    this.saving = true;
    this.successMessage = '';
    this.errorMessage = '';
    
    const formData = this.form.getRawValue();
    
    // Concatenar dirección
    const direccionCompleta = `${formData.calle}, ${formData.cp}, ${formData.localidad}, ${formData.provincia}`;

    const request: ClienteRequest = {
      idUsuario: this.currentUser!.idUsuario,
      nombre: formData.nombre,
      direccion: direccionCompleta,
      telefono: formData.telefono,
    };

    this.clienteService
      .updateCliente(this.currentUser!.idUsuario, request)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: (updatedCliente) => {
          this.cliente = updatedCliente;
          this.successMessage = '✅ Perfil actualizado correctamente';
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: (error) => {
          console.error('Error updating profile:', error);
          this.errorMessage = '❌ Error al actualizar el perfil. Inténtalo de nuevo.';
          setTimeout(() => this.errorMessage = '', 5000);
        },
      });
  }

  goBack() {
    this.router.navigateByUrl('/cliente/dashboard');
  }

  get nombreControl() {
    return this.form.get('nombre');
  }

  get direccionControl() {
    return this.form.get('direccion');
  }

  get telefonoControl() {
    return this.form.get('telefono');
  }
}
