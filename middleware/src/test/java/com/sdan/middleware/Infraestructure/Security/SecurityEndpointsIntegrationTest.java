package com.sdan.middleware.Infraestructure.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdan.middleware.Application.Ports.Repositories.IUserRepository;
import com.sdan.middleware.Application.Ports.Security.ITokenGenerator;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.ValueObjects.PasswordHash;
import com.sdan.middleware.Domain.ValueObjects.UserName;
import com.sdan.middleware.Infraestructure.In.DTOs.Request.SignInRequest;
import com.sdan.middleware.Infraestructure.In.DTOs.Request.SignUpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("dev")
class SecurityEndpointsIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ITokenGenerator tokenGenerator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testRegister_isPublic_returns201Created() throws Exception {
        String uniqueUsername = "user_" + UUID.randomUUID().toString().substring(0, 8);
        SignUpRequest request = new SignUpRequest(uniqueUsername, "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.userName.value").value(uniqueUsername))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void testLogin_isPublic_returns200Ok() throws Exception {
        String uniqueUsername = "login_" + UUID.randomUUID().toString().substring(0, 8);
        String rawPassword = "password123";
        User user = User.reCreate(
                UUID.randomUUID(),
                new UserName(uniqueUsername),
                new PasswordHash(passwordEncoder.encode(rawPassword)),
                new Date()
        );
        userRepository.save(user);

        SignInRequest request = new SignInRequest(uniqueUsername, rawPassword);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId().toString()))
                .andExpect(jsonPath("$.userName.value").value(uniqueUsername))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void testProtectedEndpoint_withoutToken_returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/files/download-url")
                        .param("key", "test-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void testProtectedEndpoint_withInvalidToken_returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/files/download-url")
                        .param("key", "test-key")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void testProtectedEndpoint_withValidToken_passesSecurityAuthentication() throws Exception {
        String uniqueUsername = "auth_" + UUID.randomUUID().toString().substring(0, 8);
        User user = User.reCreate(
                UUID.randomUUID(),
                new UserName(uniqueUsername),
                new PasswordHash(passwordEncoder.encode("pass123")),
                new Date()
        );
        userRepository.save(user);
        String validToken = tokenGenerator.Generate(user);

        // With valid token, request reaches FileController (it won't be 401 Unauthorized!)
        mockMvc.perform(get("/api/files/download-url")
                        .param("key", "test-key")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401) {
                        throw new AssertionError("Request was rejected by security with 401!");
                    }
                });
    }
}