# HackAssembler — Proyecto 3

> **Organización de Computadores · Universidad EAFIT · 2026-1**  
> Ensamblador y Desensamblador para la arquitectura **Hack** (Nand2Tetris), extendido con soporte para instrucciones de desplazamiento `<<` (Shift Left) y `>>` (Shift Right).

---

## Tabla de Contenido

1. [Descripción General](#1-descripción-general)
2. [Autoras](#2-autoras)
3. [Estructura del Proyecto](#3-estructura-del-proyecto)
4. [ Compilación y Ejecución Rápida](#4-Compilación-y-Ejecución-Rápida)
5. [Documentación](#7-Documentación)
6. [Verificación de Integridad (MD5)](#6-verificación-de-integridad-md5)
7. [Videos de prueba](#6-Videos-de-prueba)

---

## 1. Descripción General

**HackAssembler** es un ensamblador de dos pasadas para la arquitectura **Hack**, implementado en Java puro (sin dependencias externas). Traduce programas escritos en lenguaje ensamblador Hack (`.asm`) a código binario de 16 bits (`.hack`) compatible con el simulador de CPU de Nand2Tetris.

Adicionalmente, incluye un **desensamblador** (`HackDisassembler`) que realiza el proceso inverso: convierte archivos `.hack` de vuelta a  archivos legibles (`.asm`).

Como extensión propia del curso, el proyecto soporta las instrucciones de desplazamiento de bits `<<` y `>>` tanto en el ensamblador como en el desensamblador.

**NOTA**: Podrás encontrar videos que explican la lógica de este programa en el siguiente archivo:    
https://docs.google.com/document/d/1sNMs8TpuF0XBDrwYh1IRu5HCtGNMnsC9AKtqtwxkUGo/edit?usp=sharing


---


## 2. Autoras

Isabell Cadavid Posada e Isabella Ocampo Sánchez

---

## 3. Estructura del Proyecto

```
proyecto3/
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
    │   └── HackAssemblerTest.md5   # Pruebas
    └── docs/
        ├── API.md                   # Documentación de todas las clases
        ├── DESIGN.md                # Diagrama UML y decisiones de diseño
        └── USER_GUIDE.md            # Guía de instalación y uso
```
---

## 4. Compilación y Ejecución Rápida

```bash
# Compilar
cd HackAssembler/src
javac *.java

# Ensamblar
java HackAssembler Prog.asm          # genera Prog.hack

# Desensamblar
java HackAssembler -d Prog.hack      # genera ProgDis.asm

# Menú interactivo
java HackAssembler
```

Para instrucciones detalladas de instalación, compilación con Maven y ejemplos de uso, ver [`docs/USER_GUIDE.md`](docs/USER_GUIDE.md).

---

## 5. Documentación

| Documento | Contenido |
|-----------|-----------|
| [`docs/USER_GUIDE.md`](docs/USER_GUIDE.md) | Requisitos, compilación, uso, ejemplos, manejo de errores |
| [`docs/API.md`](docs/API.md) | Referencia de clases y métodos |
| [`docs/DESIGN.md`](docs/DESIGN.md) | Diagrama de clases, flujo de ensamblado, decisiones de diseño |

---

## 6. Verificación de Integridad

Cada archivo `.java` tiene su `.md5` asociado. Para verificar: [https://emn178.github.io/online-tools/md5_checksum.html](https://emn178.github.io/online-tools/md5_checksum.html)

---

## 7. Videos de prueba

* **Menú interactivo para el usuario:** https://youtu.be/1JCqFeL09ok?si=3Hs3RjXA8j2LLfJo
* **Prueba 2:** https://www.youtube.com/watch?v=iGbeoPNbOyE
---

---

> **Organización de Computadores · Universidad EAFIT · 2026-1**
