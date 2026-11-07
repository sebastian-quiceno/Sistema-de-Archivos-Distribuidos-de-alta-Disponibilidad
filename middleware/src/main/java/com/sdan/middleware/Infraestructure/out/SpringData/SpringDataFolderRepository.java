package com.sdan.middleware.Infraestructure.out.SpringData;

import com.sdan.middleware.Infraestructure.out.Entities.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataFolderRepository extends JpaRepository<FolderEntity, UUID> {

    @Query("SELECT f FROM FolderEntity f WHERE f.fatherFolder.folderId = :folderId")
    List<FolderEntity> findRelatedFolders(@Param("folderId") UUID folderId);

    @Query("SELECT f FROM FolderEntity f WHERE f.user.user_id = :userId AND f.fatherFolder IS NULL")
    List<FolderEntity> findRelatedFoldersById(@Param("userId") UUID userId);
}

