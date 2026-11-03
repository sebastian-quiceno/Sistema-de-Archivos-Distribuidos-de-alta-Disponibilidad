package com.sdan.middleware.Infraestructure.Security;

import com.sdan.middleware.Application.Ports.Repositories.IUserRepository;
import com.sdan.middleware.Application.Ports.Security.Permissions;
import com.sdan.middleware.Domain.Entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserRepository userRepository;

    public CustomUserDetailsService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("El nombre de usuario no puede ser nulo o vacío");
        }

        User user = userRepository.getByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Usuario no encontrado con el nombre de usuario: " + username)
        );

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        for (String permission : Permissions.defaultUserPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }

        return new SecurityUserDetails(
                user.getId(),
                user.getUsername().getValue(),
                user.getPasswordHash().getValue(),
                authorities
        );
    }
}

