import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_ENDPOINTS, PAYMENT_METHODS } from '../config';

export interface PedidoItemResponse {
  idArticulo: number;
  nombreArticulo: string;
  precio: number;
  cantidad: number;
  subtotal: number;
}

export interface PedidoResponse {
  idPedido: number;
  numeroPedido: string;
  fechaCreacion: string;
  estado: string;
  metodoPago: string;
  items: PedidoItemResponse[];
  totalItems: number;
  totalAmount: number;
  cliente: {
    id: number;
    nombre: string;
    email: string;
  };
}

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private http = inject(HttpClient);

  /**
   * Crear un nuevo pedido desde el carrito activo
   */
  crearPedido(metodoPago: string = PAYMENT_METHODS.TARJETA): Observable<PedidoResponse> {
    return this.http.post<PedidoResponse>(
      `${API_ENDPOINTS.PEDIDOS.CREATE}?metodoPago=${metodoPago}`, 
      {}
    );
  }

  /**
   * Obtener historial de pedidos del usuario autenticado
   */
  getPedidosUsuario(): Observable<PedidoResponse[]> {
    return this.http.get<PedidoResponse[]>(API_ENDPOINTS.PEDIDOS.GET_BY_USER);
  }

  /**
   * Obtener un pedido específico por ID
   */
  getPedidoPorId(idPedido: number): Observable<PedidoResponse> {
    return this.http.get<PedidoResponse>(`${API_ENDPOINTS.PEDIDOS.BASE}/${idPedido}`);
  }
}