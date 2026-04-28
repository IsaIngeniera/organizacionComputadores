/*********
 * Parser.java – Analiza líneas individuales de código assembler Hack.
 *   Detecta el tipo de instrucción (A, C, L, Shift) y extrae sus componentes:
 *   destino, cómputo y salto para instrucciones C; símbolo para A y etiquetas;
 *   dirección y tipo de shift para instrucciones de desplazamiento.
 *
 * Tipos de instrucción reconocidos:
 *   A_INSTRUCTION     -> @valor o @símbolo
 *   C_INSTRUCTION     -> dest=comp;jump  (instrucción estándar Hack)
 *   SHIFT_INSTRUCTION -> dest=comp<<;jump  o  dest=comp>>;jump  (extensión Hack)
 *   L_INSTRUCTION     -> (ETIQUETA)
 *
 * Autor 1: Isabella Cadavid Posada
 * Autor 2: Isabella Ocampo Sánchez
 *********/

public class Parser {

    /**
     * Limpia una línea: elimina comentarios y espacios en blanco.
     * @param line línea original del archivo
     * @return línea limpia sin comentarios ni espacios extra
     */
    public String cleanLine(String line) {
        // Eliminar comentario inline
        int commentIdx = line.indexOf("//");
        if (commentIdx >= 0) {
            line = line.substring(0, commentIdx);
        }
        return line.trim();
    }

    /**
     * Determina el tipo de instrucción de una línea ya limpiada.
     * @param cleanedLine línea sin comentarios y sin espacios extra
     * @param lineNum     número de línea original (para mensajes de error)
     * @return "A_INSTRUCTION", "C_INSTRUCTION", "SHIFT_INSTRUCTION", "L_INSTRUCTION" o null si error
     */
    public String instructionType(String cleanedLine, int lineNum) {
        if (cleanedLine.isEmpty()) return null; // no debería llegar aquí

        if (cleanedLine.startsWith("@")) {
            return "A_INSTRUCTION";
        }
        if (cleanedLine.startsWith("(") && cleanedLine.endsWith(")")) {
            return "L_INSTRUCTION";
        }
        // Detectar shift: contiene << o >>
        if (cleanedLine.contains("<<") || cleanedLine.contains(">>")) {
            return "SHIFT_INSTRUCTION";
        }
        // Todo lo demás es instrucción C
        return "C_INSTRUCTION";
    }

    /**
     * Extrae el símbolo de una instrucción A (@xxx) o de una etiqueta ((xxx)).
     * @param cleanedLine línea limpia
     * @param lineNum     número de línea para mensajes de error
     * @return el símbolo/número como String, o null si hay error
     */
    public String symbol(String cleanedLine, int lineNum) {
        if (cleanedLine.startsWith("@")) {
            String sym = cleanedLine.substring(1).trim();
            if (sym.isEmpty()) {
                System.err.println("Error en línea " + lineNum + ": Instrucción '@' sin símbolo.");
                return null;
            }
            return sym;
        }
        if (cleanedLine.startsWith("(") && cleanedLine.endsWith(")")) {
            String sym = cleanedLine.substring(1, cleanedLine.length() - 1).trim();
            if (sym.isEmpty()) {
                System.err.println("Error en línea " + lineNum + ": Etiqueta vacía '()'.");
                return null;
            }
            if (!isValidSymbol(sym)) {
                System.err.println("Error en línea " + lineNum + ": Símbolo inválido '" + sym + "'.");
                return null;
            }
            return sym;
        }
        System.err.println("Error en línea " + lineNum + ": No es instrucción A ni etiqueta.");
        return null;
    }

    /**
     * Extrae el campo dest de una instrucción C o Shift.
     * Formato: dest=comp;jump  o  comp;jump  o  dest=comp
     * @param cleanedLine línea limpia
     * @return dest como String (puede ser vacío si no hay '=')
     */
    public String dest(String cleanedLine) {
        if (cleanedLine.contains("=")) {
            return cleanedLine.substring(0, cleanedLine.indexOf("=")).trim();
        }
        return ""; // sin destino
    }

    /**
     * Extrae el campo comp de una instrucción C o Shift.
     * Para Shift elimina los << o >> del comp antes de retornarlo como base.
     * @param cleanedLine línea limpia
     * @return comp como String
     */
    public String comp(String cleanedLine) {
        String rest = cleanedLine;
        // Quitar dest
        if (rest.contains("=")) {
            rest = rest.substring(rest.indexOf("=") + 1).trim();
        }
        // Quitar jump
        if (rest.contains(";")) {
            rest = rest.substring(0, rest.indexOf(";")).trim();
        }
        return rest;
    }

    /**
     * Extrae el campo jump de una instrucción C o Shift.
     * @param cleanedLine línea limpia
     * @return jump como String (vacío si no hay ';')
     */
    public String jump(String cleanedLine) {
        if (cleanedLine.contains(";")) {
            return cleanedLine.substring(cleanedLine.indexOf(";") + 1).trim();
        }
        return "";
    }

    /**
     * Para instrucciones Shift: indica si es left (<<) o right (>>).
     * @param cleanedLine línea limpia
     * @return "LEFT" o "RIGHT" o null si no es shift
     */
    public String shiftDirection(String cleanedLine) {
        if (cleanedLine.contains("<<")) return "LEFT";
        if (cleanedLine.contains(">>")) return "RIGHT";
        return null;
    }

    /**
     * Extrae el operando de una instrucción Shift (lo que se desplaza).
     * Ej: "D=D<<;JGT" -> operand = "D"
     * @param cleanedLine línea limpia
     * @return el registro/valor a desplazar
     */
    public String shiftOperand(String cleanedLine) {
        String compField = comp(cleanedLine);
        // Remover << o >>
        compField = compField.replace("<<", "").replace(">>", "").trim();
        return compField;
    }

    /** Valida que un símbolo sea alfanumérico y no empiece por dígito */
    private boolean isValidSymbol(String sym) {
        if (sym.isEmpty()) return false;
        char first = sym.charAt(0);
        if (Character.isDigit(first)) return false;
        for (char c : sym.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '.' && c != '$' && c != ':') {
                return false;
            }
        }
        return true;
    }
}
