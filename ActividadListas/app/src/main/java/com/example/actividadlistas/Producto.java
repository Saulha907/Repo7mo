package com.example.actividadlistas;

public class    Producto {
    private final String nombre;
    private final String descripcion;
    private final String precio;
    private final String icono;

    public Producto(String nombre, String descripcion, String precio, String icono) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.icono = icono;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getPrecio() { return precio; }
    public String getIcono() { return icono; }
}
