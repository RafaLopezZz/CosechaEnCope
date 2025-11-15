/**
 * Modelos para el sistema de carrito de compras
 * Sincronizados con backend Spring Boot CarritoResponse y DetalleCarritoResponse
 */

/**
 * Respuesta del carrito completo desde el backend
 */
export interface CarritoResponse {
  id: number; // Cambiado de idCarrito a id (backend usa 'id')
  fechaCreacion?: string;
  items: DetalleCarritoResponse[];
  subtotal: number;
  impuestos: number;
  gastosEnvio: number;
  total: number;
}

/**
 * Detalle de un item en el carrito
 */
export interface DetalleCarritoResponse {
  id: number; // ID del detalle
  idArticulo: number;
  nombreArticulo: string;
  cantidad: number;
  precioUnitario: number;
  totalLinea: number; // Cambiado de subtotal a totalLinea (backend usa totalLinea)
  imagenUrl?: string; // URL de la imagen del artículo
}

/**
 * Request para agregar un artículo al carrito
 */
export interface AddToCarritoRequest {
  idArticulo: number;
  cantidad: number;
}

/**
 * Item del carrito invitado almacenado en localStorage
 */
export interface CarritoInvitadoItem {
  idArticulo: number;
  cantidad: number;
}