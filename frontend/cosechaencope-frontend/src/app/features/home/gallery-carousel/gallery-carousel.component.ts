import { Component, ElementRef, ViewChild, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ArticuloService } from '../../../core/services/articulo.service';
import { ArticuloResponse } from '../../../shared/models/articulo.models';
import { finalize } from 'rxjs/operators';

interface GalleryItem {
  id: number;
  nombre: string;
  precio: number;
  imagenUrl: string;
  nombreProductor: string;
  nombreCategoria: string;
}

@Component({
  standalone: true,
  selector: 'app-gallery-carousel',
  imports: [CommonModule, RouterModule],
  templateUrl: './gallery-carousel.component.html',
  styleUrls: ['./gallery-carousel.component.scss'],
})
export class GalleryCarouselComponent implements OnInit {
  @ViewChild('track', { static: true }) track!: ElementRef<HTMLElement>;
  
  private articuloService = inject(ArticuloService);
  
  galleryItems: GalleryItem[] = [];
  loading = true;
  error = false;
  index = 0;

  // Imágenes por defecto como fallback
  images: string[] = [
    '/gal/1.webp', '/gal/2.webp', '/gal/3.webp', '/gal/4.webp',
    '/gal/5.jpg', '/gal/6.jpg', '/gal/7.jpg', '/gal/8.webp',
  ];

  private defaultImages: string[] = [
    '/gal/1.webp', '/gal/2.webp', '/gal/3.webp', '/gal/4.webp',
    '/gal/5.jpg', '/gal/6.jpg', '/gal/7.jpg', '/gal/8.webp',
  ];

  ngOnInit() {
    this.loadArticulos();
  }

  private loadArticulos(): void {
    this.loading = true;
    this.error = false;
    
    this.articuloService.getArticulos()
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (articulos: ArticuloResponse[]) => {
          // Filtrar artículos que tienen imágenes válidas y tomar los primeros 8
          const articulosConImagen = articulos
            .filter(articulo => articulo.imagenUrl && articulo.imagenUrl.trim() !== '')
            .slice(0, 8);
          
          if (articulosConImagen.length > 0) {
            this.galleryItems = articulosConImagen.map(articulo => ({
              id: articulo.idArticulo,
              nombre: articulo.nombre,
              precio: articulo.precio,
              imagenUrl: articulo.imagenUrl,
              nombreProductor: articulo.nombreProductor,
              nombreCategoria: articulo.nombreCategoria
            }));
            
            // Actualizar images array para compatibilidad con template
            this.images = this.galleryItems.map(item => item.imagenUrl);
          } else {
            // Si no hay artículos con imágenes, usar imágenes por defecto
            this.galleryItems = this.defaultImages.map((img, index) => ({
              id: index + 1,
              nombre: `Producto ${index + 1}`,
              precio: 0,
              imagenUrl: img,
              nombreProductor: 'Productor Local',
              nombreCategoria: 'Productos Frescos'
            }));
            
            this.images = this.defaultImages;
          }
        },
        error: (err) => {
          console.error('Error al cargar artículos:', err);
          this.error = true;
          
          // Fallback a imágenes por defecto en caso de error
          this.galleryItems = this.defaultImages.map((img, index) => ({
            id: index + 1,
            nombre: `Producto ${index + 1}`,
            precio: 0,
            imagenUrl: img,
            nombreProductor: 'Productor Local',
            nombreCategoria: 'Productos Frescos'
          }));
          
          this.images = this.defaultImages;
        }
      });
  }

  scroll(direction: number): void {
    const el = this.track?.nativeElement;
    if (!el) return;
    
    const totalSlides = this.galleryItems.length;
    if (totalSlides === 0) return;
    
    // Actualizar índice
    this.index += direction;
    
    // Loop infinito
    if (this.index < 0) {
      this.index = totalSlides - 1;
    } else if (this.index >= totalSlides) {
      this.index = 0;
    }
    
    this.updateGallery();
  }

  goToSlide(targetIndex: number): void {
    this.index = targetIndex;
    this.updateGallery();
  }

  private updateGallery(): void {
    const el = this.track?.nativeElement;
    if (!el) return;
    
    const cardWidth = this.getCardWidth(el);
    const offset = -(this.index * cardWidth);
    
    el.style.transform = `translateX(${offset}px)`;
  }

  private getCardWidth(el: HTMLElement): number {
    const first = el.querySelector<HTMLElement>('.gallery__item');
    if (!first) return 328;
    
    const style = getComputedStyle(el);
    const gap = parseFloat(style.columnGap || style.gap || '32') || 32;
    return first.getBoundingClientRect().width + gap;
  }

  /**
   * Maneja el error cuando una imagen no se puede cargar.
   */
  onImageError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    if (imgElement && !imgElement.src.includes('placeholder-product.svg')) {
      imgElement.src = '/images/placeholder-product.svg';
    }
  }

  reloadArticulos(): void {
    this.loadArticulos();
  }
}
