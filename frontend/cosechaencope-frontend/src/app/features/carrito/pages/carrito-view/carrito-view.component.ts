import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CarritoService } from '../../../../core/services/carrito.service';
import { CarritoResponse, CarritoInvitadoItem } from '../../../../shared/models/carrito.models';
import { ArticuloService } from '../../../../core/services/articulo.service';
import { forkJoin } from 'rxjs';

/**
 * Componente de vista del carrito mejorado con soporte para invitados
 * 
 * MEJORAS IMPLEMENTADAS:
 * ✅ Vista del carrito para usuarios autenticados (backend)
 * ✅ Vista del carrito para usuarios invitados (localStorage)
 * ✅ Modal de autenticación antes del checkout
 * ✅ Gestión de imágenes de artículos
 */
@Component({
  selector: 'app-carrito-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './carrito-view.component.html',
  styleUrls: ['./carrito-view.component.scss']
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
        }
      });
    }
  }

  /**
   * Carga el carrito invitado desde localStorage y obtiene datos de artículos
   */
  private cargarCarritoInvitado(): void {
    const items = this.carritoService.obtenerCarritoInvitado();
    
    if (items.length === 0) {
      this.carrito = null;
      this.loading = false;
      return;
    }

    // Obtener detalles de cada artículo desde el backend
    const requests = items.map(item => this.articuloService.getArticuloPorId(item.idArticulo));
    
    forkJoin(requests).subscribe({
      next: (articulos) => {
        // Construir CarritoResponse mock con los datos
        this.carrito = {
          id: 0,
          items: items.map((item, index) => ({
            id: index,
            idArticulo: item.idArticulo,
            nombreArticulo: articulos[index].nombre,
            cantidad: item.cantidad,
            precioUnitario: articulos[index].precio,
            totalLinea: articulos[index].precio * item.cantidad,
            imagenUrl: articulos[index].imagenUrl
          })),
          subtotal: 0,
          impuestos: 0,
          gastosEnvio: 0,
          total: 0
        };

        // Calcular totales
        this.carrito.subtotal = this.carrito.items.reduce((sum, item) => sum + item.totalLinea, 0);
        this.carrito.impuestos = this.carrito.subtotal * 0.21; // IVA 21%
        this.carrito.gastosEnvio = this.carrito.subtotal > 50 ? 0 : 4.99;
        this.carrito.total = this.carrito.subtotal + this.carrito.impuestos + this.carrito.gastosEnvio;

        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar detalles del carrito.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  /**
   * Incrementa la cantidad de un artículo
   */
  incrementarItem(idArticulo: number): void {
    if (this.isUsuarioInvitado) {
      // Usuario invitado: actualizar localStorage
      const items = this.carritoService.obtenerCarritoInvitado();
      const item = items.find(i => i.idArticulo === idArticulo);
      if (item) {
        item.cantidad++;
        localStorage.setItem('carritoInvitado', JSON.stringify(items));
        this.cargarCarritoInvitado();
      }
    } else {
      // Usuario autenticado: llamar al backend
      this.carritoService.agregarItem(idArticulo, 1).subscribe({
        next: (carrito) => {
          this.carrito = carrito;
        },
        error: (err) => {
          this.error = 'Error al actualizar la cantidad.';
          console.error(err);
        }
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
        }
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
          }
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
}
