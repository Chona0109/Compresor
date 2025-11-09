package Herramienta;

public class Nodo {

    private byte simbolo;           // símbolo o carácter representado
    private long frecuencia;        // frecuencia de aparición
    private Nodo izquierdo;         // hijo izquierdo
    private Nodo derecho;           // hijo derecho

    // Constructor para hojas (símbolo y frecuencia)
    public Nodo(byte simbolo, long frecuencia) {
        this.simbolo = simbolo;
        this.frecuencia = frecuencia;
    }

    // Constructor para nodos internos (sin símbolo)
    public Nodo(byte simbolo, long frecuencia, Nodo izquierdo, Nodo derecho) {
        this.simbolo = simbolo;
        this.frecuencia = frecuencia;
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }

    // Métodos de acceso (getters)
    public byte obtenerSimbolo() {
        return simbolo;
    }

    public long obtenerFrecuencia() {
        return frecuencia;
    }

    public Nodo obtenerIzquierdo() {
        return izquierdo;
    }

    public Nodo obtenerDerecho() {
        return derecho;
    }

    // Saber si es una hoja (nodo sin hijos)
    public boolean esHoja() {
        return (izquierdo == null && derecho == null);
    }

    // Para impresión rápida (depuración)
    @Override
    public String toString() {
        if (esHoja()) {
            char c = (char) (simbolo & 0xFF);
            return "'" + c + "' (" + frecuencia + ")";
        } else {
            return "[" + frecuencia + "]";
        }
    }
}
