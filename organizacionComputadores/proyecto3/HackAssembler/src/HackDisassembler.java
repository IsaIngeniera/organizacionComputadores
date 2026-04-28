
/*********
 * HackDisassembler.java – Desensamblador para archivos binarios Hack (.hack).
 * Lee un archivo .hack (secuencia de líneas de 16 caracteres '0' y '1') y
 * traduce cada línea a su instrucción assembler correspondiente:
 * - Instrucción A:     @valor
 * - Instrucción C:     dest=comp;jump  (variantes sin dest o sin jump)
 * - Instrucción Shift: dest=operando<<1;jump  o  dest=operando>>1;jump
 * El archivo de salida se llama <nombre>Dis.asm.
 *
 * Uso: java HackAssembler -d Prog.hack  ->  genera ProgDis.asm
 *
 * Autor 1: Isabella Cadavid Posada
 * Autor 2: Isabella Ocampo
 *********/

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HackDisassembler {

    // Tabla inversa de comp: bits (7) -> mnemónico
    private static final Map<String, String> COMP_INV = new HashMap<>();
    // Tabla inversa de dest: bits (3) -> mnemónico
    private static final Map<String, String> DEST_INV = new HashMap<>();
    // Tabla inversa de jump: bits (3) -> mnemónico
    private static final Map<String, String> JUMP_INV = new HashMap<>();

    static {
        // comp (a + cccccc = 7 bits)
        COMP_INV.put("0101010", "0");
        COMP_INV.put("0111111", "1");
        COMP_INV.put("0111010", "-1");
        COMP_INV.put("0001100", "D");
        COMP_INV.put("0110000", "A");
        COMP_INV.put("0001101", "!D");
        COMP_INV.put("0110001", "!A");
        COMP_INV.put("0001111", "-D");
        COMP_INV.put("0110011", "-A");
        COMP_INV.put("0011111", "D+1");
        COMP_INV.put("0110111", "A+1");
        COMP_INV.put("0001110", "D-1");
        COMP_INV.put("0110010", "A-1");
        COMP_INV.put("0000010", "D+A");
        COMP_INV.put("0010011", "D-A");
        COMP_INV.put("0000111", "A-D");
        COMP_INV.put("0000000", "D&A");
        COMP_INV.put("0010101", "D|A");
        // con M (a=1)
        COMP_INV.put("1110000", "M");
        COMP_INV.put("1110001", "!M");
        COMP_INV.put("1110011", "-M");
        COMP_INV.put("1110111", "M+1");
        COMP_INV.put("1110010", "M-1");
        COMP_INV.put("1000010", "D+M");
        COMP_INV.put("1010011", "D-M");
        COMP_INV.put("1000111", "M-D");
        COMP_INV.put("1000000", "D&M");
        COMP_INV.put("1010101", "D|M");

        // dest (3 bits)
        DEST_INV.put("000", "");
        DEST_INV.put("001", "M");
        DEST_INV.put("010", "D");
        DEST_INV.put("011", "MD");
        DEST_INV.put("100", "A");
        DEST_INV.put("101", "AM");
        DEST_INV.put("110", "AD");
        DEST_INV.put("111", "AMD");

        // jump (3 bits)
        JUMP_INV.put("000", "");
        JUMP_INV.put("001", "JGT");
        JUMP_INV.put("010", "JEQ");
        JUMP_INV.put("011", "JGE");
        JUMP_INV.put("100", "JLT");
        JUMP_INV.put("101", "JNE");
        JUMP_INV.put("110", "JLE");
        JUMP_INV.put("111", "JMP");

        // operandos shift (bits [11:6] como 6 bits)
        // D -> 001100, A -> 110000, M -> 110000 (a-bit=1)
    }

    /**
     * Desensambla el archivo .hack y genera el archivo .asm de salida.
     * * @param inputFile ruta al archivo .hack
     * 
     * @param outputFile ruta al archivo de salida (_Dis.asm)
     * @return true si exitoso, false si hubo error
     */
    public boolean disassemble(String inputFile, String outputFile) {
        PrintWriter writer = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
            String line;
            int lineNum = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty())
                    continue;

                if (line.length() != 16) {
                    System.err.println("Error en línea " + lineNum
                            + ": Se esperaban 16 bits, se encontraron " + line.length() + ".");
                    writer.close();
                    new File(outputFile).delete();
                    return false;
                }

                if (!line.matches("[01]+")) {
                    System.err.println("Error en línea " + lineNum
                            + ": Caracteres inválidos (solo '0' y '1' permitidos).");
                    writer.close();
                    new File(outputFile).delete();
                    return false;
                }

                String asm = decodeInstruction(line, lineNum);
                if (asm == null) {
                    writer.close();
                    new File(outputFile).delete();
                    return false;
                }
                writer.println(asm);
            }
            return true;

        } catch (FileNotFoundException e) {
            System.err.println("Error: No se encontró el archivo '" + inputFile + "'");
            return false;
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
            return false;
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    /**
     * Decodifica una línea binaria de 16 bits a su mnemónico assembler.
     * * @param bits string de 16 caracteres '0'/'1'
     * 
     * @param lineNum número de línea para error
     * @return mnemónico assembler o null si error
     */
    private String decodeInstruction(String bits, int lineNum) {
        char bit15 = bits.charAt(0);
        char bit14 = bits.charAt(1);
        char bit13 = bits.charAt(2);

        // Instrucción A: bit 15 = 0
        if (bit15 == '0') {
            int value = Integer.parseInt(bits.substring(1), 2);
            return "@" + value;
        }

        // Instrucción Shift: bits 15=1, 14=0
        if (bit14 == '0') {
            return decodeShift(bits, lineNum);
        }

        // Instrucción C: bits 15=1, 14=1, 13=1
        if (bit13 == '1') {
            return decodeC(bits, lineNum);
        }

        System.err.println("Error en línea " + lineNum + ": Patrón de bits desconocido: " + bits);
        return null;
    }

    /** Decodifica instrucción C estándar (111accccccdddjjj) */
    private String decodeC(String bits, int lineNum) {
        // bits: 111 [a cccccc] [ddd] [jjj]
        // 0 3 9 10 12 13 15
        String compBits = bits.substring(3, 10); // a + cccccc = 7 bits
        String destBits = bits.substring(10, 13);
        String jumpBits = bits.substring(13, 16);

        String comp = COMP_INV.get(compBits);
        String dest = DEST_INV.get(destBits);
        String jump = JUMP_INV.get(jumpBits);

        if (comp == null) {
            System.err.println("Error en línea " + lineNum + ": Bits de comp desconocidos: " + compBits);
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (dest != null && !dest.isEmpty()) {
            sb.append(dest).append("=");
        }
        sb.append(comp);
        if (jump != null && !jump.isEmpty()) {
            sb.append(";").append(jump);
        }
        return sb.toString();
    }

    /**
     * Decodifica instrucción Shift.
     * Formato: [15:13] = 101 (left) o 100 (right)
     * [12] = a-bit
     * [11:6] = comp (operando)
     * [5:3] = dest
     * [2:0] = jump
     */
    private String decodeShift(String bits, int lineNum) {
        char bit13 = bits.charAt(2); // 1=left, 0=right
        char aBit = bits.charAt(3);
        String compBits = bits.substring(4, 10); // 6 bits
        String destBits = bits.substring(10, 13);
        String jumpBits = bits.substring(13, 16);

        // Determinar dirección agregando el 1 estático
        String shiftOp = (bit13 == '1') ? "<<1" : ">>1";

        // Determinar operando
        String operand;
        if (compBits.equals("001100")) {
            operand = "D";
        } else if (compBits.equals("110000")) {
            operand = (aBit == '1') ? "M" : "A";
        } else {
            System.err.println("Error en línea " + lineNum
                    + ": Bits de operando Shift desconocidos: " + compBits);
            return null;
        }

        String dest = DEST_INV.get(destBits);
        String jump = JUMP_INV.get(jumpBits);

        StringBuilder sb = new StringBuilder();
        if (dest != null && !dest.isEmpty()) {
            sb.append(dest).append("=");
        }
        sb.append(operand).append(shiftOp);
        if (jump != null && !jump.isEmpty()) {
            sb.append(";").append(jump);
        }
        return sb.toString();
    }
}