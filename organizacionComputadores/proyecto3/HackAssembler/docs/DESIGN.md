# DESIGN.md – Diagrama de Clases y Arquitectura

## Proyecto 3 – HackAssembler / HackDisassembler
**Organización de Computadores | Universidad EAFIT | 2026-1**  
**Autores:** Isabella Cadavid Posada · Isabella Ocampo Sánchez  

---

## Tabla de Contenidos

1. [Visión General](#1-vision-general)
2. [Diagrama de Clases UML](#2-diagrama-de-clases-uml)
3. [Flujo de Ensamblado – Dos Pasadas](#3-flujo-de-ensamblado--dos-pasadas)
4. [Flujo de Desensamblado](#4-flujo-de-desensamblado-de-hack-a-disasm)
5. [Proceso de Creación de Clases](#5-proceso-de-creacion-de-clases)
6. [Formatos Binarios](#6-formatos-binarios)
7. [Decision de Estructura](#7-decision-de-estructura)
8. [Referencias](#8-referencias)

---

## 1. Vision General

El sistema **HackAssembler** implementa un ensamblador y desensamblador completo para la arquitectura **Hack** (Nand2Tetris), con soporte extendido para instrucciones de desplazamiento (`<<` shift left, `>>` shift right) según la especificación EAFIT.

El sistema sigue el patrón de **dos pasadas** para la resolución de símbolos hacia adelante, y aplica el **Principio de Responsabilidad Única (SRP)** asignando a cada clase un rol claro y acotado.

### Tipos de Instruccion Soportados

| Tipo | Ejemplo | Descripción |
|---|---|---|
| `A_INSTRUCTION` | `@42`, `@suma` | Instrucción de dirección |
| `C_INSTRUCTION` | `D=M+1;JGT` | Instrucción de cómputo estándar |
| `SHIFT_INSTRUCTION` | `D=D<<`, `A=M>>` | Instrucción de desplazamiento |
| `L_INSTRUCTION` | `(LOOP)` | Declaración de etiqueta |

---

## 2. Diagrama de Clases UML

![Diagrama de Clases](https://github.com/user-attachments/assets/223ba1b4-851a-4699-9db0-4d344d621eec)

---

## 3. Flujo de Ensamblado – Dos Pasadas

Esta tabla resume el proceso de dos pasadas para convertir código ensamblador en binario.

| Fase | Objetivo Principal | Operaciones Clave | Manejo de Errores |
|------|------------------|------------------|-------------------|
| **1. Inicio** | Preparar el entorno | Lee el archivo `.asm` completo y carga las líneas en memoria (`rawLines`). | Si el archivo no existe: imprime error en `stderr` y retorna `false`. |
| **2. Primera Pasada** | Construir Tabla de Símbolos | - `L_INSTRUCTION`: Guarda el símbolo con dirección ROM.<br>- Otras: `romAddress++`. | Ninguno |
| **3. Segunda Pasada** | Generar Código Binario | Traducción de A, C y SHIFT. | Detiene proceso si hay error |
| **4. Finalización** | Guardar resultados | Escribe archivo `.hack`. | Elimina archivo si hay error |

---

## 4. Flujo de Desensamblado de .hack a Dis.asm

| Fase | Objetivo | Identificación | Acción |
|------|---------|---------------|--------|
| **1. Validación** | Verificar archivo | N/A | Valida formato binario |
| **2. A** | Dirección | `0xx` | Genera `@valor` |
| **3. Shift** | Desplazamiento | `10x` | Genera `<<` o `>>` |
| **4. C** | Cómputo | `111` | Genera `dest=comp;jump` |
| **5. Final** | Escritura | `110` inválido | Guarda archivo |


---

## 5. Proceso de Creacion de Clases

Este diagrama se realizó con ayuda de inteligencia artificial, con el objetivo de modelar y justificar nuestras desiciones para la construcción del codigo:
Se adjunta con el objetivo de que sea mas ameno y legible para entender la arquitectura del proyecto.

<img width="766" height="421" alt="image" src="https://github.com/user-attachments/assets/13a674b2-d456-4c5c-bf5b-f37088bc189c" />

* NOTA: Imagen Generada con apoyo de IA 


---

## 6. Formatos Binarios

### Instrucción A — `0vvvvvvvvvvvvvvv`

```
Bit:  15  14  13  12  11  10   9   8   7   6   5   4   3   2   1   0
       0   v   v   v   v   v   v   v   v   v   v   v   v   v   v   v
```

- Bit 15 siempre `0`.
- Los 15 bits restantes representan un entero sin signo de 0 a 32 767.

### Instrucción C — `111accccccdddjjj`

```
Bit:  15  14  13  12  11  10   9   8   7   6   5   4   3   2   1   0
       1   1   1   a   c   c   c   c   c   c   d   d   d   j   j   j
```

| Campo | Bits | Descripción |
|---|---|---|
| Prefijo | `[15:13]` | Siempre `111` |
| `a` | `[12]` | `0` → A/D como fuente; `1` → M como fuente |
| `cccccc` | `[11:6]` | Operación ALU |
| `ddd` | `[5:3]` | Registro(s) destino |
| `jjj` | `[2:0]` | Condición de salto |

### Instrucción Shift Left — `101accccccdddjjj`

```
Bit:  15  14  13  12  11  10   9   8   7   6   5   4   3   2   1   0
       1   0   1   a   c   c   c   c   c   c   d   d   d   j   j   j
```

### Instrucción Shift Right — `100accccccdddjjj`

```
Bit:  15  14  13  12  11  10   9   8   7   6   5   4   3   2   1   0
       1   0   0   a   c   c   c   c   c   c   d   d   d   j   j   j
```

> El bit 14 es `0` (diferencia respecto a C). El bit 13 distingue left (`1`) de right (`0`).

**Codificación del operando Shift:**

| Operando | a-bit `[12]` | `cccccc` `[11:6]` |
|---|---|---|
| `D` | `0` | `001100` |
| `A` | `0` | `110000` |
| `M` | `1` | `110000` |

---

## 7. Decision de Diseño

### Separación de Responsabilidades 

Cada clase tiene exactamente un rol dentro del sistema:

| Clase | Responsabilidad única |
|---|---|
| `HackAssembler` | Punto de entrada y delegación según modo (CLI / menú) |
| `Assembler` | Orquestar las dos pasadas; gestionar archivos de entrada/salida |
| `Parser` | Analizar líneas de texto `.asm` y extraer campos semánticos |
| `CodeGenerator` | Traducir campos semánticos a cadenas binarias de 16 bits |
| `SymbolTable` | Mantener el mapa símbolo → dirección |
| `HackDisassembler` | Invertir el proceso binario → mnemónicos |

### Detección de Instrucción Shift

La detección se realiza **antes** de evaluar instrucciones C estándar: si la línea contiene `<<` o `>>`, se clasifica como `SHIFT_INSTRUCTION`. Esto evita colisiones con el campo `comp` de instrucciones C normales.

### Codificación de Shift

Se usa el espacio de opcodes con `bit15:14 = 10`, que no es ocupado por instrucciones A (`0xx`) ni por instrucciones C (`111`). El bit 13 diferencia la dirección: `1` = left, `0` = right.

### Manejo de Errores

Ante cualquier error de parseo o traducción el sistema:
1. Imprime el número de línea exacto en `stderr`.
2. Cierra el archivo de salida.
3. **Elimina** el archivo parcial para evitar archivos corruptos.
4. Retorna con código de salida `1`.

### Variables en RAM

Las variables nuevas (no declaradas como etiquetas) se asignan en orden de primera aparición, comenzando desde la dirección RAM `16`, de acuerdo con la especificación Hack estándar.

---

## 8. Referencias
Para la estructuración de la lógica de los diagramas de flujo y la organización de los formatos de documentación técnica, se ha contado con el apoyo de herramientas de inteligencia artificial.

[1] Google, "Gemini," Modelo de lenguaje grande, 2026. [En línea]. Disponible: https://gemini.google.com

