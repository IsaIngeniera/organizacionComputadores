# Organización de Computadores 

Este repositorio contiene los 2 proyectos desarrollados para  **Organización de Computadores**. El objetivo de este repositorio es unir tanto la construcción de la arquitectura de hardware (basada en el computador Hack) como el desarrollo de herramientas de software (Ensamblador y Desensamblador).

2026-1

## Autoras
- [Isabella Cadavid Posada]
- [Isabella Ocampo Sánchez]

---

## 📂 Contenido del Repositorio

A continuación, se detallan los dos proyectos principales incluidos en este repositorio. Puedes hacer clic en el título de cada proyecto para navegar directamente a su directorio correspondiente.

**IMPORTANTE:** Cada proyecto tiene su propia documentación, allí se encontrarán videos explicativos que permiten al usuario entender mejor el contenido.

### 1. [Proyecto 2: Arquitectura de Hardware (Hack Computer)](./organizacionComputadores/organizacionComputadores/proyecto2)
Este proyecto se centra en la implementación de los componentes de hardware fundamentales del computador Hack utilizando **HDL (Hardware Description Language)**. 

**Componentes principales desarrollados:**
- **ALU (Unidad Aritmético Lógica):** Encargada de realizar todas las operaciones aritméticas y lógicas.
- **CPU (Unidad Central de Procesamiento):** El núcleo del computador, que integra la ALU y los registros para ejecutar las instrucciones.
- **Memory (Memoria):** Implementación del bloque de memoria principal que incluye la RAM, la memoria de pantalla (Screen) y el mapa de entrada del teclado (Keyboard).
- **Computer:** La integración final de la CPU, la Memoria y la ROM para formar el computador Hack completo.
- **Shifter:** Componente para operaciones de desplazamiento de bits.

### 2. [Proyecto 3: Hack Assembler y Disassembler](./organizacionComputadores/organizacionComputadores/proyecto3)
Este proyecto consiste en el desarrollo de un ensamblador y desensamblador para el lenguaje de máquina de la arquitectura Hack, implementado en **Java**.

**Características principales:**
- **Hack Assembler:** Traduce programas escritos en lenguaje ensamblador simbólico de Hack (`.asm`) a código binario de máquina (`.hack`) comprensible por la arquitectura de hardware. Maneja la resolución de símbolos, etiquetas y saltos.
- **Hack Disassembler:** Realiza el proceso inverso, tomando código de máquina binario (`.hack`) y traduciéndolo nuevamente a instrucciones en lenguaje ensamblador (`.asm`).
- **Validación y Pruebas:** Incluye herramientas de validación, control de errores léxicos y de sintaxis, y generación de hashes MD5 para asegurar la exactitud de los archivos traducidos.

---

## 🛠️ Tecnologías Utilizadas
- **Hardware:** HDL (Hardware Description Language) de la plataforma Nand to Tetris.
- **Software:** Java para el desarrollo de los traductores.

## 📝 Referencias y Contribuciones
Para más detalles sobre las versiones, puedes consultar el archivo [CHANGELOG.md](./organizacionComputadores/organizacionComputadores/CHANGELOG.md) y revisar la lista de participantes en [CONTRIBUTORS.md](./organizacionComputadores/organizacionComputadores/CONTRIBUTORS.md).
