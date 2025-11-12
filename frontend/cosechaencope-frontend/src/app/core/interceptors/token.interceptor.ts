import { HttpErrorResponse, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { inject } from '@angular/core';
import { Router } from '@angular/router';

export const tokenInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn) => {
  const router = inject(Router);
  const token = sessionStorage.getItem('authToken');
  
  // Verificar si la URL requiere autenticación
  const publicEndpoints = ['/cosechaencope/auth/', '/swagger'];
  const publicGetUrls = ['/cosechaencope/articulos', '/cosechaencope/categorias'];
  
  const isPublicEndpoint = publicEndpoints.some(endpoint => req.url.includes(endpoint));
  const isPublicGetUrl = req.method === 'GET' && publicGetUrls.some(url => 
    req.url === url || req.url.startsWith(url + '?')
  );
  
  const requiresAuth = !isPublicEndpoint && !isPublicGetUrl;
  
  let cloned = req;
  
  if (token && requiresAuth) {
    // Solo añadir Authorization header, NO tocar Content-Type
    // Angular y el navegador gestionan automáticamente Content-Type para FormData
    cloned = req.clone({ 
      setHeaders: {
        'Authorization': `Bearer ${token}`
      }
    });
  }

  return next(cloned).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        console.error('❌ 401 Unauthorized - Redirigiendo a login');
        sessionStorage.removeItem('authToken');
        router.navigateByUrl('/login');
      } else if (err.status === 403) {
        console.error('❌ 403 Forbidden - Acceso denegado');
        router.navigate(['/error'], { queryParams: { code: 403, m: 'Acceso denegado' } });
      } else if (err.status === 404) {
        console.error('❌ 404 Not Found:', err.url);
      }
      return throwError(() => err);
    })
  );
};
