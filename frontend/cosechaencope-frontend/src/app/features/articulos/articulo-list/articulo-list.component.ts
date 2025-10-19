import { Component, OnInit } from '@angular/core';
import { NavbarComponent } from '../../home/navbar/navbar.component';
import { FooterComponent } from '../../home/footer/footer.component';
import { ArticuloService } from '../../../core/services/articulo.service';
import { ArticuloResponse } from '../../../shared/models/articulo.models';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-articulo-list',
  imports: [CommonModule, FormsModule, NavbarComponent, FooterComponent],
  standalone: true,
  templateUrl: './articulo-list.component.html',
  styleUrls: ['./articulo-list.component.scss'],
})
export class ArticuloListComponent implements OnInit {
  articulos: ArticuloResponse[] = [];
  categoriaSeleccionada: string = '';

  get articulosFiltrados() {
    if (!this.categoriaSeleccionada) return this.articulos;
    return this.articulos.filter(
      (a) => String(a.idCategoria) === this.categoriaSeleccionada
    );
  }

  constructor(
    private articuloService: ArticuloService,
    private route: ActivatedRoute
  ) {}

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
        console.log(
          `Artículos de la categoría ${idCategoria} recibidos:`,
          data
        );
        this.articulos = data;
      },
      error: (err) =>
        console.error(
          `Error al cargar artículos de la categoría ${idCategoria}`,
          err
        ),
    });
  }

  /**
   * Maneja el error cuando una imagen no se puede cargar.
   * Establece una imagen por defecto SVG.
   */
  onImageError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    if (imgElement && !imgElement.src.includes('default-product.png')) {
      // Solo cambiar si no es ya la imagen por defecto (evitar bucle infinito)
      imgElement.src = '/app/assets/images/default-product.png';
    }
  }

  /**
   * Añade un artículo al carrito
   * TODO: Implementar funcionalidad de carrito completa
   */
  addToCart(articulo: ArticuloResponse): void {
    console.log('Añadiendo al carrito:', articulo);
    // TODO: Implementar lógica de carrito
    alert(`Producto "${articulo.nombre}" añadido al carrito`);
  }
}
