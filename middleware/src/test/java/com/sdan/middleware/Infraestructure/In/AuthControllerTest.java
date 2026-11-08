package com.sdan.middleware.Infraestructure.In;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdan.middleware.Application.Inputs.Commands.SignUpUserCommand;
import com.sdan.middleware.Application.Outputs.UserCredentialsOutput;
import com.sdan.middleware.Application.Users.CreateNewUserUseCase;
import com.sdan.middleware.Application.Users.UserLoginUseCase;
import com.sdan.middleware.Domain.Exceptions.Users.PasswordNotMatchException;
import com.sdan.middleware.Domain.Exceptions.Users.UsernameAlreadyExist;
import com.sdan.middleware.Domain.Exceptions.Users.UsernameNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.UserName;
import com.sdan.middleware.Infraestructure.In.DTOs.Request.SignInRequest;
import com.sdan.middleware.Infraestructure.In.DTOs.Request.SignUpRequest;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CreateNewUserUseCase createNewUserUseCase;

    @Mock
    private UserLoginUseCase userLoginUseCase;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/register exitoso retorna 201 Created con credenciales y token")
    void register_withValidData_returns201Created() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID rootFolderId = UUID.randomUUID();
        String token = "jwt.token.here";
        UserCredentialsOutput output = new UserCredentialsOutput(userId, "testuser", token, rootFolderId);

        when(createNewUserUseCase.execute(any(SignUpUserCommand.class))).thenReturn(output);

        SignUpRequest request = new SignUpRequest("testuser", "Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.userName").value("testuser"))
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.rootFolderId").value(rootFolderId.toString()));
    }

    @Test
    @DisplayName("POST /api/auth/register con usuario duplicado retorna 409 Conflict")
    void register_withExistingUsername_returns409Conflict() throws Exception {
        when(createNewUserUseCase.execute(any(SignUpUserCommand.class)))
                .thenThrow(new UsernameAlreadyExist("El nombre de usuario 'existinguser' ya existe"));

        SignUpRequest request = new SignUpRequest("existinguser", "Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("El nombre de usuario 'existinguser' ya existe"));
    }

    @Test
    @DisplayName("POST /api/auth/register con username vacío retorna 400 Bad Request")
    void register_withBlankUsername_returns400BadRequest() throws Exception {
        SignUpRequest request = new SignUpRequest("   ", "Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/auth/register con password vacía retorna 400 Bad Request")
    void register_withBlankPassword_returns400BadRequest() throws Exception {
        SignUpRequest request = new SignUpRequest("validuser", "   ");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/auth/login exitoso retorna 200 OK con credenciales y token")
    void login_withValidCredentials_returns200Ok() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID rootFolderId = UUID.randomUUID();
        UserName username = new UserName("testuser");
        String token = "jwt.token.here";
        UserCredentialsOutput output = new UserCredentialsOutput(userId, "testuser", token, rootFolderId);

        when(userLoginUseCase.execute(eq(username), eq("Password123!"))).thenReturn(output);

        SignInRequest request = new SignInRequest("testuser", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.userName").value("testuser"))
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.rootFolderId").value(rootFolderId.toString()));
    }

    @Test
    @DisplayName("POST /api/auth/login con usuario inexistente retorna 404 Not Found")
    void login_withNonExistentUser_returns404NotFound() throws Exception {
        when(userLoginUseCase.execute(any(UserName.class), any()))
                .thenThrow(new UsernameNotFoundException("El nombre de usuario 'nonexistent' no existe"));

        SignInRequest request = new SignInRequest("nonexistent", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("El nombre de usuario 'nonexistent' no existe"));
    }

    @Test
    @DisplayName("POST /api/auth/login con contraseña incorrecta retorna 401 Unauthorized")
    void login_withIncorrectPassword_returns401Unauthorized() throws Exception {
        when(userLoginUseCase.execute(any(UserName.class), any()))
                .thenThrow(new PasswordNotMatchException("Contraseña incorrecta"));

        SignInRequest request = new SignInRequest("testuser", "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Contraseña incorrecta"));
    }

    @Test
    @DisplayName("POST /api/auth/login con username vacío retorna 400 Bad Request")
    void login_withBlankUsername_returns400BadRequest() throws Exception {
        SignInRequest request = new SignInRequest("", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/auth/login con password vacía retorna 400 Bad Request")
    void login_withBlankPassword_returns400BadRequest() throws Exception {
        SignInRequest request = new SignInRequest("validuser", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}

