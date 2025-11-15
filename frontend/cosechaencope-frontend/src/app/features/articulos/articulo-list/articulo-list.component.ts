import { Component, OnInit, inject } from '@angular/core';
import { NavbarComponent } from '../../home/navbar/navbar.component';
import { FooterComponent } from '../../home/footer/footer.component';
import { ArticuloService } from '../../../core/services/articulo.service';
import { ArticuloResponse } from '../../../shared/models/articulo.models';
import { CarritoService } from '../../../core/services/carrito.service';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

/**
 * Componente para listar artículos con soporte para carrito invitado
 * 
 * MEJORAS IMPLEMENTADAS:
 * ✅ Agregar al carrito sin autenticación (localStorage)
 * ✅ Notificaciones de éxito/error al agregar items
 * ✅ Soporte para cargar artículos por categoría
 */
@Component({
  selector: 'app-articulo-list',
  imports: [CommonModule, FormsModule, NavbarComponent, FooterComponent],
  standalone: true,
  templateUrl: './articulo-list.component.html',
  styleUrls: ['./articulo-list.component.scss'],
})
export class ArticuloListComponent implements OnInit {
  // Servicios inyectados
  private articuloService = inject(ArticuloService);
  private carritoService = inject(CarritoService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  articulos: ArticuloResponse[] = [];
  categoriaSeleccionada: string = '';
  isLoading = false;

  get articulosFiltrados() {
    if (!this.categoriaSeleccionada) return this.articulos;
    return this.articulos.filter((a) => String(a.idCategoria) === this.categoriaSeleccionada);
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const idCategoria = params.get('idCategoria');
      if (idCategoria) {
        this.cargarArticulosPorCategoria(Number(idCategoria));
      } else {
        this.cargarTodosLosArticulos();
      }
    });
  }

  cargarTodosLosArticulos(): void {
    this.articuloService.getArticulos().subscribe({
      next: (data) => (this.articulos = data),
      error: (err) => console.error('Error al cargar artículos', err),
    });
  }

  cargarArticulosPorCategoria(idCategoria: number): void {
    this.articuloService.getArticulosPorCategoria(idCategoria).subscribe({
      next: (data) => {
        console.log(`Artículos de la categoría ${idCategoria} recibidos:`, data);
        this.articulos = data;
      },
      error: (err) =>
        console.error(`Error al cargar artículos de la categoría ${idCategoria}`, err),
    });
  }

  /**
   * Maneja el error cuando una imagen no se puede cargar.
   * Establece una imagen por defecto.
   */
  onImageError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    if (imgElement && !imgElement.src.includes('placeholder')) {
      imgElement.src = 'https://via.placeholder.com/300/8BC34A/FFFFFF?text=Producto';
    }
  }

  /**
   * Agrega un artículo al carrito (autenticado o invitado)
   * 
   * MEJORA: Ahora funciona sin autenticación usando localStorage
   */
  addToCart(articulo: ArticuloResponse): void {
    this.isLoading = true;
    
    this.carritoService.agregarItem(articulo.idArticulo, 1).subscribe({
      next: (carrito) => {
        console.log(`✅ ${articulo.nombre} agregado al carrito`);
        // El CarritoService ya actualiza el BehaviorSubject automáticamente
      },
      error: (err) => {
        console.error('❌ Error al agregar al carrito:', err);
        alert('Error al agregar el artículo al carrito');
      },
      complete: () => (this.isLoading = false),
    });
  }
}
