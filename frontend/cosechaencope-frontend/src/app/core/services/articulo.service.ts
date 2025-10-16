import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ArticuloRequest,
  ArticuloResponse,
} from '../../shared/models/articulo.models';
import { API_ENDPOINTS } from '../config';

@Injectable({ providedIn: 'root' })
export class ArticuloService {
  private http = inject(HttpClient);

  getArticulos(): Observable<ArticuloResponse[]> {
    return this.http.get<ArticuloResponse[]>(API_ENDPOINTS.ARTICULOS.GET_ALL);
  }

  getArticulosPorCategoria(
    idCategoria: number
  ): Observable<ArticuloResponse[]> {
    return this.http.get<ArticuloResponse[]>(
      API_ENDPOINTS.CATEGORIAS.GET_ARTICULOS_BY_CATEGORIA(idCategoria)
    );
  }

  getArticuloPorId(idArticulo: number): Observable<ArticuloResponse> {
    return this.http.get<ArticuloResponse>(
      API_ENDPOINTS.ARTICULOS.GET_BY_ID(idArticulo)
    );
  }

  crearArticulo(articulo: ArticuloRequest): Observable<ArticuloResponse> {
    return this.http.post<ArticuloResponse>(
      API_ENDPOINTS.ARTICULOS.CREATE,
      articulo
    );
  }

  actualizarArticulo(
    idArticulo: number,
    articulo: ArticuloRequest
  ): Observable<ArticuloResponse> {
    return this.http.put<ArticuloResponse>(
      API_ENDPOINTS.ARTICULOS.UPDATE(idArticulo),
      articulo
    );
  }

  eliminarArticulo(idArticulo: number): Observable<string> {
    return this.http.delete<string>(API_ENDPOINTS.ARTICULOS.DELETE(idArticulo));
  }
}
