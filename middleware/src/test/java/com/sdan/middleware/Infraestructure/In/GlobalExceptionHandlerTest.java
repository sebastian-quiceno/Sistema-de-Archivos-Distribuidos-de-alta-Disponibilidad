package com.sdan.middleware.Infraestructure.In;

import com.sdan.middleware.BuildingBlocks.Domain.DomainException;
import com.sdan.middleware.Domain.Exceptions.File.FileDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.File.FileNotFoundException;
import com.sdan.middleware.Domain.Exceptions.FileType.FileTypeNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderDoesNotBelongToUserException;
import com.sdan.middleware.Domain.Exceptions.Folder.FolderNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Node.NodeNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Users.PasswordNotMatchException;
import com.sdan.middleware.Domain.Exceptions.Users.UnauthorizedUserException;
import com.sdan.middleware.Domain.Exceptions.Users.UserNotFoundException;
import com.sdan.middleware.Domain.Exceptions.Users.UsernameAlreadyExist;
import com.sdan.middleware.Domain.Exceptions.Users.UsernameNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/unauthorized")
        public void throwUnauthorized() {
            throw new UnauthorizedUserException("Usuario no autenticado");
        }

        @GetMapping("/test/password-not-match")
        public void throwPasswordNotMatch() {
            throw new PasswordNotMatchException("Contraseña incorrecta");
        }

        @GetMapping("/test/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException("Acceso denegado");
        }

        @GetMapping("/test/file-not-belong")
        public void throwFileNotBelong() {
            throw new FileDoesNotBelongToUserException("El archivo no pertenece al usuario");
        }

        @GetMapping("/test/folder-not-belong")
        public void throwFolderNotBelong() {
            throw new FolderDoesNotBelongToUserException("La carpeta no pertenece al usuario");
        }

        @GetMapping("/test/username-conflict")
        public void throwUsernameConflict() {
            throw new UsernameAlreadyExist("El usuario ya existe");
        }

        @GetMapping("/test/file-not-found")
        public void throwFileNotFound() {
            throw new FileNotFoundException("Archivo no encontrado");
        }

        @GetMapping("/test/folder-not-found")
        public void throwFolderNotFound() {
            throw new FolderNotFoundException("Carpeta no encontrada");
        }

        @GetMapping("/test/user-not-found")
        public void throwUserNotFound() {
            throw new UserNotFoundException("Usuario no encontrado");
        }

        @GetMapping("/test/username-not-found")
        public void throwUsernameNotFound() {
            throw new UsernameNotFoundException("Nombre de usuario no encontrado");
        }

        @GetMapping("/test/filetype-not-found")
        public void throwFileTypeNotFound() {
            throw new FileTypeNotFoundException("Tipo de archivo no encontrado");
        }

        @GetMapping("/test/node-not-found")
        public void throwNodeNotFound() {
            throw new NodeNotFoundException("Nodo no encontrado");
        }

        @GetMapping("/test/illegal-argument")
        public void throwIllegalArgument() {
            throw new IllegalArgumentException("Parámetro inválido");
        }

        @GetMapping("/test/domain-exception")
        public void throwDomainException() {
            throw new DomainException("Error de regla de dominio");
        }

        @GetMapping("/test/max-upload-size")
        public void throwMaxUploadSize() {
            throw new MaxUploadSizeExceededException(10485760);
        }

        @GetMapping("/test/generic")
        public void throwGeneric() {
            throw new RuntimeException("Error inesperado del servidor");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("UnauthorizedUserException retorna 401 Unauthorized")
    void handleUnauthorized_returns401() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Usuario no autenticado"))
                .andExpect(jsonPath("$.path").value("/test/unauthorized"));
    }

    @Test
    @DisplayName("PasswordNotMatchException retorna 401 Unauthorized")
    void handlePasswordNotMatch_returns401() throws Exception {
        mockMvc.perform(get("/test/password-not-match"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Contraseña incorrecta"));
    }

    @Test
    @DisplayName("AccessDeniedException retorna 403 Forbidden")
    void handleAccessDenied_returns403() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Acceso denegado"));
    }

    @Test
    @DisplayName("FileDoesNotBelongToUserException retorna 403 Forbidden")
    void handleFileNotBelong_returns403() throws Exception {
        mockMvc.perform(get("/test/file-not-belong"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("FolderDoesNotBelongToUserException retorna 403 Forbidden")
    void handleFolderNotBelong_returns403() throws Exception {
        mockMvc.perform(get("/test/folder-not-belong"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("UsernameAlreadyExist retorna 409 Conflict")
    void handleUsernameConflict_returns409() throws Exception {
        mockMvc.perform(get("/test/username-conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("El usuario ya existe"));
    }

    @Test
    @DisplayName("FileNotFoundException retorna 404 Not Found")
    void handleFileNotFound_returns404() throws Exception {
        mockMvc.perform(get("/test/file-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("FolderNotFoundException retorna 404 Not Found")
    void handleFolderNotFound_returns404() throws Exception {
        mockMvc.perform(get("/test/folder-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("NodeNotFoundException retorna 404 Not Found")
    void handleNodeNotFound_returns404() throws Exception {
        mockMvc.perform(get("/test/node-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("IllegalArgumentException retorna 400 Bad Request")
    void handleIllegalArgument_returns400() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Parámetro inválido"));
    }

    @Test
    @DisplayName("DomainException retorna 400 Bad Request")
    void handleDomainException_returns400() throws Exception {
        mockMvc.perform(get("/test/domain-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("MaxUploadSizeExceededException retorna 413 Payload Too Large")
    void handleMaxUploadSizeExceeded_returns413() throws Exception {
        mockMvc.perform(get("/test/max-upload-size"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.error").value("Payload Too Large"))
                .andExpect(jsonPath("$.message").value("El archivo supera el tamaño máximo de carga permitido"));
    }

    @Test
    @DisplayName("Exception genérica retorna 500 Internal Server Error")
    void handleGenericException_returns500() throws Exception {
        mockMvc.perform(get("/test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Error inesperado del servidor"));
    }
}

