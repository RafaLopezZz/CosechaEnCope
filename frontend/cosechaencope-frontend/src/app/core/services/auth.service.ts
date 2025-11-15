import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LoginRequest, JwtResponse } from '../../shared/models/auth.models';
import { UsuarioRequest, UsuarioResponse } from '../../shared/models/usuario.models';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { UserStoreService } from './user-store.service';
import { CarritoService } from './carrito.service';
import { API_ENDPOINTS } from '../config';

/**
 * Servicio de autenticación mejorado con fusión de carrito
 * 
 * MEJORAS IMPLEMENTADAS:
 * ✅ Fusión automática del carrito invitado tras login exitoso (asíncrona)
 * ✅ Sincronización del estado del carrito con el backend
 * 
 * NOTA: La fusión del carrito se ejecuta de forma no bloqueante para no afectar
 * el flujo de login. Los errores de fusión se logean pero no bloquean la autenticación.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private store = inject(UserStoreService);
  private router = inject(Router);
  private carritoService = inject(CarritoService);

  login(dto: LoginRequest) {
    return this.http.post<JwtResponse>(API_ENDPOINTS.AUTH.LOGIN, dto).pipe(
      tap(res => {
        sessionStorage.setItem('authToken', res.token);
        this.store.set(res);
        
        // Fusionar carrito invitado de forma asíncrona (no bloqueante)
        console.log('[AuthService] Login exitoso, fusionando carrito invitado...');
        this.carritoService.fusionarCarritoInvitado().subscribe({
          next: () => console.log('[AuthService] Carrito fusionado exitosamente'),
          error: (err) => console.error('[AuthService] Error al fusionar carrito:', err)
        });
      })
    );
  }

  loginProductor(dto: LoginRequest) {
    return this.http.post<JwtResponse>(API_ENDPOINTS.AUTH.LOGIN_PRODUCTORES, dto).pipe(
      tap(res => {
        sessionStorage.setItem('authToken', res.token);
        this.store.set(res);
        
        // Fusionar carrito invitado de forma asíncrona (no bloqueante)
        console.log('[AuthService] Login productor exitoso, fusionando carrito invitado...');
        this.carritoService.fusionarCarritoInvitado().subscribe({
          next: () => console.log('[AuthService] Carrito fusionado exitosamente'),
          error: (err) => console.error('[AuthService] Error al fusionar carrito:', err)
        });
      })
    );
  }

  registerCliente(dto: UsuarioRequest) {
    return this.http.post<UsuarioResponse>(API_ENDPOINTS.AUTH.REGISTRO_CLIENTES, dto);
  }

  registerProductor(dto: UsuarioRequest) {
    return this.http.post<UsuarioResponse>(API_ENDPOINTS.AUTH.REGISTRO_PRODUCTORES, dto);
  }

  logout() {
    sessionStorage.removeItem('authToken');
    this.store.clear();
    
    // NO limpiar el carrito invitado aquí, permitir que el usuario
    // mantenga sus items si cierra sesión
    
    this.router.navigateByUrl('/login');
  }
}
