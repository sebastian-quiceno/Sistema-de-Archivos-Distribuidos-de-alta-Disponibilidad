package com.sdan.middleware.Infraestructure.In;

import com.sdan.middleware.Application.Folder.CreateFolderUseCase;
import com.sdan.middleware.Application.Folder.DeleteFolderUseCase;
import com.sdan.middleware.Application.Folder.GetFoldersByFatherFolderUseCase;
import com.sdan.middleware.Application.Folder.MoveFolderUserCase;
import com.sdan.middleware.Application.Inputs.Commands.CreateFolderCommand;
import com.sdan.middleware.Application.Inputs.Commands.MoveFolderCommand;
import com.sdan.middleware.Application.Outputs.FolderOutput;
import com.sdan.middleware.Infraestructure.In.DTOs.Request.CreateFolderRequest;
import com.sdan.middleware.Infraestructure.In.DTOs.Request.MoveFolderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final CreateFolderUseCase createFolderUseCase;
    private final GetFoldersByFatherFolderUseCase getFoldersByFatherFolderUseCase;
    private final MoveFolderUserCase moveFolderUserCase;
    private final DeleteFolderUseCase deleteFolderUseCase;

    public FolderController(
            CreateFolderUseCase createFolderUseCase,
            GetFoldersByFatherFolderUseCase getFoldersByFatherFolderUseCase,
            MoveFolderUserCase moveFolderUserCase,
            DeleteFolderUseCase deleteFolderUseCase) {
        this.createFolderUseCase = createFolderUseCase;
        this.getFoldersByFatherFolderUseCase = getFoldersByFatherFolderUseCase;
        this.moveFolderUserCase = moveFolderUserCase;
        this.deleteFolderUseCase = deleteFolderUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('folders.create')")
    public ResponseEntity<Void> createFolder(@RequestBody CreateFolderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud no puede ser nulo");
        }
        if (request.folderName() == null || request.folderName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la carpeta no puede estar vacío");
        }

        CreateFolderCommand command = new CreateFolderCommand(
                request.fatherId(),
                request.folderName().strip()
        );
        createFolderUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/subfolders")
    @PreAuthorize("hasAuthority('folders.read')")
    public ResponseEntity<List<FolderOutput>> getSubfolders(@PathVariable("id") UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador de la carpeta no puede ser nulo");
        }
        List<FolderOutput> outputs = getFoldersByFatherFolderUseCase.execute(id);
        return ResponseEntity.ok(outputs);
    }

    @PatchMapping("/{id}/move")
    @PreAuthorize("hasAuthority('folders.update')")
    public ResponseEntity<Void> moveFolder(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) MoveFolderRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador de la carpeta no puede ser nulo");
        }

        UUID newFatherFolderId = request != null ? request.newFatherFolderId() : null;
        MoveFolderCommand command = new MoveFolderCommand(id, newFatherFolderId);
        moveFolderUserCase.execute(command);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('folders.delete')")
    public ResponseEntity<Void> deleteFolder(@PathVariable("id") UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador de la carpeta no puede ser nulo");
        }
        deleteFolderUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}

