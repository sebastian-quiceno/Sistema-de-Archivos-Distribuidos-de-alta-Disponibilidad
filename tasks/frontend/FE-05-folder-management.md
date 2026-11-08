# Tarea FE-05: Gestión integral de carpetas en el explorador (Creación, Renombrado, Eliminación y Refresco)

## 1. Identificación
- **ID:** FE-05
- **Título:** Gestión integral de carpetas en el explorador: creación de directorios, corrección de modales y sincronización reactiva
- **Dominio:** Frontend
- **Tipo:** Funcionalidad faltante / Error / UX
- **Prioridad:** Alta

---

## 2. Problema
### Estado actual
El backend cuenta con endpoints completos para el ciclo de vida de directorios en `FolderController`:
- `POST /api/folders` (`CreateFolderUseCase`)
- `GET /api/folders/{id}/subfolders` (`GetFoldersByFatherFolderUseCase`)
- `PATCH /api/folders/{id}/rename` (`RenameFolderUseCase`)
- `PATCH /api/folders/{id}/move` (`MoveFolderUserCase`)
- `DELETE /api/folders/{id}` (`DeleteFolderUseCase`)

Sin embargo, en el Frontend se evidencian las siguientes deficiencias:

1. **Inexistencia de acción para crear carpetas:**
   No existe ningún botón, diálogo o modal en la interfaz para crear una nueva carpeta dentro del directorio actual, a pesar de que el servicio `folderService.createFolder` ya está implementado.
2. **Defectos en `WriteModal.tsx` (Renombrar carpeta):**
   - El botón de confirmación tiene como texto `"Si, borrar carpeta"` (copiado por error de `DeleteModal.tsx`), lo cual desconcierta gravemente al usuario al intentar renombrar.
   - Tras enviar el formulario exitosamente, el modal no se cierra automáticamente (`closeMethod()` nunca es invocado en el `try`).
   - El estado `error` es asignado en el bloque `catch`, pero no se renderiza en el JSX, ocultando al usuario los motivos de un posible fallo.
   - No notifica al componente padre (`FileExplorer`) que debe actualizar el nombre de la carpeta.
3. **Defectos en `DeleteModal.tsx` (Borrar carpeta):**
   - La ejecución de `deleteFolder(folderId)` en la línea 39 no es asíncrona (`onClick={() => deleteFolder(folderId)}`), no maneja excepciones ni estados de carga, y no cierra el modal tras completar la eliminación.
   - No notifica a `FileExplorer` para remover la carpeta del estado local, requiriendo recargar la página.
4. **Acción inválida de descarga en carpetas:**
   En `FolderBlock.tsx:48`, se renderiza `<ActionButton onClick={() => null} variant="download" />`. Las carpetas no disponen de descarga como archivo plano, lo que confunde al usuario con un botón que no produce respuesta alguna.
5. **Formato de fecha:**
   Al igual que en archivos, la fecha se presenta como `modifiedDate.getDate()`, mostrando solo el número de día.

### Impacto
El usuario no puede organizar su estructura de almacenamiento porque no puede crear carpetas. Los diálogos de renombrar y borrar muestran textos equívocos, no gestionan errores y no actualizan la vista tras completar la acción.

---

## 3. Ubicación
- `ClientFrontEnd/src/components/folders/FileExplorer.tsx`
- `ClientFrontEnd/src/components/folders/FolderBlock.tsx`
- `ClientFrontEnd/src/components/folders/WriteModal.tsx`
- `ClientFrontEnd/src/components/folders/DeleteModal.tsx`
- `ClientFrontEnd/src/components/folders/AsideButtonsFileExplorer.tsx`
- `ClientFrontEnd/src/services/folderService.ts`

---

## 4. Objetivo
Implementar la funcionalidad completa de creación de carpetas desde la interfaz, corregir los textos, control de errores y ciclo de vida de los modales de edición y eliminación de directorios, y asegurar la actualización reactiva del árbol de carpetas tras cualquier modificación.

---

## 5. Requisitos
- **Creación de directorios:**
  - Crear un nuevo componente modal `CreateFolderModal.tsx` con input para el nombre de la carpeta y validación de obligatoriedad.
  - Agregar un botón accesible "Nueva Carpeta" en la barra lateral (`AsideButtonsFileExplorer.tsx`) o en la cabecera de la lista de archivos.
  - Conectar el modal con `folderService.createFolder({ folderName, fatherId: currentFolderId })`.
  - Al crearse satisfactoriamente, cerrar el modal y refrescar la lista de carpetas en `FileExplorer`.
- **Corrección de `WriteModal.tsx`:**
  - Corregir el texto del botón principal a `"Guardar Cambios"` o `"Renombrar"`.
  - Asegurar que al completar el renombrado exitosamente se invoque `closeMethod()` y un callback `onRenameSuccess(newFolderName)`.
  - Renderizar en el JSX el mensaje de error si la petición falla (ej. nombre duplicado o permisos insuficientes).
  - Pre-llenar el input con el nombre actual de la carpeta (`folderName`).
- **Corrección de `DeleteModal.tsx`:**
  - Transformar el handler a una función asíncrona con gestión de `try/catch`.
  - Añadir estado de carga (`isDeleting`) y deshabilitar los botones mientras se ejecuta `DELETE /api/folders/{id}`.
  - Al completar la eliminación, cerrar el modal e invocar `onDeleteSuccess(folderId)` para remover la carpeta de la vista.
  - Mostrar feedback visible en caso de error (ej: si la carpeta contiene archivos o no pertenece al usuario).
- **Limpieza de acciones en `FolderBlock.tsx`:**
  - Ocultar o remover el botón de descarga en `FolderBlock`, o deshabilitarlo con un tooltip aclaratorio si no existe descarga comprimida en backend.
  - Aplicar formato de fecha legible mediante utilidad compartida.

---

## 6. Alcance
- **Cambios necesarios:**
  - Creación del componente `CreateFolderModal.tsx`.
  - Refactorización de `WriteModal.tsx`, `DeleteModal.tsx`, `FolderBlock.tsx` y `AsideButtonsFileExplorer.tsx`.
  - Exposición de callbacks de refresco en `FileExplorer.tsx`.
- **Restricciones:**
  - Respetar los contratos vigentes de los endpoints `/api/folders/**`.

---

## 7. Criterios de aceptación
- El usuario puede hacer clic en "Nueva Carpeta", escribir un nombre válido y crear una subcarpeta dentro del directorio actual (`POST /api/folders`); la carpeta aparece de inmediato en la lista.
- En el modal de renombrar carpeta, el botón dice claramente "Renombrar" o "Guardar"; al confirmar, la carpeta se renombra en backend y la interfaz refleja el cambio de inmediato sin recargar.
- En el modal de eliminar carpeta, al presionar "Sí, borrar carpeta", se muestra estado de eliminación, se cierra el modal y la carpeta se retira de la vista.
- Los modales muestran mensajes de error legibles ante respuestas HTTP 400, 403 o 500 del backend.
- Las carpetas no muestran un botón de descarga ficticio que confunda al usuario.

---

## 8. Pruebas
1. **Creación de carpeta raíz y subcarpeta:** Crear una carpeta en la raíz y otra dentro de una subcarpeta existente; validar en DevTools las llamadas a `POST /api/folders` con `fatherId` correcto y comprobar su renderizado en la tabla.
2. **Renombrar carpeta:** Renombrar una carpeta existente; verificar `PATCH /api/folders/{id}/rename` y corroborar que el nuevo nombre se visualice de inmediato.
3. **Eliminar carpeta:** Eliminar una carpeta recién creada; comprobar petición `DELETE /api/folders/{id}` y verificar que desaparece de la vista.
4. **Validaciones de formulario:** Intentar crear o renombrar con cadena vacía o espacios en blanco; verificar que se impida el envío y se muestre la advertencia correspondiente.

---

## 9. Dependencias
- **Tareas previas:** FE-01, FE-02.
- **Tareas relacionadas:** FE-04 (operaciones sobre archivos), FE-06 (navegación y migas de pan).

