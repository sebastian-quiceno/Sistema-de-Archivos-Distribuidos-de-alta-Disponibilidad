package com.sdan.middleware.Infraestructure.In;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdan.middleware.Application.Folder.CreateFolderUseCase;
import com.sdan.middleware.Application.Folder.DeleteFolderUseCase;
import com.sdan.middleware.Application.Folder.GetFoldersByFatherFolderUseCase;
import com.sdan.middleware.Application.Folder.MoveFolderUserCase;
import com.sdan.middleware.Application.Inputs.Commands.CreateFolderCommand;
import com.sdan.middleware.Application.Inputs.Commands.MoveFolderCommand;
import com.sdan.middleware.Application.Outputs.FolderOutput;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderNotFoundException;
import com.sdan.middleware.Infraestructure.In.DTOs.Request.CreateFolderRequest;
import com.sdan.middleware.Infraestructure.In.DTOs.Request.MoveFolderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FolderControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CreateFolderUseCase createFolderUseCase;

    @Mock
    private GetFoldersByFatherFolderUseCase getFoldersByFatherFolderUseCase;

    @Mock
    private MoveFolderUserCase moveFolderUserCase;

    @Mock
    private DeleteFolderUseCase deleteFolderUseCase;

    @InjectMocks
    private FolderController folderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(folderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/folders exitoso retorna 201 Created")
    void createFolder_withValidData_returns201Created() throws Exception {
        UUID fatherId = UUID.randomUUID();
        CreateFolderRequest request = new CreateFolderRequest(fatherId, "Documentos");
        doNothing().when(createFolderUseCase).execute(any(CreateFolderCommand.class));

        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(createFolderUseCase).execute(new CreateFolderCommand(fatherId, "Documentos"));
    }

    @Test
    @DisplayName("POST /api/folders con nombre vacío retorna 400 Bad Request")
    void createFolder_withBlankName_returns400BadRequest() throws Exception {
        CreateFolderRequest request = new CreateFolderRequest(null, "   ");

        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/folders en carpeta padre ajena retorna 403 Forbidden")
    void createFolder_whenFatherNotOwned_returns403Forbidden() throws Exception {
        UUID fatherId = UUID.randomUUID();
        CreateFolderRequest request = new CreateFolderRequest(fatherId, "Subcarpeta");
        doThrow(new FolderDoesNotBelongToUserException("La carpeta no pertenece al usuario"))
                .when(createFolderUseCase).execute(any(CreateFolderCommand.class));

        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("GET /api/folders/{id}/subfolders exitoso retorna 200 OK con lista de carpetas")
    void getSubfolders_withValidId_returns200Ok() throws Exception {
        UUID folderId = UUID.randomUUID();
        UUID subfolderId = UUID.randomUUID();
        List<FolderOutput> outputs = List.of(
                new FolderOutput(subfolderId, folderId, "Proyectos", new Date())
        );

        when(getFoldersByFatherFolderUseCase.execute(folderId)).thenReturn(outputs);

        mockMvc.perform(get("/api/folders/{id}/subfolders", folderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].folderId").value(subfolderId.toString()))
                .andExpect(jsonPath("$[0].name").value("Proyectos"));
    }

    @Test
    @DisplayName("GET /api/folders/{id}/subfolders cuando no existe retorna 404 Not Found")
    void getSubfolders_whenNotFound_returns404NotFound() throws Exception {
        UUID folderId = UUID.randomUUID();
        when(getFoldersByFatherFolderUseCase.execute(folderId))
                .thenThrow(new FolderNotFoundException("No se encontro la carpeta con el id: " + folderId));

        mockMvc.perform(get("/api/folders/{id}/subfolders", folderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /api/folders/{id}/subfolders sin permisos sobre carpeta retorna 403 Forbidden")
    void getSubfolders_whenNotOwned_returns403Forbidden() throws Exception {
        UUID folderId = UUID.randomUUID();
        when(getFoldersByFatherFolderUseCase.execute(folderId))
                .thenThrow(new FolderDoesNotBelongToUserException("El usuario no tiene acceso a la carpeta"));

        mockMvc.perform(get("/api/folders/{id}/subfolders", folderId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("PATCH /api/folders/{id}/move exitoso retorna 204 No Content")
    void moveFolder_withValidData_returns204NoContent() throws Exception {
        UUID folderId = UUID.randomUUID();
        UUID newFatherId = UUID.randomUUID();
        MoveFolderRequest request = new MoveFolderRequest(newFatherId);
        doNothing().when(moveFolderUserCase).execute(any(MoveFolderCommand.class));

        mockMvc.perform(patch("/api/folders/{id}/move", folderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(moveFolderUserCase).execute(new MoveFolderCommand(folderId, newFatherId));
    }

    @Test
    @DisplayName("PATCH /api/folders/{id}/move hacia raíz (null) retorna 204 No Content")
    void moveFolder_toRoot_returns204NoContent() throws Exception {
        UUID folderId = UUID.randomUUID();
        doNothing().when(moveFolderUserCase).execute(any(MoveFolderCommand.class));

        mockMvc.perform(patch("/api/folders/{id}/move", folderId))
                .andExpect(status().isNoContent());

        verify(moveFolderUserCase).execute(new MoveFolderCommand(folderId, null));
    }

    @Test
    @DisplayName("PATCH /api/folders/{id}/move cuando carpeta no existe retorna 404 Not Found")
    void moveFolder_whenNotFound_returns404NotFound() throws Exception {
        UUID folderId = UUID.randomUUID();
        doThrow(new FolderNotFoundException("No se encontró la carpeta"))
                .when(moveFolderUserCase).execute(any(MoveFolderCommand.class));

        mockMvc.perform(patch("/api/folders/{id}/move", folderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("DELETE /api/folders/{id} exitoso retorna 204 No Content")
    void deleteFolder_withValidId_returns204NoContent() throws Exception {
        UUID folderId = UUID.randomUUID();
        doNothing().when(deleteFolderUseCase).execute(folderId);

        mockMvc.perform(delete("/api/folders/{id}", folderId))
                .andExpect(status().isNoContent());

        verify(deleteFolderUseCase).execute(folderId);
    }

    @Test
    @DisplayName("DELETE /api/folders/{id} cuando carpeta no existe retorna 404 Not Found")
    void deleteFolder_whenNotFound_returns404NotFound() throws Exception {
        UUID folderId = UUID.randomUUID();
        doThrow(new FolderNotFoundException("No se encontro la carpeta con el id: " + folderId))
                .when(deleteFolderUseCase).execute(folderId);

        mockMvc.perform(delete("/api/folders/{id}", folderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("DELETE /api/folders/{id} cuando carpeta no pertenece al usuario retorna 403 Forbidden")
    void deleteFolder_whenNotOwned_returns403Forbidden() throws Exception {
        UUID folderId = UUID.randomUUID();
        doThrow(new FolderDoesNotBelongToUserException("La carpeta no pertenece al usuario"))
                .when(deleteFolderUseCase).execute(folderId);

        mockMvc.perform(delete("/api/folders/{id}", folderId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}

