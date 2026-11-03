# Implementación de EndPoints

## Objetivo

Implementar los endpoints en base a los casos de uso existentes en la capa de aplicación (`com.sdan.middleware.Application`). Cada endpoint debe corroborar que el usuario esté autenticado y autorizado para realizar la acción correspondiente, delegando la lógica de negocio exclusivamente en los casos de uso.

El objetivo es exponer las funcionalidades del backend por medio de una API REST utilizando Spring Boot, asegurando el cumplimiento de los principios SOLID, la separación en capas de la arquitectura hexagonal/limpia y las convenciones existentes en el proyecto.

---

## 1. AuthController (`/api/auth`)

**Prioridad: Alta**

Controlador encargado de la autenticación, registro de nuevos usuarios y emisión de credenciales/tokens JWT.

### Endpoints a revisar / consolidar

| Método | Endpoint | Caso de Uso | Permiso Requerido | Entrada | Salida |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | `CreateNewUserUseCase` | Público (`permitAll`) | `SignUpRequest` (`username`, `password`) | `201 Created` - `UserCredentialsOutput` |
| `POST` | `/api/auth/login` | `UserLoginUseCase` | Público (`permitAll`) | `SignInRequest` (`username`, `password`) | `200 OK` - `UserCredentialsOutput` |

### Tareas

- Revisar los endpoints `register` y `login` existentes en la clase `AuthController`.
- Validar exhaustivamente las entradas antes de construir los Value Objects (`UserName`, etc.) para evitar excepciones no deseadas por strings vacíos o nulos.
- Asegurar que `register` retorne código `201 Created` con las credenciales y el token JWT generado.
- Asegurar que `login` retorne código `200 OK` con las credenciales y el token JWT generado.
- Comprobar que las excepciones de usuario (`UsernameAlreadyExist`, `UsernameNotFoundException`, `PasswordNotMatchException`) sean capturadas y transformadas correctamente en respuestas HTTP por el `GlobalExceptionHandler`.

### Criterios de aceptación

- Un usuario nuevo puede registrarse exitosamente desde `/api/auth/register` recibiendo su token JWT y status 201.
- No se permite el registro de usuarios duplicados (retorna status 409 Conflict).
- Un usuario registrado puede iniciar sesión desde `/api/auth/login` recibiendo su token JWT y status 200.
- Intentos de inicio de sesión con credenciales inválidas retornan status 401 Unauthorized o 404 Not Found según corresponda.
- Los inputs y outputs están correctamente desacoplados y formateados en formato JSON.

---

## 2. FolderController (`/api/folders`)

**Prioridad: Alta**

Nuevo controlador encargado de la gestión de directorios jerárquicos por usuario (`mkdir`, `ls`, mover y eliminar carpetas).

### Endpoints a implementar

| Método | Endpoint | Caso de Uso | Permiso Requerido | Entrada | Salida |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/folders` | `CreateFolderUseCase` | `folders.create` | `CreateFolderRequest` (`fatherId`, `folderName`) | `201 Created` |
| `GET` | `/api/folders/{id}/subfolders` | `GetFoldersByFatherFolderUseCase` | `folders.read` | Path Variable `id` (UUID de carpeta padre) | `200 OK` - `List<FolderOutput>` |
| `PATCH` | `/api/folders/{id}/move` | `MoveFolderUserCase` | `folders.update` | Path Variable `id`, `MoveFolderRequest` (`newFatherFolderId`) | `200 OK` o `204 No Content` |
| `DELETE` | `/api/folders/{id}` | `DeleteFolderUseCase` | `folders.delete` | Path Variable `id` (UUID) | `204 No Content` |

### Tareas

- Crear la clase controladora `FolderController` en `middleware/src/main/java/com/sdan/middleware/Infraestructure/In/FolderController.java` anotada con `@RestController` y `@RequestMapping("/api/folders")`.
- Implementar el endpoint `POST /api/folders` para crear carpetas:
  - Validar los datos de entrada (`folderName` no nulo ni vacío).
  - Mapear a `CreateFolderCommand(UUID fatherId, String folderName)`.
  - Invocar `CreateFolderUseCase.execute(command)`.
  - Proteger con `@PreAuthorize("hasAuthority('folders.create')")`.
- Implementar el endpoint `GET /api/folders/{id}/subfolders` para listar subcarpetas:
  - Recibir el identificador `id` de la carpeta padre como variable de ruta.
  - Invocar `GetFoldersByFatherFolderUseCase.execute(folderId)`.
  - Proteger con `@PreAuthorize("hasAuthority('folders.read')")`.
  - Retornar la lista de `FolderOutput`.
- Implementar el endpoint `PATCH /api/folders/{id}/move` para mover una carpeta en el árbol:
  - Recibir `id` de la carpeta a mover y `MoveFolderRequest` con `newFatherFolderId` (puede ser nulo si se mueve a raíz).
  - Mapear a `MoveFolderCommand(UUID folderId, UUID newFatherFolderId)`.
  - Invocar `MoveFolderUserCase.execute(command)`.
  - Proteger con `@PreAuthorize("hasAuthority('folders.update')")`.
- Implementar el endpoint `DELETE /api/folders/{id}` para eliminar carpetas:
  - Recibir `id` de la carpeta a eliminar.
  - Invocar `DeleteFolderUseCase.execute(folderId)`.
  - Proteger con `@PreAuthorize("hasAuthority('folders.delete')")`.
  - Retornar status 204 No Content.

### Criterios de aceptación

- Solo usuarios autenticados con los permisos correspondientes pueden acceder a las operaciones de carpetas.
- El usuario solo puede crear, consultar, mover o eliminar carpetas que le pertenezcan; de lo contrario, se rechaza la petición con 403 Forbidden (`FolderDoesNotBelongToUserException`).
- Si una carpeta solicitada no existe, se retorna 404 Not Found (`FolderNotFoundException`).
- Las operaciones respetan la integridad del árbol de directorios del usuario.

---

## 3. FileController (`/api/files`)

**Prioridad: Alta**

Controlador encargado de la transferencia, persistencia distribuida por chunks, reconstrucción, actualización, borrado y auditoría de integridad de archivos en el clúster DFSha.

### Endpoints a implementar / refactorizar

| Método | Endpoint | Caso de Uso | Permiso Requerido | Entrada | Salida |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/files/upload` | `UploadFileUseCase` | `files.create` | `multipart/form-data`: `file`, `folderId`, `fileTypeId`, `name` (opcional) | `201 Created` |
| `GET` | `/api/files/{id}/download` | `GetFileUseCase` | `files.read` | Path Variable `id` (UUID de archivo) | `200 OK` - Stream binario (`octet-stream`) |
| `PUT` | `/api/files/{id}` | `UpdateFileUseCase` | `files.update` | Path Variable `id`, `multipart/form-data`: `file` | `200 OK` o `204 No Content` |
| `DELETE` | `/api/files/{id}` | `DeleteFileUseCase` | `files.delete` | Path Variable `id` (UUID de archivo) | `204 No Content` |
| `POST` | `/api/files/check-integrity` | `CheckFilesUseCase` | `ROLE_ADMIN` o `files.read` | Query Param opcional `pageSize` (default: 50) | `200 OK` - `IntegrityCheckReport` |

### Tareas

- Refactorizar la clase `FileController` existente en `middleware/src/main/java/com/sdan/middleware/Infraestructure/In/FileController.java` para reemplazar las operaciones legadas de S3 directo y conectarla con los casos de uso del clúster de almacenamiento distribuido.
- Implementar el endpoint `POST /api/files/upload`:
  - Recibir la petición como `multipart/form-data` con el archivo binario (`MultipartFile file`), `folderId` (`UUID`), `fileTypeId` (`UUID`) y nombre alternativo opcional.
  - Obtener el `InputStream` del archivo y su tamaño en bytes (`file.getSize()`).
  - Mapear a `CreateFileCommand(folderId, fileTypeId, fileName, inputStream, size)`.
  - Invocar `UploadFileUseCase.execute(command)`.
  - Asegurar la correcta liberación y cierre de streams.
  - Proteger con `@PreAuthorize("hasAuthority('files.create')")` y retornar status 201 Created.
- Implementar el endpoint `GET /api/files/{id}/download`:
  - Recibir el `id` del archivo como variable de ruta.
  - Proteger con `@PreAuthorize("hasAuthority('files.read')")`.
  - Invocar `GetFileUseCase.execute(fileId)`.
  - Envolver el `InputStream` retornado en una respuesta HTTP adecuada para streaming (usando `StreamingResponseBody` o `InputStreamResource`).
  - Configurar cabeceras de respuesta estándar: `Content-Type: application/octet-stream`, `Content-Disposition: attachment; filename="..."`.
- Implementar el endpoint `PUT /api/files/{id}` para actualización de contenido:
  - Recibir `id` de archivo y nuevo contenido mediante `MultipartFile file`.
  - Mapear a `UpdateFileCommand(fileId, file.getInputStream(), file.getSize())`.
  - Invocar `UpdateFileUseCase.execute(command)`.
  - Proteger con `@PreAuthorize("hasAuthority('files.update')")`.
- Implementar el endpoint `DELETE /api/files/{id}`:
  - Recibir el identificador del archivo.
  - Invocar `DeleteFileUseCase.execute(fileId)`.
  - Proteger con `@PreAuthorize("hasAuthority('files.delete')")`.
  - Retornar status 204 No Content.
- Implementar el endpoint `POST /api/files/check-integrity` (o `/api/files/audit`):
  - Permitir auditar la integridad de los chunks distribuidos y ejecutar auto-sanación (*healing*).
  - Recibir parámetro opcional de paginación `pageSize`.
  - Invocar `CheckFilesUseCase.execute(pageSize)`.
  - Restringir acceso mediante seguridad (`@PreAuthorize("hasRole('ADMIN')")`).
  - Retornar el reporte `IntegrityCheckReport` en formato JSON.
- **Clarificación técnica sobre `CreateFileUseCase`:**
  - Dejar documentado que `CreateFileUseCase` corresponde a un esbozo anterior basado en almacenamiento simple de URLs prefirmadas, mientras que `UploadFileUseCase` es la implementación definitiva de DFSha que orquesta la partición en chunks, replicación ($R=3$) y almacenamiento multi-nodo mediante `FileService`.

### Criterios de aceptación

- Subir un archivo particiona correctamente el contenido en chunks y los almacena en los nodos de datos respetando el factor de replicación configurado.
- Descargar un archivo recupera y ensambla los chunks en orden secuencial, verificando la consistencia e integridad sin corromper el contenido binario.
- La actualización de un archivo coordina el reemplazo de chunks antiguos por los nuevos.
- La eliminación de un archivo borra la metadata y coordina la eliminación física de los chunks en los nodos de almacenamiento.
- Se valida la pertenencia del archivo al usuario autenticado en todas las operaciones (`FileDoesNotBelongToUserException` -> 403 Forbidden).
- Los recursos de red y streams de I/O se liberan de forma segura sin provocar fugas de memoria o descriptores abiertos.

---

## 4. Configuración e Inyección de Dependencias (Beans de Casos de Uso)

**Prioridad: Alta**

Dado que las clases de la capa de aplicación (`Application/File` y `Application/Folder`) no contienen anotaciones del framework Spring para preservar la pureza de la arquitectura hexagonal, es necesario registrarlas como beans en la capa de infraestructura.

### Tareas

- Crear la clase de configuración `FolderUseCaseConfig` en `middleware/src/main/java/com/sdan/middleware/Infraestructure/Config/FolderUseCaseConfig.java`:
  - Registrar bean para `CreateFolderUseCase(IUserRepository, IFolderRepository, ICurrentUser)`.
  - Registrar bean para `DeleteFolderUseCase(IFolderRepository, ICurrentUser)`.
  - Registrar bean para `GetFoldersByFatherFolderUseCase(IFolderRepository, ICurrentUser)`.
  - Registrar bean para `MoveFolderUserCase(IFolderRepository, ICurrentUser)`.
- Crear la clase de configuración `FileUseCaseConfig` en `middleware/src/main/java/com/sdan/middleware/Infraestructure/Config/FileUseCaseConfig.java`:
  - Registrar beans para los servicios del dominio/aplicación de archivos:
    - `FilePartitionService`
    - `NodeAssignmentService`
    - `ChunkStorageService`
    - `ChunkReplicationService`
    - `FileService`
  - Registrar beans para los casos de uso:
    - `UploadFileUseCase(IFileTypeRepository, IFolderRepository, ICurrentUser, FileService)`
    - `GetFileUseCase(ICurrentUser, IFileRepository, IFileReconstructionService)`
    - `UpdateFileUseCase(ICurrentUser, IFileRepository, IFileUpdateService)`
    - `DeleteFileUseCase(ICurrentUser, IFileRepository, IFileDeletionService)`
    - `CheckFilesUseCase(IFileRepository, IFileIntegrityService)`
- Verificar que no existan dependencias circulares y que todos los puertos requeridos cuenten con su correspondiente adaptador de infraestructura registrado.

### Criterios de aceptación

- La aplicación de Spring Boot inicia limpiamente sin errores de beans faltantes (`NoSuchBeanDefinitionException`) ni dependencias insatisfechas.
- Los controladores reciben todas sus dependencias mediante inyección en el constructor.

---

## 5. Manejo Global de Excepciones y Seguridad

**Prioridad: Media**

Asegurar que todas las excepciones que puedan surgir durante la ejecución de los endpoints sean gestionadas adecuadamente en `GlobalExceptionHandler`.

### Tareas

- Revisar el mapeo existente en `GlobalExceptionHandler.java`:
  - `UnauthorizedUserException`, `PasswordNotMatchException` $\rightarrow$ `401 Unauthorized`
  - `AccessDeniedException`, `FileDoesNotBelongToUserException`, `FolderDoesNotBelongToUserException` $\rightarrow$ `403 Forbidden`
  - `UsernameAlreadyExist` $\rightarrow$ `409 Conflict`
  - `FileNotFoundException`, `FolderNotFoundException`, `UserNotFoundException`, `UsernameNotFoundException`, `FileTypeNotFoundException` $\rightarrow$ `404 Not Found`
  - `IllegalArgumentException`, `DomainException`, errores de validación de argumentos $\rightarrow$ `400 Bad Request`
- Añadir manejador específico para `MethodArgumentNotValidException` y `MaxUploadSizeExceededException` (para archivos que sobrepasen el tamaño permitido de carga).
- Asegurar que `SecurityConfig` mantenga protegidas todas las rutas bajo `/api/folders/**` y `/api/files/**`, requiriendo cabecera `Authorization: Bearer <JWT>`.

### Criterios de aceptación

- Ninguna excepción del backend se fuga como traza de pila (stacktrace) sin procesar al cliente; todas devuelven la estructura JSON uniforme con código de estado HTTP y mensaje descriptivo.
- Peticiones sin token o con token inválido/expirado son rechazadas con status 401 Unauthorized por el `RestAuthenticationEntryPoint`.

---

## Consideraciones importantes

- **Reutilización y pureza:** Antes de crear nuevas implementaciones, verificar los servicios existentes en la capa de aplicación (`FileService`, `FilePartitionService`, `ChunkStorageService`, etc.). Los controladores deben actuar únicamente como adaptadores de entrada (deserializar petición, verificar permisos HTTP, invocar caso de uso y serializar respuesta).
- **Evitar lógica de negocio en controladores:** No colocar validaciones de permisos de recursos o lógica de particionado en los controladores; dicha lógica debe permanecer dentro de los casos de uso y servicios de aplicación.
- **Gestión de Streams en descarga y subida:** Al transferir archivos grandes, procesar siempre el flujo como `InputStream` / `StreamingResponseBody` para prevenir consumo desmedido de memoria RAM en el middleware.
- **Convenciones del proyecto:** Mantener la convención de paquetes existente (`Infraestructure/In`, `Infraestructure/Config`, `DTOs/Request`, `DTOs/Response`), inyección por constructor y uso de Records para DTOs.
- **Contratos de interfaces:** Si alguna interfaz o caso de uso requiere ajustes en su firma para recibir un stream o ID necesario, documentar el cambio previamente respetando los principios de diseño de la solución.
