package com.sdan.middleware.Infraestructure.In;

import com.sdan.middleware.Application.File.CheckFilesUseCase;
import com.sdan.middleware.Application.File.DeleteFileUseCase;
import com.sdan.middleware.Application.File.GetFileUseCase;
import com.sdan.middleware.Application.File.UpdateFileUseCase;
import com.sdan.middleware.Application.File.UploadFileUseCase;
import com.sdan.middleware.Application.Inputs.Commands.CreateFileCommand;
import com.sdan.middleware.Application.Inputs.Commands.UpdateFileCommand;
import com.sdan.middleware.Application.Outputs.IntegrityCheckReport;
import com.sdan.middleware.Domain.Exceptions.File.FileDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.File.FileNotFoundException;
import com.sdan.middleware.Domain.Exceptions.FileType.FileTypeNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UploadFileUseCase uploadFileUseCase;

    @Mock
    private GetFileUseCase getFileUseCase;

    @Mock
    private UpdateFileUseCase updateFileUseCase;

    @Mock
    private DeleteFileUseCase deleteFileUseCase;

    @Mock
    private CheckFilesUseCase checkFilesUseCase;

    @InjectMocks
    private FileController fileController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(fileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/files/upload exitoso retorna 201 Created")
    void uploadFile_withValidData_returns201Created() throws Exception {
        UUID folderId = UUID.randomUUID();
        UUID fileTypeId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Contenido binario de prueba".getBytes(StandardCharsets.UTF_8)
        );

        doNothing().when(uploadFileUseCase).execute(any(CreateFileCommand.class));

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("folderId", folderId.toString())
                        .param("fileTypeId", fileTypeId.toString())
                        .param("name", "custom_name.pdf"))
                .andExpect(status().isCreated());

        verify(uploadFileUseCase).execute(any(CreateFileCommand.class));
    }

    @Test
    @DisplayName("POST /api/files/upload con archivo vacío retorna 400 Bad Request")
    void uploadFile_withEmptyFile_returns400BadRequest() throws Exception {
        UUID folderId = UUID.randomUUID();
        UUID fileTypeId = UUID.randomUUID();
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(emptyFile)
                        .param("folderId", folderId.toString())
                        .param("fileTypeId", fileTypeId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/files/upload en carpeta inexistente retorna 404 Not Found")
    void uploadFile_whenFolderNotFound_returns404NotFound() throws Exception {
        UUID folderId = UUID.randomUUID();
        UUID fileTypeId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", MediaType.APPLICATION_PDF_VALUE, "data".getBytes()
        );

        doThrow(new FolderNotFoundException("La carpeta no existe"))
                .when(uploadFileUseCase).execute(any(CreateFileCommand.class));

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("folderId", folderId.toString())
                        .param("fileTypeId", fileTypeId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("POST /api/files/upload en carpeta ajena retorna 403 Forbidden")
    void uploadFile_whenFolderNotOwned_returns403Forbidden() throws Exception {
        UUID folderId = UUID.randomUUID();
        UUID fileTypeId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", MediaType.APPLICATION_PDF_VALUE, "data".getBytes()
        );

        doThrow(new FolderDoesNotBelongToUserException("La carpeta no pertenece al usuario"))
                .when(uploadFileUseCase).execute(any(CreateFileCommand.class));

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("folderId", folderId.toString())
                        .param("fileTypeId", fileTypeId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("POST /api/files/upload con tipo inexistente retorna 404 Not Found")
    void uploadFile_whenFileTypeNotFound_returns404NotFound() throws Exception {
        UUID folderId = UUID.randomUUID();
        UUID fileTypeId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", MediaType.APPLICATION_PDF_VALUE, "data".getBytes()
        );

        doThrow(new FileTypeNotFoundException("No se encontro el FileType"))
                .when(uploadFileUseCase).execute(any(CreateFileCommand.class));

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("folderId", folderId.toString())
                        .param("fileTypeId", fileTypeId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/files/{id}/download exitoso retorna 200 OK con StreamingResponseBody")
    void downloadFile_withValidId_returns200OkWithStream() throws Exception {
        UUID fileId = UUID.randomUUID();
        byte[] expectedBytes = "Contenido de archivo descargado".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(expectedBytes);

        when(getFileUseCase.execute(fileId)).thenReturn(inputStream);

        MvcResult result = mockMvc.perform(get("/api/files/{id}/download", fileId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileId + "\""))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch(result))
                .andExpect(status().isOk());

        assertArrayEquals(expectedBytes, result.getResponse().getContentAsByteArray());
    }

    @Test
    @DisplayName("GET /api/files/{id}/download cuando archivo no existe retorna 404 Not Found")
    void downloadFile_whenNotFound_returns404NotFound() throws Exception {
        UUID fileId = UUID.randomUUID();
        when(getFileUseCase.execute(fileId))
                .thenThrow(new FileNotFoundException("No se encontro el archivo con el id: " + fileId));

        mockMvc.perform(get("/api/files/{id}/download", fileId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /api/files/{id}/download cuando archivo es ajeno retorna 403 Forbidden")
    void downloadFile_whenNotOwned_returns403Forbidden() throws Exception {
        UUID fileId = UUID.randomUUID();
        when(getFileUseCase.execute(fileId))
                .thenThrow(new FileDoesNotBelongToUserException("El archivo no pertenece al usuario"));

        mockMvc.perform(get("/api/files/{id}/download", fileId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("PUT /api/files/{id} exitoso retorna 204 No Content")
    void updateFile_withValidData_returns204NoContent() throws Exception {
        UUID fileId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "nuevo_contenido.txt", MediaType.TEXT_PLAIN_VALUE, "Nuevo contenido".getBytes()
        );

        doNothing().when(updateFileUseCase).execute(any(UpdateFileCommand.class));

        mockMvc.perform(multipart("/api/files/{id}", fileId)
                        .file(file)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andExpect(status().isNoContent());

        verify(updateFileUseCase).execute(any(UpdateFileCommand.class));
    }

    @Test
    @DisplayName("PUT /api/files/{id} con archivo inexistente retorna 404 Not Found")
    void updateFile_whenNotFound_returns404NotFound() throws Exception {
        UUID fileId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "nuevo.txt", MediaType.TEXT_PLAIN_VALUE, "Contenido".getBytes()
        );

        doThrow(new FileNotFoundException("No se encontro el archivo"))
                .when(updateFileUseCase).execute(any(UpdateFileCommand.class));

        mockMvc.perform(multipart("/api/files/{id}", fileId)
                        .file(file)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/files/{id} exitoso retorna 204 No Content")
    void deleteFile_withValidId_returns204NoContent() throws Exception {
        UUID fileId = UUID.randomUUID();
        doNothing().when(deleteFileUseCase).execute(fileId);

        mockMvc.perform(delete("/api/files/{id}", fileId))
                .andExpect(status().isNoContent());

        verify(deleteFileUseCase).execute(fileId);
    }

    @Test
    @DisplayName("DELETE /api/files/{id} cuando archivo no existe retorna 404 Not Found")
    void deleteFile_whenNotFound_returns404NotFound() throws Exception {
        UUID fileId = UUID.randomUUID();
        doThrow(new FileNotFoundException("No se encontro el archivo con el id: " + fileId))
                .when(deleteFileUseCase).execute(fileId);

        mockMvc.perform(delete("/api/files/{id}", fileId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/files/check-integrity exitoso retorna 200 OK con reporte de auditoría")
    void checkIntegrity_returns200OkWithReport() throws Exception {
        IntegrityCheckReport report = new IntegrityCheckReport(
                10,
                8,
                2,
                0,
                Collections.emptyList()
        );

        when(checkFilesUseCase.execute(50)).thenReturn(report);

        mockMvc.perform(post("/api/files/check-integrity")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFilesChecked").value(10))
                .andExpect(jsonPath("$.healthyFilesCount").value(8))
                .andExpect(jsonPath("$.healedFilesCount").value(2))
                .andExpect(jsonPath("$.corruptedFilesCount").value(0))
                .andExpect(jsonPath("$.issues").isArray());

        verify(checkFilesUseCase).execute(eq(50));
    }
}

