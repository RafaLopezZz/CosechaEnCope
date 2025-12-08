import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { API_ENDPOINTS } from '../config';
import { OrdenVentaProductor, OvpState, ActualizarEstadoOvpRequest, EstadoOrdenVenta } from '../../shared/models/ovp.models';

@Injectable({ providedIn: 'root' })
export class OrdenesVentaService {
  private http = inject(HttpClient);

  // Estado inicial
  private initialState: OvpState = {
    ordenes: [],
    loading: false,
    error: null,
    filtrosActivos: {}
  };

  // Store
  private ovpStateSubject = new BehaviorSubject<OvpState>(this.initialState);
  
  // Selectores Públicos
  public state$ = this.ovpStateSubject.asObservable();
  public ordenes$ = this.state$.pipe(map(s => s.ordenes));
  public loading$ = this.state$.pipe(map(s => s.loading));

  /**
   * Cargar listado de órdenes por Productor
   */
  cargarOrdenes(idProductor: number): void {
    this.updateState({ loading: true, error: null });

    this.http.get<OrdenVentaProductor[]>(
      API_ENDPOINTS.ORDENES_VENTA_PRODUCTOR.GET_BY_PRODUCTOR(idProductor)
    ).pipe(
      map(ordenes => this.mapearOrdenes(ordenes)),
      tap(ordenes => {
        // Más recientes primero
        const ordenadas = ordenes.sort((a, b) => 
          new Date(b.fechaCreacion).getTime() - new Date(a.fechaCreacion).getTime()
        );
        
        this.updateState({
          loading: false,
          ordenes: ordenadas
        });
      }),
      catchError(error => {
        this.handleError(error);
        return throwError(() => error);
      })
    ).subscribe();
  }

  /**
   * Obtener una orden específica del Store (sin llamar al back si ya existe)
   */
  getOrdenPorId(idOvp: number): Observable<OrdenVentaProductor | undefined> {
    return this.ordenes$.pipe(
      map(ordenes => ordenes.find(o => o.idOvp === idOvp))
    );
  }

  /**
   * Cambiar estado de la orden (Optimistic Update)
   * 
   * @param idProductor ID del productor (para la URL)
   * @param idOvp ID de la orden
   * @param nuevoEstado Enum EstadoOrdenVenta
   * @param observaciones Texto opcional
   */
  actualizarEstado(
    idProductor: number, 
    idOvp: number, 
    nuevoEstado: EstadoOrdenVenta, 
    observaciones: string = ''
  ): Observable<OrdenVentaProductor> {
    
    // Snapshot del estado actual (para poder revertir si falla)
    const previousState = this.ovpStateSubject.getValue();
    const previousOrden = previousState.ordenes.find(o => o.idOvp === idOvp);

    if (!previousOrden) return throwError(() => new Error('Orden no encontrada en memoria'));

    // Actualización Optimista: Se actualiza la UI inmediatamente
    const ordenesOptimistas = previousState.ordenes.map(orden => {
      if (orden.idOvp === idOvp) {
        return { 
          ...orden, 
          estado: nuevoEstado, 
          observaciones: observaciones || orden.observaciones, // Se actualizan obs si hay
          isUpdating: true // Bandera para mostrar spinner pequeño en la tarjeta
        };
      }
      return orden;
    });

    this.updateState({ ordenes: ordenesOptimistas });

    // Preparar el DTO para el Backend
    const payload: ActualizarEstadoOvpRequest = {
      estado: nuevoEstado, // Se envía como string "EN_PROCESO", etc.
      observaciones: observaciones
    };

    // 4. Llamada HTTP
    return this.http.patch<OrdenVentaProductor>(
      `${API_ENDPOINTS.ORDENES_VENTA_PRODUCTOR.GET_BY_PRODUCTOR(idProductor)}/ordenes/${idOvp}/estado`,
      payload
    ).pipe(
      tap((ordenActualizadaBack) => {
        // Éxito: Se reemplaza con la respuesta real del servidor (confirmación)
        const currentState = this.ovpStateSubject.getValue();
        const ordenesFinales = currentState.ordenes.map(o => 
          o.idOvp === idOvp ? { ...ordenActualizadaBack, isUpdating: false } : o
        );
        this.updateState({ ordenes: ordenesFinales });
        console.log(`[OVP] Estado actualizado a ${nuevoEstado}`);
      }),
      catchError(error => {
        // Error: Se revierte al estado anterior (Rollback)
        console.error('[OVP] Fallo al actualizar, revirtiendo...', error);
        this.updateState({ 
          ordenes: previousState.ordenes, // Volvemos a poner las órdenes como estaban
          error: 'No se pudo actualizar el estado. Intente nuevamente.' 
        });
        return throwError(() => error);
      })
    );
  }

  /**
   * Mapea las órdenes del backend al modelo del frontend
   * El backend puede usar "lineas" en lugar de "detalles"
   */
  private mapearOrdenes(ordenes: any[]): OrdenVentaProductor[] {
    return ordenes.map(orden => this.mapearOrden(orden));
  }

  /**
   * Mapea una orden individual, normalizando el campo lineas/detalles
   */
  private mapearOrden(orden: any): OrdenVentaProductor {
    return {
      ...orden,
      // El backend usa "lineas", normalizamos a "detalles" para el frontend
      detalles: orden.detalles || orden.lineas || []
    };
  }

  private updateState(newState: Partial<OvpState>): void {
    this.ovpStateSubject.next({
      ...this.ovpStateSubject.getValue(),
      ...newState
    });
  }

  private handleError(error: HttpErrorResponse): void {
    const msg = error.error?.message || 'Error de conexión';
    this.updateState({ loading: false, error: msg });
  }
}