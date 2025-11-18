import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { UserStoreService } from '../../../core/services/user-store.service';
import { ClienteService, ClienteResponse } from '../../../core/services/cliente.service';
import { PedidoService } from '../../../core/services/pedido.service';
import { AuthService } from '../../../core/services/auth.service';
import { PedidoResponse } from '../../../shared/models/pedido.models';

interface EstadisticasCliente {
  totalPedidos: number;
  pedidosPendientes: number;
  pedidosEntregados: number;
  totalGastado: number;
  ultimoPedido?: PedidoResponse;
}

@Component({
  selector: 'app-dashboard-cliente',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardClienteComponent implements OnInit {
  private userStore = inject(UserStoreService);
  private clienteService = inject(ClienteService);
  private pedidoService = inject(PedidoService);
  private authService = inject(AuthService);
  private router = inject(Router);

  cliente: ClienteResponse | null = null;
  estadisticas: EstadisticasCliente | null = null;
  pedidosRecientes: PedidoResponse[] = [];
  loading = true;
  sidebarCollapsed = false;

  currentUser = this.userStore.snapshot();

  ngOnInit() {
    if (!this.currentUser || this.currentUser.tipoUsuario !== 'CLIENTE') {
      this.router.navigateByUrl('/cliente/login');
      return;
    }

    this.loadClienteData();
  }

  loadClienteData() {
    if (!this.currentUser) return;

    this.clienteService.getClientePorUsuario(this.currentUser.idUsuario).subscribe({
      next: (cliente) => {
        this.cliente = cliente;
        this.loadPedidos();
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  loadPedidos() {
    this.pedidoService.getPedidosUsuario().subscribe({
      next: (pedidos) => {
        this.pedidosRecientes = pedidos.slice(0, 5); // Últimos 5 pedidos
        this.calcularEstadisticas(pedidos);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  calcularEstadisticas(pedidos: PedidoResponse[]) {
    this.estadisticas = {
      totalPedidos: pedidos.length,
      pedidosPendientes: pedidos.filter(p => 
        p.estadoPedido === 'PENDIENTE' || 
        p.estadoPedido === 'CONFIRMADO' || 
        p.estadoPedido === 'EN_PREPARACION' ||
        p.estadoPedido === 'ENVIADO'
      ).length,
      pedidosEntregados: pedidos.filter(p => p.estadoPedido === 'ENTREGADO').length,
      totalGastado: pedidos
        .filter(p => p.estadoPedido !== 'CANCELADO')
        .reduce((sum, p) => sum + p.total, 0),
      ultimoPedido: pedidos.length > 0 ? pedidos[0] : undefined
    };
  }

  toggleSidebar() {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  getEstadoClass(estado: string): string {
    switch(estado) {
      case 'PENDIENTE': return 'estado-pendiente';
      case 'CONFIRMADO': return 'estado-confirmado';
      case 'EN_PREPARACION': return 'estado-preparacion';
      case 'ENVIADO': return 'estado-enviado';
      case 'ENTREGADO': return 'estado-entregado';
      case 'CANCELADO': return 'estado-cancelado';
      default: return '';
    }
  }

  getEstadoLabel(estado: string): string {
    switch(estado) {
      case 'PENDIENTE': return 'Pendiente';
      case 'CONFIRMADO': return 'Confirmado';
      case 'EN_PREPARACION': return 'En Preparación';
      case 'ENVIADO': return 'Enviado';
      case 'ENTREGADO': return 'Entregado';
      case 'CANCELADO': return 'Cancelado';
      default: return estado;
    }
  }

  logout() {
    this.authService.logout();
  }
}
