package com.sdan.middleware.Infraestructure.Security;

import com.sdan.middleware.Application.Ports.Security.IPasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements IPasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public BcryptPasswordHasher() {
        this(new BCryptPasswordEncoder());
    }

    public BcryptPasswordHasher(PasswordEncoder passwordEncoder) {
        if (passwordEncoder == null) {
            throw new IllegalArgumentException("PasswordEncoder no puede ser nulo");
        }
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String Hash(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía");
        }
        return passwordEncoder.encode(password);
    }

    @Override
    public Boolean Verify(String password, String passwordHash) {
        if (password == null || passwordHash == null) {
            return false;
        }
        return passwordEncoder.matches(password, passwordHash);
    }
}

