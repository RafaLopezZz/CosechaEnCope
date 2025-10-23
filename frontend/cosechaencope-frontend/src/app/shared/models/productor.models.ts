import { UsuarioResponse } from './usuario.models';

export interface ProductorResponse {
  idProductor: number;
  usuario: UsuarioResponse;
  nombre: string;
  direccion: string;
  telefono: string;
  fechaRegistro: string;
  imagenUrl: string;
}

export interface ProductorRequest {
  idUsuario: number;
  nombre: string;
  direccion: string;
  telefono: string;
  imagenUrl: string;
}