import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_ENDPOINTS } from '../config/global';

/**
 * Interfaz para la respuesta de subida de imagen
 */
export interface ImageUploadResponse {
  imageUrl: string;
  message: string;
  fileName?: string;
  fileSize?: number;
  success: boolean;
}

/**
 * Servicio para la gestión de imágenes con AWS S3.
 *
 * Proporciona métodos para subir y eliminar imágenes que serán utilizadas
 * en artículos y categorías del sistema.
 *
 * @author rafalopezzz
 */
@Injectable({ providedIn: 'root' })
export class ImagenService {
  private http = inject(HttpClient);

  /**
   * Sube una imagen para un artículo.
   *
   * @param file Archivo de imagen a subir
   * @param idUsuario ID del usuario que sube la imagen
   * @param tipoUsuario Tipo de usuario (CLIENTE, PRODUCTOR)
   * @returns Observable con la respuesta de la subida
   */
  subirImagenArticulo(
    file: File,
    idUsuario: number,
    tipoUsuario: string
  ): Observable<ImageUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('idUsuario', idUsuario.toString());
    formData.append('tipoUsuario', tipoUsuario);

    return this.http.post<ImageUploadResponse>(API_ENDPOINTS.IMAGENES.UPLOAD_ARTICULO, formData);
  }

  /**
   * Sube una imagen para una categoría.
   *
   * @param file Archivo de imagen a subir
   * @param idUsuario ID del usuario que sube la imagen
   * @param tipoUsuario Tipo de usuario (CLIENTE, PRODUCTOR)
   * @returns Observable con la respuesta de la subida
   */
  subirImagenCategoria(
    file: File,
    idUsuario: number,
    tipoUsuario: string
  ): Observable<ImageUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('idUsuario', idUsuario.toString());
    formData.append('tipoUsuario', tipoUsuario);

    return this.http.post<ImageUploadResponse>(API_ENDPOINTS.IMAGENES.UPLOAD_CATEGORIA, formData);
  }

  /**
   * Sube una imagen para un perfil de usuario.
   *
   * @param file Archivo de imagen a subir
   * @param idUsuario ID del usuario que sube la imagen
   * @param tipoUsuario Tipo de usuario (CLIENTE, PRODUCTOR)
   * @returns Observable con la respuesta de la subida
   */
  subirImagenPerfil(
    file: File,
    idUsuario: number,
    tipoUsuario: string
  ): Observable<ImageUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('idUsuario', idUsuario.toString());
    formData.append('tipoUsuario', tipoUsuario);

    return this.http.post<ImageUploadResponse>(API_ENDPOINTS.IMAGENES.UPLOAD_PERFIL, formData);
  }

  /**
   * Elimina una imagen del bucket S3.
   *
   * @param imageUrl URL completa de la imagen a eliminar
   * @returns Observable con el mensaje de confirmación
   */
  eliminarImagen(imageUrl: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(
      `${API_ENDPOINTS.IMAGENES.DELETE}?imageUrl=${encodeURIComponent(imageUrl)}`
    );
  }
}
