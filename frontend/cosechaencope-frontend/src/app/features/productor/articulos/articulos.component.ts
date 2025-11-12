import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

import { UserStoreService } from '../../../core/services/user-store.service';
import { ProductorService } from '../../../core/services/productor.service';
import { ArticuloResponse, ArticuloRequest } from '../../../shared/models/articulo.models';
import { CategoriaResponse } from '../../../shared/models/categoria.models';
import { ProductorResponse } from '../../../shared/models/productor.models';
import { ImageUploadPanelComponent } from '../../../shared/components/image-upload/panel/image-upload-panel.component';

@Component({
  selector: 'app-articulos-productor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ImageUploadPanelComponent],
  templateUrl: './articulos.component.html',
  styleUrl: './articulos.component.scss'
})
export class ArticulosProductorComponent implements OnInit {
  private fb = inject(FormBuilder);
  private userStore = inject(UserStoreService);
  private productorService = inject(ProductorService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  productor: ProductorResponse | null = null;
  articulos: ArticuloResponse[] = [];
  categorias: CategoriaResponse[] = [];
  loading = true;
  showForm = false;
  editingArticulo: ArticuloResponse | null = null;
  saving = false;
  imagePreview: string | null = null;

  currentUser = this.userStore.snapshot();

  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(2)]],
    descripcion: ['', [Validators.required, Validators.minLength(10)]],
    precio: [0, [Validators.required, Validators.min(0.01)]],
    stock: [0, [Validators.required, Validators.min(0)]],
    idCategoria: [0, [Validators.required, Validators.min(1)]],
    imagenUrl: ['']
  });

  private clearAuthSession() {
    sessionStorage.removeItem('authToken');
    this.userStore.clear();
    this.router.navigateByUrl('/login/productores');
  }

  ngOnInit() {
    // Obtener el usuario actual de forma reactiva
    this.currentUser = this.userStore.snapshot();
    
    // Si no hay usuario, intentar verificar el token en sessionStorage
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
    
    if (this.currentUser.tipoUsuario !== 'PRODUCTOR') {
      console.error('User is not a producer:', this.currentUser);
      this.router.navigateByUrl('/login/productores');
      return;
    }

    console.log('Current user validated:', this.currentUser);
    console.log('User properties:', Object.keys(this.currentUser));
    console.log('User ID field value:', this.currentUser.idUsuario);

    // Verificar si viene de crear nuevo producto
    const nuevo = this.route.snapshot.url.some(segment => segment.path === 'nuevo');
    if (nuevo) {
      this.showForm = true;
    }

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
    
    // Validar token antes de hacer requests
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
        this.loadArticulos();
      },
      error: (error) => {
        console.error('Error loading productor:', error);
        console.log('Error details:', {
          status: error.status,
          statusText: error.statusText,
          message: error.message,
          url: error.url
        });
        this.loading = false;
        
        let errorMessage = 'Error al cargar la información del productor. ';
        if (error.status === 401) {
          errorMessage += 'Su sesión ha expirado o no tiene permisos. Por favor, inicie sesión nuevamente.';
          // Limpiar datos de sesión
          console.warn('🚨 401 error detected - but NOT clearing session for debugging');
          // this.clearAuthSession(); // Comentado para debugging
        } else if (error.status === 404) {
          errorMessage += 'No se encontró información de productor para este usuario.';
        } else if (error.status === 0) {
          errorMessage += 'No se pudo conectar con el servidor. Verifique su conexión a internet.';
        } else {
          errorMessage += `Error del servidor (${error.status}). Inténtelo de nuevo más tarde.`;
        }
        
        alert(errorMessage);
      }
    });

    // Cargar categorías
    this.productorService.getCategorias().subscribe({
      next: (categorias) => {
        console.log('Categorias loaded:', categorias);
        this.categorias = categorias;
      },
      error: (error) => {
        console.error('Error loading categorias:', error);
        alert('Error al cargar las categorías');
      }
    });
  }

  loadArticulos() {
    if (!this.productor) return;

    this.productorService.getArticulosProductor(this.productor.idProductor).subscribe({
      next: (articulos) => {
        this.articulos = articulos;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  showNewForm() {
    this.editingArticulo = null;
    this.form.reset({
      nombre: '',
      descripcion: '',
      precio: 0,
      stock: 0,
      idCategoria: 0,
      imagenUrl: ''
    });
    
    // Marcar los campos como pristine para evitar mostrar errores inmediatamente
    Object.keys(this.form.controls).forEach(key => {
      this.form.get(key)?.markAsUntouched();
      this.form.get(key)?.markAsPristine();
    });
    
    this.imagePreview = null;
    this.showForm = true;
    
    console.log('New form initialized:', this.form.value);
  }

  editArticulo(articulo: ArticuloResponse) {
    this.editingArticulo = articulo;
    this.form.patchValue({
      nombre: articulo.nombre,
      descripcion: articulo.descripcion,
      precio: articulo.precio,
      stock: articulo.stock,
      idCategoria: articulo.idCategoria || 0,
      imagenUrl: articulo.imagenUrl
    });
    this.imagePreview = articulo.imagenUrl;
    this.showForm = true;
  }

  cancelForm() {
    this.showForm = false;
    this.editingArticulo = null;
    this.form.reset();
    this.imagePreview = null;
    
    // Si venía de /nuevo, volver al dashboard
    if (this.route.snapshot.url.some(segment => segment.path === 'nuevo')) {
      this.router.navigateByUrl('/productor/articulos');
    }
  }

  onImageUploaded(url: string) {
    this.form.patchValue({ imagenUrl: url });
    this.imagePreview = url;
  }

  onImageRemoved() {
    this.form.patchValue({ imagenUrl: '' });
    this.imagePreview = null;
  }

  onSubmit() {
    if (!this.productor) {
      console.error('No se encontró información del productor');
      alert('Error: No se encontró información del productor');
      return;
    }

    if (this.form.invalid) {
      console.error('Form is invalid:', {
        formErrors: this.form.errors,
        formValue: this.form.value,
        controls: Object.keys(this.form.controls).map(key => ({
          key,
          errors: this.form.get(key)?.errors,
          value: this.form.get(key)?.value
        }))
      });
      
      // Marcar todos los campos como tocados para mostrar errores
      Object.keys(this.form.controls).forEach(key => {
        this.form.get(key)?.markAsTouched();
      });
      
      alert('Por favor, completa todos los campos obligatorios correctamente');
      return;
    }

    this.saving = true;
    const formData = this.form.getRawValue();
    
    const request: ArticuloRequest = {
      nombre: formData.nombre.trim(),
      descripcion: formData.descripcion.trim(),
      precio: Number(formData.precio),
      stock: Number(formData.stock),
      idCategoria: Number(formData.idCategoria),
      idProductor: this.productor.idProductor,
      imagenUrl: formData.imagenUrl || ''
    };

    console.log('Sending request to create/update articulo:', request);

    const operation$ = this.editingArticulo 
      ? this.productorService.updateArticulo(this.productor.idProductor, this.editingArticulo.idArticulo, request)
      : this.productorService.createArticulo(this.productor.idProductor, request);

    operation$.subscribe({
      next: (response) => {
        console.log('Articulo saved successfully:', response);
        this.loadArticulos();
        this.cancelForm();
        this.saving = false;
        alert(this.editingArticulo ? 'Producto actualizado correctamente' : 'Producto creado correctamente');
      },
      error: (error) => {
        console.error('Error saving articulo:', error);
        let errorMessage = 'Error al guardar el producto. ';
        
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
      }
    });
  }

  deleteArticulo(articulo: ArticuloResponse) {
    if (!confirm(`¿Estás seguro de que quieres eliminar el producto "${articulo.nombre}"?`)) {
      return;
    }

    if (!this.productor) {
      alert('Error: No se encontró información del productor');
      return;
    }

    this.productorService.deleteArticulo(this.productor.idProductor, articulo.idArticulo).subscribe({
      next: () => {
        this.loadArticulos();
        alert('Producto eliminado correctamente');
      },
      error: (error) => {
        console.error('Error deleting articulo:', error);
        alert('Error al eliminar el producto. Inténtalo de nuevo.');
      }
    });
  }

  goBack() {
    this.router.navigateByUrl('/productor/dashboard');
  }
}