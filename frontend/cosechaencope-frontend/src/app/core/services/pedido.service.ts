import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError, map, switchMap } from 'rxjs/operators';
import { API_ENDPOINTS, PAYMENT_METHODS } from '../config';
import {
  PedidoResponse,
  CheckoutRequest,
  CheckoutState,
  CheckoutStep,
  MetodoPago,
  CheckoutClienteData
} from '../../shared/models/pedido.models';
import { ClienteService } from './cliente.service';
import { UserStoreService } from './user-store.service';

/**
 * Servicio profesional para gestión de pedidos y proceso de checkout
 * 
 * CARACTERÍSTICAS:
 * ✅ Gestión completa del proceso de checkout
 * ✅ Validaciones de datos del cliente
 * ✅ Estados reactivos con BehaviorSubject
 * ✅ Manejo robusto de errores
 * ✅ Feedback inmediato de operaciones
 * 
 * FLUJO DE CHECKOUT:
 * 1. RESUMEN: Revisar items del carrito
 * 2. DATOS: Confirmar/editar datos del cliente
 * 3. PAGO: Seleccionar método de pago
 * 4. CONFIRMACION: Pedido creado exitosamente
 * 
 * @author rafalopezzz
 */
@Injectable({ providedIn: 'root' })
export class PedidoService {
  private http = inject(HttpClient);
  private clienteService = inject(ClienteService);
  private userStore = inject(UserStoreService);

  // =============================
  // ESTADO REACTIVO DEL CHECKOUT
  // =============================

  /**
   * Estado actual del proceso de checkout
   */
  private checkoutStateSubject = new BehaviorSubject<CheckoutState>({
    step: CheckoutStep.RESUMEN,
    clienteData: null,
    metodoPago: null,
    isProcessing: false,
    error: null
  });

  public checkoutState$ = this.checkoutStateSubject.asObservable();

  /**
   * Último pedido creado (para mostrar en confirmación)
   */
  private ultimoPedidoSubject = new BehaviorSubject<PedidoResponse | null>(null);
  public ultimoPedido$ = this.ultimoPedidoSubject.asObservable();

  // =============================
  // OPERACIONES DE PEDIDOS
  // =============================

  /**
   * Crear un nuevo pedido desde el carrito activo
   * 
   * IMPORTANTE: Antes de crear el pedido, actualiza el perfil del cliente
   * con dirección y teléfono desde los datos del checkout.
   * 
   * El backend requiere que el cliente tenga dirección y teléfono configurados
   * para poder crear un pedido. Si faltan estos datos, la validación falla.
   * 
   * @param metodoPago Método de pago seleccionado (TARJETA, TRANSFERENCIA, etc.)
   * @returns Observable con los datos del pedido creado
   * 
   * @example
   * ```typescript
   * pedidoService.crearPedido(MetodoPago.TARJETA).subscribe({
   *   next: (pedido) => console.log('Pedido creado:', pedido.idTransaccion),
   *   error: (err) => console.error('Error:', err)
   * });
   * ```
   */
  crearPedido(metodoPago: string = PAYMENT_METHODS.TARJETA): Observable<PedidoResponse> {
    console.log('[PedidoService] 🛒 Iniciando creación de pedido con método de pago:', metodoPago);
    
    this.updateCheckoutState({ 
      isProcessing: true, 
      error: null 
    });

    // Obtener datos del cliente del estado del checkout
    const checkoutState = this.getCheckoutState();
    const clienteData = checkoutState.clienteData;
    const currentUser = this.userStore.snapshot();

    // Si hay datos del cliente y un usuario autenticado, actualizar el perfil primero
    if (clienteData && currentUser?.idUsuario) {
      console.log('[PedidoService] 📝 Actualizando perfil del cliente antes de crear pedido');
      
      return this.clienteService.updateCliente(currentUser.idUsuario, {
        nombre: clienteData.nombre,
        direccion: clienteData.direccion,
        telefono: clienteData.telefono
      }).pipe(
        tap(() => console.log('[PedidoService] ✅ Perfil actualizado, procediendo a crear pedido')),
        switchMap(() => this.http.post<PedidoResponse>(
          `${API_ENDPOINTS.PEDIDOS.CREATE}?metodoPago=${metodoPago}`, 
          {}
        )),
        tap((pedido) => {
          this.ultimoPedidoSubject.next(pedido);
          this.updateCheckoutState({
            step: CheckoutStep.CONFIRMACION,
            isProcessing: false
          });
          console.log('[PedidoService] ✅ Pedido creado exitosamente:', pedido.idTransaccion);
        }),
        catchError((error) => {
          this.updateCheckoutState({
            isProcessing: false,
            error: this.extractErrorMessage(error)
          });
          console.error('[PedidoService] ❌ Error al crear pedido:', error);
          return throwError(() => error);
        })
      );
    }

    // Si no hay datos del cliente, proceder directamente (caso legacy)
    console.warn('[PedidoService] ⚠️ No hay datos del cliente en checkout, creando pedido sin actualizar perfil');
    
    return this.http.post<PedidoResponse>(
      `${API_ENDPOINTS.PEDIDOS.CREATE}?metodoPago=${metodoPago}`, 
      {}
    ).pipe(
      tap((pedido) => {
        this.ultimoPedidoSubject.next(pedido);
        this.updateCheckoutState({
          step: CheckoutStep.CONFIRMACION,
          isProcessing: false
        });
        console.log('[PedidoService] ✅ Pedido creado:', pedido.idTransaccion);
      }),
      catchError((error) => {
        this.updateCheckoutState({
          isProcessing: false,
          error: this.extractErrorMessage(error)
        });
        console.error('[PedidoService] ❌ Error al crear pedido:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Obtener historial de pedidos del usuario autenticado
   * 
   * @returns Observable con lista de pedidos ordenados por fecha (más reciente primero)
   */
  getPedidosUsuario(): Observable<PedidoResponse[]> {
    return this.http.get<PedidoResponse[]>(API_ENDPOINTS.PEDIDOS.GET_BY_USER).pipe(
      map((pedidos) => {
        // Ordenar por fecha descendente (más reciente primero)
        return pedidos.sort((a, b) => 
          new Date(b.fechaPedido).getTime() - new Date(a.fechaPedido).getTime()
        );
      }),
      catchError((error) => {
        console.error('[PedidoService] Error al obtener pedidos:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Obtener un pedido específico por ID
   * 
   * @param idPedido ID del pedido a consultar
   * @returns Observable con los datos completos del pedido
   */
  getPedidoPorId(idPedido: number): Observable<PedidoResponse> {
    return this.http.get<PedidoResponse>(`${API_ENDPOINTS.PEDIDOS.BASE}/pedidos/${idPedido}`).pipe(
      catchError((error) => {
        console.error(`[PedidoService] Error al obtener pedido ${idPedido}:`, error);
        return throwError(() => error);
      })
    );
  }

  // =============================
  // GESTIÓN DEL CHECKOUT
  // =============================

  /**
   * Iniciar el proceso de checkout
   * Resetea el estado y establece el paso inicial
   */
  iniciarCheckout(): void {
    this.checkoutStateSubject.next({
      step: CheckoutStep.RESUMEN,
      clienteData: null,
      metodoPago: null,
      isProcessing: false,
      error: null
    });
    console.log('[PedidoService] 🛒 Checkout iniciado');
  }

  /**
   * Avanzar al siguiente paso del checkout
   * 
   * @param step Paso al que avanzar
   */
  avanzarPaso(step: CheckoutStep): void {
    this.updateCheckoutState({ step, error: null });
    console.log('[PedidoService] ➡️ Avanzando a paso:', step);
  }

  /**
   * Retroceder al paso anterior del checkout
   */
  retrocederPaso(): void {
    const currentState = this.checkoutStateSubject.getValue();
    const steps = Object.values(CheckoutStep);
    const currentIndex = steps.indexOf(currentState.step);
    
    if (currentIndex > 0) {
      const previousStep = steps[currentIndex - 1];
      this.updateCheckoutState({ step: previousStep, error: null });
      console.log('[PedidoService] ⬅️ Retrocediendo a paso:', previousStep);
    }
  }

  /**
   * Guardar datos del cliente en el estado del checkout
   * 
   * @param clienteData Datos del cliente validados
   */
  guardarDatosCliente(clienteData: CheckoutClienteData): void {
    this.updateCheckoutState({ clienteData });
    console.log('[PedidoService] 💾 Datos del cliente guardados');
  }

  /**
   * Guardar método de pago seleccionado
   * 
   * @param metodoPago Método de pago elegido
   */
  guardarMetodoPago(metodoPago: MetodoPago): void {
    this.updateCheckoutState({ metodoPago });
    console.log('[PedidoService] 💳 Método de pago guardado:', metodoPago);
  }

  /**
   * Validar que los datos del cliente son correctos
   * 
   * @param data Datos del cliente a validar
   * @returns true si son válidos, false en caso contrario
   */
  validarDatosCliente(data: CheckoutClienteData): boolean {
    if (!data.nombre || data.nombre.trim().length < 3) {
      this.updateCheckoutState({ error: 'El nombre debe tener al menos 3 caracteres' });
      return false;
    }

    if (!data.email || !this.validarEmail(data.email)) {
      this.updateCheckoutState({ error: 'Email inválido' });
      return false;
    }

    if (!data.telefono || data.telefono.trim().length < 9) {
      this.updateCheckoutState({ error: 'Teléfono inválido' });
      return false;
    }

    if (!data.direccion || data.direccion.trim().length < 10) {
      this.updateCheckoutState({ error: 'La dirección debe ser más completa' });
      return false;
    }

    return true;
  }

  /**
   * Cancelar el proceso de checkout
   */
  cancelarCheckout(): void {
    this.checkoutStateSubject.next({
      step: CheckoutStep.RESUMEN,
      clienteData: null,
      metodoPago: null,
      isProcessing: false,
      error: null
    });
    this.ultimoPedidoSubject.next(null);
    console.log('[PedidoService] ❌ Checkout cancelado');
  }

  /**
   * Obtener el estado actual del checkout (snapshot)
   */
  getCheckoutState(): CheckoutState {
    return this.checkoutStateSubject.getValue();
  }

  // =============================
  // MÉTODOS AUXILIARES
  // =============================

  /**
   * Actualizar parcialmente el estado del checkout
   */
  private updateCheckoutState(partialState: Partial<CheckoutState>): void {
    const currentState = this.checkoutStateSubject.getValue();
    this.checkoutStateSubject.next({ ...currentState, ...partialState });
  }

  /**
   * Extraer mensaje de error legible del response HTTP
   */
  private extractErrorMessage(error: any): string {
    if (error.error?.message) {
      return error.error.message;
    }
    if (error.message) {
      return error.message;
    }
    return 'Error desconocido al procesar el pedido';
  }

  /**
   * Validar formato de email
   */
  private validarEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }
}