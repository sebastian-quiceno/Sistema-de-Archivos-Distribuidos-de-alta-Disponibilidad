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
        if (request == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud no puede ser nulo");
        }
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        System.out.println("credenciales: \n"+ "Contraseña: "+request.password() +"\n Usuario:" +request.username());

        SignUpUserCommand command = new SignUpUserCommand(
                new UserName(request.username().strip()),
                request.password()
        );
        UserCredentialsOutput output = createNewUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PostMapping("/login")
    public ResponseEntity<UserCredentialsOutput> login(@RequestBody SignInRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud no puede ser nulo");
        }
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        UserCredentialsOutput output = userLoginUseCase.execute(
                new UserName(request.username().strip()),
                request.password()
        );
        return ResponseEntity.ok(output);
    }
}
