import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_ENDPOINTS } from '../config';
import { UsuarioResponse } from '../../shared/models/usuario.models';

/**
 * DTO para actualizar perfil de cliente (debe coincidir con backend)
 */
export interface ClienteRequest {
  idUsuario?: number;
  nombre?: string;
  telefono?: string;
  direccion?: string;
}

/**
 * Respuesta del backend con datos del cliente
 */
export interface ClienteResponse {
  idCliente: number;
  nombre: string;
  telefono: string;
  direccion: string;
  fechaRegistro: string;
  usuario: UsuarioResponse;
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
   * Actualizar datos del cliente (dirección y teléfono)
   * Este método se usa antes de crear un pedido para asegurar
   * que el cliente tiene los datos requeridos
   */
  updateCliente(idUsuario: number, cliente: ClienteRequest): Observable<ClienteResponse> {
    console.log('[ClienteService] 📝 Actualizando datos del cliente:', { idUsuario, cliente });
    return this.http.put<ClienteResponse>(
      API_ENDPOINTS.CLIENTES.UPDATE_BY_USER_ID(idUsuario), 
      cliente
    );
  }
}