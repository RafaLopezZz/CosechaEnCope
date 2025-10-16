import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductorResponse, ProductorRequest } from '../../shared/models/productor.models';
import { ArticuloResponse, ArticuloRequest } from '../../shared/models/articulo.models';
import { CategoriaResponse, CategoriaRequest } from '../../shared/models/categoria.models';
import { API_ENDPOINTS } from '../config';

@Injectable({ providedIn: 'root' })
export class ProductorService {
  private http = inject(HttpClient);

  // Gestión del perfil del productor
  getProductorPorUsuario(idUsuario: number): Observable<ProductorResponse> {
    return this.http.get<ProductorResponse>(API_ENDPOINTS.PRODUCTORES.GET_BY_USER_ID(idUsuario));
  }

  updateProductor(idUsuario: number, productor: ProductorRequest): Observable<ProductorResponse> {
    return this.http.put<ProductorResponse>(API_ENDPOINTS.PRODUCTORES.UPDATE_BY_USER_ID(idUsuario), productor);
  }

  // Gestión de artículos del productor
  getArticulosProductor(idProductor: number): Observable<ArticuloResponse[]> {
    return this.http.get<ArticuloResponse[]>(API_ENDPOINTS.PRODUCTORES.GET_ARTICULOS_BY_PRODUCTOR(idProductor));
  }

  createArticulo(idProductor: number, articulo: ArticuloRequest): Observable<ArticuloResponse> {
    return this.http.post<ArticuloResponse>(API_ENDPOINTS.PRODUCTORES.CREATE_ARTICULO_BY_PRODUCTOR(idProductor), articulo);
  }

  updateArticulo(idProductor: number, idArticulo: number, articulo: ArticuloRequest): Observable<ArticuloResponse> {
    return this.http.put<ArticuloResponse>(API_ENDPOINTS.PRODUCTORES.UPDATE_ARTICULO_BY_PRODUCTOR(idProductor, idArticulo), articulo);
  }

  deleteArticulo(idProductor: number, idArticulo: number): Observable<void> {
    return this.http.delete<void>(API_ENDPOINTS.PRODUCTORES.DELETE_ARTICULO_BY_PRODUCTOR(idProductor, idArticulo));
  }

  // Gestión de categorías (si el productor tiene permisos)
  getCategorias(): Observable<CategoriaResponse[]> {
    return this.http.get<CategoriaResponse[]>(API_ENDPOINTS.CATEGORIAS.GET_ALL);
  }

  createCategoria(categoria: CategoriaRequest): Observable<CategoriaResponse> {
    return this.http.post<CategoriaResponse>(API_ENDPOINTS.CATEGORIAS.CREATE, categoria);
  }

  updateCategoria(id: number, categoria: CategoriaRequest): Observable<CategoriaResponse> {
    return this.http.put<CategoriaResponse>(API_ENDPOINTS.CATEGORIAS.UPDATE(id), categoria);
  }

  deleteCategoria(id: number): Observable<void> {
    return this.http.delete<void>(API_ENDPOINTS.CATEGORIAS.DELETE(id));
  }

  // Dashboard - Estadísticas
  getEstadisticasProductor(idProductor: number): Observable<any> {
    return this.http.get(`${API_ENDPOINTS.PRODUCTORES.BASE}/${idProductor}/estadisticas`);
  }

  getPedidosProductor(idProductor: number): Observable<any[]> {
    return this.http.get<any[]>(API_ENDPOINTS.ORDENES_VENTA_PRODUCTOR.GET_BY_PRODUCTOR(idProductor));
  }
}