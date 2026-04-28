/*********
 * SymbolTable.java – Tabla de símbolos para el ensamblador Hack.
 *   Mantiene un mapa de nombres de símbolos (etiquetas y variables) a sus
 *   direcciones de memoria o ROM correspondientes.
 *   Pre-cargada con todos los símbolos predefinidos de la arquitectura Hack.
 *
 * Autor 1: Isabella Cadavid Posada
 * Autor 2: Isabella Ocampo Sánchez
 *********/

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private final Map<String, Integer> table = new HashMap<>();

    /** Constructor: inicializa con los símbolos predefinidos de Hack */
    public SymbolTable() {
        // Registros
        for (int i = 0; i <= 15; i++) {
            table.put("R" + i, i);
        }
        // Símbolos especiales
        table.put("SP",     0);
        table.put("LCL",    1);
        table.put("ARG",    2);
        table.put("THIS",   3);
        table.put("THAT",   4);
        table.put("SCREEN", 16384);
        table.put("KBD",    24576);
    }

    /**
     * Agrega un nuevo símbolo con su dirección.
     * @param symbol  nombre del símbolo
     * @param address dirección asignada
     */
    public void addEntry(String symbol, int address) {
        table.put(symbol, address);
    }

    /**
     * Verifica si el símbolo ya existe en la tabla.
     * @param symbol nombre a verificar
     * @return true si existe
     */
    public boolean contains(String symbol) {
        return table.containsKey(symbol);
    }

    /**
     * Retorna la dirección asociada al símbolo.
     * @param symbol nombre del símbolo
     * @return dirección como int
     */
    public int getAddress(String symbol) {
        return table.get(symbol);
    }
}
