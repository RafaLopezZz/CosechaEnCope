import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable, map } from 'rxjs';
import { Router } from '@angular/router';

// Servicios y Modelos
import { OrdenesVentaService } from '../../../core/services/orden-venta.service';
import { UserStoreService } from '../../../core/services/user-store.service';
import { ProductorService } from '../../../core/services/productor.service';
import { OrdenVentaProductor, EstadoOrdenVenta } from '../../../shared/models/ovp.models';

@Component({
  selector: 'app-ordenes-productor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ovp.component.html',
  styleUrls: ['./ovp.component.scss'],
})
export class OrdenesProductorComponent implements OnInit {
  private ordenesService = inject(OrdenesVentaService);
  private userStore = inject(UserStoreService);
  private productorService = inject(ProductorService);
  private router = inject(Router);

  // Exponemos el Enum al template para comparaciones
  EstadoOrden = EstadoOrdenVenta;

  // Observables del Store
  ordenes$ = this.ordenesService.ordenes$;
  loading$ = this.ordenesService.loading$;

  // Estado local
  idProductor: number | null = null;
  filtroEstado: string = 'TODOS';

  goBack(): void {
    this.router.navigate(['/productor/dashboard']);
  }

  ngOnInit() {
    this.inicializarDatos();
  }

  private inicializarDatos() {
    const user = this.userStore.snapshot();
    if (!user) return;

    // 1. Obtenemos el ID del Productor basado en el Usuario logueado
    this.productorService
      .getProductorPorUsuario(user.idUsuario)
      .pipe(
        map((productor) => {
          this.idProductor = productor.idProductor;
          return productor.idProductor;
        })
      )
      .subscribe((idProd) => {
        // 2. Cargamos las órdenes en el Store
        this.ordenesService.cargarOrdenes(idProd);
      });
  }

  /**
   * Cambia el estado de una orden.
   * La UI se actualizará instantáneamente gracias al servicio optimista.
   */
  cambiarEstado(orden: OrdenVentaProductor, nuevoEstado: EstadoOrdenVenta) {
    if (!this.idProductor) return;

    // Confirmación simple (opcional, podrías usar un modal más bonito)
    if (!confirm(`¿Estás seguro de cambiar el estado a ${this.getEstadoLabel(nuevoEstado)}?`)) return;

    this.ordenesService
      .actualizarEstado(
        this.idProductor,
        orden.idOvp,
        nuevoEstado,
        `Actualización manual desde panel a las ${new Date().toLocaleTimeString()}`
      )
      .subscribe({
        next: () => console.log('Estado actualizado correctamente'),
        error: (err) => alert('Error al actualizar: ' + err), // Feedback básico de error
      });
  }

  /**
   * Filtra las órdenes en el template (Pipe manual simple)
   */
  filtrarOrdenes(ordenes: OrdenVentaProductor[]): OrdenVentaProductor[] {
    if (this.filtroEstado === 'TODOS') return ordenes;
    return ordenes.filter((o) => o.estado === this.filtroEstado);
  }

  verDetalle(idOvp: number) {
    this.router.navigate(['/productor/pedidos', idOvp]);
  }

  /**
   * Obtiene el nombre del cliente de la orden
   */
  getClienteNombre(orden: OrdenVentaProductor): string {
    if (orden.pedidoCliente?.cliente) {
      const cliente = orden.pedidoCliente.cliente;
      const nombreCompleto = cliente.apellidos 
        ? `${cliente.nombre} ${cliente.apellidos}` 
        : cliente.nombre;
      return nombreCompleto || 'Cliente';
    }
    return 'Cliente Final';
  }

  /**
   * Helper para obtener la etiqueta legible del estado
   */
  getEstadoLabel(estado: string): string {
    switch (estado) {
      case 'PENDIENTE': return 'Pendiente';
      case 'EN_PROCESO': return 'En Proceso';
      case 'ENVIADA': return 'Enviado';
      case 'ENTREGADA': return 'Entregado';
      case 'CANCELADA': return 'Cancelado';
      default: return estado;
    }
  }

  /**
   * Helper para clases CSS de las badges
   */
  getBadgeClass(estado: string): string {
    switch (estado) {
      case 'PENDIENTE':
        return 'estado-pendiente';
      case 'EN_PROCESO':
        return 'estado-preparacion';
      case 'ENVIADA':
        return 'estado-enviado';
      case 'ENTREGADA':
        return 'estado-entregado';
      case 'CANCELADA':
        return 'estado-cancelado';
      default:
        return '';
    }
  }

  getIconClass(estado: string): string {
    switch (estado) {
      case 'PENDIENTE':
        return 'fa-clock';
      case 'EN_PROCESO':
        return 'fa-box-open';
      case 'ENVIADA':
        return 'fa-truck';
      case 'ENTREGADA':
        return 'fa-check-circle';
      case 'CANCELADA':
        return 'fa-times-circle';
      default:
        return 'fa-circle';
    }
  }
}
