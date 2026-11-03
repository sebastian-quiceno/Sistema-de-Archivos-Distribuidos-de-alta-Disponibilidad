package com.sdan.middleware.Infraestructure.out;

import com.sdan.middleware.Domain.Entities.Node;
import com.sdan.middleware.Domain.Enums.Status;
import com.sdan.middleware.Domain.ValueObjects.NodeName;
import com.sdan.middleware.Domain.ValueObjects.RegionName;
import com.sdan.middleware.Infraestructure.out.Entities.NodeEntity;
import com.sdan.middleware.Infraestructure.out.Entities.StatusEntity;
import com.sdan.middleware.Infraestructure.out.Mappers.NodeMapper;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataNodeRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NodeRepositoryTest {

    @Mock
    private SpringDataNodeRepository springDataNodeRepository;
    @Mock
    private SpringDataStatusRepository springDataStatusRepository;

    private NodeMapper nodeMapper;
    private NodeRepository nodeRepository;

    @BeforeEach
    void setUp() {
        nodeMapper = new NodeMapper();
        nodeRepository = new NodeRepository(springDataNodeRepository, springDataStatusRepository, nodeMapper);
    }

    @Test
    void testGetById_found() {
        UUID id = UUID.randomUUID();
        StatusEntity statusEntity = new StatusEntity(UUID.randomUUID(), "ONLINE", "Online");
        NodeEntity entity = new NodeEntity(id, statusEntity, "node-1", "us-east-1", 1000L, 500L, new Date());

        when(springDataNodeRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Node> result = nodeRepository.getById(id);

        assertTrue(result.isPresent());
        assertEquals("node-1", result.get().getName().getValue());
        assertEquals(Status.ONLINE, result.get().getStatus());
    }

    @Test
    void testGetIdsOnline() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(springDataNodeRepository.findOnlineNodeIds()).thenReturn(List.of(id1, id2));

        List<UUID> ids = nodeRepository.getIdsOnline();

        assertEquals(2, ids.size());
        assertTrue(ids.contains(id1));
    }

    @Test
    void testSave() {
        Node node = Node.create(new NodeName("node-test"), new RegionName("eu-west-1"), 2000L, 1000L);
        StatusEntity statusEntity = new StatusEntity(UUID.randomUUID(), "ONLINE", "Online");
        when(springDataStatusRepository.findByNameIgnoreCase("ONLINE")).thenReturn(Optional.of(statusEntity));

        nodeRepository.save(node);

        verify(springDataNodeRepository, times(1)).save(any(NodeEntity.class));
    }

    @Test
    void testDeleteById() {
        UUID id = UUID.randomUUID();
        nodeRepository.deleteById(id);

        verify(springDataNodeRepository, times(1)).deleteById(id);
    }
}

