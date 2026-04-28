
/*********
 * CodeGenerator.java – Genera el código binario de 16 bits para instrucciones Hack.
 *   Convierte instrucciones A (tipo @xxx) e instrucciones C estándar y de Shift
 *   al formato binario de la arquitectura Hack modificada.
 *
 *   Formato instrucción A:  0vvvvvvvvvvvvvvv  (15 bits de valor)
 *   Formato instrucción C:  111accccccdddjjj
 *   Formato Shift left:     101cccc000ddd000  (bits 14=0, 13=1 para shift left)
 *   Formato Shift right:    100cccc000ddd000  (bits 14=0, 13=0 para shift right)
 *
 *   Extensión Shift (basada en especificación EAFIT Nand2Tetris):
 *     Los bits [15:13] = 101 para shift left, 100 para shift right.
 *     Bit 12 (a-bit) = 0 siempre en shift.
 *     Bits [11:6] codifican el registro fuente: D=001100, A=110000, M=110000 con a=1.
 *
 * Autor 1: Isabella Cadavid Posada
 * Autor 2: Isabella Ocampo
 *********/

import java.util.HashMap;
import java.util.Map;

public class CodeGenerator {

    // Tabla de comp para instrucciones C estándar (a=0 y a=1)
    private static final Map<String, String> COMP_TABLE = new HashMap<>();
    // Tabla de dest
    private static final Map<String, String> DEST_TABLE = new HashMap<>();
    // Tabla de jump
    private static final Map<String, String> JUMP_TABLE = new HashMap<>();

    static {
        // comp a=0
        COMP_TABLE.put("0", "0101010");
        COMP_TABLE.put("1", "0111111");
        COMP_TABLE.put("-1", "0111010");
        COMP_TABLE.put("D", "0001100");
        COMP_TABLE.put("A", "0110000");
        COMP_TABLE.put("!D", "0001101");
        COMP_TABLE.put("!A", "0110001");
        COMP_TABLE.put("-D", "0001111");
        COMP_TABLE.put("-A", "0110011");
        COMP_TABLE.put("D+1", "0011111");
        COMP_TABLE.put("A+1", "0110111");
        COMP_TABLE.put("D-1", "0001110");
        COMP_TABLE.put("A-1", "0110010");
        COMP_TABLE.put("D+A", "0000010");
        COMP_TABLE.put("D-A", "0010011");
        COMP_TABLE.put("A-D", "0000111");
        COMP_TABLE.put("D&A", "0000000");
        COMP_TABLE.put("D|A", "0010101");
        // comp a=1 (M en lugar de A)
        COMP_TABLE.put("M", "1110000");
        COMP_TABLE.put("!M", "1110001");
        COMP_TABLE.put("-M", "1110011");
        COMP_TABLE.put("M+1", "1110111");
        COMP_TABLE.put("M-1", "1110010");
        COMP_TABLE.put("D+M", "1000010");
        COMP_TABLE.put("D-M", "1010011");
        COMP_TABLE.put("M-D", "1000111");
        COMP_TABLE.put("D&M", "1000000");
        COMP_TABLE.put("D|M", "1010101");

        // dest
        DEST_TABLE.put("", "000");
        DEST_TABLE.put("M", "001");
        DEST_TABLE.put("D", "010");
        DEST_TABLE.put("MD", "011");
        DEST_TABLE.put("A", "100");
        DEST_TABLE.put("AM", "101");
        DEST_TABLE.put("AD", "110");
        DEST_TABLE.put("AMD", "111");

        // jump
        JUMP_TABLE.put("", "000");
        JUMP_TABLE.put("JGT", "001");
        JUMP_TABLE.put("JEQ", "010");
        JUMP_TABLE.put("JGE", "011");
        JUMP_TABLE.put("JLT", "100");
        JUMP_TABLE.put("JNE", "101");
        JUMP_TABLE.put("JLE", "110");
        JUMP_TABLE.put("JMP", "111");
    }

    private final Parser parser = new Parser();

    /**
     * Genera el código binario para una instrucción A.
     * 
     * @param value   valor numérico (0..32767)
     * @param lineNum número de línea para error
     * @return String de 16 bits o null si error
     */
    public String generateA(int value, int lineNum) {
        if (value < 0 || value > 32767) {
            System.err.println("Error en línea " + lineNum + ": Valor fuera de rango (0-32767): " + value);
            return null;
        }
        return String.format("0%15s", Integer.toBinaryString(value)).replace(' ', '0');
    }

    /**
     * Genera el código binario para una instrucción C o Shift.
     * 
     * @param cleanedLine línea limpia (sin comentarios)
     * @param lineNum     número de línea para error
     * @return String de 16 bits o null si error
     */
    public String generateC(String cleanedLine, int lineNum) {
        String instrType = parser.instructionType(cleanedLine, lineNum);
        if (instrType == null)
            return null;

        if ("SHIFT_INSTRUCTION".equals(instrType)) {
            return generateShift(cleanedLine, lineNum);
        }
        return generateStandardC(cleanedLine, lineNum);
    }

    /** Genera binario para instrucción C estándar: 111accccccdddjjj */
    private String generateStandardC(String line, int lineNum) {
        String destStr = parser.dest(line);
        String compStr = parser.comp(line);
        String jumpStr = parser.jump(line);

        if (!COMP_TABLE.containsKey(compStr)) {
            System.err.println("Error en línea " + lineNum + ": Comp inválido: '" + compStr + "'");
            return null;
        }
        if (!DEST_TABLE.containsKey(destStr)) {
            System.err.println("Error en línea " + lineNum + ": Dest inválido: '" + destStr + "'");
            return null;
        }
        if (!JUMP_TABLE.containsKey(jumpStr)) {
            System.err.println("Error en línea " + lineNum + ": Jump inválido: '" + jumpStr + "'");
            return null;
        }

        String compBits = COMP_TABLE.get(compStr); // 7 bits (a + cccccc)
        String destBits = DEST_TABLE.get(destStr); // 3 bits
        String jumpBits = JUMP_TABLE.get(jumpStr); // 3 bits

        return "111" + compBits + destBits + jumpBits;
    }

    /**
     * Genera binario para instrucción Shift.
     *
     * Formato: bits[15:13] indican shift (101=left, 100=right)
     * bit[12] = 0 (a-bit fijo en 0 para shift)
     * bits[11:6] codifican el operando:
     * D -> 001100 (shift left) / 001100 (shift right)
     * A -> 110000
     * M -> 110000 con bit12=1 (no soportado en extensión básica)
     * bits[5:3] = dest
     * bits[2:0] = jump (normalmente 000 para shift)
     *
     * Instrucciones soportadas:
     * D=D<< D=A<< D=D>> D=A>>
     * y sus variantes con dest (A, M, AMD, etc.) y jump.
     */
    private String generateShift(String line, int lineNum) {
        String destStr = parser.dest(line);
        String jumpStr = parser.jump(line);
        String operand = parser.shiftOperand(line);
        String direction = parser.shiftDirection(line);

        // Bits de tipo shift: 15=1, 14=0, 13=1(left)/0(right)
        String prefix;
        if ("LEFT".equals(direction)) {
            prefix = "101";
        } else {
            prefix = "100";
        }

        // bit 12 (a-bit): 0 para A/D, 1 para M
        String aBit;
        // Bits [11:6] -> comp del operando
        String compBits;
        switch (operand) {
            case "D":
                aBit = "0";
                compBits = "001100";
                break;
            case "A":
                aBit = "0";
                compBits = "110000";
                break;
            case "M":
                aBit = "1";
                compBits = "110000";
                break;
            default:
                System.err.println("Error en línea " + lineNum
                        + ": Operando de Shift inválido: '" + operand + "'. Use D, A o M.");
                return null;
        }

        if (!DEST_TABLE.containsKey(destStr)) {
            System.err.println("Error en línea " + lineNum + ": Dest inválido: '" + destStr + "'");
            return null;
        }
        if (!JUMP_TABLE.containsKey(jumpStr)) {
            System.err.println("Error en línea " + lineNum + ": Jump inválido: '" + jumpStr + "'");
            return null;
        }

        String destBits = DEST_TABLE.get(destStr);
        String jumpBits = JUMP_TABLE.get(jumpStr);

        // Formato: [15:13]=prefix [12]=aBit [11:6]=compBits [5:3]=destBits
        // [2:0]=jumpBits
        return prefix + aBit + compBits + destBits + jumpBits;
    }
}
