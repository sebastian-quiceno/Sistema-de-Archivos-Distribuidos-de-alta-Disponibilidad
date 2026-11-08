# Tarea FE-07: Validación robusta de subida de archivos y alineación con restricciones de DFSha

## 1. Identificación
- **ID:** FE-07
- **Título:** Validación robusta en la carga de archivos, control de extensiones compatibles y retroalimentación de progreso en DFSha
- **Dominio:** Frontend
- **Tipo:** Integración / Validación / Error
- **Prioridad:** Alta

---

## 2. Problema
### Estado actual
El componente de carga de archivos (`UploadFileModal.tsx`) presenta inconsistencias de dominio con el backend de almacenamiento distribuido:

1. **Mapeo de extensiones frágil y choque con la base de datos:**
   En `UploadFileModal.tsx:13-34`, la función `getFileType` solo reconoce: `doc`, `docx`, `pdf`, `png`, `jpg`, `jpeg`, `mp3`, `mp4`. Cualquier otra extensión (como `.txt`, `.csv`, `.zip`, `.xlsx`) retorna `"noType"`.
   En el backend (`UploadFileUseCase.java:41-43`), se ejecuta:
   ```java
   FileType fileType = fileTypeRepository.getByName(dto.fileTypeName()).orElseThrow(
       () -> new FileTypeNotFoundException("No se encontro el FileType con el id: '" + dto.fileTypeName() + "'")
   );
   ```
   Como `"noType"` no existe en la tabla `file_type` de la base de datos (donde solo están registrados `doc`, `pdf`, `png`, `jpg`, `mp3`, `mp4`), la petición falla con un error `404 Not Found` en lugar de una validación clara de cliente.

2. **Ausencia del atributo `accept` en el selector de archivos:**
   El input `<input type="file">` no cuenta con atributo `accept`. El usuario puede seleccionar cualquier formato no soportado en su explorador de archivos local sin que la interfaz le prevenga.

3. **Sin validación de tamaño máximo en cliente:**
   No se valida el peso del archivo antes de transferirlo al servidor. Si el archivo supera el límite permitido por Spring Boot (`MaxUploadSizeExceededException`), el servidor aborta la conexión con un error 413 Payload Too Large.

4. **Experiencia de usuario durante la carga:**
   La subida de archivos grandes en un sistema distribuido con particionamiento y replicación de chunks ($R=3$) puede tomar varios segundos. Aunque existe `isUploading`, no se muestra una barra de progreso real o un spinner claro que indique las fases de la subida.

### Impacto
Subir archivos comunes (como textos, hojas de cálculo o archivos comprimidos) genera fallos crípticos 404 del backend. No se previene al usuario sobre formatos válidos y la experiencia de subida carece de retroalimentación en tiempo real.

---

## 3. Ubicación
- `ClientFrontEnd/src/components/folders/UploadFileModal.tsx`
- `ClientFrontEnd/src/services/fileService.ts`
- `ClientFrontEnd/src/types/fileType.ts`
- `ClientFrontEnd/src/styles/folder/UploadFileModal.css`

---

## 4. Objetivo
Alinear las validaciones del modal de carga con el catálogo de tipos admitidos por el backend DFSha, restringir el selector de archivos a formatos compatibles, validar anticipadamente el peso máximo en cliente, y proporcionar un indicador visual de progreso y retroalimentación clara durante la transferencia de datos.

---

## 5. Requisitos
- **Restricción de tipos de archivo:**
  - Configurar en el `<input type="file">` el atributo `accept`:
    ```html
    accept=".doc,.docx,.pdf,.png,.jpg,.jpeg,.mp3,.mp4"
    ```
  - Mapear adecuadamente las extensiones compatibles al nombre exacto esperado por el backend (`docx` $\rightarrow$ `doc`, `jpeg` $\rightarrow$ `jpg`).
  - Si el usuario selecciona un archivo con extensión no admitida, mostrar inmediatamente un mensaje de error descriptivo: *"El formato del archivo no es compatible. Formatos admitidos: Documentos (DOC, PDF), Imágenes (PNG, JPG) y Multimedia (MP3, MP4)."* y evitar habilitar el botón de subida.
- **Validación de tamaño máximo:**
  - Definir una constante de tamaño máximo permitido (ej. 100 MB o según configuración de `spring.servlet.multipart.max-file-size`).
  - Si el archivo excede dicho límite, informar al usuario antes de iniciar la solicitud HTTP.
- **Retroalimentación de carga:**
  - Utilizar el evento `onUploadProgress` de Axios en `fileService.uploadFile` para calcular y reportar el porcentaje de subida (0% a 100%).
  - Mostrar una barra de progreso animada en el modal mientras `isUploading` sea verdadero.
  - Al completar la subida (100%), mostrar un mensaje breve de *"Procesando y distribuyendo chunks en nodos..."* antes de cerrar el modal.

---

## 6. Alcance
- **Cambios necesarios:**
  - Refactorización de `UploadFileModal.tsx` con manejo de progreso y validación de tipos.
  - Modificación de la firma de `uploadFile` en `fileService.ts` para aceptar opcionalmente un callback `onProgress?: (percent: number) => void`.
  - Estilos de barra de progreso en `UploadFileModal.css`.
- **Restricciones:**
  - La lista de extensiones admitidas debe mantenerse sincronizada con los tipos registrados en la base de datos del middleware.

---

## 7. Criterios de aceptación
- El diálogo selector de archivos filtra de forma predeterminada los formatos soportados.
- Si un usuario fuerza la selección de un archivo incompatible (ej. `.zip`), la interfaz muestra el error de validación y bloquea el botón de envío.
- Durante la carga, se visualiza el porcentaje de transferencia completado.
- Al completarse la carga, el modal se cierra y la lista de archivos de la carpeta actual se actualiza automáticamente con el nuevo archivo.
- Si la subida falla en el servidor, se expone el mensaje de error devuelto por la API sin cerrar el modal.

---

## 8. Pruebas
1. **Filtro de extensiones:** Abrir el modal de subida y validar que el explorador de archivos del sistema limite las opciones a las extensiones configuradas en `accept`.
2. **Validación de archivo inválido:** Seleccionar un archivo no permitido y verificar que aparezca la advertencia en pantalla sin realizar peticiones de red.
3. **Barra de progreso:** Subir un archivo de tamaño moderado (ej. 10 MB - 30 MB) y corroborar que la barra de progreso avance de forma continua hasta el 100%.
4. **Refresco automático:** Finalizada la subida, verificar que el nuevo archivo aparezca en la tabla de `FileExplorer` con su tamaño y fecha correspondientes.

---

## 9. Dependencias
- **Tareas previas:** FE-01, FE-04.
- **Tareas relacionadas:** FE-05 (carpetas).

