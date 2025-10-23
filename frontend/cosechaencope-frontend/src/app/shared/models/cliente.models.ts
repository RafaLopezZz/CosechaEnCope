import { UsuarioResponse } from './usuario.models';

export interface ClienteResponse {
  idCliente: number;
  nombre?: string;
  direccion: string;
  telefono: string;
  fechaRegistro: string;
  usuario: UsuarioResponse;
}

export interface ClienteRequest {
  idUsuario: number;
  nombre?: string;
  direccion: string;
  telefono: string;
  imagenUrl: string;
}