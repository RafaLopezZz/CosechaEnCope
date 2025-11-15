/**
 * Configuración global de endpoints para la aplicación Cosecha en Cope
 *
 * Este archivo centraliza todas las rutas de la API para facilitar el mantenimiento
 * y evitar duplicación de código. Los endpoints están organizados por módulos
 * correspondientes a los controladores del backend.
 *
 * @author Generated from Spring Boot Controllers
 * @version 1.0.0
 */

import { environment } from '../../../environments/environment';

const API_BASE_URL = environment.apiUrl;

/**
 * Configuración global de endpoints de la API
 */
export const API_ENDPOINTS = {
  /**
   * Endpoints de Autenticación (AuthController)
   * Base: /cosechaencope/auth
   */
  AUTH: {
    LOGIN: `${API_BASE_URL}/auth/login`,
    LOGIN_PRODUCTORES: `${API_BASE_URL}/auth/login/productores`,
    REGISTRO_CLIENTES: `${API_BASE_URL}/auth/registro/clientes`,
    REGISTRO_PRODUCTORES: `${API_BASE_URL}/auth/registro/productores`,
  },

  /**
   * Endpoints de Artículos (ArticuloController)
   * Base: /cosechaencope/articulos
   */
  ARTICULOS: {
    BASE: `${API_BASE_URL}/articulos`,
    CREATE: `${API_BASE_URL}/articulos`,
    GET_ALL: `${API_BASE_URL}/articulos`,
    GET_BY_ID: (id: number) => `${API_BASE_URL}/articulos/${id}`,
    UPDATE: (id: number) => `${API_BASE_URL}/articulos/${id}`,
    DELETE: (id: number) => `${API_BASE_URL}/articulos/${id}`,
  },

  /**
   * Endpoints de Categorías (CategoriaController)
   * Base: /cosechaencope/categorias
   */
  CATEGORIAS: {
    BASE: `${API_BASE_URL}/categorias`,
    CREATE: `${API_BASE_URL}/categorias`,
    GET_ALL: `${API_BASE_URL}/categorias`,
    GET_BY_ID: (id: number) => `${API_BASE_URL}/categorias/${id}`,
    GET_ARTICULOS_BY_CATEGORIA: (id: number) =>
      `${API_BASE_URL}/categorias/${id}/articulos`,
    UPDATE: (id: number) => `${API_BASE_URL}/categorias/${id}`,
    DELETE: (id: number) => `${API_BASE_URL}/categorias/${id}`,
  },

  /**
   * Endpoints de Carrito (CarritoController)
   * Base: /cosechaencope/carrito
   * Nota: Todos los endpoints requieren autenticación
   */
  CARRITO: {
    BASE: `${API_BASE_URL}/carrito`,
    ADD_ITEM: `${API_BASE_URL}/carrito/agregar`,                       // POST
    GET_CART: `${API_BASE_URL}/carrito`,                                // GET
    DECREMENT_ITEM: (idArticulo: number) =>                            // POST con {idArticulo}
      `${API_BASE_URL}/carrito/decrementar/${idArticulo}`,
    CLEAR_CART: `${API_BASE_URL}/carrito/vaciar`,                      // DELETE
  },

  /**
   * Endpoints de Clientes (ClienteController)
   * Base: /cosechaencope/usuarios/clientes
   * Nota: Todos los endpoints requieren autenticación
   */
  CLIENTES: {
    BASE: `${API_BASE_URL}/usuarios/clientes`,
    GET_ALL: `${API_BASE_URL}/usuarios/clientes`,
    GET_BY_USER_ID: (idUsuario: number) =>
      `${API_BASE_URL}/usuarios/clientes/${idUsuario}`,
    UPDATE_BY_USER_ID: (idUsuario: number) =>
      `${API_BASE_URL}/usuarios/clientes/${idUsuario}`,
  },

  /**
   * Endpoints de Productores (ProductorController)
   * Base: /cosechaencope/usuarios/productores
   * Nota: Todos los endpoints requieren autenticación
   */
  PRODUCTORES: {
    BASE: `${API_BASE_URL}/usuarios/productores`,
    GET_ALL: `${API_BASE_URL}/usuarios/productores`,
    GET_BY_USER_ID: (idUsuario: number) =>
      `${API_BASE_URL}/usuarios/productores/${idUsuario}`,
    UPDATE_BY_USER_ID: (idUsuario: number) =>
      `${API_BASE_URL}/usuarios/productores/${idUsuario}`,
    // Endpoints para la gestión de artículos por parte de un productor
    GET_ARTICULOS_BY_PRODUCTOR: (idProductor: number) =>
      `${API_BASE_URL}/usuarios/productores/${idProductor}/articulos`,
    CREATE_ARTICULO_BY_PRODUCTOR: (idProductor: number) =>
      `${API_BASE_URL}/usuarios/productores/${idProductor}/articulos`,
    UPDATE_ARTICULO_BY_PRODUCTOR: (idProductor: number, idArticulo: number) =>
      `${API_BASE_URL}/usuarios/productores/${idProductor}/articulos/${idArticulo}`,
    DELETE_ARTICULO_BY_PRODUCTOR: (idProductor: number, idArticulo: number) =>
      `${API_BASE_URL}/usuarios/productores/${idProductor}/articulos/${idArticulo}`,
  },

  /**
   * Endpoints de Pedidos (PedidoController)
   * Base: /cosechaencope/pedidos
   * Nota: Todos los endpoints requieren autenticación
   */
  PEDIDOS: {
    BASE: `${API_BASE_URL}/pedidos`,
    CREATE: `${API_BASE_URL}/pedidos`,
    GET_BY_USER: `${API_BASE_URL}/pedidos`,
  },

  /**
   * Endpoints de Órdenes de Venta por Productor (OrdenVentaProductorController)
   * Base: /cosechaencope/productores/{idProductor}/ordenes
   */
  ORDENES_VENTA_PRODUCTOR: {
    GET_BY_PRODUCTOR: (idProductor: number) =>
      `${API_BASE_URL}/productores/${idProductor}/ordenes`,
  },

  /**
   * Endpoints de Usuarios (UsuarioController) - Solo Admin
   * Base: /cosechaencope/usuarios
   * Nota: Endpoints administrativos, requieren permisos especiales
   */
  USUARIOS: {
    BASE: `${API_BASE_URL}/usuarios`,
    REGISTRO: `${API_BASE_URL}/usuarios/registro`,
    GET_ALL: `${API_BASE_URL}/usuarios`,
    GET_BY_ID: (id: number) => `${API_BASE_URL}/usuarios/${id}`,
    UPDATE: (id: number) => `${API_BASE_URL}/usuarios/${id}`,
    DELETE: (id: number) => `${API_BASE_URL}/usuarios/${id}`,
  },

  /**
   * Endpoints de Gestión de Imágenes (ImagenController)
   * Base: /cosechaencope/imagenes
   * Nota: Para subir y eliminar imágenes en AWS S3
   */
  IMAGENES: {
    BASE: `${API_BASE_URL}/imagenes`,
    UPLOAD_ARTICULO: `${API_BASE_URL}/imagenes/articulos`,
    UPLOAD_CATEGORIA: `${API_BASE_URL}/imagenes/categorias`,
    UPLOAD_PERFIL: `${API_BASE_URL}/imagenes/perfiles`,
    DELETE: `${API_BASE_URL}/imagenes`,
  },
} as const;

/**
 * Métodos HTTP utilizados en la aplicación
 */
export const HTTP_METHODS = {
  GET: 'GET',
  POST: 'POST',
  PUT: 'PUT',
  DELETE: 'DELETE',
  PATCH: 'PATCH',
} as const;

/**
 * Headers estándar para las peticiones HTTP
 */
export const HTTP_HEADERS = {
  CONTENT_TYPE: 'Content-Type',
  AUTHORIZATION: 'Authorization',
  ACCEPT: 'Accept',
} as const;

/**
 * Tipos de contenido para las peticiones HTTP
 */
export const CONTENT_TYPES = {
  JSON: 'application/json',
  FORM_DATA: 'multipart/form-data',
  URL_ENCODED: 'application/x-www-form-urlencoded',
} as const;

/**
 * Códigos de estado HTTP comunes
 */
export const HTTP_STATUS_CODES = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  INTERNAL_SERVER_ERROR: 500,
} as const;

/**
 * Configuración para paginación (si se implementa en el futuro)
 */
export const PAGINATION_CONFIG = {
  DEFAULT_PAGE_SIZE: 10,
  MAX_PAGE_SIZE: 100,
  DEFAULT_PAGE: 0,
} as const;

/**
 * Configuración de timeout para peticiones HTTP
 */
export const REQUEST_CONFIG = {
  DEFAULT_TIMEOUT: 30000, // 30 segundos
  UPLOAD_TIMEOUT: 120000, // 2 minutos para uploads
} as const;

/**
 * Parámetros estándar para query params
 */
export const QUERY_PARAMS = {
  PAGE: 'page',
  SIZE: 'size',
  SORT: 'sort',
  SEARCH: 'search',
  FILTER: 'filter',
} as const;

/**
 * Configuración para AWS S3 (endpoints futuros)
 */
export const AWS_CONFIG = {
  BUCKET_NAME: 'cosecha-en-cope-images',
  REGION: 'eu-west-1',
  // Endpoints para presigned URLs se añadirán cuando se implementen
} as const;

/**
 * Configuración de roles de usuario
 */
export const USER_ROLES = {
  ADMIN: 'ADMIN',
  SUPER_ADMIN: 'SUPERADMIN',
  USER: 'USER',
} as const;

/**
 * Tipos de usuario
 */
export const USER_TYPES = {
  CLIENTE: 'CLIENTE',
  PRODUCTOR: 'PRODUCTOR',
} as const;

/**
 * Métodos de pago disponibles
 */
export const PAYMENT_METHODS = {
  TARJETA: 'TARJETA',
  EFECTIVO: 'EFECTIVO',
  TRANSFERENCIA: 'TRANSFERENCIA',
} as const;

/**
 * Estados de pedido
 */
export const ORDER_STATUS = {
  PENDIENTE: 'PENDIENTE',
  CONFIRMADO: 'CONFIRMADO',
  EN_PREPARACION: 'EN_PREPARACION',
  ENVIADO: 'ENVIADO',
  ENTREGADO: 'ENTREGADO',
  CANCELADO: 'CANCELADO',
} as const;

/**
 * Utility function para construir URLs con parámetros
 */
export const buildUrlWithParams = (
  baseUrl: string,
  params: Record<string, any>
): string => {
  const url = new URL(baseUrl);
  Object.keys(params).forEach((key) => {
    if (params[key] !== null && params[key] !== undefined) {
      url.searchParams.append(key, params[key].toString());
    }
  });
  return url.toString();
};

/**
 * Utility function para construir headers de autorización
 */
export const buildAuthHeaders = (token?: string): Record<string, string> => {
  const headers: Record<string, string> = {
    [HTTP_HEADERS.CONTENT_TYPE]: CONTENT_TYPES.JSON,
    [HTTP_HEADERS.ACCEPT]: CONTENT_TYPES.JSON,
  };

  if (token) {
    headers[HTTP_HEADERS.AUTHORIZATION] = `Bearer ${token}`;
  }

  return headers;
};

/**
 * Utility function para validar si un endpoint requiere autenticación
 */
export const requiresAuth = (endpoint: string): boolean => {
  const publicEndpoints = [
    API_ENDPOINTS.AUTH.LOGIN,
    API_ENDPOINTS.AUTH.LOGIN_PRODUCTORES,
    API_ENDPOINTS.AUTH.REGISTRO_CLIENTES,
    API_ENDPOINTS.AUTH.REGISTRO_PRODUCTORES,
    API_ENDPOINTS.ARTICULOS.GET_ALL,
    API_ENDPOINTS.CATEGORIAS.GET_ALL,
  ];

  return !publicEndpoints.some((publicEndpoint) =>
    endpoint.startsWith(publicEndpoint)
  );
};
