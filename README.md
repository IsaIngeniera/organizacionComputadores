# Proyecto 2 

**Curso:** Organización de Computadores  
**Plataforma:** nand2tetris — Hardware Simulator  
**Autoras:** Isabella Cadavid Posada, Isabella Ocampo Sánchez

---

## Tabla de Contenido

1. [Descripción general](#1-descripción-general)
2. [Estructura del repositorio](#2-estructura-del-repositorio)
3. [Chips implementados](#3-chips-implementados)
   - [3.1 Shifter](#32-shifter)
   - [3.2 ALU](#33-alu)
   - [3.3 CPU](#34-cpu)
   - [3.4 Memory](#35-memory)
   - [3.5 Computer](#36-computer)
4. [Design](#4-Arquitectura de la instrucción)
5. [Como ejecutar el proyecto](#5-como-ejecutar-el-proyecto)
6. [Colaboradores](#6-colaboradores)

---

## 1. Descripcion general

Este proyecto implementa la arquitectura del computador Hack, desarrollada en el curso de Organización de Computadores, mediante la construcción en HDL de sus principales componentes de hardware, siguiendo las especificaciones estudiadas en clase. El desarrollo se realiza con un enfoque jerárquico (bottom-up), en el que cada módulo se implementa y valida de manera independiente antes de su integración en el sistema completo. En este contexto, el proyecto abarca la implementación de los chips **Shifter**, **ALU**, **Memory**, **CPU** y **Computer**, los cuales se presentan en las secciones siguientes.

---

## 2. Estructura del repositorio

```
[suUsuarioGit]/
└── organizacionComputadores/
    ├── proyecto2/
    │   ├── ALU.hdl          # Unidad Aritmetico-Lógica extendida con Shifter
    │   ├── ALU.md5          
    │   ├── Computer.hdl     # Computador Hack completo
    │   ├── Computer.md5
    │   ├── CPU.hdl          # Unidad Central de Procesamiento
    │   ├── CPU.md5
    │   ├── Memory.hdl       # Memoria principal del sistema
    │   ├── Memory.md5
    │   ├── Shifter.hdl      # Chip de desplazamiento de 1 bit
    │   ├── Shifter.md5
    │   ├── design.txt       # Documentación de diseño adicional
    │   └── design.md5
    ├── CONTRIBUTORS.md      # Colaboradores y roles
    ├── CHANGELOG.md         # Historial de cambios
    ├── LICENSE              # Licencia del proyecto
    └── README.md            # Este archivo
```

---

## 3. Chips implementados

### 3.1 Shifter

**Archivo:** `Shifter.hdl`

#### Interfaz

| Puerto | Ancho | Direccion | Descripcion |
|--------|-------|-----------|-------------|
| `in` | 16 bits | Entrada | Dato a desplazar |
| `direction` | 1 bit | Entrada | `0` = desplazamiento a la izquierda, `1` = desplazamiento a la derecha |
| `out` | 16 bits | Salida | Dato desplazado |
| `result` | 1 bit | Salida | Bit que sale por el extremo correspondiente |

#### Comportamiento

```
Si direction == 0 (desplazamiento a la izquierda):
    out[0]  = 0
    out[i]  = in[i-1]   para i = 1..15
    result  = in[15]

Si direction == 1 (desplazamiento a la derecha):
    out[15] = 0
    out[i]  = in[i+1]   para i = 0..14
    result  = in[0]
```

#### Implementacion

El chip calcula simultáneamente ambas variantes del desplazamiento (izquierda y derecha) y selecciona el resultado final bit a bit mediante 16 compuertas `Mux` individuales. Cada una de estas compuertas utiliza la señal de control para elegir, en cada posición, el valor correspondiente según la dirección del desplazamiento.

```
Mux(a = leftShift[i], b = rightShift[i], sel = direction, out = out[i])
```

Donde:

- `leftShift[0]  = false`,  `leftShift[i]  = in[i-1]`  para `i = 1..15`
- `rightShift[15] = false`, `rightShift[i] = in[i+1]`  para `i = 0..14`

La salida `result` se obtiene con un ultimo `Mux`:

```
Mux(a = in[15], b = in[0], sel = direction, out = result)
```

#### Tabla de verdad  (ejemplo de 4 bits para claridad)

| `in` | `direction` | `out` | `result` |
|------|------------|-------|----------|
| `0110` | `0` | `1100` | `0` |
| `0110` | `1` | `0011` | `0` |
| `1001` | `0` | `0010` | `1` |
| `1001` | `1` | `0100` | `1` |

---

### 3.2 ALU

**Archivo:** `ALU.hdl`

#### Interfaz

| Puerto | Ancho | Direccion | Descripcion |
|--------|-------|-----------|-------------|
| `x` | 16 bits | Entrada | Primer operando |
| `y` | 16 bits | Entrada | Segundo operando |
| `zx` | 1 bit | Entrada | Si `1`, fuerza `x = 0` |
| `nx` | 1 bit | Entrada | Si `1`, niega `x` bit a bit |
| `zy` | 1 bit | Entrada | Si `1`, fuerza `y = 0` |
| `ny` | 1 bit | Entrada | Si `1`, niega `y` bit a bit |
| `f` | 1 bit | Entrada | `0` = AND, `1` = ADD (o dirección del Shifter) |
| `no` | 1 bit | Entrada | Si `1`, niega la salida bit a bit |
| `out` | 16 bits | Salida | Resultado de la operacion |
| `zr` | 1 bit | Salida | `1` si `out == 0` |
| `ng` | 1 bit | Salida | `1` si `out < 0` (bit 15 en 1) |
| `result` | 1 bit | Salida | Bit desplazado en modo Shifter; `0` en operaciones normales |

#### Operaciones soportadas

La ALU calcula la siguiente función de acuerdo a la combinacion de bits de control:

| `zx` | `nx` | `zy` | `ny` | `f` | `no` | `out` |
|------|------|------|------|-----|------|-------|
| 1 | 0 | 1 | 0 | 1 | 0 | 0 |
| 1 | 1 | 1 | 1 | 1 | 1 | 1 |
| 1 | 1 | 1 | 0 | 1 | 0 | -1 |
| 0 | 0 | 1 | 1 | 0 | 0 | x |
| 1 | 1 | 0 | 0 | 0 | 0 | y |
| 0 | 0 | 1 | 1 | 0 | 1 | !x |
| 1 | 1 | 0 | 0 | 0 | 1 | !y |
| 0 | 0 | 1 | 1 | 1 | 1 | -x |
| 1 | 1 | 0 | 0 | 1 | 1 | -y |
| 0 | 1 | 1 | 1 | 1 | 1 | x+1 |
| 1 | 1 | 0 | 1 | 1 | 1 | y+1 |
| 0 | 0 | 1 | 1 | 1 | 0 | x-1 |
| 1 | 1 | 0 | 0 | 1 | 0 | y-1 |
| 0 | 0 | 0 | 0 | 1 | 0 | x+y |
| 0 | 1 | 0 | 0 | 1 | 1 | x-y |
| 0 | 0 | 0 | 1 | 1 | 1 | y-x |
| 0 | 0 | 0 | 0 | 0 | 0 | x&y |
| 0 | 1 | 0 | 1 | 0 | 1 | x\|y |
| 0 | 0 | 0 | 0 | 0 | 1 | shift left x |
| 0 | 0 | 0 | 0 | 1 | 1 | shift right x |

#### Modo Shifter dentro de la ALU

El Shifter se activa cuando `zx=0`, `nx=0`, `zy=0`, `ny=0` y `no=1`. La combinación de estas cinco señales constituye la firma de activación del modo desplazamiento. Bajo esta condicioó, la señal `f` determina la dirección:

- `f=0`: desplazamiento a la izquierda, `result = x[15]`
- `f=1`: desplazamiento a la derecha, `result = x[0]`

La detección del modo Shifter se implementa como:

```
isShift = (NOT zx) AND (NOT nx) AND (NOT zy) AND (NOT ny) AND no
```

Cuando `isShift = 1`, la salida final del chip proviene del `Shifter` directamente, sin pasar por la lógica de negacion final (`no`) de la rama ALU. Cuando `isShift = 0`, la salida corresponde a la rama aritmetico-lógica estandar con negación opcional.

#### Calculo de flags

- `ng`: corresponde al bit 15 de `out`. Se obtiene directamente como salida parcial del `Mux16` final, el bit mas significativo.
- `zr`: se calcula aplicando `Or8Way` sobre los 8 bits bajos y los 8 bits altos de `out`, uniendo ambos resultados con `Or` y negando el resultado.

#### Implementación 

La implementación de la ALU se realiza siguiendo la especificación vista en clase, utilizando los bloques básicos estudiados en el curso de Organización de Computadores. Primero, las entradas `x` e `y` se procesan mediante las señales de control `zx`, `nx`, `zy` y `ny`, que permiten anular o negar los operandos. Luego, se calculan en paralelo las operaciones `AND` y `ADD`, seleccionadas por la señal `f`. Adicionalmente, se integra el chip **Shifter**, el cual opera directamente sobre la entrada `x` y permite realizar desplazamientos a la izquierda o a la derecha, generando también la señal `result` con el bit desplazado. Para activar este modo, se implementa una lógica que detecta la condición específica de control (`zx=nx=zy=ny=0` y `no=1`), permitiendo seleccionar entre la salida normal de la ALU y el resultado del Shifter mediante un multiplexor. Finalmente, se calculan las señales `zr` y `ng` a partir de la salida, manteniendo el comportamiento estándar de la ALU y extendiéndolo con la funcionalidad de corrimiento.

---

### 3.3 CPU

**Archivo:** `CPU.hdl`

> La logica interna de este chip se documenta en `design.txt`. Abajo se reserva el espacio para ampliar la descripcion cuando sea requerido.

#### Interfaz

#### Logica de diseño

---

### 3.4 Memory

**Archivo:** `Memory.hdl`

#### Interfaz

#### Mapa de memoria

#### Logica de diseño


---

### 3.5 Computer

**Archivo:** `Computer.hdl`

#### Logica de diseño


---

## 4. Desing (Arquitectura de la instrucción)



---

## 5. Como ejecutar el proyecto

### Prerrequisitos

- Herramientas nand2tetris (Hardware Simulator incluido)

### Paso 1: Clonar el repositorio

```bash
git clone https://github.com/IsaIngeniera/organizacionComputadores
cd organizacionComputadores/proyecto2
```

### Paso 2: Descargar nand2tetris

Si no tienes instalada la plataforma, descargala desde el sitio oficial:

```
https://www.nand2tetris.org/software
```

Descomprime el archivo descargado. La estructura resultante debe contener una carpeta `nand2tetris/tools/`.

### Paso 3: Abrir el Hardware Simulator

En sistemas Unix/macOS:

```bash
cd nand2tetris/tools
bash HardwareSimulator.sh
```

En Windows, ejecuta directamente:

```
nand2tetris\tools\HardwareSimulator.bat
```

### Paso 4: Cargar un chip

1. En el menu superior, selecciona **File > Load Chip**.
2. Navega hasta la carpeta `proyecto2/` del repositorio clonado.
3. Selecciona el archivo `.hdl` que deseas probar (por ejemplo, `Shifter.hdl`).

### Paso 5: Cargar el script de prueba

1. Selecciona **File > Load Script**.
2. Carga el archivo `.tst` correspondiente al chip (por ejemplo, `Shifter.tst`).

### Paso 6: Ejecutar la simulacion

- Haz clic en el boton **Run Script** (icono de play doble) para ejecutar todas las pruebas automaticamente.
- El panel de salida mostrara `Comparison ended successfully` si todas las pruebas pasan.

Para depuracion paso a paso, usa el boton de avance individual y puede observa los valores de cada caso.

---

## 6. Colaboradores

Consulta el archivo `CONTRIBUTORS.md` para ver el detalle de roles y contribuciones individuales de cada integrante del equipo.

---

*Proyecto desarrollado en el marco del curso Organizacion de Computadores.*