package Herramienta;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LectorBits implements AutoCloseable {

    private final InputStream entrada;
    private int buffer = 0;     // byte actual
    private int disponibles = 0; // cuantos bits quedan por leer del buffer (0..8)
    private long leidos = 0;    // total de bits entregados (útiles)

    public LectorBits(byte[] datos) {
        this.entrada = new ByteArrayInputStream(datos);
    }

    public LectorBits(InputStream entrada) {
        this.entrada = entrada;
    }

    /** Lee un bit (0 o 1). Retorna -1 si EOF real (no confundir con fin de bits útiles). */
    public int leerBit() throws IOException {
        if (disponibles == 0) {
            int b = entrada.read();
            if (b < 0) return -1; // EOF
            buffer = b & 0xFF;
            disponibles = 8;
        }
        int bit = (buffer & 0x80) != 0 ? 1 : 0; // MSB-first
        buffer <<= 1;
        disponibles--;
        leidos++;
        return bit;
    }

    /** Cantidad de bits leídos (útiles) hasta ahora. */
    public long obtenerTotalLeidos() {
        return leidos;
    }

    @Override
    public void close() throws IOException {
        entrada.close();
    }
}