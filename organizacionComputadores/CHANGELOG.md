# Historial de Cambios Proyecto 2

## [1.4.0] - 2026-04-27

### Arreglado
* **Estructura del repositorio:** Se corrigió la ruta de las carpetas principales para cumplir exactamente con la entrega exigida (`[suUsuarioGit]/organizacionComputadores/proyecto2/`).

* **Conflictos de Git:** Se resolvieron conflictos de integración (merge) entre el repositorio local y el remoto.

* **README.md:** Se arregló tabla de contenido y problemas de indentación.


### Descripción
* Se realizaron mejoras en la documentación para facilitar la comprensión del proyecto.
* Se transformó la ruta para que cumpliera exactamente con la entrega exigida.



## [1.3.0] - 2026-04-18

### Mejorado
* `README.md`: Se mejoró la redacción y organización de la documentación general del proyecto.
* Documentación del sistema: Se ampliaron las explicaciones sobre el funcionamiento y la estructura del computador.

### Agregado
* `design.txt`: Definición del formato binario para instrucciones tipo C con operaciones shift.
* Se definió la codificación de instrucciones tipo C para incluir comandos `<<` y `>>`.

### Descripción
* Se realizaron mejoras en la documentación para facilitar la comprensión del proyecto.
* Se formalizó la especificación del diseño de instrucciones tipo C con soporte para desplazamientos.

---

## [1.2.0] - 2026-04-18

### Agregado
* `Computer.hdl`: Implementación del computador uniendo CPU, Memory y ROM.
* `Computer.md5`: Hash de validación del computador.

### Descripción
* Se implementó el chip del computador. 
* Se conectó con éxito la CPU y la Memoria (Chips creados anteriormente).
* No se utilizarón compuertas lógicas como AND, OR.... Solamente usa los chips que ya creamos.

## [1.1.0] - 2026-04-17

### Agregado
* `Memory.hdl`: Implementación del chip para Ram, Pantalla y Teclado.
* `Memory.md5`: Hash de validación de la memoria con MD5 File Check.
* `CPU.hdl`: Unidad Central de Procesamiento construida.
* `CPU.md5`: Hash de validación de la CPU .

### Descripción
* Se implementó el circuito de la Memoria integrando la RAM16K, Pantalla y Teclado usando un DMux para escritura y un Mux para lectura.
* Se conectó con éxito la ALU modificada dentro de la CPU.
* Se implementó la lógica de decodificación de instrucciones (Tipo A y Tipo C).

## [1.0.0] - 2026-04-16

### Agregado

* Shifter.hdl: circuito de desplazamiento (shift left / shift right) con señal `result`
* ALU.hdl: integración del Shifter en la ALU estándar para soportar operaciones de corrimiento
* design.txt: definición del formato binario para instrucciones tipo C con operaciones shift

### Descripción

* Se implementó el módulo **Shifter**, permitiendo desplazamientos a izquierda y derecha sobre datos de 16 bits.
* Se modificó la **ALU** para soportar nuevas operaciones de corrimiento, manteniendo compatibilidad con las operaciones originales.
* Se definió la codificación de instrucciones tipo C para incluir comandos `<<` y `>>`.

---

## [0.2.0] - 2026-04-14

### Agregado

* Definición de la arquitectura del sistema
* Creación de los backlogs del proyecto

### Descripción

* Se estableció la estructura general del computador (CPU, Memory, ALU, Computer, Shifter, design).
* Se organizaron las tareas del proyecto mediante backlog para guiar el desarrollo.

---

## [0.1.0] - 2026-03-25

### Agregado

* Commit inicial con estructura del repositorio


# Historial de Cambios Proyecto 3

## [1.0.2] – 2026-04-28

### Added
- Documentación: `Readme.md`, `DESIGN.md`, `CHAGELOG.md`.

## [1.0.1] – 2026-04-28

### Added
- `HackDisassembler.java`: desensamblado de archivos `.hack` a mnemónicos `.asm`.
- `CodeGenerator.java`: generación de código binario de 16 bits.
- `HackAssemblerTest.java`: suite de pruebas sin dependencias externas.
- Documentación: `API.md`,  `USER_GUIDE.md`,  `CHAGELOG.md`, `CONTRIBUTORS.md`

## [1.0.0] – 2026-04-27

### Added
- `HackAssembler.java`: punto de entrada con soporte CLI y menú interactivo.
- `Assembler.java`: proceso de ensamblado en dos pasadas.
- `Parser.java`: análisis de instrucciones A, C, Shift y etiquetas.
- `SymbolTable.java`: tabla de símbolos con símbolos predefinidos Hack.
- Soporte para instrucciones Shift Left (`<<`) y Shift Right (`>>`) en ensamblador y desensamblador.

