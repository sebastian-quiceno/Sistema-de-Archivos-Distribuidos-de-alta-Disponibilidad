package com.sdan.middleware.Infraestructure.out.Entities;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "node")
public class NodeEntity {

    @Id
    @Column(name = "node_id")
    private UUID nodeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private StatusEntity status;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "region_name", nullable = false, length = 50)
    private String regionName;

    @Column(name = "max_disk_size", nullable = false)
    private Long maxDiskSize;

    @Column(name = "free_disk_size", nullable = false)
    private Long freeDiskSize;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_heatbeat", nullable = false)
    private Date lastHeatbeat;

    public NodeEntity() {
    }

    public NodeEntity(UUID nodeId, StatusEntity status, String name, String regionName, Long maxDiskSize, Long freeDiskSize, Date lastHeatbeat) {
        this.nodeId = nodeId;
        this.status = status;
        this.name = name;
        this.regionName = regionName;
        this.maxDiskSize = maxDiskSize;
        this.freeDiskSize = freeDiskSize;
        this.lastHeatbeat = lastHeatbeat;
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public void setNodeId(UUID nodeId) {
        this.nodeId = nodeId;
    }

    public StatusEntity getStatus() {
        return status;
    }

    public void setStatus(StatusEntity status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public Long getMaxDiskSize() {
        return maxDiskSize;
    }

    public void setMaxDiskSize(Long maxDiskSize) {
        this.maxDiskSize = maxDiskSize;
    }

    public Long getFreeDiskSize() {
        return freeDiskSize;
    }

    public void setFreeDiskSize(Long freeDiskSize) {
        this.freeDiskSize = freeDiskSize;
    }

    public Date getLastHeatbeat() {
        return lastHeatbeat;
    }

    public void setLastHeatbeat(Date lastHeatbeat) {
        this.lastHeatbeat = lastHeatbeat;
    }
}

