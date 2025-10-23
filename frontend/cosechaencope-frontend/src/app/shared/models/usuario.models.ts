export interface UsuarioRequest {
  email: string;
  password: string;
  nombre?: string;
  direccion?: string;
  telefono?: string;
}
export interface UsuarioResponse {
  idUsuario: number;
  email: string;
  tipoUsuario: string;
  rol: string;
}