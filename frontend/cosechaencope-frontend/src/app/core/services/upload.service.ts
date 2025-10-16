import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_ENDPOINTS } from '../config';

export interface UploadResponse {
  url: string;
  key: string;
}

export interface PresignedUrlRequest {
  fileName: string;
  fileType: string;
  folder?: string;
}

export interface PresignedUrlResponse {
  uploadUrl: string;
  fileUrl: string;
  key: string;
}

@Injectable({ providedIn: 'root' })
export class UploadService {
  private http = inject(HttpClient);

  /**
   * Obtiene una URL prefirmada de S3 para subir un archivo
   */
  getPresignedUrl(request: PresignedUrlRequest): Observable<PresignedUrlResponse> {
    return this.http.post<PresignedUrlResponse>(API_ENDPOINTS.UPLOAD.PRESIGNED_URL, request);
  }

  /**
   * Sube un archivo directamente a S3 usando la URL prefirmada
   */
  uploadToS3(file: File, uploadUrl: string): Observable<any> {
    return this.http.put(uploadUrl, file, {
      headers: {
        'Content-Type': file.type
      }
    });
  }

  /**
   * Proceso completo: obtiene URL prefirmada y sube el archivo
   */
  uploadFile(file: File, folder: string = 'general'): Observable<UploadResponse> {
    return new Observable(observer => {
      const request: PresignedUrlRequest = {
        fileName: file.name,
        fileType: file.type,
        folder
      };

      this.getPresignedUrl(request).subscribe({
        next: (presigned) => {
          this.uploadToS3(file, presigned.uploadUrl).subscribe({
            next: () => {
              observer.next({
                url: presigned.fileUrl,
                key: presigned.key
              });
              observer.complete();
            },
            error: (error) => observer.error(error)
          });
        },
        error: (error) => observer.error(error)
      });
    });
  }

  /**
   * Valida que el archivo sea una imagen y tenga un tamaño apropiado
   */
  validateImageFile(file: File): { valid: boolean; error?: string } {
    const maxSize = 5 * 1024 * 1024; // 5MB
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];

    if (!allowedTypes.includes(file.type)) {
      return { valid: false, error: 'Solo se permiten imágenes (JPEG, PNG, WebP)' };
    }

    if (file.size > maxSize) {
      return { valid: false, error: 'La imagen no puede superar los 5MB' };
    }

    return { valid: true };
  }
}