package com.rlp.cosechaencope.model;

public enum EstadoOrdenVenta {

    PENDIENTE("Pendiente", "Orden creada y esperando procesamiento"),
    EN_PROCESO("En Proceso", "El productor está preparando el pedido"),
    ENVIADA("Enviada", "El pedido ha sido enviado"),
    ENTREGADA("Entregada", "El pedido ha sido entregado exitosamente"),
    CANCELADA("Cancelada", "La orden ha sido cancelada");

    private final String nombre;
    private final String descripcion;

    EstadoOrdenVenta(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
