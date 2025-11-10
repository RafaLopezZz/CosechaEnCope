import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_ENDPOINTS } from '../config';

@Injectable({ providedIn: 'root' })
export class OrdenesVentaService {
  private http = inject(HttpClient);

  /**
   * Listar mis órdenes de venta
   * @param idProductor ID del productor
   */
  listarMisOrdenes(idProductor: number): Observable<any[]> {
    return this.http.get<any[]>(
      API_ENDPOINTS.ORDENES_VENTA_PRODUCTOR.GET_BY_PRODUCTOR(idProductor)
    );
  }

  /**
   * Obtener orden por ID
   * @param idProductor ID del productor
   * @param idOrden ID de la orden
   */
  obtenerOrden(idProductor: number, idOrden: number): Observable<any> {
    return this.http.get(
      `${API_ENDPOINTS.ORDENES_VENTA_PRODUCTOR.GET_BY_PRODUCTOR(idProductor)}/${idOrden}`
    );
  }

  /**
   * Cambiar estado de orden
   * @param idProductor ID del productor
   * @param idOrden ID de la orden
   * @param nuevoEstado Nuevo estado
   */
  cambiarEstado(idProductor: number, idOrden: number, nuevoEstado: string): Observable<any> {
    return this.http.patch(
      `${API_ENDPOINTS.ORDENES_VENTA_PRODUCTOR.GET_BY_PRODUCTOR(idProductor)}/${idOrden}/estado`,
      { estado: nuevoEstado }
    );
  }
}
