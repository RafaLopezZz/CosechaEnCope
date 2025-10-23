import { Component, Input, Output, EventEmitter, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-image-upload',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="image-upload-container">
      <div class="image-preview" [class.uploading]="uploading">
        <img 
          [src]="imageUrl || defaultImage" 
          [alt]="alt"
          class="preview-image">
        
        <div class="upload-overlay" *ngIf="uploading">
          <div class="spinner"></div>
          <p>{{ uploadMessage }}</p>
        </div>
        
        <div class="upload-actions" *ngIf="!uploading">
          <button 
            type="button"
            class="upload-btn"
            (click)="fileInput.click()">
            <i class="fas fa-camera"></i>
          </button>
          
          <button 
            type="button"
            class="remove-btn"
            *ngIf="imageUrl && showRemove"
            (click)="removeImage()">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </div>

      <input 
        #fileInput
        type="file" 
        accept="image/*"
        (change)="onFileSelected($event)"
        class="file-input">
      
      <div class="upload-info" *ngIf="showInfo">
        <p class="upload-hint">{{ hint }}</p>
        <div class="upload-error" *ngIf="error">
          <i class="fas fa-exclamation-triangle"></i>
          {{ error }}
        </div>
      </div>
    </div>
  `,
  styleUrls: ['image-upload.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ImageUploadComponent),
      multi: true
    }
  ]
})
export class ImageUploadComponent implements ControlValueAccessor {
  @Input() defaultImage = '/images/default-image.png';
  @Input() alt = 'Imagen';
  @Input() hint = 'JPG, PNG, WebP. Máximo 5MB.';
  @Input() uploadMessage = 'Subiendo imagen...';
  @Input() showRemove = true;
  @Input() showInfo = true;
  @Input() maxSizeMB = 5;
  @Input() acceptedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];

  @Output() fileSelected = new EventEmitter<File>();
  @Output() uploadStart = new EventEmitter<void>();
  @Output() uploadComplete = new EventEmitter<string>();
  @Output() uploadError = new EventEmitter<string>();
  @Output() imageRemoved = new EventEmitter<void>();

  imageUrl: string | null = null;
  uploading = false;
  error: string | null = null;

  private onChange = (value: string | null) => {};
  private onTouched = () => {};

  onFileSelected(event: Event) {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];

    if (!file) return;

    this.error = null;

    // Validar archivo
    const validation = this.validateFile(file);
    if (!validation.valid) {
      this.error = validation.error || 'Archivo inválido';
      return;
    }

    // Preview inmediato
    const reader = new FileReader();
    reader.onload = (e) => {
      this.imageUrl = e.target?.result as string;
    };
    reader.readAsDataURL(file);

    // Emitir eventos
    this.fileSelected.emit(file);
    this.uploadStart.emit();

    // Limpiar el input para permitir seleccionar el mismo archivo
    target.value = '';
  }

  removeImage() {
    this.imageUrl = null;
    this.error = null;
    this.onChange(null);
    this.onTouched();
    this.imageRemoved.emit();
  }

  setUploading(status: boolean) {
    this.uploading = status;
  }

  setImageUrl(url: string) {
    this.imageUrl = url;
    this.uploading = false;
    this.error = null;
    this.onChange(url);
    this.uploadComplete.emit(url);
  }

  setError(error: string) {
    this.error = error;
    this.uploading = false;
    this.uploadError.emit(error);
  }

  private validateFile(file: File): { valid: boolean; error?: string } {
    if (!this.acceptedTypes.includes(file.type)) {
      return { 
        valid: false, 
        error: `Solo se permiten archivos: ${this.acceptedTypes.map(t => t.split('/')[1].toUpperCase()).join(', ')}` 
      };
    }

    const maxSize = this.maxSizeMB * 1024 * 1024;
    if (file.size > maxSize) {
      return { 
        valid: false, 
        error: `El archivo no puede superar los ${this.maxSizeMB}MB` 
      };
    }

    return { valid: true };
  }

  // ControlValueAccessor implementation
  writeValue(value: string | null): void {
    this.imageUrl = value;
  }

  registerOnChange(fn: (value: string | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    // Implementar si es necesario
  }
}