export interface ArticuloResponse {
  idArticulo: number;
  nombre: string;
  precio: number;
  descripcion: string;
  stock: number;
  imagenUrl: string;
  idCategoria?: number;
  idProductor?: number;
  nombreCategoria: string;
  nombreProductor: string;
}

export interface ArticuloRequest {
  nombre: string;
  precio: number;
  descripcion: string;
  stock: number;
  imagenUrl: string;
  idCategoria: number;
  idProductor: number;
}