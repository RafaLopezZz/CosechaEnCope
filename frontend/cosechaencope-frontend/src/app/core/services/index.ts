/**
 * Barrel export para todos los servicios de la aplicación
 * 
 * Este archivo facilita las importaciones centralizando todas las
 * exportaciones de servicios en un solo punto de entrada.
 */

// Servicios principales
export * from './auth.service';
export * from './articulo.service';
export * from './carrito.service';
export * from './categorias.service';
export * from './cliente.service';
export * from './pedido.service';
export * from './productor.service';
export * from './upload.service';
export * from './user-store.service';

// Re-exportar tipos útiles
export type { AddToCarritoRequest, CarritoResponse, CarritoItemResponse } from './carrito.service';
export type { UploadResponse, PresignedUrlRequest, PresignedUrlResponse } from './upload.service';
export type { PedidoResponse, PedidoItemResponse } from './pedido.service';
export type { ClienteRequest, ClienteResponse } from './cliente.service';