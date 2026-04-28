# PROYECTO 2

**Curso:** Organización de Computadores  
**Año:** 2026-1  
**Integrantes:** Isabella Cadavid Posada, Isabella Ocampo Sánchez  
**Plataforma:** nand2tetris — Hardware Simulator

---

## Tabla de Contenido

1. [Descripcion general](#1-descripcion-general)
2. [Estructura del repositorio](#2-estructura-del-repositorio)
3. [Chips implementados](#3-chips-implementados)
   - [3.1 Shifter](#31-shifter)
   - [3.2 ALU](#32-alu)
   - [3.3 CPU](#33-cpu)
   - [3.4 Memory](#34-memory)
   - [3.5 Computer](#35-computer)
4. [Design.txt](#4-designtxt)
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
|    ├── proyecto2/
|    |   ├── docs/
|    |      ├── README.md
|    |      ├── Proyecto2.pdf
|    │   ├── ALU.hdl          # Unidad Aritmetico-Lógica extendida con Shifter
|    │   ├── ALU.md5
|    │   ├── Computer.hdl     # Computador Hack completo
|    │   ├── Computer.md5
|    │   ├── CPU.hdl          # Unidad Central de Procesamiento
|    │   ├── CPU.md5
|    │   ├── Memory.hdl       # Memoria principal del sistema
|    │   ├── Memory.md5
|    │   ├── Shifter.hdl      # Chip de desplazamiento de 1 bit
|    │   ├── Shifter.md5
|    │   ├── design.txt       # Documentación de diseño adicional
|    │   └── design.md5
|    ├── CONTRIBUTORS.md      # Colaboradores y roles
|    ├── CHANGELOG.md         # Historial de cambios
|    ├── LICENSE              # Licencia del proyecto
|    ├── Proyecto2.pdf        # Documentación completa
└── README.md                 # Este archivo
```

---

## 3. Chips implementados y Documentación

En el desarrollo de este proyecto de Organización del Computador, se diseñaron e implementaron los chips del proyecto 2 mediante el Lenguaje de Descripción de Hardware (HDL). A continuación, se detalla la arquitectura y el propósito de cada uno.
https://docs.google.com/document/d/1USXFE6XYVFGMo7dCSvY8bNdL_b0_ivao2cPqWWYyJxk/edit?usp=sharing

### 3.1 Shifter

**Archivo:** `Shifter.hdl`

#### Interfaz

| Puerto | Ancho | Dirección | Descripción |
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

---

### 3.2 ALU

**Archivo:** `ALU.hdl`

#### Interfaz

| Puerto | Ancho | Dirección | Descripción |
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

#### Modo Shifter dentro de la ALU

El Shifter se activa cuando `zx=0`, `nx=0`, `zy=0`, `ny=0` y `no=1`. La combinación de estas cinco señales constituye la firma de activación del modo desplazamiento. Bajo esta condición, la señal `f` determina la dirección:

- `f=0`: desplazamiento a la izquierda, `result = x[15]`
- `f=1`: desplazamiento a la derecha, `result = x[0]`

La detección del modo Shifter se implementa como:

```
isShift = (NOT zx) AND (NOT nx) AND (NOT zy) AND (NOT ny) AND no
```

Cuando `isShift = 1`, la salida final del chip proviene del `Shifter` directamente, sin pasar por la lógica de negacion final (`no`) de la rama ALU. Cuando `isShift = 0`, la salida corresponde a la rama aritmetico-lógica estandar con negación opcional.

#### Implementacion

La implementación de la ALU se realiza siguiendo la especificación vista en clase. Primero, las entradas `x` e `y` se procesan mediante las señales de control `zx`, `nx`, `zy` y `ny`, que permiten anular o negar los operandos. Luego, se calculan en paralelo las operaciones `AND` y `ADD`, seleccionadas por la señal `f`. Adicionalmente, se integra el chip **Shifter**, el cual opera directamente sobre la entrada `x` y permite realizar desplazamientos a la izquierda o a la derecha, generando también la señal `result` con el bit desplazado. Para activar este modo, se implementa una lógica que detecta la condición específica de control (`zx=nx=zy=ny=0` y `no=1`), permitiendo seleccionar entre la salida normal de la ALU y el resultado del Shifter mediante un multiplexor. Finalmente, se calculan las señales `zr` y `ng` a partir de la salida.

---

### 3.3 CPU

**Archivo:** `CPU.hdl`

La Unidad Central de Procesamiento (CPU) es el núcleo del computador Hack. Su función es ejecutar la instrucción actual (recibida desde la memoria ROM) y decidir qué operación realizar, si debe guardar datos en memoria o en registros, y cuál será la siguiente instrucción a ejecutar.

#### Interfaz

| Puerto | Ancho | Dirección | Descripción |
|--------|-------|-----------|-------------|
| `inM` | 16 bits | Entrada | Valor almacenado en la RAM |
| `instruction` | 16 bits | Entrada | Instrucción a ejecutar |
| `reset` | 1 bit | Entrada | `1` reinicia el programa |
| `outM` | 16 bits | Salida | Valor a escribir en RAM |
| `writeM` | 1 bit | Salida | Señal de escritura en RAM |
| `addressM` | 15 bits | Salida | Dirección de memoria RAM |
| `pc` | 15 bits | Salida | Dirección de próxima instrucción |

#### Logica de diseño

- **Decodificación:** Identifica si es instrucción Tipo A (bit 15 = 0) o Tipo C (bit 15 = 1).
- **Registros:** El Registro A carga valores si es Tipo A o si el destino d1 está activo en Tipo C. El Registro D solo carga resultados de la ALU si d2 está activo.
- **Ejecución:** Utiliza la ALU extendida. El operando Y se elige entre el Registro A o la entrada de memoria `inM` mediante el bit `a`.
- **Flujo:** La lógica de salto evalúa las banderas `zr` y `ng` junto a los bits `j1`, `j2`, `j3` para determinar si el PC debe cargar una nueva dirección desde el Registro A.

---

### 3.4 Memory

**Archivo:** `Memory.hdl`

#### Interfaz

Este chip es el encargado de manejar todo el almacenamiento del computador. Sus conexiones son:

| Puerto | Ancho | Dirección | Descripción |
|--------|-------|-----------|-------------|
| `in` | 16 bits | Entrada | Dato a guardar |
| `load` | 1 bit | Entrada | `1` = escritura, `0` = solo lectura |
| `address` | 15 bits | Entrada | Dirección a la que se accede |
| `out` | 16 bits | Salida | Dato que sale hacia la CPU |

#### Mapa de memoria

| Rango de direcciones | Componente | Proporcion |
|----------------------|------------|------------|
| 0 – 16383 | RAM16K | 50% |
| 16384 – 24575 | Screen | 25% |
| 24576 | Keyboard | 25% |

#### Logica de diseño

- **No se usa el bit 15:** Solo se usan los bits 0–14 para direccionar, ya que el bit 15 es exclusivo de las instrucciones tipo A.
- **DMux4Way para escritura:** Como el chip divide en 4 partes iguales (25% cada una) y la RAM ocupa el 50%, se asignan las salidas `a` y `b` del DMux4Way a la RAM, uniéndolas con una compuerta `Or` para generar una sola señal de `load`.
- **Mux4Way16 para lectura:** Realiza el proceso inverso: selecciona la salida correcta (RAM, Screen o Keyboard) según la dirección. Como la RAM ocupa las posiciones `a` y `b`, su salida se conecta a ambas entradas del Mux.
- **El teclado es de solo lectura:** No recibe `in` ni `load`, ya que solo se lee lo que el usuario digita.

---

### 3.5 Computer

**Archivo:** `Computer.hdl`

#### Logica de diseño

El computador une los tres chips principales en un flujo continuo:

- **ROM:** Almacena las instrucciones y las envía a la CPU.
- **CPU:** Gestiona las operaciones con la ALU, decodifica el tipo de instrucción (Tipo A o Tipo C) y coordina la escritura en memoria.
- **Memory:** Guarda los valores resultantes en el registro M correspondiente.

En conclusión, el Computer es el chip que integra todos los componentes anteriores y permite ejecutar programas completos, habilitando pruebas más robustas del sistema.

---

## 4. Design.txt

Este documento describe cómo se extendió la arquitectura de la computadora **Hack** para soportar operaciones de desplazamiento de bits (`<<` y `>>`) dentro de las instrucciones tipo C, sin modificar su formato original de 16 bits.

### Problema

La ALU original de Hack **no soporta operaciones shift**, y como todas las operaciones deben pasar por el campo `comp`, era necesario definir una nueva codificación que permitiera:

- Integrar `shift left` y `shift right`
- Mantener el formato estándar: `111 a cccccc ddd jjj`
- No romper la compatibilidad con el diseño existente

### Solucion

Se reutilizaron combinaciones específicas del campo `comp` para representar operaciones de desplazamiento.

**Reglas clave:**

| Señal | Valor | Significado |
|-------|-------|-------------|
| `c1` | `0` | Usa `D` como operando |
| `c1` | `1` | Usa `A` o `M` como operando |
| `c2–c5` | `1000` | Firma que identifica operacion shift |
| `c6` | `0` | Shift izquierda (`<<`) |
| `c6` | `1` | Shift derecha (`>>`) |
| `a` (si `c1=1`) | `0` | Usa `A` |
| `a` (si `c1=1`) | `1` | Usa `M` |

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

En Windows:

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

- Haz clic en el boton **Run Script** para ejecutar todas las pruebas automaticamente.
- El panel de salida mostrara `Comparison ended successfully` si todas las pruebas pasan.
- Para depuracion paso a paso, usa el boton de avance individual y observa los valores de cada caso.

---

## 6. Colaboradores

Consulta el archivo `CONTRIBUTORS.md` para ver el detalle de roles y contribuciones individuales de cada integrante del equipo.

---

*Proyecto desarrollado en el marco del curso Organizacion de Computadores.*
