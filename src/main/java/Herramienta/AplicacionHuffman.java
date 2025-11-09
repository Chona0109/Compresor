package Herramienta;
import java.nio.file.*;
import java.util.Locale;

public class AplicacionHuffman {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Uso:");
            System.out.println("  java AplicacionHuffman comprimir <entrada.txt> <salida.huff>");
            System.out.println("  java AplicacionHuffman descomprimir <entrada.huff> <salida.txt>");
            System.out.println();
            System.out.println("Ejemplo: java AplicacionHuffman comprimir hola.txt hola.huff");
            return;
        }

        String comando = args[0].toLowerCase(Locale.ROOT);
        Path entrada = Paths.get(args[1]);
        Path salida = Paths.get(args[2]);

        long inicio = System.nanoTime();

        switch (comando) {
            case "comprimir" -> {
                CompresorHuffman compresor = new CompresorHuffman();
                compresor.comprimir(entrada, salida);
            }
            case "descomprimir" -> {
                DescompresorHuffman descompresor = new DescompresorHuffman();
                descompresor.descomprimir(entrada, salida);
            }
            default -> {
                System.out.println("Comando no reconocido: " + comando);
                return;
            }
        }

        long fin = System.nanoTime();
        double segundos = (fin - inicio) / 1e9;
        System.out.printf("Operación '%s' completada en %.3f segundos.%n", comando, segundos);
    }
}