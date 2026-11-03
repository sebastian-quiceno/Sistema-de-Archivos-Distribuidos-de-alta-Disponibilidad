package com.sdan.middleware.Infraestructure.out.SpringData;

import com.sdan.middleware.Infraestructure.out.Entities.NodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataNodeRepository extends JpaRepository<NodeEntity, UUID> {

    @Query("SELECT n.nodeId FROM NodeEntity n WHERE UPPER(n.status.name) = 'ONLINE'")
    List<UUID> findOnlineNodeIds();
}

