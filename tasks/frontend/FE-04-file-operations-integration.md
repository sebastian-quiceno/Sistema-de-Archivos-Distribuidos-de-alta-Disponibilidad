# Tarea FE-04: Conexión y estabilización de operaciones sobre archivos (Descarga, Borrado, Actualización)

## 1. Identificación
- **ID:** FE-04
- **Título:** Conexión y estabilización de operaciones sobre archivos (Descarga, Eliminación, Actualización y Formateo)
- **Dominio:** Frontend
- **Tipo:** Integración / Error / Funcionalidad incompleta
- **Prioridad:** Crítica

---

## 2. Problema
### Estado actual
Las operaciones sobre archivos individuales dentro del explorador de archivos (`FileBlock.tsx`) presentan graves fallos de integración y operaciones desconectadas:

1. **Descarga de archivos inoperante:**
   El botón de descarga en `FileBlock.tsx:49` tiene configurado un callback vacío:
   ```tsx
   <ActionButton onClick={() => null} variant="download" />
   ```
   La función `downloadFile` implementada en `src/services/fileService.ts` (que invoca `/api/files/{id}/download` y reconstruye el Blob en el navegador) nunca es llamada.

2. **Borrado de archivos utiliza el modal de carpetas:**
   En `FileBlock.tsx:9` y `FileBlock.tsx:56`, para borrar un archivo se instancia:
   ```tsx
   <DeleteModal
     folderId={fileId}
     folderName={fileName}
     closeMethod={() => setShowDeleteModal(false)}
   />
   ```
   Esto provoca que al intentar borrar un archivo se llame a `deleteFolder(fileId)` contra `/api/folders/{id}`. Como el UUID corresponde a un archivo y no a una carpeta, el backend responde con error 404 (`FolderNotFoundException`).
   Existe un componente específico `DeleteFileModal.tsx` en `src/components/folders/` que invoca `deleteFile(fileId)` pero se encuentra huérfano y no es utilizado.

3. **Edición de archivos utiliza el modal de carpetas:**
   En `FileBlock.tsx:42`, para editar un archivo se abre `WriteModal` (renombrar carpeta). Existe un archivo `WriteFileModal.tsx`, pero es una copia duplicada de `WriteModal.tsx` que sigue llamando a `renameFolder`.
   Además, el backend soporta la actualización de contenido binario mediante `PUT /api/files/{id}` (manejado por `fileService.updateFile`), pero no hay forma de usarlo desde la UI.

4. **Falta de sincronización tras la eliminación:**
   Ni `DeleteModal` ni `DeleteFileModal` cuentan con una función callback `onSuccess` para notificar al componente padre (`FileExplorer`) que debe remover el elemento o refrescar la lista de archivos.

5. **Formateo deficiente de datos:**
   - La fecha de última modificación se formatea como `modifiedDate.getDate()`, mostrando únicamente el día del mes (ej: "8" en lugar de "08/11/2026 10:30").
   - El tamaño se muestra como el número bruto de bytes (`{size}`, ej: `31457288`) sin unidades humanas legibles (B, KB, MB, GB).

### Impacto
El usuario no puede descargar archivos, no puede eliminarlos (se dispara una llamada errónea de carpeta que falla), no puede actualizar el contenido del archivo y la información desplegada en la tabla es cruda e imprecisa.

---

## 3. Ubicación
- `ClientFrontEnd/src/components/folders/FileBlock.tsx`
- `ClientFrontEnd/src/components/folders/DeleteFileModal.tsx`
- `ClientFrontEnd/src/components/folders/WriteFileModal.tsx`
- `ClientFrontEnd/src/components/folders/FileExplorer.tsx`
- `ClientFrontEnd/src/services/fileService.ts`
- `ClientFrontEnd/src/types/fileType.ts`

---

## 4. Objetivo
Conectar completamente el ciclo de vida de los archivos en la interfaz: permitir la descarga directa del archivo binario con su nombre original y extensión, posibilitar la eliminación segura mediante `DeleteFileModal` con refresco automático de la lista, implementar o conectar el modal para actualizar/reemplazar el contenido de un archivo (`fileService.updateFile`), y formatear adecuadamente los tamaños y fechas.

---

## 5. Requisitos
- **Descarga de archivos:**
  - En `FileBlock.tsx`, conectar el `ActionButton variant="download"` con `downloadFile`.
  - Pasar a `downloadFile` tanto el `fileId` como el `fileName` para garantizar que el archivo descargado por el navegador conserve su nombre original y extensión adecuada.
  - Mostrar feedback visual o estado de espera si la reconstrucción de chunks en el DFSha toma tiempo.
- **Eliminación de archivos:**
  - Sustituir el uso de `DeleteModal` por `DeleteFileModal` en `FileBlock.tsx`.
  - En `DeleteFileModal.tsx`:
    - Hacer la llamada a `deleteFile(fileId)` asíncrona (`await`).
    - Deshabilitar botones durante la eliminación para evitar doble clic.
    - Manejar errores mostrando el mensaje de fallo si el backend rechaza la operación.
    - Al completarse con éxito, llamar a un callback `onDeleteSuccess` y cerrar el modal.
- **Actualización de contenido de archivo:**
  - Adaptar `WriteFileModal.tsx` (o crear `UpdateFileModal.tsx`) para permitir reemplazar el contenido binario del archivo llamando a `updateFile({ fileId, file })`.
  - Incluir selector de archivo (`<input type="file">`) y botón de confirmación.
- **Formateo de metadatos:**
  - Crear utilidades de formateo:
    - `formatBytes(bytes: number): string` $\rightarrow$ transforma ej. `31457288` en `"30.0 MB"`.
    - `formatDate(date: Date): string` $\rightarrow$ transforma la fecha en formato legible local (ej: `"08 nov 2026, 10:30"`).

---

## 6. Alcance
- **Cambios necesarios:**
  - Refactorización de `FileBlock.tsx` para importar los componentes correctos y conectar callbacks.
  - Ajustes en `DeleteFileModal.tsx` para control de errores y propagación de eventos.
  - Creación o corrección del modal de actualización de archivo.
  - Modificación de `FileExplorer.tsx` para refrescar el listado cuando se elimine o actualice un archivo.
- **Restricciones:**
  - No modificar la lógica de particionamiento ni almacenamiento del backend; aprovechar las capacidades existentes en `FileController` y `fileService`.

---

## 7. Criterios de aceptación
- Al hacer clic en el botón de descarga de un archivo en la tabla, el navegador descarga el archivo con su contenido binario completo, tamaño exacto y nombre original.
- Al hacer clic en eliminar archivo, se abre el modal de confirmación con el nombre del archivo. Al confirmar, se invoca `DELETE /api/files/{id}`; el modal se cierra y el archivo desaparece de la vista sin requerir recarga manual.
- Si la eliminación falla (ej. error 403 Forbidden por no pertenencia), el modal muestra el error y no cierra abruptamente.
- Al seleccionar la acción de actualizar archivo, se puede elegir un nuevo archivo binario y ejecutar `PUT /api/files/{id}`, refrescando la fecha y tamaño en la tabla.
- La columna de tamaño muestra valores amigables (`512 KB`, `2.4 MB`, etc.) y la columna de fecha muestra día, mes, año y hora.

---

## 8. Pruebas
1. **Descarga de archivos:** Subir un archivo de prueba (ej. PDF o PNG); hacer clic en el botón de descarga y verificar que el archivo descargado se abra correctamente y contenga la información original.
2. **Eliminación exitosa:** Eliminar un archivo creado por el usuario; verificar petición `DELETE /api/files/{id}` con código 204 y desaparición del elemento en la interfaz.
3. **Manejo de error en eliminación:** Simular fallo de red o permisos y comprobar que el modal despliegue el mensaje de error.
4. **Formateo visual:** Verificar que archivos de diferentes tamaños (bytes, kilobytes, megabytes) se rendericen con su correspondiente sufijo de unidad.

---

## 9. Dependencias
- **Tareas previas:** FE-01, FE-02.
- **Tareas relacionadas:** FE-05 (operaciones sobre carpetas), FE-07 (subida de archivos), BE-01 (nombre en Content-Disposition).

