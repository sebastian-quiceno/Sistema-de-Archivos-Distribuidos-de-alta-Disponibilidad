package com.sdan.middleware.Infraestructure.out.SpringData;

import com.sdan.middleware.Infraestructure.out.Entities.FileTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataFileTypeRepository extends JpaRepository<FileTypeEntity, UUID> {
    Optional<FileTypeEntity> findByName(String name);
}

