package com.sdan.middleware.Infraestructure.Security;

import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import com.sdan.middleware.Domain.Exceptions.Users.UnauthorizedUserException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUser implements ICurrentUser {

    @Override
    public UUID getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedUserException("No hay un usuario autenticado en la sesión actual");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof SecurityUserDetails securityUserDetails) {
            return securityUserDetails.getUserId();
        }

        if (principal instanceof String principalStr) {
            try {
                return UUID.fromString(principalStr);
            } catch (IllegalArgumentException ignored) {
            }
        }

        throw new UnauthorizedUserException("No se pudo resolver el identificador del usuario autenticado");
    }

    @Override
    public String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedUserException("No hay un usuario autenticado en la sesión actual");
        }
        return auth.getName();
    }

    @Override
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }
}

