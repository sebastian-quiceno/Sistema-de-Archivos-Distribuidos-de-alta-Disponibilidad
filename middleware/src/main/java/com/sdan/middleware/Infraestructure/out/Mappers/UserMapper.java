package com.sdan.middleware.Infraestructure.out.Mappers;

import com.sdan.middleware.Domain.Entities.User;
import com.sdan.middleware.Domain.ValueObjects.PasswordHash;
import com.sdan.middleware.Domain.ValueObjects.UserName;
import com.sdan.middleware.Infraestructure.out.Entities.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.reCreate(
                entity.getUserId(),
                new UserName(entity.getUsername()),
                new PasswordHash(entity.getPasswordHash()),
                entity.getDateCreated()
        );
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        return new UserEntity(
                domain.getId(),
                domain.getUsername() != null ? domain.getUsername().getValue() : null,
                domain.getPasswordHash() != null ? domain.getPasswordHash().getValue() : null,
                domain.getDate_created()
        );
    }
}

