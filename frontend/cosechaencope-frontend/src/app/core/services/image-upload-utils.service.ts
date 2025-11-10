import { Injectable } from '@angular/core';

type ValidationErrorKey = 'size' | 'type' | 'empty';

export interface ImageValidationOptions {
  maxSizeMb?: number;
  allowedMimeTypes?: string[];
}

export interface ImageValidationResult {
  valid: boolean;
  error?: ValidationErrorKey;
}

@Injectable({
  providedIn: 'root',
})
export class ImageUploadUtilsService {
  validateFile(file: File | null, options: ImageValidationOptions = {}): ImageValidationResult {
    if (!file) {
      return { valid: false, error: 'empty' };
    }

    const { maxSizeMb, allowedMimeTypes } = options;

    if (typeof maxSizeMb === 'number') {
      const maxSizeBytes = maxSizeMb * 1024 * 1024;
      if (file.size > maxSizeBytes) {
        return { valid: false, error: 'size' };
      }
    }

    if (allowedMimeTypes && allowedMimeTypes.length > 0) {
      const isAllowed = allowedMimeTypes.includes(file.type);
      if (!isAllowed) {
        return { valid: false, error: 'type' };
      }
    }

    return { valid: true };
  }

  buildErrorMessage(result: ImageValidationResult, options: ImageValidationOptions): string | null {
    if (result.valid || !result.error) {
      return null;
    }

    if (result.error === 'size' && typeof options.maxSizeMb === 'number') {
      return `El archivo no puede superar los ${options.maxSizeMb}MB`;
    }

    if (result.error === 'type' && options.allowedMimeTypes && options.allowedMimeTypes.length > 0) {
      const formattedTypes = options.allowedMimeTypes
        .map((type) => type.split('/')[1]?.toUpperCase() ?? type)
        .join(', ');
      return `Solo se permiten archivos: ${formattedTypes}`;
    }

    if (result.error === 'empty') {
      return 'Selecciona un archivo';
    }

    return 'Archivo inválido';
  }

  formatFileSize(bytes: number): string {
    if (!Number.isFinite(bytes) || bytes <= 0) {
      return '0 KB';
    }

    const units = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    let value = bytes;
    let unitIndex = 0;

    while (value >= 1024 && unitIndex < units.length - 1) {
      value /= 1024;
      unitIndex += 1;
    }

    const precision = unitIndex === 0 ? 0 : 1;
    return `${value.toFixed(precision)} ${units[unitIndex]}`;
  }
}
