package com.sdan.middleware.Infraestructure.out.SpringData;

import com.sdan.middleware.Infraestructure.out.Entities.StatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataStatusRepository extends JpaRepository<StatusEntity, UUID> {
    Optional<StatusEntity> findByNameIgnoreCase(String name);
}

