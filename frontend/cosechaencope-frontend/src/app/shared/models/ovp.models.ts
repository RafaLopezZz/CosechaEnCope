import { ProductorResponse } from './productor.models';
import { ArticuloResponse } from './articulo.models';

/**
 * Estados posibles de una orden de venta a productor
 * Sincronizado con EstadoOrdenVenta enum del backend
 */
export enum EstadoOrdenVenta {
  PENDIENTE = 'PENDIENTE',
  EN_PROCESO = 'EN_PROCESO',
  ENVIADA = 'ENVIADA',
  ENTREGADA = 'ENTREGADA',
  CANCELADA = 'CANCELADA'
}

/**
 * Detalle de línea de orden de venta
 * Campos sincronizados con DetalleOvpResponse del backend
 */
export interface DetalleOvpResponse {
  idDetalleOvp: number;  // Campo renombrado para coincidir con backend
  articulo: ArticuloResponse; 
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

/**
 * Información básica del cliente asociado al pedido
 */
export interface ClienteBasico {
  idCliente: number;
  nombre: string;
  apellidos?: string;
  telefono?: string;
  direccion?: string;
}

/**
 * Información básica del pedido cliente asociado a la OVP
 */
export interface PedidoClienteBasico {
  idPedido: number;
  cliente: ClienteBasico;
  direccionCliente?: string;
  metodoPago?: string;
}

/**
 * Modelo de Orden de Venta para Productor
 * Compatible con la entidad OrdenVentaProductor del backend
 */
export interface OrdenVentaProductor {
  idOvp: number;            
  numeroOrden: string;
  fechaCreacion: string;
  estado: EstadoOrdenVenta;
  total: number;
  observaciones?: string;
  productor?: ProductorResponse;
  pedidoCliente?: PedidoClienteBasico;
  
  // El backend usa "lineas", pero también puede tener "detalles" en el DTO
  lineas?: DetalleOvpResponse[];
  detalles: DetalleOvpResponse[];

  // Campos auxiliares para UI (no vienen del back)
  isUpdating?: boolean;
}

export interface ActualizarEstadoOvpRequest {
  estado: string;
  observaciones?: string;
}

export interface OvpState {
  ordenes: OrdenVentaProductor[];
  loading: boolean;
  error: string | null;
  filtrosActivos: {
    estado?: EstadoOrdenVenta;
    fechaInicio?: Date;
    fechaFin?: Date;
  };
}