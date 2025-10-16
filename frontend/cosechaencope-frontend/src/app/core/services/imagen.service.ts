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

    return this.http.post<ImageUploadResponse>(
      API_ENDPOINTS.IMAGENES.UPLOAD_ARTICULO,
      formData
    );
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

    return this.http.post<ImageUploadResponse>(
      API_ENDPOINTS.IMAGENES.UPLOAD_CATEGORIA,
      formData
    );
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

  /**
   * Valida que el archivo sea una imagen válida.
   *
   * @param file Archivo a validar
   * @returns Array de errores (vacío si es válido)
   */
  validarImagen(file: File): string[] {
    const errores: string[] = [];

    // Validar que existe un archivo
    if (!file) {
      errores.push('Debe seleccionar un archivo');
      return errores;
    }

    // Validar tipo de archivo
    const tiposPermitidos = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    if (!tiposPermitidos.includes(file.type)) {
      errores.push('Solo se permiten archivos JPG, JPEG, PNG, GIF y WEBP');
    }

    // Validar tamaño (máximo 10MB)
    const maxSize = 10 * 1024 * 1024; // 10MB en bytes
    if (file.size > maxSize) {
      errores.push('El archivo no puede exceder 10MB');
    }

    // Validar que no esté vacío
    if (file.size === 0) {
      errores.push('El archivo está vacío');
    }

    return errores;
  }

  /**
   * Convierte el tamaño del archivo a formato legible.
   *
   * @param bytes Tamaño en bytes
   * @returns Tamaño formateado (ej: "1.5 MB")
   */
  formatearTamanoArchivo(bytes: number): string {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  /**
   * Verifica si una URL es una imagen válida.
   *
   * @param url URL a verificar
   * @returns Promise que resuelve true si es una imagen válida
   */
  verificarImagenValida(url: string): Promise<boolean> {
    return new Promise((resolve) => {
      const img = new Image();
      img.onload = () => resolve(true);
      img.onerror = () => resolve(false);
      img.src = url;
    });
  }
}