import { Injectable } from '@angular/core';

export interface ImageValidationOptions {
  maxSizeMb?: number;
  allowedMimeTypes?: string[];
}

export interface ImageValidationResult {
  valid: boolean;
  errors: string[];
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
export class ImageUploadUtilsService {
  private readonly DEFAULT_MAX_SIZE_MB = 10;
  private readonly DEFAULT_MIME_TYPES = [
    'image/jpeg',
    'image/jpg',
    'image/png',
    'image/gif',
    'image/webp',
  ];

  /**
   * Valida que el archivo sea una imagen válida.
   *
   * @param file Archivo a validar
   * @returns Array de errores (vacío si es válido)
   */
  /**
   * Valida un archivo de imagen según las opciones especificadas.
   */
  validateFile(file: File | null, options: ImageValidationOptions = {}): ImageValidationResult {
    const errors: string[] = [];

    if (!file) {
      return { valid: false, errors: ['Debe seleccionar un archivo'] };
    }

    if (file.size === 0) {
      errors.push('El archivo está vacío');
    }

    const maxSizeMb = options.maxSizeMb ?? this.DEFAULT_MAX_SIZE_MB;
    const maxSizeBytes = maxSizeMb * 1024 * 1024;
    if (file.size > maxSizeBytes) {
      errors.push(`El archivo no puede exceder ${maxSizeMb}MB`);
    }

    const allowedTypes = options.allowedMimeTypes ?? this.DEFAULT_MIME_TYPES;
    if (!allowedTypes.includes(file.type)) {
      const extensions = allowedTypes.map((type) => type.split('/')[1]?.toUpperCase()).join(', ');
      errors.push(`Solo se permiten archivos: ${extensions}`);
    }

    return { valid: errors.length === 0, errors };
  }

  /**
   * Convierte el tamaño del archivo a formato legible.
   *
   * @param bytes Tamaño en bytes
   * @returns Tamaño formateado (ej: "1.5 MB")
   */
  /**
   * Formatea el tamaño de un archivo a formato legible.
   */
  formatFileSize(bytes: number): string {
    if (!Number.isFinite(bytes) || bytes <= 0) {
      return '0 KB';
    }

    const units = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    let value = bytes;
    let unitIndex = 0;

    while (value >= 1024 && unitIndex < units.length - 1) {
      value /= 1024;
      unitIndex++;
    }

    return `${value.toFixed(unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`;
  }

  /**
   * Verifica si una URL carga correctamente como imagen.
   */
  verificarImagenValida(url: string): Promise<boolean> {
    return new Promise((resolve) => {
      const img = new Image();
      img.onload = () => resolve(true);
      img.onerror = () => resolve(false);
      img.src = url;
    });
  }

  /**
   * Genera un mensaje de error basado en el resultado de validación.
   * 
   * @param result 
   * @param options 
   * @returns Mensaje de error o null si no hay errores
   */
  buildErrorMessage(result: ImageValidationResult, options: ImageValidationOptions): string | null {
    if (result.valid || !result.errors) {
      return null;
    }

    if (result.errors.includes('size') && typeof options.maxSizeMb === 'number') {
      return `El archivo no puede superar los ${options.maxSizeMb}MB`;
    }

    if (
      result.errors.includes('type') &&
      options.allowedMimeTypes &&
      options.allowedMimeTypes.length > 0
    ) {
      const formattedTypes = options.allowedMimeTypes
        .map((type) => type.split('/')[1]?.toUpperCase() ?? type)
        .join(', ');
      return `Solo se permiten archivos: ${formattedTypes}`;
    }

    if (result.errors.includes('empty')) {
      return 'Selecciona un archivo';
    }

    return 'Archivo inválido';
  }
}
