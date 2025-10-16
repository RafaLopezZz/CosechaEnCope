import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CategoriaResponse,
  CategoriaRequest,
} from '../../shared/models/categoria.models';
import { API_ENDPOINTS } from '../config';

@Injectable({
  providedIn: 'root',
})
export class CategoriasService {
  private http = inject(HttpClient);

  getCategorias(): Observable<CategoriaResponse[]> {
    return this.http.get<CategoriaResponse[]>(API_ENDPOINTS.CATEGORIAS.GET_ALL);
  }

  obtenerCategoriaPorId(idCategoria: number): Observable<CategoriaResponse> {
    return this.http.get<CategoriaResponse>(
      API_ENDPOINTS.CATEGORIAS.GET_BY_ID(idCategoria)
    );
  }

  crearCategoria(categoria: CategoriaRequest): Observable<CategoriaResponse> {
    return this.http.post<CategoriaResponse>(
      API_ENDPOINTS.CATEGORIAS.CREATE,
      categoria
    );
  }

  actualizarCategoria(
    idCategoria: number,
    categoria: CategoriaRequest
  ): Observable<CategoriaResponse> {
    return this.http.put<CategoriaResponse>(
      API_ENDPOINTS.CATEGORIAS.UPDATE(idCategoria),
      categoria
    );
  }

  eliminarCategoria(idCategoria: number): Observable<string> {
    return this.http.delete<string>(
      API_ENDPOINTS.CATEGORIAS.DELETE(idCategoria)
    );
  }
}
