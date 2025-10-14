// src/app/core/interceptors/token.interceptor.ts
import { HttpErrorResponse, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { inject } from '@angular/core';
import { Router } from '@angular/router';

export const tokenInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn) => {
  const router = inject(Router);
  const token = sessionStorage.getItem('authToken');
  
  // DEBUGGING: Forzar verificación de sessionStorage
  console.log('🔐 ==== TOKEN INTERCEPTOR DEBUG ====');
  console.log('🔐 Current sessionStorage contents:');
  for (let i = 0; i < sessionStorage.length; i++) {
    const key = sessionStorage.key(i);
    const value = sessionStorage.getItem(key!);
    console.log(`🔐   ${key}: ${value?.substring(0, 50)}...`);
  }
  console.log('🔐 Token from sessionStorage.getItem("authToken"):', token ? token.substring(0, 50) + '...' : 'NULL');
  console.log('🔐 ==================================');
  
  console.log('🔐 Token interceptor - URL:', req.url);
  console.log('🔐 Token interceptor - Method:', req.method);
  console.log('🔐 Token interceptor - Token exists:', !!token);
  console.log('🔐 Token interceptor - Token value:', token ? token.substring(0, 50) + '...' : 'null');
  console.log('🔐 Token interceptor - SessionStorage keys:', Object.keys(sessionStorage));
  console.log('🔐 Token interceptor - Full token from sessionStorage:', sessionStorage.getItem('authToken'));
  
  // Verificar si la URL requiere autenticación
  const publicEndpoints = [
    '/cosechaencope/auth/',
    '/swagger'
  ];
  
  // URLs específicas que son públicas (GET solamente)
  const publicGetUrls = [
    '/cosechaencope/articulos',
    '/cosechaencope/categorias'
  ];
  
  const isPublicEndpoint = publicEndpoints.some(endpoint => req.url.includes(endpoint));
  const isPublicGetUrl = req.method === 'GET' && publicGetUrls.some(url => 
    req.url === url || req.url.startsWith(url + '?')
  );
  
  const requiresAuth = !isPublicEndpoint && !isPublicGetUrl;
  
  console.log('🔐 URL requires auth:', requiresAuth);
  
  let cloned = req;
  
  if (token && requiresAuth) {
    console.log('🔐 Adding Authorization header: Bearer ' + token.substring(0, 20) + '...');
    
    // No añadir Content-Type para FormData (subida de archivos)
    const headers: { [key: string]: string } = {
      'Authorization': `Bearer ${token}`
    };
    
    // Solo añadir Content-Type si no es FormData
    if (!(req.body instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
    }
    
    cloned = req.clone({ 
      setHeaders: headers
    });
    console.log('🔐 Request cloned with auth headers (FormData detected:', req.body instanceof FormData, ')');
    console.log('🔐 Final cloned headers:', cloned.headers.keys());
    console.log('🔐 Authorization header value:', cloned.headers.get('Authorization'));
  } else if (requiresAuth && !token) {
    console.warn('⚠️ Request requires auth but no token found!');
    console.warn('⚠️ SessionStorage contents:', Object.keys(sessionStorage).map(key => `${key}: ${sessionStorage.getItem(key)?.substring(0, 30)}...`));
  } else {
    console.log('🔐 Request does not require auth or no token needed');
  }

  console.log('🔐 Final request headers:', Object.fromEntries(cloned.headers.keys().map(key => [key, cloned.headers.get(key)])));
  console.log('🔐 About to send request with URL:', cloned.url);
  console.log('🔐 Request method:', cloned.method);

  return next(cloned).pipe(
    catchError((err: HttpErrorResponse) => {
      console.error('❌ Token interceptor - HTTP Error:', {
        status: err.status,
        statusText: err.statusText,
        url: req.url,
        method: req.method,
        headers: err.headers?.keys?.() || 'No headers'
      });
      
      if (err.status === 401) {
        console.error('❌ 401 Error - Token might be invalid or expired');
        sessionStorage.removeItem('authToken');
        router.navigateByUrl('/login');
      } else if (err.status === 403) {
        router.navigate(['/error'], { queryParams: { code: 403, m: 'Acceso denegado' } });
      }
      return throwError(() => err);
    })
  );
};
