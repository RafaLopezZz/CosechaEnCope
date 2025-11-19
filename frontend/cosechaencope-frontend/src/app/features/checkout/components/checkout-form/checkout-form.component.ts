import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { PedidoService } from '../../../../core/services/pedido.service';
import { UserStoreService } from '../../../../core/services/user-store.service';
import { ClienteService } from '../../../../core/services/cliente.service';
import { CheckoutStep, CheckoutClienteData } from '../../../../shared/models/pedido.models';
import { CheckoutAuthModalComponent } from '../checkout-auth-modal/checkout-auth-modal.component';

/**
 * Formulario de datos del cliente en checkout
 * 
 * CARACTERÍSTICAS:
 * ✅ Validación de formulario con feedback visual
 * ✅ Verificación de autenticación antes de avanzar
 * ✅ Modal de login/registro integrado para usuarios no autenticados
 * ✅ Validación de tipo de usuario (solo CLIENTE)
 * ✅ Pre-carga de datos guardados si existen
 * 
 * FLUJO:
 * 0. Si el usuario está autenticado como CLIENTE y tiene datos completos → Oculta formulario y avanza
 * 1. Usuario completa formulario de datos de envío
 * 2. Al enviar, se valida el formulario
 * 3. Se guardan los datos en el estado del checkout
 * 4. Se verifica si el usuario está autenticado:
 *    - Si NO está autenticado → muestra modal de login/registro
 *    - Si está autenticado como CLIENTE → avanza al paso de pago
 *    - Si no es CLIENTE → muestra error
 * 
 * @author rafalopezzz
 */
@Component({
  selector: 'app-checkout-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CheckoutAuthModalComponent],
  templateUrl: './checkout-form.component.html',
  styleUrl: './checkout-form.component.scss'
})
export class CheckoutFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private pedidoService = inject(PedidoService);
  private userStore = inject(UserStoreService);
  private clienteService = inject(ClienteService);

  clienteForm!: FormGroup;
  isSubmitting = false;
  showAuthModal = false;
  datosClienteCompletos = false;
  cargandoDatosCliente = true;

  ngOnInit(): void {
    // Inicializar formulario vacío - el usuario deberá completarlo
    this.clienteForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', [Validators.required, Validators.minLength(9)]],
      // Desglose de dirección
      calle: ['', [Validators.required]],
      cp: ['', [Validators.required, Validators.pattern(/^[0-9]{5}$/)]],
      localidad: ['', [Validators.required]],
      provincia: ['', [Validators.required]]
    });

    // Verificar si el usuario está autenticado y tiene datos completos
    this.verificarDatosClienteAutenticado();
  }

  provincias = [
    'Álava', 'Albacete', 'Alicante', 'Almería', 'Asturias', 'Ávila', 'Badajoz', 'Barcelona', 'Burgos', 'Cáceres',
    'Cádiz', 'Cantabria', 'Castellón', 'Ciudad Real', 'Córdoba', 'La Coruña', 'Cuenca', 'Gerona', 'Granada', 'Guadalajara',
    'Guipúzcoa', 'Huelva', 'Huesca', 'Islas Baleares', 'Jaén', 'León', 'Lérida', 'Lugo', 'Madrid', 'Málaga', 'Murcia',
    'Navarra', 'Ourense', 'Palencia', 'Las Palmas', 'Pontevedra', 'La Rioja', 'Salamanca', 'Segovia', 'Sevilla', 'Soria',
    'Tarragona', 'Santa Cruz de Tenerife', 'Teruel', 'Toledo', 'Valencia', 'Valladolid', 'Vizcaya', 'Zamora', 'Zaragoza',
    'Ceuta', 'Melilla'
  ];

  onSubmit(): void {
    if (this.clienteForm.invalid) {
      Object.keys(this.clienteForm.controls).forEach(key => {
        const control = this.clienteForm.get(key);
        if (control?.invalid) {
          control.markAsTouched();
        }
      });
      return;
    }

    const formData = this.clienteForm.value;
    
    // Concatenar dirección
    const direccionCompleta = `${formData.calle}, ${formData.cp}, ${formData.localidad}, ${formData.provincia}`;

    const clienteData: CheckoutClienteData = {
      nombre: formData.nombre,
      email: formData.email,
      telefono: formData.telefono,
      direccion: direccionCompleta
    };
    
    if (this.pedidoService.validarDatosCliente(clienteData)) {
      // Guardar datos del cliente
      this.pedidoService.guardarDatosCliente(clienteData);

      // Verificar autenticación antes de avanzar al pago
      this.verificarAutenticacionYAvanzar();
    }
  }

  /**
   * Verificar si el usuario está autenticado como CLIENTE y tiene datos completos
   * Si tiene datos completos, ocultar formulario y avanzar automáticamente
   */
  private verificarDatosClienteAutenticado(): void {
    const userData = this.userStore.snapshot();
    const token = sessionStorage.getItem('authToken');

    // Usuario no autenticado → mostrar formulario vacío
    if (!token || !userData) {
      console.log('[CheckoutForm] Usuario no autenticado, mostrando formulario');
      this.cargandoDatosCliente = false;
      return;
    }

    // Usuario no es CLIENTE → mostrar formulario para completar datos
    if (userData.tipoUsuario !== 'CLIENTE') {
      console.log('[CheckoutForm] Usuario no es CLIENTE, mostrando formulario');
      this.cargandoDatosCliente = false;
      return;
    }

    // Usuario es CLIENTE autenticado → cargar sus datos del backend
    console.log('[CheckoutForm] Cliente autenticado, verificando datos en perfil...');
    this.clienteService.getClientePorUsuario(userData.idUsuario).subscribe({
      next: (clienteData) => {
        console.log('[CheckoutForm] Datos del cliente:', clienteData);
        
        // Verificar si tiene todos los datos requeridos
        const tieneNombre = clienteData.nombre && clienteData.nombre.trim().length >= 3;
        const tieneTelefono = clienteData.telefono && clienteData.telefono.trim().length >= 9;
        const tieneDireccion = clienteData.direccion && clienteData.direccion.trim().length >= 10;
        
        if (tieneNombre && tieneTelefono && tieneDireccion) {
          console.log('[CheckoutForm] ✅ Cliente tiene datos completos, omitiendo formulario');
          this.datosClienteCompletos = true;
          
          // Guardar datos en el estado del checkout
          const clienteCheckoutData: CheckoutClienteData = {
            nombre: clienteData.nombre,
            email: clienteData.usuario.email,
            telefono: clienteData.telefono,
            direccion: clienteData.direccion
          };
          this.pedidoService.guardarDatosCliente(clienteCheckoutData);
          
          // Avanzar automáticamente al paso de pago
          this.pedidoService.avanzarPaso(CheckoutStep.PAGO);
        } else {
          console.log('[CheckoutForm] ⚠️ Cliente tiene datos incompletos, mostrando formulario');
          
          // Parsear dirección
          let calle = '', cp = '', localidad = '', provincia = '';
          if (clienteData.direccion) {
            const parts = clienteData.direccion.split(', ');
            if (parts.length === 4) {
              [calle, cp, localidad, provincia] = parts;
            } else {
              calle = clienteData.direccion;
            }
          }

          // Prellenar formulario con los datos existentes
          this.clienteForm.patchValue({
            nombre: clienteData.nombre || '',
            email: clienteData.usuario.email,
            telefono: clienteData.telefono || '',
            calle,
            cp,
            localidad,
            provincia
          });
        }
        
        this.cargandoDatosCliente = false;
      },
      error: (error) => {
        console.error('[CheckoutForm] Error al cargar datos del cliente:', error);
        // En caso de error, mostrar formulario con datos del checkout state si existen
        const checkoutState = this.pedidoService.getCheckoutState();
        if (checkoutState.clienteData) {
          // Parsear dirección del estado si existe
          let calle = '', cp = '', localidad = '', provincia = '';
          if (checkoutState.clienteData.direccion) {
            const parts = checkoutState.clienteData.direccion.split(', ');
            if (parts.length === 4) {
              [calle, cp, localidad, provincia] = parts;
            } else {
              calle = checkoutState.clienteData.direccion;
            }
          }

          this.clienteForm.patchValue({
            ...checkoutState.clienteData,
            calle,
            cp,
            localidad,
            provincia
          });
        }
        this.cargandoDatosCliente = false;
      }
    });
  }

  /**
   * Verificar si el usuario está autenticado
   * Si no lo está, mostrar modal de login/registro
   * Si lo está, avanzar al paso de pago
   */
  private verificarAutenticacionYAvanzar(): void {
    const token = sessionStorage.getItem('authToken');
    const userData = this.userStore.snapshot();

    // Usuario no autenticado
    if (!token || !userData) {
      console.log('[CheckoutForm] Usuario no autenticado, mostrando modal');
      this.showAuthModal = true;
      return;
    }

    // Verificar que sea CLIENTE
    if (userData.tipoUsuario !== 'CLIENTE') {
      console.error('[CheckoutForm] Usuario no es de tipo CLIENTE');
      alert('Solo los clientes pueden realizar pedidos. Por favor, inicia sesión con una cuenta de cliente.');
      return;
    }

    // Usuario autenticado correctamente, avanzar al pago
    console.log('[CheckoutForm] Usuario autenticado como CLIENTE, avanzando al pago');
    this.pedidoService.avanzarPaso(CheckoutStep.PAGO);
  }

  /**
   * Callback cuando la autenticación es exitosa
   */
  onAuthSuccess(): void {
    console.log('[CheckoutForm] Autenticación exitosa, avanzando al pago');
    this.showAuthModal = false;
    this.pedidoService.avanzarPaso(CheckoutStep.PAGO);
  }

  /**
   * Callback cuando se cierra el modal sin autenticarse
   */
  onAuthModalClose(): void {
    console.log('[CheckoutForm] Modal cerrado sin autenticación');
    this.showAuthModal = false;
  }

  getErrorMessage(fieldName: string): string {
    const control = this.clienteForm.get(fieldName);
    if (control?.hasError('required')) return 'Este campo es obligatorio';
    if (control?.hasError('email')) return 'Email inválido';
    if (control?.hasError('minlength')) {
      const minLength = control.errors?.['minlength'].requiredLength;
      return `Mínimo ${minLength} caracteres`;
    }
    return '';
  }
}
