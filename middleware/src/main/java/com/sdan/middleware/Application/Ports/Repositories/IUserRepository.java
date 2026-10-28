package com.sdan.middleware.Application.Ports.Repositories;

import com.sdan.middleware.Domain.Entities.User;

import java.util.Optional;
import java.util.UUID;

public interface IUserRepository {
    Optional<User> getById(UUID id);

    void save(User user);

    void deleteById(UUID id);

}
