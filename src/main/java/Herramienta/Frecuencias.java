package Herramienta;

import java.util.*;

public class Frecuencias {

    private Map<Byte, Integer> conteoSimbolos = new HashMap<>();
    private int totalSimbolos = 0;

    // 1️⃣ Calcular frecuencias a partir del arreglo de bytes
    public void calcular(byte[] datos) {
        for (byte b : datos) {
            conteoSimbolos.put(b, conteoSimbolos.getOrDefault(b, 0) + 1);
            totalSimbolos++;
        }
    }

    // 2️⃣ Obtener la frecuencia de un símbolo específico
    public int obtenerFrecuencia(byte simbolo) {
        return conteoSimbolos.getOrDefault(simbolo, 0);
    }

    // 3️⃣ Obtener todas las frecuencias
    public Map<Byte, Integer> obtenerTodas() {
        return conteoSimbolos;
    }

    // 4️⃣ Calcular el porcentaje de aparición de un símbolo
    public double obtenerPorcentaje(byte simbolo) {
        if (totalSimbolos == 0) return 0;
        return (conteoSimbolos.getOrDefault(simbolo, 0) * 100.0) / totalSimbolos;
    }

    // 5️⃣ Mostrar la tabla de frecuencias
    public void mostrarTabla() {
        System.out.println("Símbolo | Frecuencia | Porcentaje");
                System.out.println("--------------------------------");
        for (Map.Entry<Byte, Integer> entrada : conteoSimbolos.entrySet()) {
            byte simbolo = entrada.getKey();
            int frecuencia = entrada.getValue();
            double porcentaje = obtenerPorcentaje(simbolo);
            char caracter = (char) (simbolo & 0xFF); // convierte byte a char visible
            System.out.printf("   %c     |    %d        |   %.2f%%%n", caracter, frecuencia, porcentaje);
        }
    }

    // 6️⃣ Obtener el total de símbolos analizados
    public int obtenerTotal() {
        return totalSimbolos;
    }

    public void cargarDesdeMapa(Map<Byte, Integer> mapa) {
        this.conteoSimbolos.clear();
        this.conteoSimbolos.putAll(mapa);
        this.totalSimbolos = 0;
        for (int v : mapa.values()) this.totalSimbolos += v;
    }
}
