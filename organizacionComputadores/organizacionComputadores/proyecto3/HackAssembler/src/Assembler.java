/*********
 * Assembler.java – Orquesta el proceso completo de ensamblado en dos pasadas.
 *   Primera pasada: construye la tabla de símbolos (etiquetas).
 *   Segunda pasada: traduce cada instrucción A, C o Shift a código binario de 16 bits.
 *   Soporta las instrucciones de desplazamiento extendidas: <<  (shift left) y >> (shift right).
 *
 * Autor 1: Isabella Cadavid Posada
 * Autor 2: Isabella Ocampo Sánchez
 *********/

import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class Assembler {

    /**
     * Ensambla el archivo de entrada y escribe el resultado en el archivo de salida.
     * @param inputFile  ruta al archivo .asm
     * @param outputFile ruta al archivo .hack de salida
     * @return true si el proceso fue exitoso, false si hubo algún error
     */
    public boolean assemble(String inputFile, String outputFile) {
        // --- Leer todas las líneas del archivo ---
        List<String> rawLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                rawLines.add(line);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: No se encontró el archivo '" + inputFile + "'");
            return false;
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo: " + e.getMessage());
            return false;
        }

        // --- Primera pasada: construir tabla de símbolos ---
        SymbolTable symbolTable = new SymbolTable();
        Parser parserPass1 = new Parser();
        int romAddress = 0;

        for (int i = 0; i < rawLines.size(); i++) {
            int lineNum = i + 1;
            String cleaned = parserPass1.cleanLine(rawLines.get(i));
            if (cleaned.isEmpty()) continue;

            String instrType = parserPass1.instructionType(cleaned, lineNum);
            if (instrType == null) return false; // error ya impreso

            if (instrType.equals("L_INSTRUCTION")) {
                // (LABEL) -> agrega símbolo con la dirección ROM actual
                String symbol = parserPass1.symbol(cleaned, lineNum);
                if (symbol == null) return false;
                if (!symbolTable.contains(symbol)) {
                    symbolTable.addEntry(symbol, romAddress);
                }
            } else {
                // A_INSTRUCTION o C_INSTRUCTION (incluyendo shift) incrementan ROM
                romAddress++;
            }
        }

        // --- Segunda pasada: traducir instrucciones ---
        int variableAddress = 16;
        Parser parserPass2 = new Parser();
        CodeGenerator codeGen = new CodeGenerator();

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));

            for (int i = 0; i < rawLines.size(); i++) {
                int lineNum = i + 1;
                String cleaned = parserPass2.cleanLine(rawLines.get(i));
                if (cleaned.isEmpty()) continue;

                String instrType = parserPass2.instructionType(cleaned, lineNum);
                if (instrType == null) {
                    writer.close();
                    new File(outputFile).delete();
                    return false;
                }

                if (instrType.equals("L_INSTRUCTION")) {
                    continue; // las etiquetas no generan código
                }

                String binaryLine;

                if (instrType.equals("A_INSTRUCTION")) {
                    String symbol = parserPass2.symbol(cleaned, lineNum);
                    if (symbol == null) {
                        writer.close();
                        new File(outputFile).delete();
                        return false;
                    }
                    int value;
                    try {
                        value = Integer.parseInt(symbol);
                        if (value < 0) {
                            System.err.println("Error en línea " + lineNum + ": Número negativo no permitido.");
                            writer.close();
                            new File(outputFile).delete();
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        // es un símbolo/variable
                        if (!symbolTable.contains(symbol)) {
                            symbolTable.addEntry(symbol, variableAddress);
                            variableAddress++;
                        }
                        value = symbolTable.getAddress(symbol);
                    }
                    binaryLine = codeGen.generateA(value, lineNum);
                    if (binaryLine == null) {
                        writer.close();
                        new File(outputFile).delete();
                        return false;
                    }

                } else {
                    // C_INSTRUCTION o SHIFT_INSTRUCTION
                    binaryLine = codeGen.generateC(cleaned, lineNum);
                    if (binaryLine == null) {
                        writer.close();
                        new File(outputFile).delete();
                        return false;
                    }
                }

                writer.println(binaryLine);
            }

            return true;

        } catch (IOException e) {
            System.err.println("Error escribiendo el archivo de salida: " + e.getMessage());
            return false;
        } finally {
            if (writer != null) writer.close();
        }
    }
}
