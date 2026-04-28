/*********
 * HackAssembler.java – Punto de entrada principal del ensamblador/desensamblador Hack.
 *   Traduce archivos .asm (assembler Hack con soporte de instrucciones Shift <<, >>)
 *   a archivos .hack (binario de 16 bits), o invoca el desensamblador con el flag -d.
 *   Si se ejecuta sin argumentos, muestra un menú interactivo.
 *
 * Autor 1: Isabella Cadavid Posada
 * Autor 2: Isabella Ocampo Sánchez
 *********/

import java.io.*;
import java.util.Scanner;

public class HackAssembler {

    public static void main(String[] args) {
        if (args.length == 0) {
            runInteractiveMenu();
        } else if (args.length == 1) {
            // Ensamblar: java HackAssembler Prog.asm
            String inputFile = args[0];
            if (!inputFile.endsWith(".asm")) {
                System.err.println("Error: El archivo de entrada debe tener extension .asm");
                System.exit(1);
            }
            assemble(inputFile);
        } else if (args.length == 2 && args[0].equals("-d")) {
            // Desensamblar: java HackAssembler -d Prog.hack
            String inputFile = args[1];
            if (!inputFile.endsWith(".hack")) {
                System.err.println("Error: El archivo de entrada debe tener extension .hack");
                System.exit(1);
            }
            disassemble(inputFile);
        } else {
            printUsage();
            System.exit(1);
        }
    }

    /** Ejecuta el proceso de ensamblado de un archivo .asm a .hack */
    private static void assemble(String inputFile) {
        String outputFile = inputFile.replace(".asm", ".hack");
        Assembler assembler = new Assembler();
        boolean success = assembler.assemble(inputFile, outputFile);
        if (success) {
            System.out.println("Ensamblado exitoso: " + outputFile);
        } else {
            System.exit(1);
        }
    }

    /** Ejecuta el proceso de desensamblado de un archivo .hack a .asm */
    private static void disassemble(String inputFile) {
        // Genera nombre: Prog.hack -> ProgDis.asm
        String baseName = inputFile.replace(".hack", "");
        String outputFile = baseName + "Dis.asm";
        HackDisassembler disassembler = new HackDisassembler();
        boolean success = disassembler.disassemble(inputFile, outputFile);
        if (success) {
            System.out.println("Desensamblado exitoso: " + outputFile);
        } else {
            System.exit(1);
        }
    }

    /** Menú interactivo cuando no se pasan argumentos */
    private static void runInteractiveMenu() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("  HackAssembler - Organización de Computadores");
        System.out.println("  Universidad EAFIT - 2026-1");
        System.out.println("==============================================");
        boolean running = true;
        while (running) {
            System.out.println("\nSeleccione una opción:");
            System.out.println("  1) Ensamblar archivo .asm -> .hack");
            System.out.println("  2) Desensamblar archivo .hack -> .asm");
            System.out.println("  3) Salir");
            System.out.print("Opción: ");
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1":
                    System.out.print("Ingrese la ruta del archivo .asm: ");
                    String asmFile = scanner.nextLine().trim();
                    if (!asmFile.endsWith(".asm")) {
                        System.err.println("Error: el archivo debe tener extension .asm");
                    } else {
                        assemble(asmFile);
                    }
                    break;
                case "2":
                    System.out.print("Ingrese la ruta del archivo .hack: ");
                    String hackFile = scanner.nextLine().trim();
                    if (!hackFile.endsWith(".hack")) {
                        System.err.println("Error: el archivo debe tener extension .hack");
                    } else {
                        disassemble(hackFile);
                    }
                    break;
                case "3":
                    running = false;
                    System.out.println("Hasta luego.");
                    break;
                default:
                    System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
        scanner.close();
    }

    private static void printUsage() {
        System.out.println("Uso:");
        System.out.println("  java HackAssembler <archivo.asm>       -> Ensambla a .hack");
        System.out.println("  java HackAssembler -d <archivo.hack>   -> Desensambla a .asm");
        System.out.println("  java HackAssembler                     -> Menú interactivo");
    }
}