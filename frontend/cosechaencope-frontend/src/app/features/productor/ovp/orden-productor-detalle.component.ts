import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { Observable, map, switchMap } from 'rxjs';

// Servicios
import { OrdenesVentaService } from '../../../core/services/orden-venta.service';
import { ProductorService } from '../../../core/services/productor.service';
import { UserStoreService } from '../../../core/services/user-store.service';

// Modelos y Enums
import { OrdenVentaProductor, EstadoOrdenVenta } from '../../../shared/models/ovp.models';

@Component({
  selector: 'app-orden-productor-detalle',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './orden-productor-detalle.component.html',
  styleUrls: ['./orden-productor-detalle.component.scss'],
})
export class OrdenProductorDetalleComponent implements OnInit {
  
  // Inyección de dependencias
  private ordenesService = inject(OrdenesVentaService);
  private productorService = inject(ProductorService);
  private userStore = inject(UserStoreService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  // Exponemos el Enum al HTML para poder hacer comparaciones (ej: orden.estado === EstadoOrden.PENDIENTE)
  protected readonly EstadoOrden = EstadoOrdenVenta;

  // Observables del Store (Reactividad)
  ordenes$: Observable<OrdenVentaProductor[]> = this.ordenesService.ordenes$;
  loading$: Observable<boolean> = this.ordenesService.loading$;

  // Orden actualmente seleccionada (traída desde la ruta + store)
  ordenSeleccionada$: Observable<OrdenVentaProductor | undefined> = this.route.paramMap.pipe(
    map((params) => Number(params.get('id'))),
    switchMap((id) => this.ordenesService.getOrdenPorId(id))
  );

  // Estado local del componente
  idProductor: number | null = null;
  filtroEstado: string = 'TODOS';
  showBackButton: boolean = false; // Configurable según necesites

  ngOnInit(): void {
    this.cargarDatosIniciales();
  }

  /**
   * 1. Identifica al usuario logueado
   * 2. Obtiene su perfil de Productor
   * 3. Carga las órdenes asociadas
   */
  private cargarDatosIniciales(): void {
    const user = this.userStore.snapshot();
    
    if (user && user.tipoUsuario === 'PRODUCTOR') {
      this.productorService.getProductorPorUsuario(user.idUsuario).subscribe({
        next: (productor) => {
          this.idProductor = productor.idProductor;
          // Disparamos la carga de órdenes en el servicio
          this.ordenesService.cargarOrdenes(this.idProductor);
        },
        error: (err) => console.error('Error al obtener perfil de productor', err)
      });
    } else {
      // Redirigir si no es productor (seguridad básica)
      this.router.navigate(['/auth/login']);
    }
  }

  /**
   * Lógica de filtrado visual.
   * Se ejecuta en el HTML sobre la lista 'ordenes'.
   */
  filtrarOrdenes(ordenes: OrdenVentaProductor[]): OrdenVentaProductor[] {
    if (this.filtroEstado === 'TODOS') {
      return ordenes;
    }
    return ordenes.filter(o => o.estado === this.filtroEstado);
  }

  /**
   * Navegación al detalle
   */
  verDetalle(idOvp: number): void {
    this.router.navigate(['/productor/pedidos', idOvp]);
  }

  /**
   * Acción Rápida: Cambiar estado desde la lista
   */
  cambiarEstado(orden: OrdenVentaProductor, nuevoEstado: EstadoOrdenVenta): void {
    if (!this.idProductor) return;

    // Opcional: Confirmación visual
    // if (!confirm(`¿Cambiar estado a ${nuevoEstado}?`)) return;

    this.ordenesService.actualizarEstado(
      this.idProductor,
      orden.idOvp,
      nuevoEstado,
      'Cambio rápido desde listado' // Observación automática
    ).subscribe({
      next: () => {
        // No hace falta hacer nada aquí, el servicio actualiza la UI automáticamente
        // gracias al BehaviorSubject
      },
      error: (err) => {
        alert('No se pudo actualizar el estado: ' + err);
      }
    });
  }

  goBack(): void {
    // Lógica para volver al dashboard principal si se requiere
    this.router.navigate(['/productor/dashboard']);
  }

  /**
   * Devuelve el porcentaje de progreso visual según el estado.
   */
  getProgressPercentage(estado: string | EstadoOrdenVenta): number {
    const steps = [
      EstadoOrdenVenta.PENDIENTE,
      EstadoOrdenVenta.EN_PROCESO,
      EstadoOrdenVenta.ENVIADA,
      EstadoOrdenVenta.ENTREGADA,
    ];
    const idx = steps.indexOf(estado as EstadoOrdenVenta);
    if (idx <= 0) return 0;
    return (idx / (steps.length - 1)) * 100;
  }

  /**
   * Determina si un paso del tracking está activo según el estado actual.
   */
  isStepActive(currentEstado: string | EstadoOrdenVenta, stepEstado: EstadoOrdenVenta): boolean {
    const steps = [
      EstadoOrdenVenta.PENDIENTE,
      EstadoOrdenVenta.EN_PROCESO,
      EstadoOrdenVenta.ENVIADA,
      EstadoOrdenVenta.ENTREGADA,
    ];
    const currentIdx = steps.indexOf(currentEstado as EstadoOrdenVenta);
    const stepIdx = steps.indexOf(stepEstado);
    return currentIdx >= stepIdx;
  }

  // ==========================================
  // HELPERS DE ESTILO (Para el HTML)
  // ==========================================

  /**
   * Mapea el Enum de Estado a las clases CSS que definiste en el SCSS
   */
  getBadgeClass(estado: string): string {
    switch (estado) {
      case EstadoOrdenVenta.PENDIENTE: return 'estado-pendiente';
      case EstadoOrdenVenta.EN_PROCESO: return 'estado-preparacion'; // Mapeado a tu clase existente
      case EstadoOrdenVenta.ENVIADA: return 'estado-enviado';
      case EstadoOrdenVenta.ENTREGADA: return 'estado-entregado';
      case EstadoOrdenVenta.CANCELADA: return 'estado-cancelado';
      default: return '';
    }
  }

  /**
   * Devuelve el icono FontAwesome correspondiente
   */
  getIconClass(estado: string): string {
    switch (estado) {
      case EstadoOrdenVenta.PENDIENTE: return 'fa-clock';
      case EstadoOrdenVenta.EN_PROCESO: return 'fa-box-open';
      case EstadoOrdenVenta.ENVIADA: return 'fa-truck';
      case EstadoOrdenVenta.ENTREGADA: return 'fa-check-circle';
      case EstadoOrdenVenta.CANCELADA: return 'fa-times-circle';
      default: return 'fa-circle';
    }
  }
}