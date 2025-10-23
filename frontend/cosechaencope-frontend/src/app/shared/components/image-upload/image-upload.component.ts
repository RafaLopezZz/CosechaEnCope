import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { ImagenService, ImageUploadResponse } from '../../../core/services/imagen.service';
import { UserStoreService } from '../../../core/services/user-store.service';

/**
 * Componente reutilizable para subir imágenes.
 * 
 * Proporciona una interfaz de usuario para seleccionar y subir imágenes
 * a AWS S3, con validaciones y preview de la imagen seleccionada.
 * 
 * @author rafalopezzz
 */
@Component({
  standalone: true,
  selector: 'app-image-upload',
  imports: [CommonModule],
  template: `
    <div class="image-upload-container">
      <!-- Área de Drop & Upload -->
      <div 
        class="upload-zone"
        [class.drag-over]="isDragOver"
        [class.has-error]="hasError"
        (click)="fileInput.click()"
        (dragover)="onDragOver($event)"
        (dragleave)="onDragLeave($event)"
        (drop)="onDrop($event)">
        
        <!-- Preview de imagen actual -->
        <div *ngIf="currentImageUrl && !selectedFile" class="current-image">
          <img [src]="currentImageUrl" [alt]="'Imagen actual'" class="preview-img">
          <div class="overlay">
            <i class="fas fa-camera"></i>
            <p>Click para cambiar imagen</p>
          </div>
        </div>

        <!-- Preview de imagen seleccionada -->
        <div *ngIf="selectedFile && previewUrl" class="preview-container">
          <img [src]="previewUrl" [alt]="selectedFile.name" class="preview-img">
          <div class="file-info">
            <p class="file-name">{{ selectedFile.name }}</p>
            <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
          </div>
          <button 
            type="button" 
            class="btn btn-sm btn-outline-danger remove-btn"
            (click)="removeFile($event)"
            title="Quitar imagen">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <!-- Área de subida vacía -->
        <div *ngIf="!currentImageUrl && !selectedFile" class="upload-prompt">
          <i class="fas fa-cloud-upload-alt upload-icon"></i>
          <h5>Subir Imagen</h5>
          <p>Arrastra una imagen aquí o click para seleccionar</p>
          <small class="text-muted">JPG, PNG, GIF, WEBP - Máximo 10MB</small>
        </div>

        <!-- Loading -->
        <div *ngIf="isUploading" class="upload-loading">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Subiendo...</span>
          </div>
          <p class="mt-2">Subiendo imagen...</p>
        </div>
      </div>

      <!-- Input file oculto -->
      <input 
        #fileInput
        type="file" 
        accept="image/*"
        (change)="onFileSelected($event)"
        style="display: none;">

      <!-- Mensajes de error -->
      <div *ngIf="errorMessages.length > 0" class="error-messages mt-2">
        <div *ngFor="let error of errorMessages" class="alert alert-danger alert-sm">
          <i class="fas fa-exclamation-triangle"></i> {{ error }}
        </div>
      </div>

      <!-- Mensaje de éxito -->
      <div *ngIf="successMessage" class="alert alert-success alert-sm mt-2">
        <i class="fas fa-check-circle"></i> {{ successMessage }}
      </div>

      <!-- Botones de acción -->
      <div *ngIf="selectedFile && !isUploading" class="action-buttons mt-3">
        <button 
          type="button" 
          class="btn btn-primary me-2"
          [disabled]="hasError"
          (click)="uploadFile()">
          <i class="fas fa-upload"></i> Subir Imagen
        </button>
        <button 
          type="button" 
          class="btn btn-outline-secondary"
          (click)="cancelUpload()">
          <i class="fas fa-times"></i> Cancelar
        </button>
      </div>
    </div>
  `,
  styles: [`
    .image-upload-container {
      width: 100%;
    }

    .upload-zone {
      border: 2px dashed #dee2e6;
      border-radius: 8px;
      padding: 20px;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s ease;
      position: relative;
      min-height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .upload-zone:hover {
      border-color: #28a745;
      background-color: #f8f9fa;
    }

    .upload-zone.drag-over {
      border-color: #28a745;
      background-color: #e8f5e8;
    }

    .upload-zone.has-error {
      border-color: #dc3545;
    }

    .current-image, .preview-container {
      position: relative;
      width: 100%;
      max-width: 300px;
      margin: 0 auto;
    }

    .preview-img {
      width: 100%;
      height: 200px;
      object-fit: cover;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .current-image .overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.7);
      color: white;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    .current-image:hover .overlay {
      opacity: 1;
    }

    .overlay i {
      font-size: 2rem;
      margin-bottom: 10px;
    }

    .file-info {
      margin-top: 10px;
      text-align: center;
    }

    .file-name {
      font-weight: 500;
      margin-bottom: 5px;
      word-break: break-all;
    }

    .file-size {
      color: #6c757d;
      font-size: 0.9rem;
      margin: 0;
    }

    .remove-btn {
      position: absolute;
      top: -10px;
      right: -10px;
      border-radius: 50%;
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: white;
      border: 2px solid #dc3545;
    }

    .upload-prompt {
      color: #6c757d;
    }

    .upload-icon {
      font-size: 3rem;
      color: #28a745;
      margin-bottom: 15px;
    }

    .upload-loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
    }

    .error-messages .alert {
      padding: 8px 12px;
      margin-bottom: 5px;
      font-size: 0.9rem;
    }

    .alert-sm {
      padding: 8px 12px;
      font-size: 0.9rem;
    }

    .action-buttons {
      text-align: center;
    }

    @media (max-width: 576px) {
      .upload-zone {
        padding: 15px;
        min-height: 150px;
      }
      
      .preview-img {
        height: 150px;
      }
      
      .upload-icon {
        font-size: 2rem;
      }
    }
  `]
})
export class ImageUploadComponent {
  private imagenService = inject(ImagenService);
  private userStore = inject(UserStoreService);

  @Input() currentImageUrl?: string;
  @Input() uploadType: 'articulo' | 'categoria' = 'articulo';
  @Input() placeholder: string = 'Subir imagen';

  @Output() imageUploaded = new EventEmitter<string>();
  @Output() imageRemoved = new EventEmitter<void>();

  selectedFile: File | null = null;
  previewUrl: string | null = null;
  errorMessages: string[] = [];
  successMessage: string = '';
  isUploading: boolean = false;
  isDragOver: boolean = false;

  get hasError(): boolean {
    return this.errorMessages.length > 0;
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.processFile(file);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
    
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.processFile(files[0]);
    }
  }

  private processFile(file: File): void {
    this.clearMessages();
    
    // Validar archivo
    const errors = this.imagenService.validarImagen(file);
    if (errors.length > 0) {
      this.errorMessages = errors;
      return;
    }

    this.selectedFile = file;
    
    // Crear preview
    const reader = new FileReader();
    reader.onload = (e) => {
      this.previewUrl = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  }

  uploadFile(): void {
    if (!this.selectedFile) return;

    const currentUser = this.userStore.snapshot();
    if (!currentUser) {
      this.errorMessages = ['No hay usuario autenticado'];
      return;
    }

    this.isUploading = true;
    this.clearMessages();

    const uploadObservable = this.uploadType === 'articulo' 
      ? this.imagenService.subirImagenArticulo(this.selectedFile, currentUser.idUsuario, currentUser.tipoUsuario)
      : this.imagenService.subirImagenCategoria(this.selectedFile, currentUser.idUsuario, currentUser.tipoUsuario);

    uploadObservable.subscribe({
      next: (response: ImageUploadResponse) => {
        this.isUploading = false;
        if (response.success) {
          this.successMessage = response.message;
          this.imageUploaded.emit(response.imageUrl);
          this.currentImageUrl = response.imageUrl;
          this.selectedFile = null;
          this.previewUrl = null;
        } else {
          this.errorMessages = [response.message];
        }
      },
      error: (error) => {
        this.isUploading = false;
        this.errorMessages = [error.error?.message || 'Error al subir la imagen'];
      }
    });
  }

  removeFile(event: Event): void {
    event.stopPropagation();
    this.selectedFile = null;
    this.previewUrl = null;
    this.clearMessages();
  }

  cancelUpload(): void {
    this.selectedFile = null;
    this.previewUrl = null;
    this.clearMessages();
  }

  removeCurrentImage(): void {
    if (this.currentImageUrl) {
      this.imagenService.eliminarImagen(this.currentImageUrl).subscribe({
        next: () => {
          this.currentImageUrl = undefined;
          this.imageRemoved.emit();
          this.successMessage = 'Imagen eliminada correctamente';
        },
        error: (error) => {
          this.errorMessages = [error.error?.message || 'Error al eliminar la imagen'];
        }
      });
    }
  }

  formatFileSize(bytes: number): string {
    return this.imagenService.formatearTamanoArchivo(bytes);
  }

  private clearMessages(): void {
    this.errorMessages = [];
    this.successMessage = '';
  }
}