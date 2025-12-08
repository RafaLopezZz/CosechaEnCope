import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { UserStoreService } from '../../../core/services/user-store.service';
import { ProductorService } from '../../../core/services/productor.service';
import { OrdenesVentaService } from '../../../core/services/orden-venta.service';
import { AuthService } from '../../../core/services/auth.service';
import { ProductorResponse } from '../../../shared/models/productor.models';
import { ArticuloResponse } from '../../../shared/models/articulo.models';
import { OrdenVentaProductor, EstadoOrdenVenta } from '../../../shared/models/ovp.models';

interface EstadisticasProductor {
  totalProductos: number;
  productosActivos: number;
  totalOrdenes: number;
  ordenesPendientes: number;
  ordenesCompletadas: number;
  ingresosTotales: number;
  ingresosMes: number;
  ultimaOrden?: OrdenVentaProductor;
}

@Component({
  selector: 'app-dashboard-productor',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardProductorComponent implements OnInit {
  private userStore = inject(UserStoreService);
  private productorService = inject(ProductorService);
  private ordenesService = inject(OrdenesVentaService);
  private authService = inject(AuthService);
  private router = inject(Router);

  productor: ProductorResponse | null = null;
  estadisticas: EstadisticasProductor | null = null;
  ordenesRecientes: OrdenVentaProductor[] = [];
  articulosRecientes: ArticuloResponse[] = [];
  loading = true;
  sidebarCollapsed = false;

  currentUser = this.userStore.snapshot();

  ngOnInit() {
    if (!this.currentUser || this.currentUser.tipoUsuario !== 'PRODUCTOR') {
      this.router.navigateByUrl('/login/productores');
      return;
    }

    this.loadProductorData();
  }

  loadProductorData() {
    if (!this.currentUser) return;

    this.productorService.getProductorPorUsuario(this.currentUser.idUsuario).subscribe({
      next: (productor) => {
        this.productor = productor;
        this.loadDashboardData();
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  /**
   * Carga artículos y órdenes en paralelo para calcular estadísticas
   */
  loadDashboardData() {
    if (!this.productor) return;

    forkJoin({
      articulos: this.productorService.getArticulosProductor(this.productor.idProductor),
      ordenes: this.productorService.getPedidosProductor(this.productor.idProductor)
    }).subscribe({
      next: ({ articulos, ordenes }) => {
        this.articulosRecientes = articulos.slice(0, 5);
        this.ordenesRecientes = ordenes.slice(0, 5);
        this.calcularEstadisticas(articulos, ordenes);
        this.loading = false;
      },
      error: () => {
        // Si falla, intentamos cargar solo los artículos
        this.productorService.getArticulosProductor(this.productor!.idProductor).subscribe({
          next: (articulos) => {
            this.articulosRecientes = articulos.slice(0, 5);
            this.calcularEstadisticas(articulos, []);
            this.loading = false;
          },
          error: () => {
            this.loading = false;
          }
        });
      }
    });
  }

  /**
   * Calcula estadísticas localmente (igual que hace el dashboard de cliente)
   */
  calcularEstadisticas(articulos: ArticuloResponse[], ordenes: OrdenVentaProductor[]) {
    const ahora = new Date();
    const inicioMes = new Date(ahora.getFullYear(), ahora.getMonth(), 1);

    // Filtrar órdenes del mes actual
    const ordenesMes = ordenes.filter(o => {
      const fechaOrden = new Date(o.fechaCreacion);
      return fechaOrden >= inicioMes;
    });

    // Calcular ingresos (solo órdenes entregadas/completadas)
    const ordenesCompletadas = ordenes.filter(o => 
      o.estado === EstadoOrdenVenta.ENTREGADA
    );
    
    const ordenesMesCompletadas = ordenesMes.filter(o => 
      o.estado === EstadoOrdenVenta.ENTREGADA
    );

    this.estadisticas = {
      totalProductos: articulos.length,
      productosActivos: articulos.filter(a => a.stock > 0).length,
      totalOrdenes: ordenes.length,
      ordenesPendientes: ordenes.filter(o => 
        o.estado === EstadoOrdenVenta.PENDIENTE || 
        o.estado === EstadoOrdenVenta.EN_PROCESO
      ).length,
      ordenesCompletadas: ordenesCompletadas.length,
      ingresosTotales: ordenesCompletadas.reduce((sum, o) => sum + (o.total || 0), 0),
      ingresosMes: ordenesMesCompletadas.reduce((sum, o) => sum + (o.total || 0), 0),
      ultimaOrden: ordenes.length > 0 ? ordenes[0] : undefined
    };
  }

  toggleSidebar() {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  logout() {
    this.authService.logout();
  }
}