package com.example.sergio.reencuadrar;

import android.graphics.Bitmap;

/**
 * Created by Alumno on 20/02/2018.
 */

public class Sitio {

    public static final String _ID = "_id";
    public static final String NOMBRE = "NOMBRE";
    public static final String DIRECCION = "DIRECCION";
    public static final String LATITUD = "LATITUD";
    public static final String LONGITUD = "LONGITUD";
    public static final String DESCRIPCION = "DESCRIPCION";
    public static final String CATEGORIA = "CATEGORIA";
    public static final String RUTA = "RUTA";
    public static final String ZONA = "ZONA";
    public static final String FAVORITO = "FAVORITO";
    public static final String IDFOTO = "IDFOTO";

    private int _id;
    private String nombre;
    private String direccion;
    private String latitud;
    private String longitud;
    private String categoria;
    private String ruta;
    private String descripcion;
    private Bitmap icono;
    private int valoracion;
    private boolean favorito;
    private String idFoto;

    public Sitio(int _id, String nombre, String direccion, String latitud, String longitud, String descripcion, String categoria, String ruta, String zona) {
        this._id = _id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.ruta = ruta;
        this.zona = zona;
    }

    public Sitio() {

    }

    public String getIdFoto() {
        return idFoto;
    }

    public void setIdFoto(String idFoto) {
        this.idFoto = idFoto;
    }

    public Sitio(int _id, String nombre, String direccion, String latitud, String longitud, String descripcion, String categoria, String ruta, String zona, int favorito) {
        this._id = _id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.ruta = ruta;

        this.zona = zona;
        if (favorito == 1) {
            this.favorito = true;
        } else {
            this.favorito = false;
        }
    }

    public Sitio(int _id, String nombre, String direccion, String latitud, String longitud, String descripcion, String categoria, String ruta, String zona, int favorito, String idFoto) {
        this._id = _id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.ruta = ruta;

        this.zona = zona;
        if (favorito == 1) {
            this.favorito = true;
        } else {
            this.favorito = false;
        }
        this.idFoto = idFoto;
    }

    public Bitmap getIcono() {
        return icono;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setIcono(Bitmap icono) {
        this.icono = icono;
    }

    public int getValoracion() {
        return valoracion;
    }

    public void setValoracion(int valoracion) {
        this.valoracion = valoracion;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    private String zona;

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isFavorito() {
        return favorito;
    }

    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }
}
