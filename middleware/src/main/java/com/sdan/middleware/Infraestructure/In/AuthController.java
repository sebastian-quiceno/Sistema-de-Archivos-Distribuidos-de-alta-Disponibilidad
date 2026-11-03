package com.sdan.middleware.Infraestructure.In;

import com.sdan.middleware.Application.Inputs.Commands.SignUpUserCommand;
import com.sdan.middleware.Application.Outputs.UserCredentialsOutput;
import com.sdan.middleware.Application.Users.CreateNewUserUseCase;
import com.sdan.middleware.Application.Users.UserLoginUseCase;
import com.sdan.middleware.Domain.ValueObjects.UserName;
import com.sdan.middleware.Infraestructure.In.DTOs.Request.SignInRequest;
import com.sdan.middleware.Infraestructure.In.DTOs.Request.SignUpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CreateNewUserUseCase createNewUserUseCase;
    private final UserLoginUseCase userLoginUseCase;

    public AuthController(CreateNewUserUseCase createNewUserUseCase, UserLoginUseCase userLoginUseCase) {
        this.createNewUserUseCase = createNewUserUseCase;
        this.userLoginUseCase = userLoginUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<UserCredentialsOutput> register(@RequestBody SignUpRequest request) {
        if (request == null || request.username() == null || request.password() == null) {
            throw new IllegalArgumentException("El nombre de usuario y la contraseña son obligatorios");
        }
        SignUpUserCommand command = new SignUpUserCommand(new UserName(request.username()), request.password());
        UserCredentialsOutput output = createNewUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PostMapping("/login")
    public ResponseEntity<UserCredentialsOutput> login(@RequestBody SignInRequest request) {
        if (request == null || request.username() == null || request.password() == null) {
            throw new IllegalArgumentException("El nombre de usuario y la contraseña son obligatorios");
        }
        UserCredentialsOutput output = userLoginUseCase.execute(new UserName(request.username()), request.password());
        return ResponseEntity.ok(output);
    }
}

