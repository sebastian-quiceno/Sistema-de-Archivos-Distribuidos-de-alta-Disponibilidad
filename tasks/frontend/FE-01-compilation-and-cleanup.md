# Tarea FE-01: Corrección de errores de compilación TypeScript y limpieza de código huérfano

## 1. Identificación
- **ID:** FE-01
- **Título:** Corrección de errores de compilación TypeScript y eliminación/limpieza de componentes y archivos huérfanos
- **Dominio:** Frontend
- **Tipo:** Error / Refactorización técnica
- **Prioridad:** Crítica

---

## 2. Problema
### Estado actual
La ejecución del comando de construcción del proyecto (`npm run build`, que ejecuta `tsc -b && vite build`) falla inmediatamente con código de salida 1 debido a 6 errores de TypeScript (`TS6133: declared but its value is never read`). Adicionalmente, el linter (`npm run lint`) reporta 7 violaciones críticas de ESLint (`@typescript-eslint/no-unused-vars` y `react-refresh/only-export-components`).

### Evidencia
Salida del compilador TypeScript:
```text
src/app/pages/SignUpPage.tsx(22,10): error TS6133: 'error' is declared but its value is never read.
src/components/folders/AsideFileExplorer.tsx(1,10): error TS6133: 'AsideFileExplorer' is declared but its value is never read.
src/components/folders/WriteFileModal.tsx(14,3): error TS6133: 'folderName' is declared but its value is never read.
src/components/folders/WriteFileModal.tsx(18,10): error TS6133: 'error' is declared but its value is never read.
src/components/folders/WriteModal.tsx(14,3): error TS6133: 'folderName' is declared but its value is never read.
src/components/folders/WriteModal.tsx(18,10): error TS6133: 'error' is declared but its value is never read.
```

Además existen archivos con anomalías estructurales:
- `src/components/folders/AsideFileExplorer.tsx`: Contiene una función vacía no exportada que no se utiliza en ningún lugar.
- `src/components/folders/FileRow.tsx`: Declaración de función `File` sin sentencia `return` de JSX, en conflicto de nombres con `File.tsx`.
- `src/index copy.css`: Archivo CSS residual duplicado y no referenciado.

### Impacto
El proyecto no puede ser empaquetado para despliegue ni verificado en pipelines de integración continua. Bloquea cualquier proceso de compilación y enmascara otros posibles fallos de tipado.

---

## 3. Ubicación
- `ClientFrontEnd/src/app/pages/SignUpPage.tsx`
- `ClientFrontEnd/src/components/folders/AsideFileExplorer.tsx`
- `ClientFrontEnd/src/components/folders/WriteModal.tsx`
- `ClientFrontEnd/src/components/folders/WriteFileModal.tsx`
- `ClientFrontEnd/src/components/folders/FileRow.tsx`
- `ClientFrontEnd/src/index copy.css`

---

## 4. Objetivo
Lograr una base de código Frontend sin advertencias ni errores de compilación de TypeScript (`tsc -b`), pasando limpiamente el linter (`npm run lint`), eliminando archivos obsoletos y asegurando que las variables de estado declaradas se utilicen correctamente en los componentes correspondientes.

---

## 5. Requisitos
- Eliminar o conectar adecuadamente las variables no utilizadas respetando `noUnusedLocals` y `noUnusedParameters` de `tsconfig.app.json`.
- En `SignUpPage.tsx`, renderizar el mensaje de error capturado en la interfaz de usuario en lugar de dejar la variable huérfana.
- En `WriteModal.tsx`, mostrar el nombre actual (`folderName`) y renderizar el mensaje de error en caso de fallo al renombrar.
- En `WriteFileModal.tsx`, resolver su estado según la refactorización de operaciones de archivo (ver tarea FE-04).
- Eliminar los archivos muertos o incompletos: `AsideFileExplorer.tsx`, `FileRow.tsx` y `index copy.css`.

---

## 6. Alcance
- **Cambios necesarios:**
  - Depuración y limpieza de imports y variables no utilizadas en componentes React.
  - Renderizado de estados de error existentes en el JSX.
  - Supresión de ficheros identificados como basura o código muerto no referenciado.
- **Restricciones:**
  - No deshabilitar las reglas `"noUnusedLocals": true` ni `"noUnusedParameters": true` en `tsconfig.app.json`. La solución debe corregir el código fuente.

---

## 7. Criterios de aceptación
- `npm --prefix ClientFrontEnd run build` finaliza con código de salida `0` y genera el directorio `dist/` sin errores de compilación.
- `npm --prefix ClientFrontEnd run lint` pasa con 0 errores y 0 advertencias.
- No quedan archivos huérfanos ni definiciones de componentes sin exportación en `src/components/folders/`.

---

## 8. Pruebas
1. **Compilación completa:** Ejecutar `npm run build` en la raíz de `ClientFrontEnd` y verificar la finalización exitosa.
2. **Auditoría estática:** Ejecutar `npm run lint` y corroborar que ESLint no arroje incidencias.
3. **Verificación de archivos:** Validar que los archivos eliminados ya no existan en el árbol de archivos y que ninguna importación se haya roto.

---

## 9. Dependencias
- **Tareas previas:** Ninguna (esta tarea debe ser ejecutada en primer lugar).
- **Tareas relacionadas:** FE-03 (formularios de autenticación), FE-04 (modal de archivo), FE-05 (modal de carpeta).

