
/*********
 * HackAssemblerTest.java – Suite de pruebas unitarias para HackAssembler y HackDisassembler.
 *   Verifica el correcto ensamblado de instrucciones A, C y Shift (<<, >>),
 *   el manejo de errores y la integridad del ciclo ensamblar->desensamblar.
 *
 * Autor 1: Isabella Cadavid Posada
 * Autor 2: Isabella Ocampo
 *********/

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class HackAssemblerTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("====================================");
        System.out.println("  HackAssembler - Suite de Pruebas");
        System.out.println("====================================\n");

        testInstructionA();
        testInstructionC();
        testShiftLeft();
        testShiftRight();
        testSymbolTable();
        testDisassemblerA();
        testDisassemblerC();
        testDisassemblerShift();
        testErrorHandling();
        testRoundTrip();

        System.out.println("\n====================================");
        System.out.printf("  Resultados: %d pasadas, %d fallidas%n", passed, failed);
        System.out.println("====================================");

        if (failed > 0)
            System.exit(1);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private static void assertEquals(String testName, String expected, String actual) {
        if (expected.equals(actual)) {
            System.out.println("[PASS] " + testName);
            passed++;
        } else {
            System.out.println("[FAIL] " + testName);
            System.out.println("       Esperado: " + expected);
            System.out.println("       Obtenido: " + actual);
            failed++;
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            passed++;
        } else {
            System.out.println("[FAIL] " + testName);
            failed++;
        }
    }

    /** Escribe un archivo temporal y retorna su ruta */
    private static Path writeTempFile(String suffix, List<String> lines) throws IOException {
        Path p = Files.createTempFile("hacktest_", suffix);
        Files.write(p, lines);
        return p;
    }

    /** Lee todas las líneas de un archivo */
    private static List<String> readLines(Path p) throws IOException {
        return Files.readAllLines(p);
    }

    // ─── Tests de CodeGenerator (instrucción A) ──────────────────────────────

    private static void testInstructionA() {
        System.out.println("\n--- Instrucción A ---");
        CodeGenerator cg = new CodeGenerator();
        assertEquals("@0  -> 0000000000000000", "0000000000000000", cg.generateA(0, 1));
        assertEquals("@1  -> 0000000000000001", "0000000000000001", cg.generateA(1, 1));
        assertEquals("@2  -> 0000000000000010", "0000000000000010", cg.generateA(2, 1));
        assertEquals("@21 -> 0000000000010101", "0000000000010101", cg.generateA(21, 1));
        assertEquals("@32767 -> max 15 bits", "0111111111111111", cg.generateA(32767, 1));
        assertTrue("@-1 retorna null", cg.generateA(-1, 1) == null);
        assertTrue("@32768 retorna null", cg.generateA(32768, 1) == null);
    }

    // ─── Tests de CodeGenerator (instrucción C) ──────────────────────────────

    private static void testInstructionC() {
        System.out.println("\n--- Instrucción C ---");
        CodeGenerator cg = new CodeGenerator();
        // D=A -> 111 0110000 010 000
        assertEquals("D=A", "1110110000010000", cg.generateC("D=A", 1));
        // D=D+A -> 111 0000010 010 000
        assertEquals("D=D+A", "1110000010010000", cg.generateC("D=D+A", 1));
        // M=D -> 111 0001100 001 000
        assertEquals("M=D", "1110001100001000", cg.generateC("M=D", 1));
        // 0;JMP -> 111 0101010 000 111
        assertEquals("0;JMP", "1110101010000111", cg.generateC("0;JMP", 1));
        // D;JGT -> 111 0001100 000 001
        assertEquals("D;JGT", "1110001100000001", cg.generateC("D;JGT", 1));
        // AMD=D|M -> 111 1010101 111 000
        assertEquals("AMD=D|M", "1111010101111000", cg.generateC("AMD=D|M", 1));
    }

    // ─── Tests de instrucción Shift Left ─────────────────────────────────────

    private static void testShiftLeft() {
        System.out.println("\n--- Instrucción Shift Left (<<) ---");
        CodeGenerator cg = new CodeGenerator();
        // D=D<< -> prefix=101, aBit=0, comp=001100, dest=010, jump=000
        String result = cg.generateC("D=D<<", 1);
        assertTrue("D=D<< bit15=1", result != null && result.charAt(0) == '1');
        assertTrue("D=D<< bit14=0", result != null && result.charAt(1) == '0');
        assertTrue("D=D<< bit13=1 (left)", result != null && result.charAt(2) == '1');
        assertEquals("D=D<< completo", "1010" + "001100" + "010" + "000", result);

        // D=A<< -> prefix=101, aBit=0, comp=110000, dest=010, jump=000
        String resultA = cg.generateC("D=A<<", 1);
        assertEquals("D=A<<", "1010" + "110000" + "010" + "000", resultA);

        // A=M<< -> prefix=101, aBit=1, comp=110000, dest=100, jump=000
        String resultM = cg.generateC("A=M<<", 1);
        assertEquals("A=M<<", "1011" + "110000" + "100" + "000", resultM);
    }

    // ─── Tests de instrucción Shift Right ────────────────────────────────────

    private static void testShiftRight() {
        System.out.println("\n--- Instrucción Shift Right (>>) ---");
        CodeGenerator cg = new CodeGenerator();
        // D=D>> -> prefix=100, aBit=0, comp=001100, dest=010, jump=000
        assertEquals("D=D>>", "1000" + "001100" + "010" + "000", cg.generateC("D=D>>", 1));
        // D=A>> -> prefix=100, aBit=0, comp=110000, dest=010, jump=000
        assertEquals("D=A>>", "1000" + "110000" + "010" + "000", cg.generateC("D=A>>", 1));
    }

    // ─── Tests de tabla de símbolos ──────────────────────────────────────────

    private static void testSymbolTable() {
        System.out.println("\n--- Tabla de Símbolos ---");
        SymbolTable st = new SymbolTable();
        assertTrue("R0=0", st.getAddress("R0") == 0);
        assertTrue("R15=15", st.getAddress("R15") == 15);
        assertTrue("SP=0", st.getAddress("SP") == 0);
        assertTrue("SCREEN=16384", st.getAddress("SCREEN") == 16384);
        assertTrue("KBD=24576", st.getAddress("KBD") == 24576);
        st.addEntry("myVar", 16);
        assertTrue("myVar added", st.contains("myVar") && st.getAddress("myVar") == 16);
    }

    // ─── Tests del desensamblador (instrucción A) ────────────────────────────

    private static void testDisassemblerA() throws IOException {
        System.out.println("\n--- Desensamblador: instrucción A ---");
        HackDisassembler dis = new HackDisassembler();
        Path input = writeTempFile(".hack", Arrays.asList(
                "0000000000000000", // @0
                "0000000000000001", // @1
                "0000000000010101" // @21
        ));
        Path output = Files.createTempFile("hackdis_", ".asm");
        boolean ok = dis.disassemble(input.toString(), output.toString());
        assertTrue("Desensamblar A: sin error", ok);
        List<String> lines = readLines(output);
        assertEquals("@0", "@0", lines.get(0));
        assertEquals("@1", "@1", lines.get(1));
        assertEquals("@21", "@21", lines.get(2));
        input.toFile().delete();
        output.toFile().delete();
    }

    // ─── Tests del desensamblador (instrucción C) ────────────────────────────

    private static void testDisassemblerC() throws IOException {
        System.out.println("\n--- Desensamblador: instrucción C ---");
        HackDisassembler dis = new HackDisassembler();
        Path input = writeTempFile(".hack", Arrays.asList(
                "1110110000010000", // D=A
                "1110001100001000", // M=D
                "1110101010000111" // 0;JMP
        ));
        Path output = Files.createTempFile("hackdis_", ".asm");
        boolean ok = dis.disassemble(input.toString(), output.toString());
        assertTrue("Desensamblar C: sin error", ok);
        List<String> lines = readLines(output);
        assertEquals("D=A", "D=A", lines.get(0));
        assertEquals("M=D", "M=D", lines.get(1));
        assertEquals("0;JMP", "0;JMP", lines.get(2));
        input.toFile().delete();
        output.toFile().delete();
    }

    // ─── Tests del desensamblador (instrucción Shift) ────────────────────────

    private static void testDisassemblerShift() throws IOException {
        System.out.println("\n--- Desensamblador: instrucción Shift ---");
        HackDisassembler dis = new HackDisassembler();
        Path input = writeTempFile(".hack", Arrays.asList(
                "1010001100010000", // D=D<<
                "1000001100010000" // D=D>>
        ));
        Path output = Files.createTempFile("hackdis_", ".asm");
        boolean ok = dis.disassemble(input.toString(), output.toString());
        assertTrue("Desensamblar Shift: sin error", ok);
        List<String> lines = readLines(output);
        assertEquals("D=D<<", "D=D<<", lines.get(0));
        assertEquals("D=D>>", "D=D>>", lines.get(1));
        input.toFile().delete();
        output.toFile().delete();
    }

    // ─── Tests de manejo de errores ──────────────────────────────────────────

    private static void testErrorHandling() throws IOException {
        System.out.println("\n--- Manejo de errores ---");
        HackDisassembler dis = new HackDisassembler();

        // Línea con menos de 16 bits
        Path bad1 = writeTempFile(".hack", Arrays.asList("000000000000000")); // 15 bits
        Path out1 = Files.createTempFile("err_", ".asm");
        assertTrue("Error: menos de 16 bits", !dis.disassemble(bad1.toString(), out1.toString()));
        bad1.toFile().delete();
        out1.toFile().delete();

        // Línea con carácter inválido
        Path bad2 = writeTempFile(".hack", Arrays.asList("000000000000002X"));
        Path out2 = Files.createTempFile("err_", ".asm");
        assertTrue("Error: carácter inválido", !dis.disassemble(bad2.toString(), out2.toString()));
        bad2.toFile().delete();
        out2.toFile().delete();

        // CodeGenerator con comp inválido
        CodeGenerator cg = new CodeGenerator();
        assertTrue("Error: comp inválido", cg.generateC("D=INVALID", 5) == null);
    }

    // ─── Test de ciclo completo (ensamblar -> desensamblar) ──────────────────

    private static void testRoundTrip() throws IOException {
        System.out.println("\n--- Ciclo completo: ASM -> HACK -> ASM ---");

        List<String> asmLines = Arrays.asList(
                "// Programa de prueba round-trip",
                "@10",
                "D=A",
                "@R0",
                "M=D",
                "D=D<<",
                "D=D>>",
                "0;JMP");

        Path asmIn = writeTempFile(".asm", asmLines);
        Path hackOut = Files.createTempFile("rt_", ".hack");
        Path asmOut = Files.createTempFile("rt_dis_", ".asm");

        Assembler asm = new Assembler();
        boolean ok1 = asm.assemble(asmIn.toString(), hackOut.toString());
        assertTrue("Round-trip: ensamblado exitoso", ok1);

        HackDisassembler dis = new HackDisassembler();
        boolean ok2 = dis.disassemble(hackOut.toString(), asmOut.toString());
        assertTrue("Round-trip: desensamblado exitoso", ok2);

        List<String> hackLines = readLines(hackOut);
        assertTrue("Round-trip: .hack tiene 7 líneas", hackLines.size() == 7);

        List<String> disLines = readLines(asmOut);
        assertEquals("Round-trip: @10", "@10", disLines.get(0));
        assertEquals("Round-trip: D=A", "D=A", disLines.get(1));
        assertEquals("Round-trip: 0;JMP", "0;JMP", disLines.get(6));

        asmIn.toFile().delete();
        hackOut.toFile().delete();
        asmOut.toFile().delete();
    }
}
