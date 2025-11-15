import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, of, forkJoin } from 'rxjs';
import { tap, catchError, map, switchMap } from 'rxjs/operators';
import { API_ENDPOINTS } from '../config/global';
import {
  CarritoResponse,
  AddToCarritoRequest,
  CarritoInvitadoItem,
} from '../../shared/models/carrito.models';

/**
 * Servicio mejorado para gestión del carrito de compras
 * 
 * MEJORAS IMPLEMENTADAS:
 * ✅ Soporte para carrito invitado en localStorage
 * ✅ Fusión automática del carrito invitado al iniciar sesión
 * ✅ Contador de items unificado (autenticado + invitado)
 * ✅ Estado reactivo con RxJS BehaviorSubject
 * 
 * @author rafalopezzz
 */
@Injectable({
  providedIn: 'root',
})
export class CarritoService {
  private http = inject(HttpClient);

  // =============================
  // ESTADO REACTIVO DEL CARRITO
  // =============================

  /**
   * BehaviorSubject para el estado del carrito autenticado.
   * Emite el carrito completo o null cuando está vacío.
   */
  public carritoSubject = new BehaviorSubject<CarritoResponse | null>(null);
  public carrito$ = this.carritoSubject.asObservable();

  /**
   * Clave para almacenar carrito invitado en localStorage
   */
  private readonly GUEST_CART_KEY = 'carritoInvitado';

  constructor() {
    this.inicializarCarrito();
  }

  // =============================
  // INICIALIZACIÓN
  // =============================

  /**
   * Inicializa el carrito: carga desde backend si hay token,
   * o mantiene el estado del localStorage para invitados
   */
  private inicializarCarrito(): void {
    const token = sessionStorage.getItem('authToken');
    if (token) {
      // Usuario autenticado: cargar carrito desde backend
      this.cargarCarrito();
    } else {
      // Usuario invitado: actualizar contador desde localStorage
      this.actualizarContadorInvitado();
    }
  }

  // =============================
  // OPERACIONES CON EL CARRITO (AUTENTICADO)
  // =============================

  /**
   * Agrega un artículo al carrito o incrementa su cantidad si ya existe.
   * Si el usuario NO está autenticado, guarda en localStorage.
   * Si el usuario está autenticado, envía al backend.
   */
  agregarItem(idArticulo: number, cantidad: number): Observable<CarritoResponse | null> {
    const token = sessionStorage.getItem('authToken');

    if (!token) {
      // Usuario invitado: guardar en localStorage
      this.agregarItemInvitado(idArticulo, cantidad);
      return of(null); // Retorna observable vacío
    }

    // Usuario autenticado: enviar al backend
    const request: AddToCarritoRequest = { idArticulo, cantidad };
    return this.http.post<CarritoResponse>(API_ENDPOINTS.CARRITO.ADD_ITEM, request).pipe(
      tap((carrito) => this.carritoSubject.next(carrito)),
      catchError(this.handleError)
    );
  }

  /**
   * Obtiene el carrito actual del usuario autenticado
   */
  obtenerCarrito(): Observable<CarritoResponse> {
    return this.http.get<CarritoResponse>(API_ENDPOINTS.CARRITO.GET_CART).pipe(
      tap((carrito) => this.carritoSubject.next(carrito)),
      catchError(this.handleError)
    );
  }

  /**
   * Decrementa la cantidad de un artículo (o lo elimina si cantidad = 1)
   */
  decrementarItem(idArticulo: number): Observable<CarritoResponse> {
    // Construir la URL con el idArticulo en la ruta
    const url = `${API_ENDPOINTS.CARRITO.BASE}/decrementar/${idArticulo}`;
    
    return this.http.post<CarritoResponse>(url, {}).pipe(
      tap((carrito) => this.carritoSubject.next(carrito)),
      catchError(this.handleError)
    );
  }

  /**
   * Vacía completamente el carrito
   */
  vaciarCarrito(): Observable<void> {
    return this.http.delete<void>(API_ENDPOINTS.CARRITO.CLEAR_CART).pipe(
      tap(() => this.carritoSubject.next(null)),
      catchError(this.handleError)
    );
  }

  /**
   * Método auxiliar para cargar el carrito al inicio
   */
  private cargarCarrito(): void {
    this.obtenerCarrito().subscribe({
      next: () => {}, // Ya se actualiza en tap()
      error: (err) => console.error('Error al cargar carrito:', err),
    });
  }

  // =============================
  // CARRITO INVITADO (LOCALSTORAGE)
  // =============================

  /**
   * Agrega un item al carrito invitado en localStorage
   */
  private agregarItemInvitado(idArticulo: number, cantidad: number): void {
    const items = this.obtenerCarritoInvitado();
    const itemExistente = items.find((item) => item.idArticulo === idArticulo);

    if (itemExistente) {
      itemExistente.cantidad += cantidad;
    } else {
      items.push({ idArticulo, cantidad });
    }

    this.guardarCarritoInvitado(items);
    this.actualizarContadorInvitado();
  }

  /**
   * Decrementa un item del carrito invitado
   */
  decrementarItemInvitado(idArticulo: number): void {
    const items = this.obtenerCarritoInvitado();
    const itemExistente = items.find((item) => item.idArticulo === idArticulo);

    if (itemExistente) {
      itemExistente.cantidad--;
      if (itemExistente.cantidad <= 0) {
        // Eliminar item si llega a 0
        const index = items.indexOf(itemExistente);
        items.splice(index, 1);
      }
    }

    this.guardarCarritoInvitado(items);
    this.actualizarContadorInvitado();
  }

  /**
   * Obtiene los items del carrito invitado desde localStorage
   */
  obtenerCarritoInvitado(): CarritoInvitadoItem[] {
    const data = localStorage.getItem(this.GUEST_CART_KEY);
    return data ? JSON.parse(data) : [];
  }

  /**
   * Guarda el carrito invitado en localStorage
   */
  private guardarCarritoInvitado(items: CarritoInvitadoItem[]): void {
    localStorage.setItem(this.GUEST_CART_KEY, JSON.stringify(items));
  }

  /**
   * Vacía el carrito invitado
   */
  vaciarCarritoInvitado(): void {
    localStorage.removeItem(this.GUEST_CART_KEY);
    this.actualizarContadorInvitado();
  }

  /**
   * Actualiza el contador del carrito invitado (para el badge)
   */
  private actualizarContadorInvitado(): void {
    const items = this.obtenerCarritoInvitado();
    const totalItems = items.reduce((sum, item) => sum + item.cantidad, 0);

    // Emitir un carrito mock con solo el contador
    // Esto permite que el badge funcione sin estar autenticado
    this.carritoSubject.next({
      id: 0,
      items: [],
      subtotal: 0,
      impuestos: 0,
      gastosEnvio: 0,
      total: 0,
    });
  }

  /**
   * Obtiene el número total de items en el carrito (autenticado o invitado)
   */
  getTotalItems(): number {
    const token = sessionStorage.getItem('authToken');
    if (token) {
      // Usuario autenticado: contar desde carritoSubject
      const carrito = this.carritoSubject.getValue();
      return carrito?.items.reduce((sum, item) => sum + item.cantidad, 0) || 0;
    } else {
      // Usuario invitado: contar desde localStorage
      const items = this.obtenerCarritoInvitado();
      return items.reduce((sum, item) => sum + item.cantidad, 0);
    }
  }

  // =============================
  // FUSIÓN DE CARRITO INVITADO
  // =============================

  /**
   * Fusiona el carrito invitado con el carrito autenticado tras login.
   * 
   * IMPORTANTE: Llamar este método después de un login exitoso desde AuthService.
   * 
   * Proceso:
   * 1. Obtiene items del localStorage
   * 2. Envía cada item al backend
   * 3. Limpia el localStorage
   * 4. Recarga el carrito actualizado
   * 
   * @returns Observable con el carrito fusionado
   */
  fusionarCarritoInvitado(): Observable<CarritoResponse | null> {
    const items = this.obtenerCarritoInvitado();

    if (items.length === 0) {
      // No hay nada que fusionar, solo cargar el carrito del usuario
      return this.obtenerCarrito();
    }

    console.log(`[CarritoService] Fusionando ${items.length} items del carrito invitado...`);

    // Enviar todos los items al backend secuencialmente
    const requests = items.map((item) =>
      this.http.post<CarritoResponse>(API_ENDPOINTS.CARRITO.ADD_ITEM, {
        idArticulo: item.idArticulo,
        cantidad: item.cantidad,
      } as AddToCarritoRequest)
    );

    // Ejecutar todas las peticiones en paralelo
    return forkJoin(requests).pipe(
      switchMap(() => {
        // Limpiar localStorage después de fusionar
        localStorage.removeItem(this.GUEST_CART_KEY);
        console.log('[CarritoService] Carrito invitado fusionado y limpiado');

        // Devolver el carrito actualizado
        return this.obtenerCarrito();
      }),
      catchError((error) => {
        console.error('[CarritoService] Error al fusionar carrito:', error);
        // Aunque falle, limpiar el localStorage para evitar inconsistencias
        localStorage.removeItem(this.GUEST_CART_KEY);
        return this.obtenerCarrito();
      })
    );
  }

  // =============================
  // MANEJO DE ERRORES
  // =============================

  /**
   * Manejo centralizado de errores
   */
  private handleError(error: any): Observable<never> {
    console.error('[CarritoService] Error:', error);
    throw error;
  }
}
