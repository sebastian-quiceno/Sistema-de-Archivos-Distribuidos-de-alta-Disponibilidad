package com.sdan.middleware.Infraestructure.In;
import com.sdan.middleware.Domain.ValueObjects.FileName;
import com.sdan.middleware.Infraestructure.In.DTOs.Response.PresignedUrlResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final S3Service s3Service;

    public FileController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload-url")
    public PresignedUrlResponse getUploadUrl(@RequestParam String crudFilename) {
        FileName filename = new FileName(crudFilename);

        return s3Service.generateUploadUrl(filename);
    }

    @GetMapping("/download-url")
    public PresignedUrlResponse getDownloadUrl(@RequestParam String key) {

        if (key.isBlank()) {
            throw new IllegalArgumentException("La clave del archivo es obligatoria");
        }

        return s3Service.generateDownloadUrl(key);
    }
}