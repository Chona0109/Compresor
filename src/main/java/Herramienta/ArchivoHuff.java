package Herramienta;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Maneja el formato binario autocontenido .huff:
 *  MAGIC(4) + VERSION(1)
 *  + tamañoOriginal(8) + totalBits(8) + banderas(1) + [checksum(8) opcional]
 *  + entradas(4) +  entradas * (simbolo(1) + frecuencia(8))
 *  + cuerpoEmpaquetado(...) + padBits(1)
 *
 *  Notas:
 *  - MSB-first dentro de cada byte del cuerpo.
 *  - banderas bit0: 1 = checksum presente; 0 = sin checksum.
 */
public class ArchivoHuff {

    // --- Constantes de formato ---
    private static final byte[] MAGIA = new byte[]{'H','U','F','F'};
    private static final int VERSION = 1;

    // --- Estructuras del contenedor ---
    public static class Cabecera {
        public long tamañoOriginal;    // bytes del archivo original
        public long totalBits;         // bits útiles en el cuerpo (sin contar relleno)
        public byte banderas;          // bit0: checksum presente
        public long checksum;          // válido si (banderas & 1) != 0
        public int cantidadSimbolos;   // tamaño de la tabla de símbolos
        public int bitsRelleno;        // 0..7 (relleno al final del cuerpo)
    }

    /** Resultado de leer un .huff */
    public static class Leido {
        public Cabecera cabecera;
        public Map<Byte, Long> frecuencias; // tabla de símbolos y sus frecuencias
        public byte[] bytesCuerpo;          // cuerpo comprimido (sin el último byte de pad)
    }

    // ===========================
    //          ESCRITURA
    // ===========================

    /**
     * Opción recomendada: escribir el .huff con el cuerpo ya empaquetado.
     *
     * @param rutaSalida destino del archivo .huff
     * @param frecuencias tabla de frecuencias (se escriben para reconstruir árbol)
     * @param tamañoOriginal bytes del original
     * @param totalBits bits útiles del cuerpo (sin incluir relleno)
     * @param cuerpoEmpaquetado bytes del bitstream ya empacado (MSB-first)
     * @param padBits número de bits de relleno al final (0..7)
     * @param incluirChecksum si true, se escribe un checksum sencillo (placeholder)
     */
    public void escribirEmpaquetado(Path rutaSalida,
                                    Frecuencias frecuencias,
                                    long tamañoOriginal,
                                    long totalBits,
                                    byte[] cuerpoEmpaquetado,
                                    int padBits,
                                    boolean incluirChecksum) throws IOException {

        Cabecera cab = new Cabecera();
        cab.tamañoOriginal = tamañoOriginal;
        cab.totalBits = totalBits;
        cab.banderas = (byte) (incluirChecksum ? 0b0000_0001 : 0);
        if (incluirChecksum) {
            cab.checksum = calcularChecksumSimple(frecuencias);
        }
        cab.cantidadSimbolos = frecuencias.obtenerTodas().size();
        cab.bitsRelleno = padBits;

        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(rutaSalida)))) {
            // 1) MAGIC + VERSION
            dos.write(MAGIA);
            dos.writeByte(VERSION);

            // 2) Metadatos
            dos.writeLong(cab.tamañoOriginal);
            dos.writeLong(cab.totalBits);
            dos.writeByte(cab.banderas);
            if ((cab.banderas & 0b1) != 0) {
                dos.writeLong(cab.checksum);
            }

            // 3) Tabla de símbolos
            dos.writeInt(cab.cantidadSimbolos);
            for (Map.Entry<Byte, Integer> e : frecuencias.obtenerTodas().entrySet()) {
                dos.writeByte(e.getKey() & 0xFF);
                dos.writeLong(e.getValue());
            }

            // 4) Cuerpo + pad
            dos.write(cuerpoEmpaquetado);
            dos.writeByte(cab.bitsRelleno);
            dos.flush();
        }
    }

    /**
     * Modo compatible: recibe la cadena de bits, la empaqueta y escribe el .huff.
     * Úsalo solo si aún no migras a EscritorBits dentro del compresor.
     */
    public void escribir(Path rutaSalida,
                         Frecuencias frecuencias,
                         TablaCodigos tabla,
                         String bitsComprimidos,
                         long tamañoOriginal,
                         boolean incluirChecksum) throws IOException {

        // Empaquetar string de bits a bytes y calcular pad
        Empaquetado empaquetado = empaquetarBits(bitsComprimidos);
        long totalBits = bitsComprimidos.length();

        escribirEmpaquetado(
                rutaSalida,
                frecuencias,
                tamañoOriginal,
                totalBits,
                empaquetado.bytesEmpaquetados,
                empaquetado.bitsRelleno,
                incluirChecksum
        );
    }

    // ===========================
    //           LECTURA
    // ===========================

    /**
     * Lee un archivo .huff y retorna cabecera, frecuencias y cuerpo.
     * El último byte (padBits) NO está incluido en bytesCuerpo.
     */
    public Leido leer(Path rutaEntrada) throws IOException {
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(rutaEntrada)))) {
            // 1) MAGIC + VERSION
            byte[] magiaLeida = dis.readNBytes(4);
            if (!Arrays.equals(magiaLeida, MAGIA)) {
                throw new IOException("Formato .huff inválido: MAGIC no coincide");
            }
            int version = dis.readUnsignedByte();
            if (version != VERSION) {
                throw new IOException("Versión no soportada: " + version);
            }

            // 2) Metadatos
            Cabecera cab = new Cabecera();
            cab.tamañoOriginal = dis.readLong();
            cab.totalBits = dis.readLong();
            cab.banderas = dis.readByte();
            if ((cab.banderas & 0b1) != 0) {
                cab.checksum = dis.readLong();
            }

            // 3) Tabla de símbolos
            cab.cantidadSimbolos = dis.readInt();
            Map<Byte, Long> frecuencias = new HashMap<>();
            for (int i = 0; i < cab.cantidadSimbolos; i++) {
                int simbolo = dis.readUnsignedByte();
                long freq = dis.readLong();
                frecuencias.put((byte) simbolo, freq);
            }

            // 4) Cuerpo + pad
            long tamañoArchivo = Files.size(rutaEntrada);
            long bytesLeidosHastaTabla = 4 + 1 + 8 + 8 + 1
                    + (((cab.banderas & 0b1) != 0) ? 8 : 0)
                    + 4 + (long) cab.cantidadSimbolos * (1 + 8);
            long restante = tamañoArchivo - bytesLeidosHastaTabla;
            if (restante < 1) {
                throw new EOFException("Archivo .huff truncado: falta byte de padBits");
            }
            byte[] cuerpoMasPad = dis.readNBytes((int) restante);
            if (cuerpoMasPad.length != restante) {
                throw new EOFException("EOF inesperado leyendo cuerpo");
            }

            cab.bitsRelleno = cuerpoMasPad[cuerpoMasPad.length - 1] & 0xFF;
            byte[] cuerpo = Arrays.copyOf(cuerpoMasPad, cuerpoMasPad.length - 1);

            Leido res = new Leido();
            res.cabecera = cab;
            res.frecuencias = frecuencias;
            res.bytesCuerpo = cuerpo;
            return res;
        }
    }

    // ===========================
    //         UTILIDADES
    // ===========================

    /** Contenedor de resultado del empaquetado de bits */
    private static class Empaquetado {
        byte[] bytesEmpaquetados;
        int bitsRelleno;
    }

    /** Empaqueta una cadena '0'/'1' a bytes (MSB-first) y calcula bits de relleno. */
    private Empaquetado empaquetarBits(String bits) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream((bits.length() + 7) / 8);
        int acum = 0;
        int usados = 0;

        for (int i = 0; i < bits.length(); i++) {
            int bit = (bits.charAt(i) == '1') ? 1 : 0;
            acum = (acum << 1) | bit; // MSB-first
            usados++;
            if (usados == 8) {
                bos.write(acum);
                acum = 0;
                usados = 0;
            }
        }
        int pad = 0;
        if (usados != 0) {
            pad = 8 - usados;
            acum <<= pad;
            bos.write(acum);
        }
        Empaquetado e = new Empaquetado();
        e.bytesEmpaquetados = bos.toByteArray();
        e.bitsRelleno = pad;
        return e;
    }

    /**
     * Checksum simple de demostración (NO criptográfico).
     * Puedes sustituirlo por CRC32 si lo piden.
     */
    private long calcularChecksumSimple(Frecuencias frecuencias) {
        long suma = 0L;
        for (Map.Entry<Byte, Integer> e : frecuencias.obtenerTodas().entrySet()) {
            suma += (e.getKey() & 0xFF) * 31L + e.getValue();
        }
        return suma;
    }
}