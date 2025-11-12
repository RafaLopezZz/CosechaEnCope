import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { UserStoreService } from '../../../core/services/user-store.service';
import { ProductorService } from '../../../core/services/productor.service';
import { ImageUploadPanelComponent } from '../../../shared/components/image-upload/panel/image-upload-panel.component';
import { ProductorResponse, ProductorRequest } from '../../../shared/models/productor.models';

@Component({
  selector: 'app-perfil-productor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ImageUploadPanelComponent],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.scss',
})
export class PerfilProductorComponent implements OnInit {
  private fb = inject(FormBuilder);
  private userStore = inject(UserStoreService);
  private productorService = inject(ProductorService);
  private router = inject(Router);

  productor: ProductorResponse | null = null;
  loading = true;
  saving = false;

  currentUser = this.userStore.snapshot();

  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(2)]],
    direccion: ['', [Validators.required]],
    telefono: ['', [Validators.required, Validators.pattern(/^[0-9]{9}$/)]],
    imagenUrl: [''],
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
          imagenUrl: productor.imagenUrl,
        });
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onImageUploaded(url: string) {
    this.form.patchValue({ imagenUrl: url });
  }

  onImageRemoved() {
    this.form.patchValue({ imagenUrl: '' });
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
      imagenUrl: formData.imagenUrl,
    };

    this.productorService
      .updateProductor(this.currentUser!.idUsuario, request)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: (updatedProductor) => {
          this.productor = updatedProductor;
          alert('Perfil actualizado correctamente');
        },
        error: (error) => {
          console.error('Error updating profile:', error);
          alert('Error al actualizar el perfil. Inténtalo de nuevo.');
        },
      });
  }

  goBack() {
    this.router.navigateByUrl('/productor/dashboard');
  }
}
