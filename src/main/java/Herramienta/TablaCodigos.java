package Herramienta;

import java.util.*;

public class TablaCodigos {

    private Map<Byte, String> codigos = new HashMap<>();

    // 1️⃣ Agregar un nuevo código a la tabla
    public void agregarCodigo(byte simbolo, String codigo) {
        codigos.put(simbolo, codigo);
    }

    // 2️⃣ Obtener el código de un símbolo específico
    public String obtenerCodigo(byte simbolo) {
        return codigos.get(simbolo);
    }

    // 3️⃣ Devolver el mapa completo (para recorrer o guardar)
    public Map<Byte, String> obtenerTodos() {
        return codigos;
    }

    // 4️⃣ Mostrar tabla con frecuencias y códigos
    public void mostrarTabla(Frecuencias frecuencias) {
        System.out.println();
        System.out.println("Símbolo | Frecuencia | Código Huffman");
        System.out.println("--------------------------------------");

        for (Map.Entry<Byte, String> entrada : codigos.entrySet()) {
            byte simbolo = entrada.getKey();
            String codigo = entrada.getValue();
            int frecuencia = frecuencias.obtenerFrecuencia(simbolo);
            char caracter = (char) (simbolo & 0xFF);

            // Mostrar carácter legible si es visible, si no, mostrar el byte en hexadecimal
            String simboloTexto = (caracter >= 32 && caracter <= 126) ? String.valueOf(caracter)
                    : String.format("\\x%02X", simbolo);

            System.out.printf("   %-6s |   %-9d |   %s%n", simboloTexto, frecuencia, codigo);
        }
        System.out.println();
    }

    // 5️⃣ Cantidad de códigos generados
    public int cantidad() {
        return codigos.size();
    }
}