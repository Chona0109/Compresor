package Herramienta;
import java.util.*;

public class ArbolHuffman {

    private Nodo raiz;

    // 1 Construir el árbol de Huffman usando las frecuencias
    public void construir(Frecuencias frecuencias) {

        // Cola de prioridad: el nodo con menor frecuencia sale primero
        PriorityQueue<Nodo> cola = new PriorityQueue<>(Comparator.comparingLong(Nodo::obtenerFrecuencia));

        // Crear un nodo por cada símbolo y agregarlo a la cola
        for (Map.Entry<Byte, Integer> entrada : frecuencias.obtenerTodas().entrySet()) {
            Nodo nodo = new Nodo(entrada.getKey(), entrada.getValue());
            cola.add(nodo);
        }

        // Si solo hay un símbolo, duplicamos el nodo (caso especial)
        if (cola.size() == 1) {
            Nodo unico = cola.poll();
            cola.add(unico);
            cola.add(new Nodo((byte) 0, 0)); // nodo vacío
        }

        // Combinar los dos nodos de menor frecuencia hasta tener un solo árbol
        while (cola.size() > 1) {
            Nodo izquierdo = cola.poll();
            Nodo derecho = cola.poll();
            Nodo combinado = new Nodo((byte) 0, izquierdo.obtenerFrecuencia() + derecho.obtenerFrecuencia(), izquierdo, derecho);
            cola.add(combinado);
        }

        // El último nodo en la cola es la raíz del árbol
        raiz = cola.poll();
    }

    // Obtener la raíz del árbol
    public Nodo obtenerRaiz() {
        return raiz;
    }

    // Generar la tabla de códigos a partir del árbol
    public TablaCodigos generarTablaCodigos() {
        TablaCodigos tabla = new TablaCodigos();
        if (raiz != null) {
            generarCodigosRecursivo(raiz, "", tabla);
        }
        return tabla;
    }

    // 4️⃣ Recorrido recursivo para asignar 0 y 1 a cada símbolo
    private void generarCodigosRecursivo(Nodo nodo, String codigoActual, TablaCodigos tabla) {
        if (nodo.esHoja()) {
            tabla.agregarCodigo(nodo.obtenerSimbolo(), codigoActual.isEmpty() ? "0" : codigoActual);
            return;
        }
        if (nodo.obtenerIzquierdo() != null)
            generarCodigosRecursivo(nodo.obtenerIzquierdo(), codigoActual + "0", tabla);
        if (nodo.obtenerDerecho() != null)
            generarCodigosRecursivo(nodo.obtenerDerecho(), codigoActual + "1", tabla);
    }
}
