# Historial de Cambios

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
