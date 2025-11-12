import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, forwardRef, inject } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { ImageUploadUtilsService, ImageValidationOptions } from '../../../../core/services/image-upload-utils.service';

@Component({
  selector: 'app-image-upload-field',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './image-upload-field.component.html',
  styleUrls: ['./image-upload-field.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ImageUploadFieldComponent),
      multi: true,
    },
  ],
})
export class ImageUploadFieldComponent implements ControlValueAccessor {
  private utils = inject(ImageUploadUtilsService);

  @Input() defaultImage = '/images/default-image.png';
  @Input() alt = 'Imagen';
  @Input() hint = 'JPG, PNG, WebP. Máximo 5MB.';
  @Input() uploadMessage = 'Subiendo imagen...';
  @Input() showRemove = true;
  @Input() showInfo = true;
  @Input() maxSizeMB = 5;
  @Input() acceptedTypes: string[] = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];

  @Output() fileSelected = new EventEmitter<File>();
  @Output() uploadStart = new EventEmitter<void>();
  @Output() uploadComplete = new EventEmitter<string>();
  @Output() uploadError = new EventEmitter<string>();
  @Output() imageRemoved = new EventEmitter<void>();

  imageUrl: string | null = null;
  uploading = false;
  error: string | null = null;

  private onChange: (value: string | null) => void = () => {};
  private onTouched: () => void = () => {};

  onFileSelected(event: Event): void {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];

    if (!file) {
      return;
    }

    this.error = null;

    const validationOptions: ImageValidationOptions = {
      maxSizeMb: this.maxSizeMB,
      allowedMimeTypes: this.acceptedTypes,
    };

    const validation = this.utils.validateFile(file, validationOptions);
    if (!validation.valid) {
      this.error = this.utils.buildErrorMessage(validation, validationOptions);
      this.uploadError.emit(this.error ?? 'Archivo inválido');
      target.value = '';
      return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      this.imageUrl = e.target?.result as string;
    };
    reader.readAsDataURL(file);

    this.fileSelected.emit(file);
    this.uploadStart.emit();

    target.value = '';
  }

  removeImage(): void {
    this.imageUrl = null;
    this.error = null;
    this.onChange(null);
    this.onTouched();
    this.imageRemoved.emit();
  }

  setUploading(status: boolean): void {
    this.uploading = status;
  }

  setImageUrl(url: string): void {
    this.imageUrl = url;
    this.uploading = false;
    this.error = null;
    this.onChange(url);
    this.uploadComplete.emit(url);
  }

  setError(error: string): void {
    this.error = error;
    this.uploading = false;
    this.uploadError.emit(error);
  }

  writeValue(value: string | null): void {
    this.imageUrl = value;
  }

  registerOnChange(fn: (value: string | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(_: boolean): void {
    // ControlValueAccessor contract; not needed for now.
  }
}
