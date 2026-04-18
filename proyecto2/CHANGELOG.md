# Historial de Cambios
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
