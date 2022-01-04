package com.abeltrans.proyectogb.entities;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Entidad {
    private int articulo;
    private String numSerie;
    private LocalDate fechaRaw;
    private String fecha;
    private String codigo;

    public Entidad() {
    }

    public Entidad(int articulo, String numSerie, String fecha, String codigo) {
        this.articulo = articulo;
        this.numSerie = numSerie;
        this.fecha = fecha;
        this.codigo = codigo;
    }

    public int getArticulo() {
        return articulo;
    }

    public void setArticulo(int articulo) {
        this.articulo = articulo;
    }

    public String getNumSerie() {
        return numSerie;
    }

    public void setNumSerie(String numSerie) {
        this.numSerie = numSerie;
    }

    public LocalDate getFechaRaw() {
        return fechaRaw;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setFechaRaw(Date fechaRaw) {
        ZoneId z = ZoneId.of("Europe/Madrid");
        this.fechaRaw = fechaRaw.toInstant().atZone(z).toLocalDate();
        setFecha(this.fechaRaw);
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @Override
    public String toString() {
        return "articulo = " + articulo + ", numSerie = " + numSerie + ", fechaRaw=" + fechaRaw + ", fecha" + fecha + ", código=" + codigo;
    }

    public boolean equals(Entidad e) {
        boolean equals;

        if (e.getCodigo().equals(codigo) && e.getNumSerie().equals(numSerie)){
            equals = true;
        }
        else{
            equals = false;
        }
        return equals;
    }
}
