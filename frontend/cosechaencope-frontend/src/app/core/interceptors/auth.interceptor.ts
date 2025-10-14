import { Injectable, inject } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { buildAuthHeaders, requiresAuth } from '../config';

/**
 * Interceptor HTTP para agregar automáticamente el token de autorización
 * a las peticiones que lo requieran.
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Verificar si el endpoint requiere autenticación
    if (!requiresAuth(req.url)) {
      return next.handle(req);
    }

    // Obtener token del sessionStorage
    const token = sessionStorage.getItem('authToken');
    
    if (token) {
      // Crear headers con autorización
      const authHeaders = buildAuthHeaders(token);
      
      // Clonar la petición con los nuevos headers
      const authReq = req.clone({
        setHeaders: authHeaders
      });
      
      return next.handle(authReq);
    }

    // Si no hay token, continuar con la petición original
    return next.handle(req);
  }
}