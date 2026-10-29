package com.sdan.middleware.Application.Ports.Repositories;

import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.ValueObjects.UserName;

import java.util.Optional;
import java.util.UUID;

public interface IUserRepository {
    Optional<User> getById(UUID id);

    Optional<User> getByUsername(String username);

    Boolean existByUsername(String username);

    //TODO: revisar si hace falta este metodo
    Boolean passwordMatches(String hashPassword);

    void save(User user);

    void deleteById(UUID id);

}
