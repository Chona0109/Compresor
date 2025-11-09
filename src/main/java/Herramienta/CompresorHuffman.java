package Herramienta;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class CompresorHuffman {

    public void comprimir(Path archivoEntrada, Path archivoSalida) {
        try {
            long inicio = System.nanoTime(); // ⏱️ empezar a medir tiempo

            // 1️⃣ Leer entrada
            byte[] datos = Files.readAllBytes(archivoEntrada);

            // 2️⃣ Calcular frecuencias
            Frecuencias frecuencias = new Frecuencias();
            frecuencias.calcular(datos);

            // 3️⃣ Construir árbol y tabla de códigos
            ArbolHuffman arbol = new ArbolHuffman();
            arbol.construir(frecuencias);
            TablaCodigos tabla = arbol.generarTablaCodigos();

            // 4️⃣ (Opcional) Mostrar información intermedia
            frecuencias.mostrarTabla();
            tabla.mostrarTabla(frecuencias);

            // 5️⃣ Codificar directo a bytes (sin String gigante)
            ByteArrayOutputStream bufferCuerpo = new ByteArrayOutputStream();
            EscritorBits escritor = new EscritorBits(bufferCuerpo);

            for (byte b : datos) {
                String codigo = tabla.obtenerCodigo(b);
                if (codigo == null) {
                    throw new IllegalStateException("No hay código para el símbolo: " + (b & 0xFF));
                }
                escritor.escribirBits(codigo);
            }

            int padBits = escritor.vaciarYObtenerRelleno();
            long totalBits = escritor.obtenerTotalBits();
            byte[] cuerpoEmpaquetado = bufferCuerpo.toByteArray();

            // 6️⃣ Escribir el contenedor .huff
            ArchivoHuff archivoHuff = new ArchivoHuff();
            archivoHuff.escribirEmpaquetado(
                    archivoSalida,
                    frecuencias,
                    datos.length,        // tamaño original
                    totalBits,           // bits útiles
                    cuerpoEmpaquetado,   // cuerpo comprimido
                    padBits,             // bits de relleno
                    false                // incluirChecksum (puedes cambiar a true)
            );

            // 7️⃣ Medir tiempo y mostrar estadísticas
            long fin = System.nanoTime();
            double segundos = (fin - inicio) / 1e9;

            Estadisticas estadisticas = Estadisticas.crearDesdeArchivos(
                    archivoEntrada,
                    archivoSalida,
                    totalBits,
                    padBits,
                    segundos
            );
            estadisticas.mostrarResumen("compresión");

        } catch (IOException e) {
            System.err.println("Error al comprimir el archivo: " + e.getMessage());
        }
    }
}