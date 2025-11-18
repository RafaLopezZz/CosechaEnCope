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
export * from './imagen.service';
export * from './image-upload-utils.service'
export * from './user-store.service';

// Re-exportar tipos útiles
export type { AddToCarritoRequest, CarritoResponse, DetalleCarritoResponse } from '../../shared/models/carrito.models';
export type { ImageUploadResponse } from './imagen.service';
export type { ImageValidationOptions, ImageValidationResult } from './image-upload-utils.service';
export type { PedidoResponse, DetallePedidoResponse } from '../../shared/models/pedido.models';
export type { ClienteRequest, ClienteResponse } from './cliente.service';