import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { JwtResponse } from '../../shared/models/auth.models';

/**
 * Servicio para gestionar el estado del usuario autenticado
 * 
 * NOTA: El estado se restaura automáticamente desde sessionStorage
 * al inicializar el servicio si existe un token válido
 */
@Injectable({ providedIn: 'root' })
export class UserStoreService {
  private _user$ = new BehaviorSubject<JwtResponse | null>(null);
  user$ = this._user$.asObservable();
  
  constructor() {
    // Restaurar estado del usuario desde sessionStorage al inicializar
    this.restoreUserFromSession();
  }
  
  set(user: JwtResponse) {
    this._user$.next(user);
    // Guardar el usuario completo en sessionStorage para restauración
    sessionStorage.setItem('currentUser', JSON.stringify(user));
  }
  
  clear() {
    this._user$.next(null);
    // Limpiar también el usuario guardado
    sessionStorage.removeItem('currentUser');
  }
  
  snapshot() {
    return this._user$.value;
  }
  
  /**
   * Restaura el estado del usuario desde sessionStorage
   * Primero intenta leer el objeto completo guardado, si no existe
   * intenta decodificar el token JWT (método legacy)
   */
  private restoreUserFromSession(): void {
    // Intentar restaurar desde el objeto guardado
    const savedUser = sessionStorage.getItem('currentUser');
    if (savedUser) {
      try {
        const user: JwtResponse = JSON.parse(savedUser);
        // Validar que el token aún existe
        const token = sessionStorage.getItem('authToken');
        if (token && user.token === token) {
          this._user$.next(user);
          return;
        }
      } catch (error) {
        console.error('[UserStoreService] Error al restaurar usuario guardado:', error);
      }
    }
    
    // Fallback: intentar decodificar el JWT (método legacy)
    const token = sessionStorage.getItem('authToken');
    if (!token) return;
    
    try {
      // Decodificar el JWT para obtener información del usuario
      const payload = this.decodeJWT(token);
      
      // ADVERTENCIA: El método legacy no puede obtener el idUsuario del token
      // porque no está incluido en los claims. Se guarda el
      // JwtResponse completo en sessionStorage durante el login.
      const user: JwtResponse = {
        token: token,
        idUsuario: 0, // No disponible en el token, requiere guardado explícito
        email: payload.sub || '',
        tipoUsuario: payload.tipoUsuario || this.extractTipoUsuarioFromRoles(payload.roles),
        roles: payload.roles || []
      };
      
      this._user$.next(user);
    } catch (error) {
      console.error('[UserStoreService] Error al restaurar usuario desde token:', error);
      // Si hay error, limpiar el token inválido
      sessionStorage.removeItem('authToken');
    }
  }
  
  /**
   * Extrae el tipo de usuario desde los roles
   * Los roles incluyen TYPE_CLIENTE o TYPE_PRODUCTOR
   */
  private extractTipoUsuarioFromRoles(roles: string[] = []): 'CLIENTE' | 'PRODUCTOR' {
    if (roles.includes('TYPE_CLIENTE')) return 'CLIENTE';
    if (roles.includes('TYPE_PRODUCTOR')) return 'PRODUCTOR';
    return 'CLIENTE'; // Valor por defecto
  }
  
  /**
   * Decodifica un token JWT sin verificar la firma
   * Solo extrae el payload para uso en el frontend
   */
  private decodeJWT(token: string): any {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        throw new Error('Token JWT inválido');
      }
      
      const payload = parts[1];
      const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(decoded);
    } catch (error) {
      throw new Error('Error al decodificar token JWT');
    }
  }
}