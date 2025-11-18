/**
 * Modelos para el sistema de pedidos y checkout
 * Sincronizados con backend Spring Boot PedidoResponse y DetallePedidoResponse
 * 
 * @author rafalopezzz
 */

/**
 * Estado del pedido - debe coincidir con EstadoPedido enum en backend
 */
export enum EstadoPedido {
  PENDIENTE = 'PENDIENTE',
  CONFIRMADO = 'CONFIRMADO',
  EN_PREPARACION = 'EN_PREPARACION',
  ENVIADO = 'ENVIADO',
  ENTREGADO = 'ENTREGADO',
  CANCELADO = 'CANCELADO'
}

/**
 * Métodos de pago disponibles
 */
export enum MetodoPago {
  TARJETA = 'TARJETA',
  TRANSFERENCIA = 'TRANSFERENCIA',
  EFECTIVO = 'EFECTIVO',
  PAYPAL = 'PAYPAL'
}

/**
 * Detalle de un artículo dentro de un pedido
 */
export interface DetallePedidoResponse {
  idPedido: number;
  idArticulo: number;
  nombreArticulo: string;
  cantidad: number;
  precioUnitario: number;
  totalLinea: number;
}

/**
 * Respuesta completa de un pedido desde el backend
 */
export interface PedidoResponse {
  idPedido: number;
  idUsuario: number;
  idCliente: number;
  nombreCliente: string;
  emailUsuario: string;
  direccionCliente: string;
  telefonoCliente?: string;
  fechaPedido: string; // ISO 8601 date string
  estadoPedido: string; // EstadoPedido as string
  subTotal: number;
  iva: number;
  gastosEnvio: number;
  total: number;
  metodoPago: string;
  idTransaccion: string;
  detalles: DetallePedidoResponse[];
}

/**
 * Request para crear un pedido con datos adicionales del checkout
 */
export interface CheckoutRequest {
  metodoPago: MetodoPago;
  direccion?: string; // Dirección de envío (opcional, puede venir del perfil)
  telefono?: string; // Teléfono de contacto
  notasAdicionales?: string; // Notas especiales para el pedido
}

/**
 * Datos del cliente para el checkout
 */
export interface CheckoutClienteData {
  nombre: string;
  email: string;
  telefono: string;
  direccion: string;
}

/**
 * Estado del proceso de checkout
 */
export enum CheckoutStep {
  RESUMEN = 'RESUMEN', // Revisar carrito
  DATOS = 'DATOS', // Confirmar datos del cliente
  PAGO = 'PAGO', // Seleccionar método de pago
  CONFIRMACION = 'CONFIRMACION' // Pedido confirmado
}

/**
 * Estado interno del checkout component
 */
export interface CheckoutState {
  step: CheckoutStep;
  clienteData: CheckoutClienteData | null;
  metodoPago: MetodoPago | null;
  isProcessing: boolean;
  error: string | null;
}
