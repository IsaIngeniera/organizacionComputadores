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
4. [Design](#4-Arquitectura-de-la-instrucción)
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

En el desarrollo de este proyecto de Organización del Computador, se diseñaron e implementaron los chips del proyecto 2 de organización de computadores mediante el Lenguaje de Descripción de Hardware (HDL). A continuación, se detalla la arquitectura y el propósito de cada uno: 
https://docs.google.com/document/d/1USXFE6XYVFGMo7dCSvY8bNdL_b0_ivao2cPqWWYyJxk/edit?usp=sharing


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

#### Modo Shifter dentro de la ALU

El Shifter se activa cuando `zx=0`, `nx=0`, `zy=0`, `ny=0` y `no=1`. La combinación de estas cinco señales constituye la firma de activación del modo desplazamiento. Bajo esta condicioó, la señal `f` determina la dirección:

- `f=0`: desplazamiento a la izquierda, `result = x[15]`
- `f=1`: desplazamiento a la derecha, `result = x[0]`

La detección del modo Shifter se implementa como:

```
isShift = (NOT zx) AND (NOT nx) AND (NOT zy) AND (NOT ny) AND no
```

Cuando `isShift = 1`, la salida final del chip proviene del `Shifter` directamente, sin pasar por la lógica de negacion final (`no`) de la rama ALU. Cuando `isShift = 0`, la salida corresponde a la rama aritmetico-lógica estandar con negación opcional.

#### Implementación 

La implementación de la ALU se realiza siguiendo la especificación vista en clase, utilizando los bloques básicos estudiados en el curso de Organización de Computadores. Primero, las entradas `x` e `y` se procesan mediante las señales de control `zx`, `nx`, `zy` y `ny`, que permiten anular o negar los operandos. Luego, se calculan en paralelo las operaciones `AND` y `ADD`, seleccionadas por la señal `f`. Adicionalmente, se integra el chip **Shifter**, el cual opera directamente sobre la entrada `x` y permite realizar desplazamientos a la izquierda o a la derecha, generando también la señal `result` con el bit desplazado. Para activar este modo, se implementa una lógica que detecta la condición específica de control (`zx=nx=zy=ny=0` y `no=1`), permitiendo seleccionar entre la salida normal de la ALU y el resultado del Shifter mediante un multiplexor. Finalmente, se calculan las señales `zr` y `ng` a partir de la salida, manteniendo el comportamiento estándar de la ALU y extendiéndolo con la funcionalidad de corrimiento.

---

### 3.3 CPU

**Archivo:** `CPU.hdl`

#### Interfaz

#### Logica de diseño

---

### 3.4 Memory

**Archivo:** `Memory.hdl`

#### Interfaz
Este chip es el encargado de manejar todo el almacenamiento de nuestra computadora. Sus conexiones son:
* **in[16]:** El dato de 16 bits que queremos guardar.
* **load:** El que nos dice si vamos a escribir (1) o solo a leer (0).
* **address[15]:** La dirección a la que queremos entrar.
* **out[16]:** El dato que sale de la memoria hacia la CPU.

#### Mapa de memoria
Para que la CPU no se confunda, dividimos las direcciones en tres grandes barrios: (barrios como analogía para saber donde está la dirección)
1. **RAM16K (0 al 16383):** Es el espacio principal para nuestras variables y datos. Ocupa la mitad de toda la memoria. Osea un 50%.
2. **Screen (16384 al 24575):** Aquí es donde "dibujamos". Cada bit que guardamos aquí se convierte en un píxel blanco o negro en la pantalla. Es un 25%.
3. **Keyboard (24576):** Un registro especial que nos dice qué tecla está hundiendo el usuario en ese momento. Es un 25%.

#### Logica de diseño
Para armar este chip, aplicamos tres ideas claves:

* **No ponemos el bit 15:** Aunque el sistema es de 16 bits, el bit más significativo (el 15) solo sirve para que la CPU identifique que es una instrucción tipo A. Por eso, al entrar a la memoria, ya sabemos que estamos en el lugar correcto y solo usamos los otros 15 bits (del 0 al 14) para buscar las direcciones.
* **Uso del DMux4Way para elegir la dirección:** Como el chip DMux4Way divide todo en 4 partes iguales (de 25% cada una) y nuestra RAM es del 50%, lo que hicimos fue asignarle las dos primeras salidas (**a** y **b**) a la RAM. Luego, usamos una compuerta **Or** para unir esos dos cables de "permiso de carga" (load) en uno solo para el chip RAM16K.Esta desición Arquitectónica se creó deibdo a los chips que teniamos antes (DMux4Way) y la forma en la que está estructurado, pues, si bien tenemos 3 partes en la memoria, necesitamos dividir la RAM en 2 partes, y así obtener un total de 4 partes para poder usar el DMux4Way.
* **La salida con el Mux4Way16:** Para leer los datos, hicimos el proceso inverso. El Mux mira la dirección y elige si lo que debe mostrar afuera es lo que viene de la RAM, de la Pantalla o del Teclado. Como la RAM ocupa las dos primeras posiciones del selector, conectamos la salida de la RAM tanto en la opción **a** como en la **b** del Mux.
* **El Teclado es especial:** Al teclado no le entra el cable de `in` ni el de `load`, porque nosotros no le escribimos información al teclado, simplemente leemos lo que él nos entrega cuando el usuario digita algo.
---

### 3.5 Computer

**Archivo:** `Computer.hdl`

#### Logica de diseño
Para construir el computador utilizamos 3 elementos clave:
* **ROM:** Se encarga de almacenar las instrucciones y mandarlas a la CPU como un parametro de entrada.
* **CPU:** Se encarga de gestionar las operaciones junto a la ALU y decidir 
    que tipo de instrucción es, (Tipo A o Tipo C). (Y mandar a la memoria a trabajar)  
* **MEMORY:** Se encarga de guardar valores en el registro M específico, utiliza 
    el valor que obtuvo la CPU luego de realizar las operaciones. 

En conclusión, el Computer es el chip que une todos los chips anteriores, y es el encargado de ejecutar las instrucciones del programa, de modo que podemos hacer pruebas más robustas con él.

---
## 4. Design.txt

Este documento describe cómo se extendió la arquitectura de la computadora **Hack** para soportar operaciones de desplazamiento de bits (`<<` y `>>`) dentro de las instrucciones tipo C, sin modificar su formato original de 16 bits.

## Problema

La ALU original de Hack **no soporta operaciones shift**, y como todas las operaciones deben pasar por el campo `comp`, era necesario definir una nueva codificación que permitiera:

- Integrar `shift left` y `shift right`
- Mantener el formato estándar:  
  `111 a cccccc ddd jjj`
- No romper la compatibilidad con el diseño existente

## Solución

Se reutilizaron combinaciones específicas del campo `comp` para representar operaciones de desplazamiento.

### Reglas clave:

- `c1` → Selecciona el operando:
  - `0`: usa `D`
  - `1`: usa `A` o `M`

- `c2–c5 = 1000` → Firma que identifica operación **shift**

- `c6` → Dirección:
  - `0`: `<<` (shift izquierda)
  - `1`: `>>` (shift derecha)

- Bit `a`:
  - Solo aplica si `c1 = 1`
  - `0`: usa `A`
  - `1`: usa `M`

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
