package com.sdan.middleware.Infraestructure.In;

import com.sdan.middleware.Application.Folder.*;
import com.sdan.middleware.Application.Inputs.Commands.CreateFolderCommand;
import com.sdan.middleware.Application.Inputs.Commands.MoveFolderCommand;
import com.sdan.middleware.Application.Outputs.FolderOutput;
import com.sdan.middleware.Application.Outputs.MetadataFileOutput;
import com.sdan.middleware.Application.Outputs.RenameFolderCommand;
import com.sdan.middleware.Infraestructure.In.DTOs.Request.CreateFolderRequest;
import com.sdan.middleware.Infraestructure.In.DTOs.Request.MoveFolderRequest;
import com.sdan.middleware.Infraestructure.In.DTOs.Request.RenameFolderRequest;
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
    private final GetFilesByFolderIdUseCase getFilesByFolderIdUseCase;
    private final MoveFolderUserCase moveFolderUserCase;
    private final DeleteFolderUseCase deleteFolderUseCase;
    private final RenameFolderUseCase renameFolderUseCase;

    public FolderController(CreateFolderUseCase createFolderUseCase, GetFoldersByFatherFolderUseCase getFoldersByFatherFolderUseCase, GetFilesByFolderIdUseCase getFilesByFolderIdUseCase, MoveFolderUserCase moveFolderUserCase, DeleteFolderUseCase deleteFolderUseCase, RenameFolderUseCase renameFolderUseCase) {
        this.createFolderUseCase = createFolderUseCase;
        this.getFoldersByFatherFolderUseCase = getFoldersByFatherFolderUseCase;
        this.getFilesByFolderIdUseCase = getFilesByFolderIdUseCase;
        this.moveFolderUserCase = moveFolderUserCase;
        this.deleteFolderUseCase = deleteFolderUseCase;
        this.renameFolderUseCase = renameFolderUseCase;
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

    @GetMapping("/{id}/files")
    @PreAuthorize("hasAuthority('folders.read')")
    public ResponseEntity<List<MetadataFileOutput>> getFiles(@PathVariable("id") UUID id) {
        List<MetadataFileOutput> outputs = getFilesByFolderIdUseCase.execute(id);
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

    @PatchMapping("/{id}/rename")
    @PreAuthorize("hasAuthority('folders.update')")
    public ResponseEntity<Void> renameFolder(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) RenameFolderRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador de la carpeta no puede ser nulo");
        }

        RenameFolderCommand command = new RenameFolderCommand(id, request.newFolderName());

        renameFolderUseCase.execute(command);

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

