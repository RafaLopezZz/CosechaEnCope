import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { UserStoreService } from '../../../core/services/user-store.service';
import { PedidoService } from '../../../core/services/pedido.service';
import { PedidoResponse } from '../../../shared/models/pedido.models';

@Component({
  selector: 'app-pedido-detalle',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './pedido-detalle.component.html',
  styleUrls: ['./pedido-detalle.component.scss']
})
export class PedidoDetalleComponent implements OnInit {
  private userStore = inject(UserStoreService);
  private pedidoService = inject(PedidoService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  pedido: PedidoResponse | null = null;
  loading = true;
  error = false;

  currentUser = this.userStore.snapshot();

  ngOnInit() {
    if (!this.currentUser || this.currentUser.tipoUsuario !== 'CLIENTE') {
      this.router.navigateByUrl('/cliente/login');
      return;
    }

    const idPedido = Number(this.route.snapshot.paramMap.get('id'));
    if (idPedido) {
      this.loadPedido(idPedido);
    } else {
      this.router.navigateByUrl('/cliente/pedidos');
    }
  }

  loadPedido(idPedido: number) {
    this.pedidoService.getPedidoPorId(idPedido).subscribe({
      next: (pedido) => {
        this.pedido = pedido;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar pedido:', error);
        this.error = true;
        this.loading = false;
      }
    });
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

  getEstadoProgress(estado: string): number {
    switch(estado) {
      case 'PENDIENTE': return 20;
      case 'CONFIRMADO': return 40;
      case 'EN_PREPARACION': return 60;
      case 'ENVIADO': return 80;
      case 'ENTREGADO': return 100;
      case 'CANCELADO': return 0;
      default: return 0;
    }
  }

  goBack() {
    this.router.navigateByUrl('/cliente/pedidos');
  }
}
