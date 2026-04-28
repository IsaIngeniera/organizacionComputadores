# API.md – Documentación Técnica de Clases

## Proyecto 3 – HackAssembler / HackDisassembler
**Organización de Computadores | Universidad EAFIT | 2026-1**
**Autores:** Isabella Cadavid Posada · Isabella Ocampo Sánchez

---

## Tabla de Contenidos

1. [Visión General del Sistema](#1-visión-general-del-sistema)
2. [HackAssembler.java](#2-hackassemblerjava)
3. [Assembler.java](#3-assemblerjava)
4. [Parser.java](#4-parserjava)
5. [CodeGenerator.java](#5-codegeneratorjava)
6. [SymbolTable.java](#6-symboltablejava)
7. [HackDisassembler.java](#7-hackdisassemblerjava)
8. [HackAssemblerTest.java](#8-hackassemblertestjava)
9. [Formatos Binarios](#9-formatos-binarios)

---

## 1. Visión General del Sistema

El sistema **HackAssembler** implementa un ensamblador y desensamblador completo para la arquitectura **Hack** (Nand2Tetris), con soporte extendido para instrucciones de desplazamiento (`<<` shift left, `>>` shift right) según la especificación EAFIT.

### Tipos de Instrucción Soportados

| Tipo | Ejemplo | Descripción |
|---|---|---|
| `A_INSTRUCTION` | `@42`, `@suma` | Instrucción de dirección |
| `C_INSTRUCTION` | `D=M+1;JGT` | Instrucción de cómputo estándar |
| `SHIFT_INSTRUCTION` | `D=D<<1`, `A=M>>1` | Instrucción de desplazamiento |
| `L_INSTRUCTION` | `(LOOP)` | Declaración de etiqueta (pseudo-instrucción) |

---

## 2. HackAssembler.java

Punto de entrada principal del sistema. Parsea los argumentos de línea de comandos y delega el trabajo al `Assembler` o `HackDisassembler` según corresponda. También ofrece un menú interactivo cuando se invoca sin argumentos.

### Método Principal

#### `main(String[] args)`

Punto de entrada de la JVM. Evalúa los argumentos recibidos y determina el modo de operación.

| Invocación | Comportamiento |
|---|---|
| `java HackAssembler Prog.asm` | Ensambla `Prog.asm` → genera `Prog.hack` |
| `java HackAssembler -d Prog.hack` | Desensambla `Prog.hack` → genera `ProgDis.asm` |
| `java HackAssembler` | Abre el menú interactivo en consola |
| Cualquier otra combinación | Imprime uso correcto y termina con código de error 1 |

**Validaciones:**
- En modo ensamblado: verifica que el archivo tenga extensión `.asm`; de lo contrario, imprime error en `stderr` y termina con `System.exit(1)`.
- En modo desensamblado: verifica que el archivo tenga extensión `.hack`; de lo contrario, imprime error en `stderr` y termina con `System.exit(1)`.

### Métodos Privados

#### `assemble(String inputFile)`

Orquesta el proceso de ensamblado para un archivo `.asm`.

- Construye el nombre del archivo de salida reemplazando `.asm` por `.hack`.
- Instancia un objeto `Assembler` e invoca `assemble(inputFile, outputFile)`.
- Si tiene éxito, imprime `"Ensamblado exitoso: <outputFile>"`.
- Si falla, termina con `System.exit(1)`.

#### `disassemble(String inputFile)`

Orquesta el proceso de desensamblado para un archivo `.hack`.

- Construye el nombre del archivo de salida con el sufijo `Dis.asm` (ej. `Prog.hack` → `ProgDis.asm`).
- Instancia un objeto `HackDisassembler` e invoca `disassemble(inputFile, outputFile)`.
- Si tiene éxito, imprime `"Desensamblado exitoso: <outputFile>"`.
- Si falla, termina con `System.exit(1)`.

#### `runInteractiveMenu()`

Despliega un menú de texto en consola con tres opciones:

```
1) Ensamblar archivo .asm -> .hack
2) Desensamblar archivo .hack -> .asm
3) Salir
```

Utiliza un `Scanner` sobre `System.in` para leer la opción y el nombre del archivo. Valida la extensión del archivo antes de proceder. El bucle continúa hasta que el usuario selecciona la opción `3`.

#### `printUsage()`

Imprime en `stdout` las instrucciones de uso del programa cuando se detectan argumentos inválidos.

---

## 3. Assembler.java

Orquesta el proceso completo de ensamblado en **dos pasadas** sobre el archivo fuente `.asm`. Coordina el uso de `Parser`, `SymbolTable` y `CodeGenerator` para producir el archivo `.hack` de salida.

### Método Principal

#### `boolean assemble(String inputFile, String outputFile)`

Ejecuta el ensamblado completo del archivo fuente.

**Parámetros:**

| Parámetro | Tipo | Descripción |
|---|---|---|
| `inputFile` | `String` | Ruta al archivo fuente `.asm` |
| `outputFile` | `String` | Ruta al archivo de salida `.hack` |

**Retorna:** `true` si el ensamblado fue exitoso; `false` si ocurrió algún error (el mensaje de error se imprime en `stderr` y el archivo de salida es eliminado automáticamente).

**Proceso detallado:**

**Fase 0 – Lectura del archivo**

Todas las líneas del archivo se cargan en una lista `List<String> rawLines` usando un `BufferedReader`. Si el archivo no existe o no puede leerse, imprime el error y retorna `false`.

**Fase 1 – Primera pasada: construcción de la tabla de símbolos**

```
Objetivo: registrar todas las etiquetas (LABEL) con su dirección ROM.
```

- Itera sobre cada línea.
- Limpia la línea con `Parser.cleanLine()`.
- Determina el tipo de instrucción con `Parser.instructionType()`.
- Si es `L_INSTRUCTION`: extrae el símbolo y lo registra en `SymbolTable` con la dirección ROM **actual** (el contador `romAddress` **no** se incrementa para etiquetas).
- Si es `A_INSTRUCTION` o `C/SHIFT_INSTRUCTION`: incrementa `romAddress` en 1.

**Fase 2 – Segunda pasada: traducción a binario**

```
Objetivo: generar una línea de código binario de 16 bits por cada instrucción real.
```

- Itera nuevamente sobre todas las líneas.
- Las `L_INSTRUCTION` se ignoran (no generan código).
- Para `A_INSTRUCTION`:
  - Si el símbolo es numérico: usa el valor directamente.
  - Si es un nombre no registrado en `SymbolTable`: lo agrega como variable con `variableAddress` (que empieza en 16 e incrementa con cada nueva variable).
  - Genera el binario con `CodeGenerator.generateA()`.
- Para `C_INSTRUCTION` / `SHIFT_INSTRUCTION`:
  - Genera el binario con `CodeGenerator.generateC()`.
- Cada línea binaria generada se escribe en el archivo de salida con `PrintWriter`.

**Manejo de errores:** Si cualquier método de parseo o generación retorna `null`, el `writer` se cierra, el archivo de salida se elimina con `new File(outputFile).delete()`, y el método retorna `false`.

**Ejemplo de flujo:**

```
// Archivo fuente (simplificado):
@2          → A_INSTRUCTION → romAddress=1
D=A         → C_INSTRUCTION → romAddress=2
@3          → A_INSTRUCTION → romAddress=3
D=D+A       → C_INSTRUCTION → romAddress=4
@0          → A_INSTRUCTION → romAddress=5
M=D         → C_INSTRUCTION → romAddress=6
```

---

## 4. Parser.java

Analizador léxico y sintáctico de líneas individuales del código fuente `.asm`. Provee métodos para limpiar líneas, detectar el tipo de instrucción y extraer cada campo (dest, comp, jump, símbolo, operando de shift).

### Métodos

#### `String cleanLine(String line)`

Preprocesa una línea del archivo fuente antes de analizarla.

- Elimina cualquier comentario en línea (todo lo que va después de `//`).
- Elimina espacios en blanco al inicio y al final (`trim()`).

**Ejemplos:**

| Entrada | Salida |
|---|---|
| `"  @42  // carga valor"` | `"@42"` |
| `"D=M+1   "` | `"D=M+1"` |
| `"// comentario completo"` | `""` |
| `"(LOOP)"` | `"(LOOP)"` |

#### `String instructionType(String cleanedLine, int lineNum)`

Determina el tipo de instrucción a partir de la línea ya limpiada.

**Parámetros:**

| Parámetro | Tipo | Descripción |
|---|---|---|
| `cleanedLine` | `String` | Línea sin comentarios ni espacios |
| `lineNum` | `int` | Número de línea original (para errores) |

**Retorna:** una de las siguientes constantes `String`, o `null` si la línea está vacía.

| Valor retornado | Condición de detección |
|---|---|
| `"A_INSTRUCTION"` | La línea comienza con `@` |
| `"L_INSTRUCTION"` | La línea comienza con `(` y termina con `)` |
| `"SHIFT_INSTRUCTION"` | La línea contiene `<<` o `>>` |
| `"C_INSTRUCTION"` | Cualquier otro caso (default) |

**Nota:** La detección de Shift tiene prioridad sobre C, ya que se evalúa primero.

#### `String symbol(String cleanedLine, int lineNum)`

Extrae el símbolo o valor de una instrucción A o etiqueta.

- Para `@xxx`: retorna `"xxx"` (todo después del `@`).
- Para `(LABEL)`: retorna `"LABEL"` (contenido entre paréntesis), validando que sea un símbolo legal con `isValidSymbol()`.
- Si la línea está vacía o el símbolo es inválido, imprime error en `stderr` y retorna `null`.

**Validación de símbolos (método privado `isValidSymbol`):**
- No puede comenzar con un dígito.
- Solo puede contener letras, dígitos y los caracteres especiales `_`, `.`, `$`, `:`.

#### `String dest(String cleanedLine)`

Extrae el campo de **destino** de una instrucción C o Shift.

- Si la línea contiene `=`: retorna todo lo que está a la izquierda del primer `=`.
- Si no hay `=`: retorna una cadena vacía `""` (sin destino).

**Ejemplos:**

| Entrada | Salida |
|---|---|
| `"D=M+1"` | `"D"` |
| `"AMD=D+1;JMP"` | `"AMD"` |
| `"D;JGT"` | `""` |

#### `String comp(String cleanedLine)`

Extrae el campo de **cómputo** de una instrucción C o Shift.

1. Quita el dest (si hay `=`, toma lo que va después).
2. Quita el jump (si hay `;`, toma lo que va antes).
3. Retorna lo que queda como el campo `comp`.

**Ejemplos:**

| Entrada | Salida |
|---|---|
| `"D=M+1;JGT"` | `"M+1"` |
| `"D=D<<1"` | `"D<<1"` |
| `"0;JMP"` | `"0"` |
| `"D=D"` | `"D"` |

#### `String jump(String cleanedLine)`

Extrae el campo de **salto** de una instrucción C o Shift.

- Si la línea contiene `;`: retorna todo lo que está a la derecha del primer `;`.
- Si no hay `;`: retorna una cadena vacía `""` (sin salto).

**Ejemplos:**

| Entrada | Salida |
|---|---|
| `"D;JGT"` | `"JGT"` |
| `"0;JMP"` | `"JMP"` |
| `"D=M+1"` | `""` |

#### `String shiftDirection(String cleanedLine)`

Determina la dirección del desplazamiento para instrucciones Shift.

| Contenido | Retorna |
|---|---|
| La línea contiene `<<` | `"LEFT"` |
| La línea contiene `>>` | `"RIGHT"` |
| Ninguno | `null` |

#### `String shiftOperand(String cleanedLine)`

Extrae el **registro fuente** que será desplazado en una instrucción Shift.

- Primero obtiene el campo `comp` completo con `comp()`.
- Luego divide por `<<` o `>>` y retorna la parte izquierda (el operando).

**Ejemplos:**

| Entrada | Salida |
|---|---|
| `"D=D<<1"` | `"D"` |
| `"A=A>>1"` | `"A"` |
| `"M=M<<1"` | `"M"` |
| `"D=A<<1"` | `"A"` |

---

## 5. CodeGenerator.java

Traduce los campos parseados de cada instrucción a su representación binaria de 16 bits, siguiendo el formato de la arquitectura Hack con la extensión de instrucciones Shift.

Internamente usa un objeto `Parser` propio para descomponer la línea antes de generar el código.

### Tablas Internas (estáticas)

#### `COMP_TABLE` – Tabla de cómputo

Mapea el mnemónico de `comp` a sus 7 bits (`a` + `cccccc`):

| Mnemónico | Bits (a cccccc) | Descripción |
|---|---|---|
| `"0"` | `0101010` | Constante 0 |
| `"1"` | `0111111` | Constante 1 |
| `"-1"` | `0111010` | Constante -1 |
| `"D"` | `0001100` | Registro D |
| `"A"` | `0110000` | Registro A |
| `"M"` | `1110000` | Memoria M (a=1) |
| `"D+A"` | `0000010` | Suma D+A |
| `"D+M"` | `1000010` | Suma D+M |
| *(y todas las demás operaciones estándar Hack)* | | |

#### `DEST_TABLE` – Tabla de destino

Mapea el mnemónico de `dest` a sus 3 bits (`ddd`):

| Mnemónico | Bits | Descripción |
|---|---|---|
| `""` | `000` | Sin destino |
| `"M"` | `001` | Memoria |
| `"D"` | `010` | Registro D |
| `"MD"` | `011` | Memoria y D |
| `"A"` | `100` | Registro A |
| `"AM"` | `101` | A y Memoria |
| `"AD"` | `110` | A y D |
| `"AMD"` | `111` | A, Memoria y D |

#### `JUMP_TABLE` – Tabla de salto

Mapea el mnemónico de `jump` a sus 3 bits (`jjj`):

| Mnemónico | Bits | Condición |
|---|---|---|
| `""` | `000` | Sin salto |
| `"JGT"` | `001` | Jump if > 0 |
| `"JEQ"` | `010` | Jump if = 0 |
| `"JGE"` | `011` | Jump if ≥ 0 |
| `"JLT"` | `100` | Jump if < 0 |
| `"JNE"` | `101` | Jump if ≠ 0 |
| `"JLE"` | `110` | Jump if ≤ 0 |
| `"JMP"` | `111` | Jump incondicional |

### Métodos

#### `String generateA(int value, int lineNum)`

Genera la representación binaria de 16 bits para una instrucción A.

**Parámetros:**

| Parámetro | Tipo | Descripción |
|---|---|---|
| `value` | `int` | Valor numérico (0 a 32767) |
| `lineNum` | `int` | Número de línea (para errores) |

**Retorna:** cadena de 16 caracteres `"0vvvvvvvvvvvvvvv"`, donde los 15 bits restantes representan el valor en binario con ceros a la izquierda. Retorna `null` si el valor está fuera del rango permitido (0–32767).

**Ejemplo:**
```
generateA(42, 1)  →  "0000000000101010"
generateA(0, 1)   →  "0000000000000000"
```

**Implementación:** usa `String.format("0%15s", Integer.toBinaryString(value)).replace(' ', '0')`.

#### `String generateC(String cleanedLine, int lineNum)`

Punto de entrada para generar código C o Shift. Delega al método correspondiente según el tipo de instrucción detectado por `Parser.instructionType()`.

- Si es `SHIFT_INSTRUCTION` → llama a `generateShift()`.
- Si es `C_INSTRUCTION` → llama a `generateStandardC()`.

**Parámetros:**

| Parámetro | Tipo | Descripción |
|---|---|---|
| `cleanedLine` | `String` | Línea limpia (sin comentarios) |
| `lineNum` | `int` | Número de línea (para errores) |

**Retorna:** cadena de 16 bits o `null` si hay error.

#### `String generateStandardC(String line, int lineNum)` *(privado)*

Genera el binario `111accccccdddjjj` para una instrucción C estándar.

1. Extrae `dest`, `comp` y `jump` con el `Parser`.
2. Busca cada campo en sus respectivas tablas.
3. Si algún campo no existe en la tabla, imprime el error correspondiente y retorna `null`.
4. Concatena: `"111"` + `compBits(7)` + `destBits(3)` + `jumpBits(3)` = 16 bits.

**Ejemplo:**
```
"D=M+1;JGT"
  dest="D"   → 010
  comp="M+1" → 1110111
  jump="JGT" → 001
  resultado  → "1111110111010001"
```

#### `String generateShift(String line, int lineNum)` *(privado)*

Genera el binario para instrucciones de desplazamiento con el formato extendido EAFIT.

1. Extrae `dest`, `jump`, `operand` y `direction` del `Parser`.
2. Determina el prefijo de 3 bits:
   - `LEFT`  → `"101"`
   - `RIGHT` → `"100"`
3. Determina el a-bit y los bits de cómputo del operando:

| Operando | a-bit | compBits |
|---|---|---|
| `D` | `0` | `001100` |
| `A` | `0` | `110000` |
| `M` | `1` | `110000` |

4. Concatena: `prefix(3)` + `aBit(1)` + `compBits(6)` + `destBits(3)` + `jumpBits(3)` = 16 bits.

**Ejemplo:**
```
"D=D<<1"
  prefix = "101" (left)
  aBit   = "0"
  comp   = "001100" (D)
  dest   = "010"    (D)
  jump   = "000"    (ninguno)
  resultado → "1010001100010000"
```

---

## 6. SymbolTable.java

Estructura de datos central para la resolución de nombres simbólicos. Implementa una tabla hash que mapea nombres de símbolos (etiquetas y variables) a sus direcciones numéricas de ROM o RAM. Se pre-carga automáticamente con todos los símbolos predefinidos del lenguaje Hack.

### Constructor

#### `SymbolTable()`

Inicializa la tabla con los **23 símbolos predefinidos** de la arquitectura Hack:

**Registros R0–R15:**

| Símbolo | Dirección RAM |
|---|---|
| `R0` | 0 |
| `R1` | 1 |
| `R2` | 2 |
| ... | ... |
| `R15` | 15 |

**Punteros de segmento:**

| Símbolo | Dirección RAM | Alias de |
|---|---|---|
| `SP` | 0 | Stack Pointer (= R0) |
| `LCL` | 1 | Local (= R1) |
| `ARG` | 2 | Argument (= R2) |
| `THIS` | 3 | This (= R3) |
| `THAT` | 4 | That (= R4) |

**Dispositivos de E/S:**

| Símbolo | Dirección | Descripción |
|---|---|---|
| `SCREEN` | 16384 | Base del mapa de pantalla |
| `KBD` | 24576 | Registro del teclado |

**Implementación interna:** usa `HashMap<String, Integer>` para O(1) en inserciones y búsquedas.

### Métodos

#### `void addEntry(String symbol, int address)`

Registra un nuevo símbolo en la tabla.

**Parámetros:**

| Parámetro | Tipo | Descripción |
|---|---|---|
| `symbol` | `String` | Nombre del símbolo (etiqueta o variable) |
| `address` | `int` | Dirección asignada (ROM para etiquetas, RAM para variables) |

**Notas:**
- Las **etiquetas** `(LABEL)` se agregan durante la primera pasada con su dirección ROM.
- Las **variables** `@varName` se agregan durante la segunda pasada con direcciones RAM a partir de 16, en orden de primera aparición.
- Si el símbolo ya existe, su valor es sobreescrito (comportamiento de `HashMap.put`).

#### `boolean contains(String symbol)`

Verifica si un símbolo ya está registrado en la tabla.

**Retorna:** `true` si el símbolo existe, `false` en caso contrario.

**Uso típico:** antes de agregar una nueva variable, para evitar asignarle una segunda dirección.

```java
if (!symbolTable.contains(symbol)) {
    symbolTable.addEntry(symbol, variableAddress++);
}
```

#### `int getAddress(String symbol)`

Recupera la dirección numérica asociada a un símbolo.

**Parámetros:**

| Parámetro | Tipo | Descripción |
|---|---|---|
| `symbol` | `String` | Nombre del símbolo a buscar |

**Retorna:** la dirección como `int`.

**Precondición:** se debe llamar a `contains()` antes para asegurarse de que el símbolo existe. Si el símbolo no existe, `HashMap.get()` retorna `null` y el unboxing automático lanza `NullPointerException`.

---

## 7. HackDisassembler.java

Proceso inverso al ensamblado. Lee un archivo `.hack` con instrucciones binarias de 16 bits y las traduce de vuelta a mnemónicos del lenguaje assembler Hack, incluyendo instrucciones de desplazamiento.

### Tablas Internas (estáticas)

Tablas inversas a las de `CodeGenerator`:

- **`COMP_INV`**: mapea 7 bits (`a` + `cccccc`) → mnemónico de comp.
- **`DEST_INV`**: mapea 3 bits (`ddd`) → mnemónico de dest.
- **`JUMP_INV`**: mapea 3 bits (`jjj`) → mnemónico de jump.

### Métodos

#### `boolean disassemble(String inputFile, String outputFile)`

Ejecuta el proceso completo de desensamblado.

**Parámetros:**

| Parámetro | Tipo | Descripción |
|---|---|---|
| `inputFile` | `String` | Ruta al archivo `.hack` |
| `outputFile` | `String` | Ruta al archivo `Dis.asm` de salida |

**Retorna:** `true` si el proceso fue exitoso; `false` en caso de error.

**Proceso:**
1. Lee cada línea del archivo de entrada.
2. Valida que cada línea tenga exactamente **16 caracteres**.
3. Valida que la línea contenga únicamente los caracteres `'0'` y `'1'`.
4. Llama a `decodeInstruction()` para obtener el mnemónico.
5. Escribe el mnemónico en el archivo de salida.
6. Ante cualquier error: cierra el writer, elimina el archivo de salida y retorna `false`.

#### `String decodeInstruction(String bits, int lineNum)` *(privado)*

Decodifica una instrucción binaria de 16 bits identificando su tipo por los bits más significativos.

**Lógica de identificación:**

| bit[15] | bit[14] | bit[13] | Tipo |
|---|---|---|---|
| `0` | cualquiera | cualquiera | Instrucción A |
| `1` | `0` | cualquiera | Instrucción Shift |
| `1` | `1` | `1` | Instrucción C |
| `1` | `1` | `0` | Patrón desconocido → error |

- **A:** extrae los 15 bits restantes como entero y retorna `"@" + valor`.
- **Shift:** delega a `decodeShift()`.
- **C:** delega a `decodeC()`.

#### `String decodeC(String bits, int lineNum)` *(privado)*

Decodifica una instrucción C estándar del formato `111accccccdddjjj`.

**Extracción de campos:**
- `compBits` = `bits[3..9]` (7 bits: a + cccccc)
- `destBits` = `bits[10..12]` (3 bits)
- `jumpBits` = `bits[13..15]` (3 bits)

Busca cada campo en las tablas inversas. Si `comp` no se encuentra, imprime error y retorna `null`.

**Ensamblado del mnemónico:**
```
si dest ≠ ""  →  "dest=comp"
si jump ≠ ""  →  "comp;jump"
si ambos      →  "dest=comp;jump"
si ninguno    →  "comp"
```

#### `String decodeShift(String bits, int lineNum)` *(privado)*

Decodifica una instrucción Shift del formato `10[d][a]ccccccdddjjj`.

**Extracción de campos:**
- `bit[2]` (posición 2 del string) determina dirección: `'1'` = left (`<<1`), `'0'` = right (`>>1`)
- `aBit` = `bits[3]`
- `compBits` = `bits[4..9]` (6 bits del operando)
- `destBits` = `bits[10..12]`
- `jumpBits` = `bits[13..15]`

**Resolución del operando:**

| compBits | aBit | Operando |
|---|---|---|
| `001100` | cualquiera | `D` |
| `110000` | `0` | `A` |
| `110000` | `1` | `M` |
| otro | cualquiera | Error |

**Resultado:** `"dest=operando<<1"` o `"dest=operando>>1"` con salto opcional.

---

## 8. HackAssemblerTest.java

Suite de pruebas de regresión completa. No requiere frameworks externos (sin JUnit). Verifica todos los componentes del sistema de manera aislada e integrada.

### Ejecución

```bash
java -cp target/classes HackAssemblerTest
```

### Grupos de Pruebas

| Grupo | Método | Qué verifica |
|---|---|---|
| Instrucción A | `testInstructionA()` | Codificación binaria correcta para valores numéricos: `@0`, `@1`, `@32767`, `@42` |
| Instrucción C | `testInstructionC()` | Instrucciones C estándar con todas las combinaciones de dest, comp y jump |
| Shift Left | `testShiftLeft()` | Generación correcta de `D=D<<1`, `A=A<<1`, `M=M<<1` y sus variantes |
| Shift Right | `testShiftRight()` | Generación correcta de `D=D>>1`, `A=A>>1`, `M=M>>1` y sus variantes |
| Tabla de Símbolos | `testSymbolTable()` | Símbolos predefinidos (R0–R15, SP, LCL, etc.) y registro de variables nuevas |
| Desensamblado A | `testDisassemblerA()` | Inversión correcta de instrucciones A: `0000000000101010` → `@42` |
| Desensamblado C | `testDisassemblerC()` | Inversión correcta de instrucciones C |
| Desensamblado Shift | `testDisassemblerShift()` | Inversión correcta de instrucciones Shift |
| Manejo de Errores | `testErrorHandling()` | Detección de: valores negativos, valores > 32767, comp inválido, dest inválido, jump inválido, líneas malformadas |
| Round-Trip | `testRoundTrip()` | Ciclo completo `ASM → HACK → ASM`: verifica que re-ensamblar el archivo desensamblado produzca el mismo `.hack` original |

---

## 9. Formatos Binarios

### Instrucción A — `0vvvvvvvvvvvvvvv`

```
Bit:  15  14  13  12  11  10  9  8  7  6  5  4  3  2  1  0
       0   v   v   v   v   v  v  v  v  v  v  v  v  v  v  v
```

- Bit 15 siempre es `0`.
- Los 15 bits restantes representan un valor sin signo de 0 a 32767.

### Instrucción C — `111accccccdddjjj`

```
Bit:  15  14  13  12  11  10  9  8  7  6  5  4  3  2  1  0
       1   1   1   a   c   c  c  c  c  c  d  d  d  j  j  j
```

| Campo | Bits | Descripción |
|---|---|---|
| Prefijo | `[15:13]` | Siempre `111` |
| `a` | `[12]` | 0 para A/D, 1 para M |
| `cccccc` | `[11:6]` | Operación ALU |
| `ddd` | `[5:3]` | Destino |
| `jjj` | `[2:0]` | Condición de salto |

### Instrucción Shift Left — `101accccccdddjjj`

```
Bit:  15  14  13  12  11  10  9  8  7  6  5  4  3  2  1  0
       1   0   1   a   c   c  c  c  c  c  d  d  d  j  j  j
```

### Instrucción Shift Right — `100accccccdddjjj`

```
Bit:  15  14  13  12  11  10  9  8  7  6  5  4  3  2  1  0
       1   0   0   a   c   c  c  c  c  c  d  d  d  j  j  j
```

**Diferencia respecto a instrucción C:** el bit 14 es `0` (en C es `1`). El bit 13 distingue left (`1`) de right (`0`).

**Codificación del operando Shift:**

| Operando | a-bit `[12]` | `cccccc` `[11:6]` |
|---|---|---|
| `D` | `0` | `001100` |
| `A` | `0` | `110000` |
| `M` | `1` | `110000` |

---