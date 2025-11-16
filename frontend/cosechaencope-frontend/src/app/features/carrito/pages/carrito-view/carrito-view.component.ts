import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CarritoService } from '../../../../core/services/carrito.service';
import { CarritoResponse, CarritoInvitadoItem } from '../../../../shared/models/carrito.models';
import { ArticuloService } from '../../../../core/services/articulo.service';
import { forkJoin } from 'rxjs';

/**
 * Componente de vista del carrito mejorado con soporte para invitados
 *
 */
@Component({
  selector: 'app-carrito-view',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './carrito-view.component.html',
  styleUrls: ['./carrito-view.component.scss'],
})
export class CarritoViewComponent implements OnInit {
  // Inyección de dependencias
  private carritoService = inject(CarritoService);
  private articuloService = inject(ArticuloService);
  private router = inject(Router);

  // Estado del componente
  carrito: CarritoResponse | null = null;
  loading = false;
  error: string | null = null;
  isUsuarioInvitado = false;

  ngOnInit(): void {
    this.verificarTipoUsuario();
    this.cargarCarrito();
  }

  /**
   * Verifica si el usuario está autenticado o es invitado
   */
  private verificarTipoUsuario(): void {
    const token = sessionStorage.getItem('authToken');
    this.isUsuarioInvitado = !token;
  }

  /**
   * Carga el carrito desde el servicio (autenticado o invitado)
   */
  cargarCarrito(): void {
    this.loading = true;
    this.error = null;

    if (this.isUsuarioInvitado) {
      // Usuario invitado: cargar desde localStorage y enriquecer con datos del backend
      this.cargarCarritoInvitado();
    } else {
      // Usuario autenticado: cargar desde backend
      this.carritoService.obtenerCarrito().subscribe({
        next: (carrito) => {
          this.carrito = carrito;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Error al cargar el carrito. Intenta nuevamente.';
          this.loading = false;
          console.error(err);
        },
      });
    }
  }


  /**
   * Carga el carrito invitado desde localStorage y obtiene datos de artículos
   * usando el endpoint batch para optimizar el número de peticiones HTTP
   */
  private cargarCarritoInvitado(): void {
    const items = this.carritoService.obtenerCarritoInvitado();

    // Validación: carrito vacío
    if (items.length === 0) {
      this.carrito = null;
      this.loading = false;
      return;
    }

    const ids = items.map((item) => item.idArticulo);

    this.articuloService.getArticulosPorIds(ids).subscribe({
      next: (articulos) => {
        // Crear un Map para lookup O(1) en lugar de búsqueda lineal O(n)
        const articulosMap = new Map(articulos.map((art) => [art.idArticulo, art]));

        // Construir CarritoResponse con datos enriquecidos
        this.carrito = {
          id: 0,
          items: items
            .map((item, index) => {
              const articulo = articulosMap.get(item.idArticulo);

              // Validación: artículo no encontrado (eliminado de BD)
              if (!articulo) {
                console.warn(`Artículo ${item.idArticulo} no encontrado`);
                return null;
              }

              return {
                id: index,
                idArticulo: item.idArticulo,
                nombreArticulo: articulo.nombre,
                cantidad: item.cantidad,
                precioUnitario: articulo.precio,
                totalLinea: articulo.precio * item.cantidad,
                imagenUrl: articulo.imagenUrl,
                stockDisponible: articulo.stock, // Incluir stock para validación
              };
            })
            .filter((item) => item !== null), // Eliminar items null
          subtotal: 0,
          impuestos: 0,
          gastosEnvio: 0,
          total: 0,
        };

        // Calcular totales
        this.carrito.subtotal = this.carrito.items.reduce((sum, item) => sum + item.totalLinea, 0);
        this.carrito.impuestos = this.carrito.subtotal * 0.21; // IVA 21%
        this.carrito.gastosEnvio = this.carrito.subtotal > 50 ? 0 : 4.99;
        this.carrito.total =
          this.carrito.subtotal + this.carrito.impuestos + this.carrito.gastosEnvio;

        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar detalles del carrito.';
        this.loading = false;
        console.error('Error cargando carrito invitado:', err);
      },
    });
  }

  /**
   * Incrementa la cantidad de un artículo
   */
  incrementarItem(idArticulo: number): void {
    // Verificar stock disponible antes de incrementar
    const item = this.carrito?.items.find((i) => i.idArticulo === idArticulo);
    if (item && !this.tieneStockDisponible(item)) {
      this.error = `No puedes agregar más unidades. Stock máximo: ${item.stockDisponible}`;
      setTimeout(() => (this.error = null), 3000); // Limpiar error después de 3 segundos
      return;
    }

    if (this.isUsuarioInvitado) {
      // Usuario invitado: actualizar localStorage
      const items = this.carritoService.obtenerCarritoInvitado();
      const itemInvitado = items.find((i) => i.idArticulo === idArticulo);
      if (itemInvitado) {
        itemInvitado.cantidad++;
        localStorage.setItem('carritoInvitado', JSON.stringify(items));
        this.cargarCarritoInvitado();
      }
    } else {
      // Usuario autenticado: llamar al backend
      this.carritoService.agregarItem(idArticulo, 1).subscribe({
        next: (carrito) => {
          this.carrito = carrito;
          this.error = null; // Limpiar error si había
        },
        error: (err) => {
          // Extraer mensaje de error del backend
          const mensajeError = err.error || 'Error al actualizar la cantidad.';
          this.error = mensajeError;
          console.error('Error al incrementar item:', err);
          
          // Recargar carrito para reflejar estado actual
          this.cargarCarrito();
        },
      });
    }
  }

  /**
   * Decrementa la cantidad de un artículo
   */
  decrementarItem(idArticulo: number): void {
    if (this.isUsuarioInvitado) {
      // Usuario invitado: actualizar localStorage
      this.carritoService.decrementarItemInvitado(idArticulo);
      this.cargarCarritoInvitado();
    } else {
      // Usuario autenticado: llamar al backend
      this.carritoService.decrementarItem(idArticulo).subscribe({
        next: (carrito) => {
          this.carrito = carrito;
        },
        error: (err) => {
          this.error = 'Error al actualizar la cantidad.';
          console.error(err);
        },
      });
    }
  }

  /**
   * Vacía todo el carrito (con confirmación)
   */
  vaciarCarrito(): void {
    if (confirm('¿Estás seguro de que deseas vaciar el carrito?')) {
      if (this.isUsuarioInvitado) {
        // Usuario invitado: limpiar localStorage
        this.carritoService.vaciarCarritoInvitado();
        this.carrito = null;
      } else {
        // Usuario autenticado: llamar al backend
        this.carritoService.vaciarCarrito().subscribe({
          next: () => {
            this.carrito = null;
          },
          error: (err) => {
            this.error = 'Error al vaciar el carrito.';
            console.error(err);
          },
        });
      }
    }
  }

  /**
   * Navega a la página de checkout (requiere autenticación)
   */
  irACheckout(): void {
    if (this.carrito && this.carrito.items.length > 0) {
      if (this.isUsuarioInvitado) {
        // Usuario invitado: redirigir a login con mensaje
        if (confirm('Debes iniciar sesión para continuar con la compra. ¿Deseas ir al login?')) {
          this.router.navigate(['/auth']);
        }
      } else {
        // Usuario autenticado: ir al checkout
        this.router.navigate(['/app/checkout']);
      }
    }
  }

  /**
   * Calcula el número total de items en el carrito
   */
  get totalItems(): number {
    return this.carrito?.items.reduce((sum, item) => sum + item.cantidad, 0) || 0;
  }

  /**
   * Verifica si el carrito está vacío
   */
  get carritoVacio(): boolean {
    return !this.carrito || this.carrito.items.length === 0;
  }

  /**
   * Verifica si hay stock disponible para incrementar un artículo
   */
  tieneStockDisponible(item: any): boolean {
    if (!item.stockDisponible) return true; // Si no hay info de stock, permitir (caso invitado)
    return item.cantidad < item.stockDisponible;
  }

  /**
   * Obtiene el mensaje de error de stock si corresponde
   */
  obtenerMensajeStock(item: any): string | null {
    if (!item.stockDisponible) return null;
    if (item.cantidad >= item.stockDisponible) {
      return 'Stock insuficiente';
    }
    return null;
  }
}
