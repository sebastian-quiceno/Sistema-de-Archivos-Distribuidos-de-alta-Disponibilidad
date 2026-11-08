# Tarea BE-01: Inclusión del nombre original y extensión en Content-Disposition en descarga de archivos

## 1. Identificación
- **ID:** BE-01
- **Título:** Envío del nombre original de archivo y su extensión en la cabecera Content-Disposition del endpoint de descarga
- **Dominio:** Backend
- **Tipo:** Integración / Backend
- **Prioridad:** Media

---

## 2. Problema
### Estado actual
En `FileController.java:117`, el endpoint `GET /api/files/{id}/download` configura la cabecera HTTP `Content-Disposition` asignando únicamente el UUID del archivo como nombre de descarga:
```java
return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + "\"")
        .body(responseBody);
```
Adicionalmente, el caso de uso `GetFileUseCase.java` únicamente retorna un `InputStream`:
```java
public InputStream execute(UUID fileId) { ... return fileReconstructionService.mergeFile(file); }
```
Por lo tanto, la capa de infraestructura del controlador no recibe la metadata del archivo (`file.getName()`, tipo o extensión) y no puede formar un nombre de archivo legible.

### Evidencia
- Al consultar `/api/files/{id}/download` con herramientas como Postman, `curl` o directamente a través de una descarga directa del navegador (sin intermediación de un script que fuerce el nombre en un elemento `<a download="...">`), el archivo resultante se guarda como `e1000000-0000-0000-0000-000000000001` sin extensión.
- El usuario final debe renombrar manualmente el archivo en su sistema operativo agregándole la extensión correspondiente (`.pdf`, `.png`, etc.) para poder abrirlo con su software predeterminado.

### Justificación técnica del cambio en Backend
Aunque desde el Frontend es posible mitigar parcialmente este defecto inyectando el atributo `download` en un tag `<a>` temporal (aprovechando que el componente de la interfaz conoce el nombre gracias al listado previo de la carpeta), esta alternativa es frágil y no cumple con el estándar de interoperabilidad HTTP REST:
1. Si el usuario abre el enlace de descarga en una nueva pestaña o accede a través de clientes HTTP de terceros, CLI o aplicaciones móviles futuras, la descarga quedará privada de su nombre y extensión.
2. La cabecera `Content-Disposition` es el mecanismo estándar de la especificación RFC 6266 para designar el nombre del archivo descargado. Corregir esto en el backend asegura que cualquier consumidor de la API obtenga archivos directamente utilizables.

### Impacto
Descargas directas degradadas y dependencia exclusiva de un mecanismo en Frontend para que el archivo descargado mantenga un nombre y extensión comprensibles por el sistema operativo.

---

## 3. Ubicación
- `middleware/src/main/java/com/sdan/middleware/Infraestructure/In/FileController.java`
- `middleware/src/main/java/com/sdan/middleware/Application/File/GetFileUseCase.java`
- `middleware/src/main/java/com/sdan/middleware/Application/Outputs/` (o nuevo DTO/Record de salida para descarga)

---

## 4. Objetivo
Permitir que el endpoint `GET /api/files/{id}/download` entregue la cabecera `Content-Disposition` con el nombre original del archivo y su extensión correspondiente (ej: `attachment; filename="documento.pdf"`), preservando la integridad del streaming binario.

---

## 5. Requisitos
- Modificar la salida de `GetFileUseCase.execute(UUID fileId)` para devolver un objeto contenedor (por ejemplo, `FileDownloadOutput(InputStream stream, String fileName)`) o proporcionar acceso a la entidad `File` procesada.
- En `FileController.java`, resolver el nombre original del archivo y formatear la cabecera:
  - Sanitizar caracteres especiales para compatibilidad con RFC 6266 (soportar `filename="..."` y opcionalmente `filename*=UTF-8''...`).
- Mantener la respuesta como flujo binario `StreamingResponseBody` para evitar consumo excesivo de memoria en transferencias de archivos grandes.
- Proteger el endpoint manteniendo `@PreAuthorize("hasAuthority('files.read')")`.

---

## 6. Alcance
- **Cambios necesarios:**
  - Creación de un record de salida en `com.sdan.middleware.Application.Outputs` (ej. `FileDownloadOutput`).
  - Actualización de la firma y retorno en `GetFileUseCase.java`.
  - Actualización de `FileController.java` para utilizar el nombre del archivo en la cabecera `Content-Disposition`.
  - Actualización de las pruebas unitarias existentes de `FileController` y `GetFileUseCase`.
- **Restricciones:**
  - No modificar la lógica de partición, reconstrucción de chunks ni almacenamiento distribuido. El cambio debe limitarse estrictamente al paso de metadatos del nombre hacia la cabecera HTTP.

---

## 7. Criterios de aceptación
- Una petición `GET /api/files/{id}/download` con credenciales válidas retorna status 200 OK y la cabecera `Content-Disposition` contiene el nombre original del archivo con su extensión correspondiente.
- El archivo descargado mediante navegador o `curl -OJ` se guarda automáticamente con el nombre correcto y se abre de inmediato en el sistema operativo.
- El stream binario no se ve alterado y conserva su checksum e integridad original.
- Las pruebas unitarias de `FileController` y `GetFileUseCase` compilan y pasan exitosamente.

---

## 8. Pruebas
1. **Prueba con cliente HTTP (curl):**
   Ejecutar `curl -OJ -H "Authorization: Bearer <TOKEN>" http://localhost:8080/api/files/{id}/download` y comprobar que el archivo se guarde con su nombre y extensión originales (ej. `Prueba DOC.doc`).
2. **Prueba de regresión en pruebas unitarias:**
   Ejecutar la suite de pruebas de `FileControllerTest` y `GetFileUseCaseTest` asegurando que todos los escenarios de autorización, archivo inexistente y streaming continúen pasando.

---

## 9. Dependencias
- **Tareas previas:** Ninguna.
- **Tareas relacionadas:** FE-04 (operaciones de archivo en frontend).

