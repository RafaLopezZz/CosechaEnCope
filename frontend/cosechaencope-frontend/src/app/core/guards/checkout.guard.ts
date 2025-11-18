import { inject } from '@angular/core';
import { Router, type CanActivateFn } from '@angular/router';
import { map, take } from 'rxjs/operators';
import { CarritoService } from '../services/carrito.service';
import { UserStoreService } from '../services/user-store.service';

/**
 * Guard para proteger la ruta de checkout
 * 
 * Verifica:
 * 1. Usuario autenticado (token en sessionStorage)
 * 2. Usuario de tipo CLIENTE (no productores)
 * 3. Carrito no vacío (al menos 1 item)
 * 
 * Si alguna condición falla, redirige apropiadamente.
 * NOTA: La verificación de tipo CLIENTE ahora se maneja en CheckoutPageComponent
 *       con un modal de autenticación para mejor UX.
 * 
 * @author rafalopezzz
 */
export const checkoutGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const carritoService = inject(CarritoService);
  const userStore = inject(UserStoreService);

  // 1. Verificar autenticación básica
  const token = sessionStorage.getItem('authToken');
  const userData = userStore.snapshot();

  // Si no hay token O no hay datos de usuario O no es CLIENTE
  // Dejar pasar pero el componente mostrará el modal de auth
  if (!token || !userData || userData.tipoUsuario !== 'CLIENTE') {
    console.warn('[checkoutGuard] Usuario no autenticado o no es cliente');
    // Permitir acceso para que el componente muestre el modal
    return true;
  }

  // 2. Verificar que el carrito no esté vacío
  return carritoService.carrito$.pipe(
    take(1),
    map((carrito) => {
      if (!carrito || carrito.items.length === 0) {
        console.warn('[checkoutGuard] Carrito vacío');
        alert('Tu carrito está vacío. Agrega productos antes de continuar.');
        router.navigate(['/articulos']);
        return false;
      }
      return true;
    })
  );
};
