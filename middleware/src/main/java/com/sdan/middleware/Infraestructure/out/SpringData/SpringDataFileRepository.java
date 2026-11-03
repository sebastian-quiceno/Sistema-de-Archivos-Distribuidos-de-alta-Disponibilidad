package com.sdan.middleware.Infraestructure.out.SpringData;

import com.sdan.middleware.Infraestructure.out.Entities.FileEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataFileRepository extends JpaRepository<FileEntity, UUID> {

    @Query("SELECT f FROM FileEntity f ORDER BY f.dateCreated DESC")
    List<FileEntity> findPagedFiles(Pageable pageable);
}

