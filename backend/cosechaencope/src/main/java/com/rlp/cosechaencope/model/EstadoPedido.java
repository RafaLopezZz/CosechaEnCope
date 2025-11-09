package com.rlp.cosechaencope.model;

public enum EstadoPedido {

    PENDIENTE("Pendiente", "Pedido recibido y pendiente de procesamiento"),
    CONFIRMADO("Confirmado", "Pedido confirmado y en proceso"),
    EN_PREPARACION("En Preparación", "Pedido en preparación para envío"),
    ENVIADO("Enviado", "Pedido enviado al cliente"),
    ENTREGADO("Entregado", "Pedido entregado al cliente"),
    CANCELADO("Cancelado", "Pedido cancelado");

    private final String nombre;
    private final String descripcion;

    EstadoPedido(String nombre, String descripcion) {
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

