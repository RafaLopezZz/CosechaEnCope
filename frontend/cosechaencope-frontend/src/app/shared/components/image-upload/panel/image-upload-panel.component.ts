import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImagenService, ImageUploadResponse } from '../../../../core/services/imagen.service';
import { UserStoreService } from '../../../../core/services/user-store.service';
import {
  ImageUploadUtilsService,
  ImageValidationOptions,
} from '../../../../core/services/image-upload-utils.service';

/**
 * Componente reutilizable para subir imágenes.
 *
 * Proporciona una interfaz de usuario para seleccionar y subir imágenes
 * a AWS S3, con validaciones y preview de la imagen seleccionada.
 *
 * @author rafalopezzz
 */
@Component({
  selector: 'app-image-upload-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './image-upload-panel.component.html',
  styleUrl: './image-upload-panel.component.scss',
})
export class ImageUploadPanelComponent {
  private imagenService = inject(ImagenService);
  private userStore = inject(UserStoreService);
  private utils = inject(ImageUploadUtilsService);

  @Input() currentImageUrl?: string;
  @Input() uploadType: 'articulo' | 'categoria' | 'perfil' = 'articulo';
  @Input() placeholder: string = 'Subir imagen';
  @Input() maxSizeMb = 10;
  @Input() allowedMimeTypes: string[] = [
    'image/jpeg',
    'image/jpg',
    'image/png',
    'image/gif',
    'image/webp',
  ];

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

    const options: ImageValidationOptions = {
      maxSizeMb: this.maxSizeMb,
      allowedMimeTypes: this.allowedMimeTypes,
    };
    const validation = this.utils.validateFile(file, options);
    if (!validation.valid) {
      const errorMessage = this.utils.buildErrorMessage(validation, options) ?? 'Archivo inválido';
      this.errorMessages = [errorMessage];
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

    let uploadObservable;

    switch (this.uploadType) {
      case 'articulo':
        uploadObservable = this.imagenService.subirImagenArticulo(
          this.selectedFile,
          currentUser.idUsuario,
          currentUser.tipoUsuario
        );
        break;
      case 'categoria':
        uploadObservable = this.imagenService.subirImagenCategoria(
          this.selectedFile,
          currentUser.idUsuario,
          currentUser.tipoUsuario
        );
        break;
      case 'perfil':
        uploadObservable = this.imagenService.subirImagenPerfil(
          this.selectedFile,
          currentUser.idUsuario,
          currentUser.tipoUsuario
        );
        break;
      default:
        this.errorMessages = ['Tipo de subida no válido'];
        this.isUploading = false;
        return;
    }
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
      },
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
        },
      });
    }
  }

  formatFileSize(bytes: number): string {
    return this.utils.formatFileSize(bytes);
  }

  private clearMessages(): void {
    this.errorMessages = [];
    this.successMessage = '';
  }
}
