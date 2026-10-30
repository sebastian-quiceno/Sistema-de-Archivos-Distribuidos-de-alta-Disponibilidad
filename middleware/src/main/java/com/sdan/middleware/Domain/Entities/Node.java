package com.sdan.middleware.Domain.Entities;

import com.sdan.middleware.BuildingBlocks.Domain.DomainException;
import com.sdan.middleware.BuildingBlocks.Domain.Guard;
import com.sdan.middleware.Domain.Enums.Status;
import com.sdan.middleware.Domain.ValueObjects.NodeName;
import com.sdan.middleware.Domain.ValueObjects.RegionName;

import java.util.Date;
import java.util.UUID;

public class Node {
    private static final Integer MAX_PORT = 65535;

    private UUID id;
    private NodeName name;
    private RegionName regionName;
    private Status status;
    private Long maxDiskSize;
    private Long freeDiskSize;
    private Date lastHeatbeat;

    private Node(UUID id, NodeName name, RegionName regionName, Status status, Long maxDiskSize, Long freeDiskSize, Date lastHeatbeat) {
        this.id = id;
        this.name = name;
        this.regionName = regionName;
        this.status = status;
        this.maxDiskSize = maxDiskSize;
        this.freeDiskSize = freeDiskSize;
        this.lastHeatbeat = lastHeatbeat;
    }

    //Getters
    public UUID getId() {
        return id;
    }

    public NodeName getName() {
        return name;
    }

    public RegionName getRegionName() {
        return regionName;
    }

    public Status getStatus() {
        return status;
    }

    public Long getMaxDiskSize() {
        return maxDiskSize;
    }

    public Long getFreeDiskSize() {
        return freeDiskSize;
    }

    public Date getLastHeatbeat() {
        return lastHeatbeat;
    }

    //Metodos creacionales estaticos
    public static Node create(NodeName name, RegionName regionName, Long maxDiskSize, Long freeDiskSize){
        return new Node(UUID.randomUUID(), name, regionName, Status.ONLINE, maxDiskSize, freeDiskSize, new Date());
    }

    public static Node reCreate(UUID id, NodeName name, RegionName regionName, Status status, Long maxDiskSize, Long freeDiskSize, Date lastHeatbeat){
        return new Node(id, name, regionName, status, maxDiskSize, freeDiskSize, lastHeatbeat);
    }

    //Metodos set
    public void changeStatus(Status newStatus){
        status = newStatus;
    }

    public void changeFreeDiskSize(Long newFreeSize){
        freeDiskSize = newFreeSize;
    }

    public void doHeartbeat(){
        lastHeatbeat = new Date();
    }

    //Metodos privados
    private Integer validPort(Integer port){
        if(Guard.positive(port, "port") > MAX_PORT)
            throw new DomainException("El campo 'port' tiene que estar entre "+0+" y "+MAX_PORT);
        return port;
    }
}
