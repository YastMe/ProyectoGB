package com.abeltrans.proyectogb.entities;

public class Botella {
    private int vacio;
    private int lleno;
    private String descripcion;

    public Botella() {
    }

    public Botella(int vacio, int lleno, String descripcion) {
        this.vacio = vacio;
        this.lleno = lleno;
        this.descripcion = descripcion;
    }

    public int getVacio() {
        return vacio;
    }

    public void setVacio(float vacio) {
        this.vacio = (int) vacio;
    }

    public int getLleno() {
        return lleno;
    }

    public void setLleno(float lleno) {
        this.lleno = (int) lleno;
    }

    public boolean isLleno(float codigo) {
        if (codigo == lleno){
            return true;
        }
        else {
            return false;
        }
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "Botella{" +
                "lleno=" + lleno +
                ", vacio=" + vacio +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
