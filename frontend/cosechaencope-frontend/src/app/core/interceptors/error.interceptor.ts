import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { HTTP_STATUS_CODES } from '../config';

/**
 * Interceptor HTTP para manejo centralizado de errores
 */
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Ha ocurrido un error inesperado';

        if (error.error instanceof ErrorEvent) {
          // Error del lado del cliente
          errorMessage = `Error del cliente: ${error.error.message}`;
        } else {
          // Error del lado del servidor
          switch (error.status) {
            case HTTP_STATUS_CODES.BAD_REQUEST:
              errorMessage = error.error?.message || 'Datos inválidos';
              break;
            case HTTP_STATUS_CODES.UNAUTHORIZED:
              errorMessage = 'No autorizado. Por favor, inicia sesión';
              // Opcional: redirigir al login
              sessionStorage.removeItem('authToken');
              break;
            case HTTP_STATUS_CODES.FORBIDDEN:
              errorMessage = 'No tienes permisos para realizar esta acción';
              break;
            case HTTP_STATUS_CODES.NOT_FOUND:
              errorMessage = 'Recurso no encontrado';
              break;
            case HTTP_STATUS_CODES.CONFLICT:
              errorMessage = error.error?.message || 'Conflicto en los datos';
              break;
            case HTTP_STATUS_CODES.INTERNAL_SERVER_ERROR:
              errorMessage = 'Error interno del servidor';
              break;
            default:
              errorMessage = `Error del servidor: ${error.status} - ${error.statusText}`;
          }
        }

        console.error('Error HTTP:', {
          url: req.url,
          method: req.method,
          status: error.status,
          message: errorMessage,
          error: error.error
        });

        // Retornar el error para que pueda ser manejado por el componente
        return throwError(() => ({
          status: error.status,
          message: errorMessage,
          originalError: error
        }));
      })
    );
  }
}