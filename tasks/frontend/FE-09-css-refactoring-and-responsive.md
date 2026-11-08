# Tarea FE-09: Refactorización de estilos CSS, consistencia visual, tipografía y diseño adaptativo

## 1. Identificación
- **ID:** FE-09
- **Título:** Normalización de estilos CSS, corrección tipográfica, solución de contraste y diseño adaptativo (Responsive Design)
- **Dominio:** Frontend
- **Tipo:** Interfaz / Responsive / Accesibilidad / Refactorización
- **Prioridad:** Media

---

## 2. Problema
### Estado actual
El código CSS del proyecto presenta graves deficiencias de arquitectura, errores tipográficos, problemas de contraste y nula adaptabilidad en dispositivos móviles o tablets:

1. **Contaminación y duplicación masiva del selector universal:**
   La regla de reseteo:
   ```css
   * {
     margin: 0;
     padding: 0;
     box-sizing: border-box;
   }
   ```
   está copiada y pegada al inicio de más de 9 archivos CSS individuales (`ActionButton.css`, `FileExplorer.css`, `FolderBlock.css`, `FileBlock.css`, `SignInPage.css`, `SignUpPage.css`, `WriteModal.css`, `DeleteModal.css`, `UserPerfileButton.css`) en lugar de estar declarada una sola vez en el reset global (`index.css`).

2. **Carga rota de la fuente tipográfica:**
   En `src/index.css:1`:
   ```css
   @import url("https://googleapis.com");
   * {
     font-family: 'Montserrat', sans-serif;
   }
   ```
   La URL `"https://googleapis.com"` es inválida para cargar hojas de estilo tipográficas. Como resultado, la fuente Montserrat nunca se descarga ni se aplica, recurriendo a la fuente genérica sans-serif del sistema.

3. **Inexistencia de Responsive Design:**
   - `FileExplorerPage.css`: Usa `width: 100vw; height: 100vh; flex-direction: row;` con proporciones fijas `flex: 1` para la barra lateral y `flex: 4` para el contenido principal. En pantallas pequeñas (< 768px), la barra lateral aplasta el contenido, la tabla de archivos se trunca y se generan desbordamientos horizontales.
   - `SignInPage.css` y `SignUpPage.css`: La ventana de login/registro tiene `width: 60vw; height: 60vh;` y `flex-direction: row;`. En teléfonos móviles con pantallas estrechas, el ancho del 60vw genera un contenedor de menos de 220px, haciendo los campos de texto e imágenes totalmente ilegibles y desbordados.
   - No existe ninguna media query (`@media`) en las páginas para adaptar la disposición a vertical en móviles o colapsar la barra lateral.

4. **Errores de contraste e inconsistencias de clases:**
   - En `WriteModal.css:38`: El icono SVG tiene `stroke: var(--color-icon-action-write);` que corresponde a `#ffffff`. Como el fondo del modal es `var(--color-text-primary-light)` (`#f6ecde`, beige claro), el icono blanco sobre fondo beige es prácticamente invisible (contraste casi nulo).
   - En `File.tsx:212`: Se detecta un error de sintaxis en el color SVG `fill="#F6ECDEr"` (letra "r" añadida al final del código hexadecimal).
   - En `ActionButton.tsx:15`: Se utiliza la clase `className="acction-button"` con un error ortográfico en inglés.
   - En `ActionButton.css:55`: Se define la regla `.profile__button:hover { transform: scale(1.05); }`, la cual pertenece conceptualmente a `UserPerfileButton.css` y no a `ActionButton.css`.
   - Falta el archivo `index copy.css` que quedó como residuo.

### Impacto
La aplicación se descompone visualmente en dispositivos móviles y tabletas. La tipografía de diseño no se visualiza. Elementos como los iconos de modales pierden visibilidad por contraste deficiente y el código CSS viola los principios de mantenibilidad y especificidad limpia.

---

## 3. Ubicación
- `ClientFrontEnd/src/index.css`
- `ClientFrontEnd/src/styles/variables.css`
- `ClientFrontEnd/src/styles/pages/FileExplorerPage.css`
- `ClientFrontEnd/src/styles/pages/SignInPage.css`
- `ClientFrontEnd/src/styles/pages/SignUpPage.css`
- `ClientFrontEnd/src/styles/folder/ActionButton.css`
- `ClientFrontEnd/src/styles/folder/WriteModal.css`
- `ClientFrontEnd/src/styles/folder/File.css`
- `ClientFrontEnd/src/components/folders/File.tsx`
- `ClientFrontEnd/src/components/folders/ActionButton.tsx`

---

## 4. Objetivo
Unificar los estilos globales en `index.css`, corregir la carga de la fuente Google Fonts (Montserrat), resolver los fallos de contraste e incoherencia de selectores, e incorporar un diseño responsivo mediante media queries que permita utilizar la aplicación de manera óptima en resoluciones móviles, tablets y escritorio.

---

## 5. Requisitos
- **Normalización CSS global:**
  - Declarar el reset de box-sizing, márgenes y paddings exclusivamente en `src/index.css`.
  - Retirar las declaraciones `* { margin: 0; padding: 0; box-sizing: border-box; }` redundantes en cada archivo CSS de componentes.
  - Corregir el `@import` en `index.css` hacia Google Fonts para descargar la familia Montserrat con sus pesos adecuados (ej: 400, 500, 600, 700):
    ```css
    @import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700&display=swap');
    ```
- **Diseño Responsivo:**
  - **Páginas de Login/Registro (`SignInPage`, `SignUpPage`):**
    - En pantallas `< 768px`, cambiar `flex-direction: row` a `column`, reemplazar `width: 60vw` por `width: min(92vw, 420px)` y `height: auto` con padding cómodo.
    - Ocultar o reducir el logotipo superior para priorizar los campos del formulario.
  - **Explorador de archivos (`FileExplorerPage`):**
    - En pantallas `< 900px`, convertir la barra lateral en un menú superior o menú colapsable (botón hamburguesa).
    - Permitir que el contenedor de archivos tenga scroll horizontal o vista en tarjetas compactas para evitar desbordes de las columnas de fecha y acciones.
- **Correcciones visuales y de accesibilidad:**
  - Corregir el color de trazo en `WriteModal.css` para que use un tono de alto contraste sobre el fondo beige (ej: `var(--color-text-primary-dark)` o `#573B2C`).
  - Corregir `fill="#F6ECDEr"` en `File.tsx` a `fill="#F6ECDE"`.
  - Corregir la clase `acction-button` a `action-button` en JSX y CSS.
  - Mover las reglas de `.profile__button` de `ActionButton.css` a `UserPerfileButton.css`.
  - Asegurar un ratio de contraste mínimo de 4.5:1 para todos los textos y botones interactivos (estándar WCAG AA).

---

## 6. Alcance
- **Cambios necesarios:**
  - Edición y limpieza de `index.css` y archivos CSS modulares.
  - Corrección de typos en `File.tsx` y `ActionButton.tsx`.
  - Inclusión de breakpoints y reglas `@media (max-width: ...)` en los archivos CSS de páginas y explorador.
- **Restricciones:**
  - Mantener la paleta cromática corporativa definida en `variables.css`.

---

## 7. Criterios de aceptación
- La tipografía Montserrat se carga y visualiza correctamente en todos los textos de la interfaz.
- La aplicación se visualiza de forma armónica sin desbordamientos de pantalla en anchos de 375px (móvil), 768px (tablet) y 1440px (escritorio).
- En el modal de renombrar, el icono superior es nítido y claramente visible con suficiente contraste contra el fondo.
- No existen reglas globales de reseteo repetidas en los archivos CSS de componentes.
- No se registran errores de sintaxis en colores SVG ni clases mal escritas.

---

## 8. Pruebas
1. **Inspección de fuentes:** Abrir la pestaña Red y verificar la solicitud a `fonts.googleapis.com` con status 200; inspeccionar en DevTools la propiedad `font-family` computada de los elementos de texto.
2. **Prueba de diseño responsivo:** Utilizar el modo de emulación de dispositivos de Chrome DevTools en tamaños iPhone 12/14 (390px) e iPad Air (820px); verificar que no haya scrollbar horizontal no deseada y que todos los botones y formularios sigan siendo interactivos y legibles.
3. **Validación de contraste:** Comprobar visualmente y con herramientas de accesibilidad (Lighthouse) el contraste del icono en `WriteModal` y los botones de acción.

---

## 9. Dependencias
- **Tareas previas:** FE-01, FE-02, FE-03, FE-04.
- **Tareas relacionadas:** FE-05, FE-06.

