package com.sdan.middleware.Infraestructure.out;

import com.sdan.middleware.Application.Ports.Repositories.IUserRepository;
import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Infraestructure.out.Entities.UserEntity;
import com.sdan.middleware.Infraestructure.out.Mappers.UserMapper;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataUserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepository implements IUserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserMapper userMapper;

    public UserRepository(SpringDataUserRepository springDataUserRepository, UserMapper userMapper) {
        this.springDataUserRepository = springDataUserRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> getById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return springDataUserRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> getByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return springDataUserRepository.findByUsername(username).map(userMapper::toDomain);
    }

    @Override
    public Boolean existByUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return springDataUserRepository.existsByUsername(username);
    }

    @Override
    public Boolean passwordMatches(String hashPassword) {
        if (hashPassword == null) {
            return false;
        }
        return springDataUserRepository.findAll().stream()
                .anyMatch(u -> hashPassword.equals(u.getPasswordHash()));
    }

    @Override
    public void save(User user) {
        if (user == null) {
            return;
        }
        UserEntity entity = userMapper.toEntity(user);
        springDataUserRepository.save(entity);
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            return;
        }
        springDataUserRepository.deleteById(id);
    }
}

