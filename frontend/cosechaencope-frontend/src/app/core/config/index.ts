/**
 * Barrel export para la configuración global de la aplicación
 * 
 * Este archivo facilita las importaciones centralizando todas las
 * exportaciones de configuración en un solo punto de entrada.
 */

export * from './global';

// Re-exportar tipos útiles para TypeScript
export type ApiEndpoint = string;
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
export type UserRole = 'ADMIN' | 'SUPERADMIN' | 'USER';
export type UserType = 'CLIENTE' | 'PRODUCTOR';
export type PaymentMethod = 'TARJETA' | 'EFECTIVO' | 'TRANSFERENCIA';
export type OrderStatus = 'PENDIENTE' | 'CONFIRMADO' | 'EN_PREPARACION' | 'ENVIADO' | 'ENTREGADO' | 'CANCELADO';

/**
 * Interfaz para configuración de peticiones HTTP
 */
export interface RequestConfig {
  url: string;
  method: HttpMethod;
  headers?: Record<string, string>;
  timeout?: number;
  data?: any;
  params?: Record<string, any>;
}

/**
 * Interfaz para respuestas de error estandarizadas
 */
export interface ApiError {
  status: number;
  message: string;
  timestamp: string;
  path: string;
}

/**
 * Interfaz para respuestas paginadas (futuro uso)
 */
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

/**
 * Interfaz para parámetros de paginación
 */
export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}

/**
 * Interfaz para filtros de búsqueda
 */
export interface SearchFilters {
  search?: string;
  category?: number;
  priceMin?: number;
  priceMax?: number;
  producer?: number;
}