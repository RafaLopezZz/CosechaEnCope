import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CarritoResponse } from '../../../../shared/models/carrito.models';
import { CarritoService } from '../../../../core/services/carrito.service';
import { PedidoService } from '../../../../core/services/pedido.service';
import { CheckoutStep } from '../../../../shared/models/pedido.models';

/**
 * Componente de resumen del carrito en checkout
 * 
 * Muestra los items del carrito con opciones para:
 * - Ver detalles de cada artículo
 * - Editar cantidades inline
 * - Eliminar items
 * - Ver totales (subtotal, IVA, envío, total)
 * 
 * @author rafalopezzz
 */
@Component({
  selector: 'app-checkout-summary',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './checkout-summary.component.html',
  styleUrl: './checkout-summary.component.scss'
})
export class CheckoutSummaryComponent {
  @Input({ required: true }) carrito!: CarritoResponse;

  private carritoService = inject(CarritoService);
  private pedidoService = inject(PedidoService);

  isUpdating = false;

  /**
   * Decrementar cantidad de un item
   */
  decrementarItem(idArticulo: number): void {
    this.isUpdating = true;
    this.carritoService.decrementarItem(idArticulo).subscribe({
      next: (carritoActualizado) => {
        this.carrito = carritoActualizado;
        this.isUpdating = false;
      },
      error: (err) => {
        console.error('Error al decrementar item:', err);
        this.isUpdating = false;
        alert('Error al actualizar el carrito');
      }
    });
  }

  /**
   * Incrementar cantidad de un item
   */
  incrementarItem(idArticulo: number): void {
    this.isUpdating = true;
    this.carritoService.agregarItem(idArticulo, 1).subscribe({
      next: (carritoActualizado) => {
        if (carritoActualizado) {
          this.carrito = carritoActualizado;
        }
        this.isUpdating = false;
      },
      error: (err) => {
        console.error('Error al incrementar item:', err);
        this.isUpdating = false;
        alert(err.error?.message || 'Error al actualizar el carrito');
      }
    });
  }

  /**
   * Continuar al siguiente paso (Datos del cliente)
   */
  continuar(): void {
    if (this.carrito.items.length === 0) {
      alert('El carrito está vacío');
      return;
    }
    this.pedidoService.avanzarPaso(CheckoutStep.DATOS);
  }

  /**
   * Calcular el número total de items en el carrito
   */
  getTotalItems(): number {
    return this.carrito.items.reduce((sum, item) => sum + item.cantidad, 0);
  }
}
