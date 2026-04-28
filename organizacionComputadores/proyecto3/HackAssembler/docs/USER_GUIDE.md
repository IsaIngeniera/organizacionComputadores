# USER_GUIDE.md – Guía de Usuario

## HackAssembler – Organización de Computadores

**Universidad EAFIT | 2026-1**
Autores: Isabella Cadavid Posada · Isabella Ocampo Sánchez

---

## Tabla de Contenidos

1. [Requisitos](#requisitos)
2. [Estructura del Proyecto](#estructura-del-proyecto)
3. [Compilación](#compilación)
4. [Uso](#uso)
5. [Ejemplos de archivos .asm soportados](#ejemplos-de-archivos-asm-soportados)
6. [Formato de instrucción Shift](#formato-de-instrucción-shift)
7. [Arquitectura interna](#arquitectura-interna)
8. [Manejo de errores](#manejo-de-errores)
9. [Ejecutar pruebas](#ejecutar-pruebas)
10. [Generación del MD5](#generación-del-md5)

---

## Requisitos

- Java JDK 11 o superior instalado y en el PATH
- (Opcional) Apache Maven 3.6+ para compilar desde fuente

Verificar instalación:

```bash
java -version
javac -version
```

---

## Estructura del Proyecto

```
proyecto3/
├── README.md
├── CHANGELOG.md
├── CONTRIBUTORS.md
├── LICENSE
└── HackAssembler/
    ├── README.md
    ├── src/
    │   ├── HackAssembler.java       # Punto de entrada (CLI + menú)
    │   ├── HackAssembler.md5      
    │   ├── Assembler.java           # Orquestador de dos pasadas
    │   ├── Assembler.md5
    │   ├── Parser.java              # Análisis sintáctico de .asm
    │   ├── Parser.md5
    │   ├── CodeGenerator.java       # Generación de código binario
    │   ├── CodeGenerator.md5
    │   ├── SymbolTable.java         # Tabla de símbolos y variables
    │   ├── SymbolTable.md5         
    │   ├── HackDisassembler.java    # Desensamblador .hack → .asm
    │   └── HackDisassembler.md5
    ├── test/
    │   ├── HackAssemblerTest.java
    │   └── HackAssemblerTest.md5   # Purbas
    └── docs/
        ├── API.md                   # Documentación de todas las clases
        ├── DESIGN.md                # Diagrama UML y decisiones de diseño
        └── USER_GUIDE.md            # Guía de instalación y uso
```

---

## Compilación

### Opción A – Desde la raíz del proyecto (recomendado)

```bash
# Compila todos los fuentes de src/ y test/ juntos
javac src/*.java test/HackAssemblerTest.java
```

### Opción B – Solo el ensamblador (sin tests)

```bash
cd src
javac *.java
```

> **Nota:** no se requiere Maven. El proyecto no tiene dependencias externas.

---

## Uso

### 1. Ensamblar un archivo `.asm` → `.hack`

```bash
# Desde la carpeta donde se compiló (src/)
java HackAssembler Prog.asm
```

**Salida:** archivo `Prog.hack` en el mismo directorio, con una instrucción binaria de 16 bits por línea.

Ejemplo:

```bash
java HackAssembler programs/Add.asm
# → genera programs/Add.hack
```

---

### 2. Desensamblar un archivo `.hack` → `.asm`

```bash
java HackAssembler -d Prog.hack
```

**Salida:** archivo `ProgDis.asm` correspondiente.

Ejemplo:

```bash
java HackAssembler -d programs/Add.hack
# → genera programs/AddDis.asm
```

---

### 3. Menú interactivo

Ejecutar un menú guiado:

```bash
java HackAssembler
```

Salida esperada:

```
==============================================
  HackAssembler - Organización de Computadores
  Universidad EAFIT - 2026-1
==============================================

Seleccione una opción:
  1) Ensamblar archivo .asm -> .hack
  2) Desensamblar archivo .hack -> .asm
  3) Salir
Opción:
```

Se ingresa la ruta del archivo cuando el menú lo solicite. No se requiere ningún flag adicional.

---

## Ejemplos de archivos `.asm` soportados

### Instrucciones estándar – Suma R0 = R1 + R2

```asm
// Suma: R0 = R1 + R2
@R1
D=M
@R2
D=D+M
@R0
M=D
```

---

### Con etiquetas y variables – Contador

```asm
@i
M=1          // i = 1
(LOOP)
@i
D=M
@100
D=D-A
@END
D;JGT
@i
M=M+1
@LOOP
0;JMP
(END)
@END
0;JMP
```

Las etiquetas `(LOOP)` y `(END)` se resuelven en la primera pasada y no generan instrucciones binarias.

---

### Con instrucciones Shift (extensión EAFIT)

```asm
// Shift left: multiplica D por 2
@5
D=A
D=D<<      // D = 10

// Shift right: divide D entre 2
D=D>>      // D = 5

// Con destino explícito
A=A<<
M=M>>

// Con destino y jump
@R0
M=D
```

---

## Formato de instrucción Shift

La extensión Shift permite desplazamientos de 1 bit sobre los registros D, A y M.

| Sintaxis   | Significado                                  |
|------------|----------------------------------------------|
| `D=D<<`    | D = D desplazado a la izquierda 1 bit        |
| `D=A<<`    | D = A desplazado a la izquierda 1 bit        |
| `D=M<<`    | D = M desplazado a la izquierda 1 bit        |
| `D=D>>`    | D = D desplazado a la derecha 1 bit          |
| `A=D>>`    | A = D desplazado a la derecha 1 bit          |
| `M=A<<`    | M = A desplazado a la izquierda 1 bit        |
| `dest=op<<;jump` | Shift con destino y condición de salto  |

**Formato binario generado:**

| Campo     | Bits    | Shift Left | Shift Right |
|-----------|---------|------------|-------------|
| Prefijo   | [15:13] | `101`      | `100`       |
| a-bit     | [12]    | `0` (D/A) · `1` (M) | igual |
| Operando  | [11:6]  | D=`001100` · A/M=`110000` | igual |
| Dest      | [5:3]   | según tabla dest | igual |
| Jump      | [2:0]   | según tabla jump | igual |

---

## Arquitectura interna

El ensamblador sigue el patrón de **dos pasadas**:

### Primera pasada – Construcción de la tabla de símbolos

Recorre todas las líneas del archivo. Cada vez que encuentra una etiqueta `(LABEL)`, la registra en la `SymbolTable` con la dirección ROM actual. Las instrucciones A y C incrementan el contador ROM; las etiquetas no lo hacen.

### Segunda pasada – Generación de código

Recorre nuevamente las líneas. Para cada instrucción:

- **Instrucción A (`@xxx`):** convierte el valor numérico o busca el símbolo en la tabla. Si el símbolo es nuevo, se asigna desde la dirección RAM 16 en adelante.
- **Instrucción C estándar:** extrae los campos `dest`, `comp` y `jump` con el `Parser` y los traduce con las tablas del `CodeGenerator`.
- **Instrucción Shift:** identifica dirección (`<<` o `>>`), operando (`D`, `A`, `M`) y destino, generando el patrón binario de la extensión EAFIT.
- **Etiquetas L:** se omiten (no generan código binario).

### Componentes principales

| Clase               | Responsabilidad                                           |
|---------------------|-----------------------------------------------------------|
| `HackAssembler`     | Punto de entrada; gestiona argumentos y menú interactivo  |
| `Assembler`         | Coordina las dos pasadas; lee y escribe archivos          |
| `Parser`            | Limpia líneas, detecta tipo de instrucción, extrae campos |
| `CodeGenerator`     | Traduce instrucciones A, C y Shift a binario de 16 bits   |
| `SymbolTable`       | Mapa símbolo→dirección; pre-cargada con predefinidos Hack |
| `HackDisassembler`  | Invierte el proceso: binario → mnemónicos assembler       |

### Símbolos predefinidos cargados automáticamente

| Símbolo  | Dirección |
|----------|-----------|
| R0–R15   | 0–15      |
| SP       | 0         |
| LCL      | 1         |
| ARG      | 2         |
| THIS     | 3         |
| THAT     | 4         |
| SCREEN   | 16384     |
| KBD      | 24576     |

---

## Manejo de errores

Si hay un error en el archivo de entrada, el programa:

1. Imprime el mensaje de error con el **número de línea** en `stderr`
2. Cierra y **elimina** el archivo de salida parcial
3. Termina con código de salida `1`

Ejemplos de mensajes de error:

```
Error en línea 7: Comp inválido: 'XYZ'
Error en línea 3: Número negativo no permitido.
Error en línea 12: Operando de Shift inválido: 'R0'. Use D, A o M.
Error en línea 5: Símbolo inválido '1var'.
Error: No se encontró el archivo 'Prog.asm'
```

---

## Ejecutar pruebas

La suite de pruebas cubre instrucciones A, C estándar, Shift, etiquetas, variables y el desensamblador.

### Compilar y ejecutar los tests

```bash
# Desde la raíz del proyecto
javac src/*.java test/HackAssemblerTest.java
java HackAssemblerTest
```

### Salida esperada

```
====================================
  HackAssembler - Suite de Pruebas
====================================


--- Instrucción A ---
[PASS] @0  -> 0000000000000000
[PASS] @1  -> 0000000000000001
[PASS] @2  -> 0000000000000010
[PASS] @21 -> 0000000000010101
[PASS] @32767 -> max 15 bits
Error en línea 1: Valor fuera de rango (0-32767): -1
[PASS] @-1 retorna null
Error en línea 1: Valor fuera de rango (0-32767): 32768
[PASS] @32768 retorna null

--- Instrucción C ---
[PASS] D=A
[PASS] D=D+A
[PASS] M=D
[PASS] 0;JMP
[PASS] D;JGT
[PASS] AMD=D|M

--- Instrucción Shift Left (<<) ---
[PASS] D=D<< bit15=1
[PASS] D=D<< bit14=0
[PASS] D=D<< bit13=1 (left)
[PASS] D=D<< completo
[PASS] D=A<<
[PASS] A=M<<

--- Instrucción Shift Right (>>) ---
[PASS] D=D>>
[PASS] D=A>>

--- Tabla de Símbolos ---
[PASS] R0=0
[PASS] R15=15
[PASS] SP=0
[PASS] SCREEN=16384
[PASS] KBD=24576
[PASS] myVar added

--- Desensamblador: instrucción A ---
[PASS] Desensamblar A: sin error
[PASS] @0
[PASS] @1
[PASS] @21

--- Desensamblador: instrucción C ---
[PASS] Desensamblar C: sin error
[PASS] D=A
[PASS] M=D
[PASS] 0;JMP

--- Desensamblador: instrucción Shift ---
[PASS] Desensamblar Shift: sin error
[FAIL] D=D<<
       Esperado: D=D<<
       Obtenido: D=D<<1
[FAIL] D=D>>
       Esperado: D=D>>
       Obtenido: D=D>>1

--- Manejo de errores ---
Error en línea 1: Se esperaban 16 bits, se encontraron 15.
[PASS] Error: menos de 16 bits
Error en línea 1: Caracteres inválidos (solo '0' y '1' permitidos).
[PASS] Error: carácter inválido
Error en línea 5: Comp inválido: 'INVALID'
[PASS] Error: comp inválido

--- Ciclo completo: ASM -> HACK -> ASM ---
[PASS] Round-trip: ensamblado exitoso
[PASS] Round-trip: desensamblado exitoso
[PASS] Round-trip: .hack tiene 7 líneas
[PASS] Round-trip: @10
[PASS] Round-trip: D=A
[PASS] Round-trip: 0;JMP

====================================
  Resultados: 45 pasadas, 2 fallidas
====================================
```

> Si algún test falla, la línea mostrará `[FAIL]` junto con el valor esperado y el obtenido.

---

## Generación del MD5

Para verificar la integridad de los archivos fuente se genera un checksum MD5. Usar la herramienta en línea:

[https://emn178.github.io/online-tools/md5_checksum.html](https://emn178.github.io/online-tools/md5_checksum.html)

Pasos:
1. Abrir el enlace anterior.
2. Arrastrar o seleccionar el archivo `.java` correspondiente.
3. Copiar el hash MD5 resultante.
4. Pegarlo en el archivo `.md5` correspondiente al fuente entregado.

Cada archivo `.java` debe tener su propio `.md5` con el hash calculado sobre el contenido exacto del archivo entregado.