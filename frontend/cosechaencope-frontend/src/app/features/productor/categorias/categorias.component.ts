import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

import { UserStoreService } from '../../../core/services/user-store.service';
import { ProductorService } from '../../../core/services/productor.service';
import { ProductorResponse } from '../../../shared/models/productor.models';
import { ImageUploadPanelComponent } from '../../../shared/components/image-upload/panel/image-upload-panel.component';
import { CategoriaResponse, CategoriaRequest } from '../../../shared/models/categoria.models';

@Component({
  selector: 'app-categorias-productor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ImageUploadPanelComponent],
  templateUrl: './categorias.component.html',
  styleUrls: ['./categorias.component.scss'],
})
export class CategoriasProductorComponent implements OnInit {
  private fb = inject(FormBuilder);
  private userStore = inject(UserStoreService);
  private productorService = inject(ProductorService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  productor: ProductorResponse | null = null;
  categorias: CategoriaResponse[] = [];
  loading = true;
  showForm = false;
  editingCategoria: CategoriaResponse | null = null;
  saving = false;
  imagePrwview: string | null = null;

  currentUser = this.userStore.snapshot();

  // Propiedades para gestión de imágenes
  selectedFile: File | null = null;
  uploadingImage = false;
  imagePreview: string | null = null;

  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(2)]],
    descripcion: ['', [Validators.required, Validators.minLength(10)]],
    imagenUrl: [''],
  });

  ngOnInit() {
    // 1. Obtener el usuario actual de forma reactiva
    this.currentUser = this.userStore.snapshot();

    // 2. Si no hay usuario, intentar verificar el token en sessionStorage
    if (!this.currentUser) {
      const token = sessionStorage.getItem('authToken');
      if (!token) {
        console.error('No user found and no token in sessionStorage');
        this.router.navigateByUrl('/login/productores');
        return;
      }

      // Intentar decodificar el token para obtener la información del usuario
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        console.log('Token payload:', payload);

        // Si el token existe pero no hay usuario en el store, hay un problema
        alert('Error: Sesión inconsistente. Por favor, inicie sesión nuevamente.');
        this.router.navigateByUrl('/login/productores');
        return;
      } catch (e) {
        console.error('Error decoding token:', e);
        this.router.navigateByUrl('/login/productores');
        return;
      }
    }

    // 3. Verificar tipo de usuario
    if (this.currentUser.tipoUsuario !== 'PRODUCTOR') {
      console.error('User is not a producer:', this.currentUser);
      this.router.navigateByUrl('/login/productores');
      return;
    }

    console.log('Current user validated:', this.currentUser);
    console.log('User properties:', Object.keys(this.currentUser));
    console.log('User ID field value:', this.currentUser.idUsuario);

    // 4. Verificar si viene de crear nueva categoría (opcional, igual que en artículos)
    const nuevo = this.route.snapshot.url.some((segment) => segment.path === 'nuevo');
    if (nuevo) {
      this.showForm = true;
    }

    // 5. Cargar datos
    this.loadData();
  }

  loadData() {
    // Verificar el estado actual del usuario
    this.currentUser = this.userStore.snapshot();

    if (!this.currentUser) {
      console.error('No current user found in store');
      alert('Error: No se ha iniciado sesión correctamente');
      this.router.navigateByUrl('/login/productores');
      return;
    }

    if (!this.currentUser.idUsuario) {
      console.error('Current user has no ID:', this.currentUser);
      alert('Error: El usuario no tiene un ID válido');
      this.router.navigateByUrl('/login/productores');
      return;
    }

    console.log('Loading data for user:', this.currentUser);

    // Verificar el token antes de hacer requests
    const token = sessionStorage.getItem('authToken');
    console.log('Token in sessionStorage:', token ? token.substring(0, 50) + '...' : 'null');

    if (!token) {
      console.error('No token found in sessionStorage');
      alert('Error: No se encontró token de autenticación. Por favor, inicie sesión nuevamente.');
      this.router.navigateByUrl('/login/productores');
      return;
    }

    // Verificar si el token ha expirado
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const now = Math.floor(Date.now() / 1000);
      console.log('Token payload:', payload);
      console.log('Token exp:', payload.exp, 'Current time:', now);

      if (payload.exp && payload.exp < now) {
        console.error('Token has expired');
        alert('Su sesión ha expirado. Por favor, inicie sesión nuevamente.');
        console.warn('🚨 Token expired - but NOT clearing session for debugging');
        // this.clearAuthSession(); // Comentado para debugging
        return;
      }
    } catch (e) {
      console.error('Error decoding token:', e);
      alert('Token inválido. Por favor, inicie sesión nuevamente.');
      console.warn('🚨 Invalid token - but NOT clearing session for debugging');
      // this.clearAuthSession(); // Comentado para debugging
      return;
    }

    // Cargar datos del productor
    console.log('Attempting to load productor for user ID:', this.currentUser.idUsuario);

    this.productorService.getProductorPorUsuario(this.currentUser.idUsuario).subscribe({
      next: (productor) => {
        console.log('Productor loaded:', productor);
        this.productor = productor;

        // AQUÍ ESTÁ EL CAMBIO CLAVE: Cargar Categorías en vez de Artículos
        this.loadCategorias();
      },
      error: (error) => {
        console.error('Error loading productor:', error);
        console.log('Error details:', {
          status: error.status,
          statusText: error.statusText,
          message: error.message,
          url: error.url,
        });

        // Asumiendo que tienes una variable loading en categorías también
        // this.loading = false;

        let errorMessage = 'Error al cargar la información del productor. ';
        if (error.status === 401) {
          errorMessage +=
            'Su sesión ha expirado o no tiene permisos. Por favor, inicie sesión nuevamente.';
          console.warn('🚨 401 error detected - but NOT clearing session for debugging');
        } else if (error.status === 404) {
          errorMessage += 'No se encontró información de productor para este usuario.';
        } else if (error.status === 0) {
          errorMessage += 'No se pudo conectar con el servidor. Verifique su conexión a internet.';
        } else {
          errorMessage += `Error del servidor (${error.status}). Inténtelo de nuevo más tarde.`;
        }

        alert(errorMessage);
      },
    });
  }

  onImageUploaded(url: string) {
    this.form.patchValue({ imagenUrl: url });
    this.imagePreview = url;
  }

  onImageRemoved() {
    this.form.patchValue({ imagenUrl: '' });
    this.imagePreview = null;
  }

  loadCategorias() {
    this.productorService.getCategorias().subscribe({
      next: (categorias) => {
        this.categorias = categorias;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
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
      descripcion: categoria.descripcion,
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
    // 1. Verificación del Productor (Igual que en Artículos)
    if (!this.productor) {
      console.error('No se encontró información del productor');
      alert('Error: No se encontró información del productor');
      return;
    }

    // 2. Validación del Formulario (Igual que en Artículos)
    if (this.form.invalid) {
      console.error('Form is invalid:', {
        formErrors: this.form.errors,
        formValue: this.form.value,
        controls: Object.keys(this.form.controls).map((key) => ({
          key,
          errors: this.form.get(key)?.errors,
          value: this.form.get(key)?.value,
        })),
      });

      // Marcar todos los campos como tocados
      Object.keys(this.form.controls).forEach((key) => {
        this.form.get(key)?.markAsTouched();
      });

      alert('Por favor, completa todos los campos obligatorios correctamente');
      return;
    }

    // 3. Preparación de datos (Igual que en Artículos)
    this.saving = true;
    const formData = this.form.getRawValue();

    // Construimos el objeto request directamente
    const request: CategoriaRequest = {
      nombre: formData.nombre.trim(),
      descripcion: formData.descripcion.trim(),
      // Asumimos la misma lógica que en artículos: la URL viene del form o se envía vacía/existente
      imagenUrl: formData.imagenUrl || this.editingCategoria?.imagenUrl || '',
    };

    console.log('Sending request to create/update categoria:', request);

    // 4. Selección de operación (Igual que en Artículos)
    const operation$ = this.editingCategoria
      ? this.productorService.updateCategoria(this.editingCategoria.idCategoria, request)
      : this.productorService.createCategoria(request);

    // 5. Suscripción y manejo de respuesta (Igual que en Artículos)
    operation$.subscribe({
      next: (response) => {
        console.log('Categoria saved successfully:', response);
        this.loadCategorias();
        this.cancelForm();
        this.saving = false;
        alert(
          this.editingCategoria
            ? 'Categoría actualizada correctamente'
            : 'Categoría creada correctamente'
        );
      },
      error: (error) => {
        console.error('Error saving categoria:', error);
        let errorMessage = 'Error al guardar la categoría. ';

        // Mismo manejo de errores detallado
        if (error.status === 400) {
          errorMessage += 'Verifique que todos los campos estén completos y sean válidos.';
        } else if (error.status === 401) {
          errorMessage += 'No tiene permisos para realizar esta operación.';
        } else if (error.status === 404) {
          errorMessage += 'No se encontró el recurso solicitado.';
        } else if (error.status === 500) {
          errorMessage += 'Error interno del servidor.';
        } else if (error.error?.message) {
          errorMessage += error.error.message;
        } else {
          errorMessage += 'Inténtalo de nuevo.';
        }

        alert(errorMessage);
        this.saving = false;
      },
    });
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

        // Manejo específico si hay productos asociados (común en categorías)
        if (
          error.status === 409 ||
          (error.error && error.error.message && error.error.message.includes('constraint'))
        ) {
          alert('No se puede eliminar la categoría porque tiene productos asociados.');
        } else {
          alert('Error al eliminar la categoría. Inténtalo de nuevo.');
        }
      },
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
