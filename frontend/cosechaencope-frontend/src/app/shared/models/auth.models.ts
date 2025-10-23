export interface LoginRequest {
  email: string;
  password: string;
}

export interface JwtResponse {
  token: string;
  type?: string;
  idUsuario: number;
  email: string;
  tipoUsuario: 'CLIENTE' | 'PRODUCTOR';
  roles: string[];
}

export interface ApiError {
  code?: number;
  message: string;
}
