package com.sdan.middleware.Infraestructure.out.Entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "status")
public class StatusEntity {

    @Id
    @Column(name = "status_id")
    private UUID statusId;

    @Column(name = "name", nullable = false, unique = true, length = 20)
    private String name;

    @Column(name = "description", nullable = false, length = 50)
    private String description;

    public StatusEntity() {
    }

    public StatusEntity(UUID statusId, String name, String description) {
        this.statusId = statusId;
        this.name = name;
        this.description = description;
    }

    public UUID getStatusId() {
        return statusId;
    }

    public void setStatusId(UUID statusId) {
        this.statusId = statusId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

