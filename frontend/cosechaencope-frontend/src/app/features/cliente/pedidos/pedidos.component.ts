import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UserStoreService } from '../../../core/services/user-store.service';
import { PedidoService } from '../../../core/services/pedido.service';
import { PedidoResponse, EstadoPedido } from '../../../shared/models/pedido.models';

@Component({
  selector: 'app-pedidos-cliente',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './pedidos.component.html',
  styleUrls: ['./pedidos.component.scss']
})
export class PedidosClienteComponent implements OnInit {
  private userStore = inject(UserStoreService);
  private pedidoService = inject(PedidoService);
  private router = inject(Router);

  pedidos: PedidoResponse[] = [];
  pedidosFiltrados: PedidoResponse[] = [];
  loading = true;
  filtroEstado: string = 'TODOS';

  currentUser = this.userStore.snapshot();

  readonly estadosPedido = [
    { value: 'TODOS', label: 'Todos los pedidos' },
    { value: EstadoPedido.PENDIENTE, label: 'Pendiente' },
    { value: EstadoPedido.CONFIRMADO, label: 'Confirmado' },
    { value: EstadoPedido.EN_PREPARACION, label: 'En Preparación' },
    { value: EstadoPedido.ENVIADO, label: 'Enviado' },
    { value: EstadoPedido.ENTREGADO, label: 'Entregado' },
    { value: EstadoPedido.CANCELADO, label: 'Cancelado' }
  ];

  ngOnInit() {
    if (!this.currentUser || this.currentUser.tipoUsuario !== 'CLIENTE') {
      this.router.navigateByUrl('/cliente/login');
      return;
    }

    this.loadPedidos();
  }

  loadPedidos() {
    this.pedidoService.getPedidosUsuario().subscribe({
      next: (pedidos) => {
        this.pedidos = pedidos;
        this.aplicarFiltro();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar pedidos:', error);
        this.loading = false;
      }
    });
  }

  aplicarFiltro() {
    if (this.filtroEstado === 'TODOS') {
      this.pedidosFiltrados = [...this.pedidos];
    } else {
      this.pedidosFiltrados = this.pedidos.filter(p => p.estadoPedido === this.filtroEstado);
    }
  }

  onFiltroChange() {
    this.aplicarFiltro();
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

  getEstadoIcon(estado: string): string {
    switch(estado) {
      case 'PENDIENTE': return 'fa-clock';
      case 'CONFIRMADO': return 'fa-check';
      case 'EN_PREPARACION': return 'fa-box';
      case 'ENVIADO': return 'fa-truck';
      case 'ENTREGADO': return 'fa-check-circle';
      case 'CANCELADO': return 'fa-times-circle';
      default: return 'fa-question';
    }
  }

  verDetalle(idPedido: number) {
    this.router.navigate(['/cliente/pedidos', idPedido]);
  }

  goBack() {
    this.router.navigateByUrl('/cliente/dashboard');
  }
}
