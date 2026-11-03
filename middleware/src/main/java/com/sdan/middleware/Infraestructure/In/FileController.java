package com.sdan.middleware.Infraestructure.In;

import com.sdan.middleware.Application.File.CheckFilesUseCase;
import com.sdan.middleware.Application.File.DeleteFileUseCase;
import com.sdan.middleware.Application.File.GetFileUseCase;
import com.sdan.middleware.Application.File.UpdateFileUseCase;
import com.sdan.middleware.Application.File.UploadFileUseCase;
import com.sdan.middleware.Application.Inputs.Commands.CreateFileCommand;
import com.sdan.middleware.Application.Inputs.Commands.UpdateFileCommand;
import com.sdan.middleware.Application.Outputs.IntegrityCheckReport;
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

    private final UploadFileUseCase uploadFileUseCase;
    private final GetFileUseCase getFileUseCase;
    private final UpdateFileUseCase updateFileUseCase;
    private final DeleteFileUseCase deleteFileUseCase;
    private final CheckFilesUseCase checkFilesUseCase;

    public FileController(
            UploadFileUseCase uploadFileUseCase,
            GetFileUseCase getFileUseCase,
            UpdateFileUseCase updateFileUseCase,
            DeleteFileUseCase deleteFileUseCase,
            CheckFilesUseCase checkFilesUseCase) {
        this.uploadFileUseCase = uploadFileUseCase;
        this.getFileUseCase = getFileUseCase;
        this.updateFileUseCase = updateFileUseCase;
        this.deleteFileUseCase = deleteFileUseCase;
        this.checkFilesUseCase = checkFilesUseCase;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('files.create')")
    public ResponseEntity<Void> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folderId") UUID folderId,
            @RequestParam("fileTypeId") UUID fileTypeId,
            @RequestParam(value = "name", required = false) String name) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo es obligatorio y no puede estar vacío");
        }
        if (folderId == null) {
            throw new IllegalArgumentException("El identificador de la carpeta es obligatorio");
        }
        if (fileTypeId == null) {
            throw new IllegalArgumentException("El tipo de archivo es obligatorio");
        }

        String resolvedName = (name != null && !name.isBlank()) ? name.strip() : file.getOriginalFilename();
        if (resolvedName == null || resolvedName.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo es obligatorio");
        }

        try (InputStream is = file.getInputStream()) {
            CreateFileCommand command = new CreateFileCommand(
                    folderId,
                    fileTypeId,
                    resolvedName,
                    is,
                    file.getSize()
            );
            uploadFileUseCase.execute(command);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('files.read')")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable("id") UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador del archivo no puede ser nulo");
        }

        InputStream inputStream = getFileUseCase.execute(id);

        StreamingResponseBody responseBody = outputStream -> {
            try (inputStream) {
                inputStream.transferTo(outputStream);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + "\"")
                .body(responseBody);
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

