import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { PedidoService } from '../../../../core/services/pedido.service';
import { CarritoService } from '../../../../core/services/carrito.service';
import { UserStoreService } from '../../../../core/services/user-store.service';
import { CheckoutStep, CheckoutState } from '../../../../shared/models/pedido.models';
import { CarritoResponse } from '../../../../shared/models/carrito.models';
import { CheckoutSummaryComponent } from '../../components/checkout-summary/checkout-summary.component';
import { CheckoutFormComponent } from '../../components/checkout-form/checkout-form.component';
import { CheckoutPaymentComponent } from '../../components/checkout-payment/checkout-payment.component';
import { OrderConfirmationComponent } from '../../components/order-confirmation/order-confirmation.component';

/**
 * Componente principal del proceso de checkout
 * 
 * CARACTERÍSTICAS:
 * ✅ Flujo de checkout en pasos progresivos
 * ✅ Validación de autenticación en paso de datos de envío
 * ✅ Validación de carrito no vacío
 * ✅ Navegación entre pasos con validación
 * ✅ Indicador de progreso visual
 * ✅ Responsive y accesible (WCAG AA)
 * 
 * FLUJO:
 * 1. RESUMEN → Revisar items del carrito
 * 2. DATOS → Confirmar/editar datos del cliente
 *    - Verificación de autenticación (modal login/registro si no está autenticado)
 * 3. PAGO → Seleccionar método de pago
 * 4. CONFIRMACION → Mostrar pedido creado
 * 
 * @author rafalopezzz
 */
@Component({
  selector: 'app-checkout-page',
  standalone: true,
  imports: [
    CommonModule,
    CheckoutSummaryComponent,
    CheckoutFormComponent,
    CheckoutPaymentComponent,
    OrderConfirmationComponent
  ],
  templateUrl: './checkout-page.component.html',
  styleUrl: './checkout-page.component.scss'
})
export class CheckoutPageComponent implements OnInit, OnDestroy {
  private pedidoService = inject(PedidoService);
  private carritoService = inject(CarritoService);
  private userStore = inject(UserStoreService);
  private router = inject(Router);

  // Referencias a enums para usar en template
  readonly CheckoutStep = CheckoutStep;

  // Estado reactivo
  checkoutState: CheckoutState | null = null;
  carrito: CarritoResponse | null = null;
  isLoadingCarrito = true;

  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    // Inicializar checkout (sin verificación de autenticación al inicio)
    // La autenticación se verificará en el paso de "Datos de envío"
    this.pedidoService.iniciarCheckout();

    // Suscribirse al estado del checkout
    this.pedidoService.checkoutState$
      .pipe(takeUntil(this.destroy$))
      .subscribe(state => {
        this.checkoutState = state;
        console.log('[CheckoutPage] Estado actualizado:', state.step);
      });

    // Cargar carrito actual
    this.carritoService.obtenerCarrito()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (carrito) => {
          this.carrito = carrito;
          this.isLoadingCarrito = false;

          // Validar que el carrito no esté vacío
          if (!carrito || carrito.items.length === 0) {
            console.warn('[CheckoutPage] Carrito vacío, redirigiendo');
            alert('Tu carrito está vacío. Agrega productos antes de continuar.');
            this.router.navigate(['/articulos']);
          }
        },
        error: (err) => {
          console.error('[CheckoutPage] Error al cargar carrito:', err);
          this.isLoadingCarrito = false;
          alert('Error al cargar el carrito. Intenta de nuevo.');
          this.router.navigate(['/carrito']);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Obtener el porcentaje de progreso según el paso actual
   */
  getProgressPercentage(): number {
    if (!this.checkoutState) return 0;
    
    const steps = [
      CheckoutStep.RESUMEN,
      CheckoutStep.DATOS,
      CheckoutStep.PAGO,
      CheckoutStep.CONFIRMACION
    ];
    
    const currentIndex = steps.indexOf(this.checkoutState.step);
    return ((currentIndex + 1) / steps.length) * 100;
  }

  /**
   * Obtener el número del paso actual (1-4)
   */
  getStepNumber(): number {
    if (!this.checkoutState) return 1;
    
    const steps = [
      CheckoutStep.RESUMEN,
      CheckoutStep.DATOS,
      CheckoutStep.PAGO,
      CheckoutStep.CONFIRMACION
    ];
    
    return steps.indexOf(this.checkoutState.step) + 1;
  }

  /**
   * Obtener el título del paso actual
   */
  getStepTitle(): string {
    if (!this.checkoutState) return '';
    
    const titles = {
      [CheckoutStep.RESUMEN]: 'Resumen del Pedido',
      [CheckoutStep.DATOS]: 'Datos de Envío',
      [CheckoutStep.PAGO]: 'Método de Pago',
      [CheckoutStep.CONFIRMACION]: '¡Pedido Confirmado!'
    };
    
    return titles[this.checkoutState.step] || '';
  }

  /**
   * Cancelar checkout y volver al carrito
   */
  cancelarCheckout(): void {
    if (this.checkoutState?.step === CheckoutStep.CONFIRMACION) {
      // Si ya está confirmado, ir al home
      this.router.navigate(['/']);
    } else {
      // Si no, preguntar antes de cancelar
      if (confirm('¿Estás seguro de que deseas cancelar el proceso de compra?')) {
        this.pedidoService.cancelarCheckout();
        this.router.navigate(['/carrito']);
      }
    }
  }

  /**
   * Verificar si se puede retroceder al paso anterior
   */
  canGoBack(): boolean {
    return this.checkoutState?.step !== CheckoutStep.RESUMEN 
        && this.checkoutState?.step !== CheckoutStep.CONFIRMACION;
  }

  /**
   * Retroceder al paso anterior
   */
  goBack(): void {
    if (this.canGoBack()) {
      this.pedidoService.retrocederPaso();
    }
  }
}
