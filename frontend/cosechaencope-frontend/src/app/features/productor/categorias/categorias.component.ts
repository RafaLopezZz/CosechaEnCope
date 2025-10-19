import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { ProductorService } from '../../../core/services/productor.service';
import { ImagenService } from '../../../core/services/imagen.service';
import { CategoriaResponse, CategoriaRequest } from '../../../shared/models/categoria.models';

@Component({
  selector: 'app-categorias-productor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './categorias.component.html',
  styleUrls: ['./categorias.component.scss']
})
export class CategoriasProductorComponent implements OnInit {
  private fb = inject(FormBuilder);
  private productorService = inject(ProductorService);
  public imagenService = inject(ImagenService);
  private router = inject(Router);

  categorias: CategoriaResponse[] = [];
  loading = true;
  showForm = false;
  editingCategoria: CategoriaResponse | null = null;
  saving = false;

  // Propiedades para gestión de imágenes
  selectedFile: File | null = null;
  uploadingImage = false;
  imagePreview: string | null = null;

  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(2)]],
    descripcion: ['', [Validators.required, Validators.minLength(10)]]
  });

  ngOnInit() {
    this.loadCategorias();
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      
      // Validar archivo
      const errores = this.imagenService.validarImagen(file);
      if (errores.length > 0) {
        alert(errores.join('\n'));
        return;
      }

      this.selectedFile = file;
      
      // Crear preview
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagePreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  removeSelectedImage() {
    this.selectedFile = null;
    this.imagePreview = null;
    // Reset file input
    const fileInput = document.getElementById('imagen') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  loadCategorias() {
    this.productorService.getCategorias().subscribe({
      next: (categorias) => {
        this.categorias = categorias;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  showNewForm() {
    this.editingCategoria = null;
    this.form.reset();
    this.selectedFile = null;
    this.imagePreview = null;
    this.uploadingImage = false;
    this.showForm = true;
  }

  editCategoria(categoria: CategoriaResponse) {
    this.editingCategoria = categoria;
    this.form.patchValue({
      nombre: categoria.nombre,
      descripcion: categoria.descripcion
    });
    
    // Si la categoría tiene imagen, mostrar preview
    if (categoria.imagenUrl) {
      this.imagePreview = categoria.imagenUrl;
    }
    
    this.showForm = true;
  }

  cancelForm() {
    this.showForm = false;
    this.editingCategoria = null;
    this.form.reset();
    this.selectedFile = null;
    this.imagePreview = null;
    this.uploadingImage = false;
    
    // Reset file input
    const fileInput = document.getElementById('imagen') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  onSubmit() {
    if (this.form.invalid) return;

    this.saving = true;
    const formData = this.form.getRawValue();
    
    // Función para crear/actualizar categoría
    const crearCategoria = (imagenUrl?: string) => {
      const request: CategoriaRequest = {
        nombre: formData.nombre,
        descripcion: formData.descripcion,
        imagenUrl: imagenUrl || this.editingCategoria?.imagenUrl || ''
      };

      const operation$ = this.editingCategoria 
        ? this.productorService.updateCategoria(this.editingCategoria.idCategoria, request)
        : this.productorService.createCategoria(request);

      operation$.subscribe({
        next: () => {
          this.loadCategorias();
          this.cancelForm();
          this.saving = false;
          alert(this.editingCategoria ? 'Categoría actualizada correctamente' : 'Categoría creada correctamente');
        },
        error: (error) => {
          console.error('Error saving categoria:', error);
          alert('Error al guardar la categoría. Inténtalo de nuevo.');
          this.saving = false;
        }
      });
    };

    // Si hay una imagen seleccionada, subirla primero
    if (this.selectedFile) {
      this.uploadingImage = true;
      
      // Obtener datos del usuario desde el localStorage
      const userData = localStorage.getItem('user');
      if (!userData) {
        alert('Error: No se encontraron datos del usuario');
        this.saving = false;
        return;
      }

      const user = JSON.parse(userData);
      
      this.imagenService.subirImagenCategoria(this.selectedFile, user.id, user.tipoUsuario).subscribe({
        next: (response) => {
          this.uploadingImage = false;
          crearCategoria(response.imageUrl);
        },
        error: (error) => {
          console.error('Error uploading image:', error);
          this.uploadingImage = false;
          this.saving = false;
          alert('Error al subir la imagen. Inténtalo de nuevo.');
        }
      });
    } else {
      // Si no hay imagen nueva, proceder directamente
      crearCategoria();
    }
  }

  deleteCategoria(categoria: CategoriaResponse) {
    if (!confirm(`¿Estás seguro de que quieres eliminar la categoría "${categoria.nombre}"?`)) {
      return;
    }

    this.productorService.deleteCategoria(categoria.idCategoria).subscribe({
      next: () => {
        this.loadCategorias();
        alert('Categoría eliminada correctamente');
      },
      error: (error) => {
        console.error('Error deleting categoria:', error);
        alert('Error al eliminar la categoría. Puede que tenga productos asociados.');
      }
    });
  }

  goBack() {
    this.router.navigateByUrl('/productor/dashboard');
  }

  onImageError(event: Event) {
    const img = event.target as HTMLImageElement;
    img.src = '/images/default-category.png';
  }
}