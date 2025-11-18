import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { PedidoService } from '../../../../core/services/pedido.service';
import { MetodoPago } from '../../../../shared/models/pedido.models';

/**
 * Componente de selección de método de pago
 */
@Component({
  selector: 'app-checkout-payment',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './checkout-payment.component.html',
  styleUrl: './checkout-payment.component.scss'
})
export class CheckoutPaymentComponent {
  @Input({ required: true }) total!: number;

  private fb = inject(FormBuilder);
  private pedidoService = inject(PedidoService);

  readonly MetodoPago = MetodoPago;
  
  paymentForm = this.fb.group({
    metodoPago: [MetodoPago.TARJETA, Validators.required]
  });

  isProcessing = false;

  confirmarPedido(): void {
    if (this.paymentForm.invalid) return;

    const metodoPago = this.paymentForm.value.metodoPago!;
    this.isProcessing = true;

    this.pedidoService.guardarMetodoPago(metodoPago);
    this.pedidoService.crearPedido(metodoPago).subscribe({
      next: () => {
        this.isProcessing = false;
      },
      error: () => {
        this.isProcessing = false;
      }
    });
  }
}
