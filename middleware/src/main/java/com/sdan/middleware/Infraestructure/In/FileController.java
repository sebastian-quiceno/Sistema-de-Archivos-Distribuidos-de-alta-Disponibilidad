package com.sdan.middleware.Infraestructure.In;

import com.sdan.middleware.Application.File.*;
import com.sdan.middleware.Application.Inputs.Commands.CreateFileCommand;
import com.sdan.middleware.Application.Inputs.Commands.UpdateFileCommand;
import com.sdan.middleware.Application.Outputs.FileDownloadOutput;
import com.sdan.middleware.Application.Outputs.IntegrityCheckReport;
import com.sdan.middleware.Application.Outputs.MetadataFileOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Controlador REST encargado de la transferencia, persistencia distribuida por chunks,
 * reconstrucción, actualización, borrado y auditoría de integridad de archivos en el clúster DFSha.
 *
 * Clarificación técnica sobre CreateFileUseCase vs UploadFileUseCase:
 * CreateFileUseCase corresponde a un esbozo anterior basado en almacenamiento simple de URLs prefirmadas,
 * mientras que UploadFileUseCase es la implementación definitiva de DFSha que orquesta la partición en chunks,
 * replicación (R=3) y almacenamiento multi-nodo mediante FileService.
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final UploadFileUseCase uploadFileUseCase;
    private final GetFileUseCase getFileUseCase;
    private final UpdateFileUseCase updateFileUseCase;
    private final DeleteFileUseCase deleteFileUseCase;
    private final CheckFilesUseCase checkFilesUseCase;
    private final GetMetadataFileUseCase getMetadataFileUseCase;

    public FileController(UploadFileUseCase uploadFileUseCase, GetFileUseCase getFileUseCase, UpdateFileUseCase updateFileUseCase, DeleteFileUseCase deleteFileUseCase, CheckFilesUseCase checkFilesUseCase, GetMetadataFileUseCase getMetadataFileUseCase) {
        this.uploadFileUseCase = uploadFileUseCase;
        this.getFileUseCase = getFileUseCase;
        this.updateFileUseCase = updateFileUseCase;
        this.deleteFileUseCase = deleteFileUseCase;
        this.checkFilesUseCase = checkFilesUseCase;
        this.getMetadataFileUseCase = getMetadataFileUseCase;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('files.create')")
    public ResponseEntity<Void> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folderId") UUID folderId,
            @RequestParam("fileTypeName") String fileTypeName,
            @RequestParam(value = "name", required = false) String name) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo es obligatorio y no puede estar vacío");
        }
        if (folderId == null) {
            throw new IllegalArgumentException("El identificador de la carpeta es obligatorio");
        }
        if (fileTypeName == null) {
            throw new IllegalArgumentException("El tipo de archivo es obligatorio");
        }

        String resolvedName = (name != null && !name.isBlank()) ? name.strip() : file.getOriginalFilename();
        if (resolvedName == null || resolvedName.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo es obligatorio");
        }

        try (InputStream is = file.getInputStream()) {
            CreateFileCommand command = new CreateFileCommand(
                    folderId,
                    fileTypeName,
                    resolvedName,
                    is,
                    file.getSize()
            );
            uploadFileUseCase.execute(command);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('files.read')")
    public ResponseEntity<MetadataFileOutput> getMetadataFile(@PathVariable("id") UUID id){
        if (id == null) {
            throw new IllegalArgumentException("El identificador del archivo no puede ser nulo");
        }

        MetadataFileOutput output = getMetadataFileUseCase.execute(id);

        return ResponseEntity.ok(output);

    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('files.read')")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable("id") UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del archivo no puede ser nulo");
        }

        FileDownloadOutput downloadOutput = getFileUseCase.execute(id);

        String rawFileName = downloadOutput.fileName();
        String safeFileName = (rawFileName != null && !rawFileName.isBlank())
                ? rawFileName.replace("\r", "").replace("\n", "").replace("\"", "")
                : id.toString();

        StreamingResponseBody responseBody = outputStream -> {
            log.info("Iniciando streaming de descarga para el archivo '{}' (ID: {})...", safeFileName, id);
            try (InputStream inputStream = downloadOutput.stream()) {
                byte[] buffer = new byte[65536]; // 64 KB buffer
                int bytesRead;
                long totalBytes = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                    totalBytes += bytesRead;
                }
                log.info("Streaming completado para '{}' (ID: {}): {} bytes transferidos con éxito.", safeFileName, id, totalBytes);
            } catch (IOException e) {
                if (e.getClass().getSimpleName().contains("ClientAbortException") ||
                    (e.getMessage() != null && e.getMessage().toLowerCase().contains("broken pipe"))) {
                    log.debug("Cliente cerró la conexión durante la descarga de '{}': {}", safeFileName, e.getMessage());
                } else {
                    log.error("Error de E/S durante la descarga de '{}': {}", safeFileName, e.getMessage(), e);
                    throw e;
                }
            } catch (Exception e) {
                log.error("Error inesperado durante la descarga de '{}': {}", safeFileName, e.getMessage(), e);
                throw e;
            }
        };

        ContentDisposition contentDisposition;
        if (StandardCharsets.US_ASCII.newEncoder().canEncode(safeFileName)) {
            contentDisposition = ContentDisposition.attachment()
                    .filename(safeFileName)
                    .build();
        } else {
            contentDisposition = ContentDisposition.attachment()
                    .filename(safeFileName, StandardCharsets.UTF_8)
                    .build();
        }

        var responseBuilder = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

        if (downloadOutput.fileSize() != null && downloadOutput.fileSize() > 0) {
            responseBuilder.contentLength(downloadOutput.fileSize());
        }

        return responseBuilder.body(responseBody);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('files.update')")
    public ResponseEntity<Void> updateFile(
            @PathVariable("id") UUID id,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (id == null) {
            throw new IllegalArgumentException("El identificador del archivo no puede ser nulo");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo es obligatorio y no puede estar vacío");
        }

        try (InputStream is = file.getInputStream()) {
            UpdateFileCommand command = new UpdateFileCommand(id, is, file.getSize());
            updateFileUseCase.execute(command);
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('files.delete')")
    public ResponseEntity<Void> deleteFile(@PathVariable("id") UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del archivo no puede ser nulo");
        }
        deleteFileUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-integrity")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('files.read')")
    public ResponseEntity<IntegrityCheckReport> checkIntegrity(
            @RequestParam(value = "pageSize", defaultValue = "50") int pageSize) {

        int effectivePageSize = pageSize > 0 ? pageSize : 50;
        IntegrityCheckReport report = checkFilesUseCase.execute(effectivePageSize);
        return ResponseEntity.ok(report);
    }
}

