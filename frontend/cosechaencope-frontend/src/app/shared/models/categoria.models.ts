export interface CategoriaResponse {
  idCategoria: number;
  nombre: string;
  descripcion: string;
  imagenUrl: string;
}

export interface CategoriaRequest {
  nombre: string;
  descripcion: string;
  imagenUrl: string;
}