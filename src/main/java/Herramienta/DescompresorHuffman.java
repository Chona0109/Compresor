package Herramienta;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DescompresorHuffman {

    public void descomprimir(Path archivoEntrada, Path archivoSalida) {
        try {
            long inicio = System.nanoTime(); // ⏱️ empezar a medir tiempo

            // 1️⃣ Leer contenedor .huff
            ArchivoHuff archivoHuff = new ArchivoHuff();
            ArchivoHuff.Leido leido = archivoHuff.leer(archivoEntrada);

            // 2️⃣ Reconstruir el árbol desde las frecuencias leídas
            Frecuencias frecuencias = new Frecuencias();
            Map<Byte, Integer> mapaEntero = new HashMap<>();
            for (Map.Entry<Byte, Long> e : leido.frecuencias.entrySet()) {
                mapaEntero.put(e.getKey(), (int) Math.min(Integer.MAX_VALUE, e.getValue()));
            }
            frecuencias.cargarDesdeMapa(mapaEntero);

            ArbolHuffman arbol = new ArbolHuffman();
            arbol.construir(frecuencias);

            // 3️⃣ Decodificar usando LectorBits
            byte[] cuerpo = leido.bytesCuerpo;
            long bitsUtiles = leido.cabecera.totalBits;
            long tamañoOriginal = leido.cabecera.tamañoOriginal;

            byte[] restaurado = decodificarConArbol(cuerpo, bitsUtiles, arbol, tamañoOriginal);

            // 4️⃣ Escribir el archivo restaurado
            Files.write(archivoSalida, restaurado);

            // 5️⃣ Verificación simple
            if (restaurado.length != tamañoOriginal) {
                System.err.println("Advertencia: el tamaño restaurado no coincide con el original.");
            }

            // 6️⃣ Medir tiempo y mostrar estadísticas
            long fin = System.nanoTime();
            double segundos = (fin - inicio) / 1e9;

            Estadisticas estadisticas = Estadisticas.crearDesdeArchivos(
                    archivoEntrada,
                    archivoSalida,
                    bitsUtiles,
                    leido.cabecera.bitsRelleno,
                    segundos
            );
            estadisticas.mostrarResumen("descompresión");

        } catch (IOException e) {
            System.err.println("Error al descomprimir el archivo: " + e.getMessage());
        }
    }

    private byte[] decodificarConArbol(byte[] bytesCuerpo,
                                       long bitsUtiles,
                                       ArbolHuffman arbol,
                                       long tamañoEsperado) throws IOException {

        ByteArrayOutputStream salida = new ByteArrayOutputStream((int) Math.min(Integer.MAX_VALUE, tamañoEsperado));
        Nodo actual = arbol.obtenerRaiz();

        try (LectorBits lector = new LectorBits(bytesCuerpo)) {
            while (lector.obtenerTotalLeidos() < bitsUtiles) {
                int bit = lector.leerBit();
                if (bit == -1) break;                // seguridad extra ante EOF

                actual = (bit == 0) ? actual.obtenerIzquierdo() : actual.obtenerDerecho();

                if (actual.esHoja()) {
                    salida.write(actual.obtenerSimbolo());
                    if (salida.size() == tamañoEsperado) break; // ya recuperamos todo
                    actual = arbol.obtenerRaiz();
                }
            }
        }
        return salida.toByteArray();
    }
}