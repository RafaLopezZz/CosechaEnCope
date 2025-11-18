import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PedidoService } from '../../../../core/services/pedido.service';
import { PedidoResponse } from '../../../../shared/models/pedido.models';

/**
 * Componente de confirmación de pedido
 */
@Component({
  selector: 'app-order-confirmation',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-confirmation.component.html',
  styleUrl: './order-confirmation.component.scss',
})
export class OrderConfirmationComponent implements OnInit {
  private pedidoService = inject(PedidoService);
  private router = inject(Router);

  pedido: PedidoResponse | null = null;

  ngOnInit(): void {
    this.pedidoService.ultimoPedido$.subscribe((pedido) => {
      this.pedido = pedido;
    });
  }

  irAlInicio(): void {
    this.router.navigate(['/']);
  }

  /**
   * Navega a la raíz del sitio (landing SSR)
   * Sale de la SPA Angular y carga la página Thymeleaf
   */
  navigateToRoot(event: Event): void {
    event.preventDefault();
    window.location.href = '/';
  }

  /**
   * Verifica si estamos en la ruta raíz
   */
  isRootPath(): boolean {
    return window.location.pathname === '/';
  }

  verPedidos(): void {
    this.router.navigate(['/cliente/pedidos']);
  }
}
