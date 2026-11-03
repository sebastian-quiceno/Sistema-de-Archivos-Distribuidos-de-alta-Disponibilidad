package com.sdan.middleware.Infraestructure.Security;

import com.sdan.middleware.Application.Ports.Security.IAuthorizationService;
import com.sdan.middleware.Application.Ports.Security.ICurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthorizationService implements IAuthorizationService {

    private final ICurrentUser currentUser;

    public AuthorizationService(ICurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public void authorize(String permission) {
        if (!hasPermission(permission))
            throw new AccessDeniedException("Acceso denegado: el usuario no cuenta con el permiso requerido: " + permission);

    }

    @Override
    public boolean hasPermission(String permission) {
        if (permission == null || permission.isBlank()) {
            return false;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return false;
        }

        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (authority.getAuthority().equals(permission) || authority.getAuthority().equals("ROLE_ADMIN")) {
                return true;
            }
        }
        return false;
    }

    @Override
    //Revisa si el recurso pertenece al usuario loggeado, si no lo es, mira si tiene el rol de admin para darle permiso
    public void authorizeOwner(UUID resourceOwnerId) {
        if (resourceOwnerId == null) {
            throw new IllegalArgumentException("El identificador del propietario del recurso no puede ser nulo");
        }

        UUID loggedUserId = currentUser.getUserId();
        if (!loggedUserId.equals(resourceOwnerId)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new AccessDeniedException("Acceso denegado: el recurso pertenece a otro usuario");
            }
        }
    }
}

