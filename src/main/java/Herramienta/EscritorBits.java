package Herramienta;

import java.io.IOException;
import java.io.OutputStream;

public class EscritorBits {

    private final OutputStream salida;
    private int acumulador = 0;   // guarda los bits parciales
    private int usados = 0;       // cuántos bits hay en 'acumulador' (0..7)
    private long totalBits = 0;   // bits útiles escritos (sin contar el relleno)

    public EscritorBits(OutputStream salida) {
        this.salida = salida;
    }

    /**
     * Escribe una secuencia de '0' y '1' (por ejemplo "10110") al flujo.
     * Codifica en orden MSB-first dentro de cada byte.
     */
    public void escribirBits(String bits) throws IOException {
        for (int i = 0; i < bits.length(); i++) {
            char c = bits.charAt(i);
            if (c != '0' && c != '1') {
                throw new IllegalArgumentException("Solo se aceptan '0' y '1'");
            }
            int bit = (c == '1') ? 1 : 0;
            acumulador = (acumulador << 1) | bit;
            usados++;
            totalBits++;
            if (usados == 8) {
                salida.write(acumulador);
                acumulador = 0;
                usados = 0;
            }
        }
    }

    /**
     * Escribe un único bit (0 o 1).
     */
    public void escribirBit(int bit) throws IOException {
        if (bit != 0 && bit != 1) throw new IllegalArgumentException("bit debe ser 0 o 1");
        acumulador = (acumulador << 1) | bit;
        usados++;
        totalBits++;
        if (usados == 8) {
            salida.write(acumulador);
            acumulador = 0;
            usados = 0;
        }
    }

    /**
     * Vacía el buffer y rellena con ceros si el último byte no estaba completo.
     * @return bits de relleno agregados (0..7)
     */
    public int vaciarYObtenerRelleno() throws IOException {
        int pad = 0;
        if (usados != 0) {
            pad = 8 - usados;
            acumulador <<= pad;        // completar con ceros a la derecha
            salida.write(acumulador);
            acumulador = 0;
            usados = 0;
        }
        salida.flush();
        return pad;
    }

    /**
     * @return cantidad total de bits útiles escritos (antes del relleno).
     */
    public long obtenerTotalBits() {
        return totalBits;
    }
}