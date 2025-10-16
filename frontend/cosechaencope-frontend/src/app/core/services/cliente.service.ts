import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_ENDPOINTS } from '../config';

export interface ClienteRequest {
  nombre: string;
  apellidos: string;
  telefono: string;
  direccion: string;
  ciudad: string;
  codigoPostal: string;
  fechaNacimiento?: string;
}

export interface ClienteResponse {
  idCliente: number;
  nombre: string;
  apellidos: string;
  telefono: string;
  direccion: string;
  ciudad: string;
  codigoPostal: string;
  fechaNacimiento?: string;
  fechaRegistro: string;
  usuario: {
    id: number;
    email: string;
    fechaCreacion: string;
  };
}

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private http = inject(HttpClient);

  /**
   * Obtener todos los clientes (solo para administradores)
   */
  getClientes(): Observable<ClienteResponse[]> {
    return this.http.get<ClienteResponse[]>(API_ENDPOINTS.CLIENTES.GET_ALL);
  }

  /**
   * Obtener cliente por ID de usuario
   */
  getClientePorUsuario(idUsuario: number): Observable<ClienteResponse> {
    return this.http.get<ClienteResponse>(API_ENDPOINTS.CLIENTES.GET_BY_USER_ID(idUsuario));
  }

  /**
   * Actualizar datos del cliente
   */
  updateCliente(idUsuario: number, cliente: ClienteRequest): Observable<ClienteResponse> {
    return this.http.put<ClienteResponse>(
      API_ENDPOINTS.CLIENTES.UPDATE_BY_USER_ID(idUsuario), 
      cliente
    );
  }
}