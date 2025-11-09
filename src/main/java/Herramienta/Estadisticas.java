package Herramienta;

import java.nio.file.*;

public class Estadisticas {

    private long tamañoOriginal;
    private long tamañoComprimido;
    private long bitsTotales;
    private int bitsRelleno;
    private double proporcion;
    private double tiempoSegundos;

    public Estadisticas() {
    }

    // --- Métodos para registrar datos ---
    public void setTamañoOriginal(long tamañoOriginal) {
        this.tamañoOriginal = tamañoOriginal;
    }

    public void setTamañoComprimido(long tamañoComprimido) {
        this.tamañoComprimido = tamañoComprimido;
    }

    public void setBitsTotales(long bitsTotales) {
        this.bitsTotales = bitsTotales;
    }

    public void setBitsRelleno(int bitsRelleno) {
        this.bitsRelleno = bitsRelleno;
    }

    public void setTiempoSegundos(double tiempoSegundos) {
        this.tiempoSegundos = tiempoSegundos;
    }

    // --- Cálculos ---
    public void calcularProporcion() {
        if (tamañoOriginal > 0)
            this.proporcion = (tamañoComprimido * 1.0) / tamañoOriginal;
        else
            this.proporcion = 0;
    }

    // --- Mostrar resumen ---
    public void mostrarResumen(String operacion) {
        System.out.println();
        System.out.println("=== Estadísticas de " + operacion.toUpperCase() + " ===");
        System.out.printf("Tamaño original:    %d bytes%n", tamañoOriginal);
        System.out.printf("Tamaño resultado:   %d bytes%n", tamañoComprimido);
        System.out.printf("Bits útiles:        %d bits%n", bitsTotales);
        System.out.printf("Bits de relleno:    %d%n", bitsRelleno);
        System.out.printf("Proporción:         %.2f%%%n", proporcion * 100);
        System.out.printf("Tiempo total:       %.3f segundos%n", tiempoSegundos);
        System.out.println("==============================");
    }

    // --- Método estático de conveniencia ---
    public static Estadisticas crearDesdeArchivos(Path original, Path comprimido,
                                                  long bitsTotales, int bitsRelleno,
                                                  double tiempoSegundos) {
        Estadisticas e = new Estadisticas();
        try {
            e.setTamañoOriginal(Files.size(original));
            e.setTamañoComprimido(Files.size(comprimido));
        } catch (Exception ex) {
            e.setTamañoOriginal(0);
            e.setTamañoComprimido(0);
        }
        e.setBitsTotales(bitsTotales);
        e.setBitsRelleno(bitsRelleno);
        e.setTiempoSegundos(tiempoSegundos);
        e.calcularProporcion();
        return e;
    }
}