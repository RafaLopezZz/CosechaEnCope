import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';

import { UserStoreService } from '../../../core/services/user-store.service';
import { ProductorService } from '../../../core/services/productor.service';
import { UploadService } from '../../../core/services/upload.service';
import { ProductorResponse, ProductorRequest } from '../../../shared/models/productor.models';

@Component({
  selector: 'app-perfil-productor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './perfil.component.html',
  styleUrls: ['./perfil.component.scss']
})
export class PerfilProductorComponent implements OnInit {
  private fb = inject(FormBuilder);
  private userStore = inject(UserStoreService);
  private productorService = inject(ProductorService);
  private uploadService = inject(UploadService);
  private router = inject(Router);

  productor: ProductorResponse | null = null;
  loading = true;
  saving = false;
  uploading = false;
  imagePreview: string | null = null;

  currentUser = this.userStore.snapshot();

  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(2)]],
    direccion: ['', [Validators.required]],
    telefono: ['', [Validators.required, Validators.pattern(/^[0-9]{9}$/)]],
    imagenUrl: ['']
  });

  ngOnInit() {
    if (!this.currentUser || this.currentUser.tipoUsuario !== 'PRODUCTOR') {
      this.router.navigateByUrl('/login/productores');
      return;
    }

    this.loadProductor();
  }

  loadProductor() {
    if (!this.currentUser) return;

    this.productorService.getProductorPorUsuario(this.currentUser.idUsuario).subscribe({
      next: (productor) => {
        this.productor = productor;
        this.form.patchValue({
          nombre: productor.nombre,
          direccion: productor.direccion,
          telefono: productor.telefono,
          imagenUrl: productor.imagenUrl
        });
        this.imagePreview = productor.imagenUrl;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onFileSelected(event: Event) {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];

    if (!file) return;

    const validation = this.uploadService.validateImageFile(file);
    if (!validation.valid) {
      alert(validation.error);
      return;
    }

    // Preview inmediato
    const reader = new FileReader();
    reader.onload = (e) => {
      this.imagePreview = e.target?.result as string;
    };
    reader.readAsDataURL(file);

    // Upload a S3
    this.uploading = true;
    this.uploadService.uploadFile(file, 'productores').subscribe({
      next: (response) => {
        this.form.patchValue({ imagenUrl: response.url });
        this.uploading = false;
      },
      error: (error) => {
        console.error('Error uploading image:', error);
        alert('Error al subir la imagen. Inténtalo de nuevo.');
        this.uploading = false;
        this.imagePreview = this.form.value.imagenUrl || null;
      }
    });
  }

  onSubmit() {
    if (this.form.invalid || !this.productor) return;

    this.saving = true;
    const formData = this.form.getRawValue();
    
    const request: ProductorRequest = {
      idUsuario: this.currentUser!.idUsuario,
      nombre: formData.nombre,
      direccion: formData.direccion,
      telefono: formData.telefono,
      imagenUrl: formData.imagenUrl
    };

    this.productorService.updateProductor(this.productor.idProductor, request)
      .pipe(finalize(() => this.saving = false))
      .subscribe({
        next: (updatedProductor) => {
          this.productor = updatedProductor;
          alert('Perfil actualizado correctamente');
        },
        error: (error) => {
          console.error('Error updating profile:', error);
          alert('Error al actualizar el perfil. Inténtalo de nuevo.');
        }
      });
  }

  goBack() {
    this.router.navigateByUrl('/productor/dashboard');
  }
}