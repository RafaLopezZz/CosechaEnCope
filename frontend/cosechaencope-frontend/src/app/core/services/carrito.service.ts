import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_ENDPOINTS } from '../config';

export interface AddToCarritoRequest {
  idArticulo: number;
  cantidad: number;
}

export interface CarritoItemResponse {
  idArticulo: number;
  nombreArticulo: string;
  precio: number;
  cantidad: number;
  subtotal: number;
  imagenUrl?: string;
}

export interface CarritoResponse {
  items: CarritoItemResponse[];
  totalItems: number;
  totalAmount: number;
}

@Injectable({
  providedIn: 'root'
})
export class CarritoService {
  private http = inject(HttpClient);

  // Agregar artículo al carrito
  agregarAlCarrito(request: AddToCarritoRequest): Observable<CarritoResponse> {
    return this.http.post<CarritoResponse>(API_ENDPOINTS.CARRITO.ADD_ITEM, request);
  }

  // Obtener contenido del carrito
  getCarrito(): Observable<CarritoResponse> {
    return this.http.get<CarritoResponse>(API_ENDPOINTS.CARRITO.GET_CART);
  }

  // Decrementar cantidad de un artículo
  decrementarArticulo(idArticulo: number): Observable<CarritoResponse> {
    return this.http.post<CarritoResponse>(API_ENDPOINTS.CARRITO.DECREMENT_ITEM(idArticulo), {});
  }

  // Vaciar carrito
  vaciarCarrito(): Observable<void> {
    return this.http.delete<void>(API_ENDPOINTS.CARRITO.CLEAR_CART);
  }
}
