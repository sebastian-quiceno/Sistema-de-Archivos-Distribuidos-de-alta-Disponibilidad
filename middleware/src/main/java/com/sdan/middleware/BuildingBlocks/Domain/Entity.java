package com.sdan.middleware.BuildingBlocks.Domain;

import java.util.Objects;

public abstract class Entity <Tid>{
    private Tid id;

    protected Entity(){}

    public Tid getId() {
        return id;
    }

    @Override
    public boolean equals(Object object) {
        return this == object;
    }

    @Override
    public int hashCode(){
        return Objects.hash(id);
    }

}


//    public override bool Equals(object? obj) =>
//       obj is Entity<TId> other
//       && GetType() == other.GetType()
//       && _tieneId && other._tieneId
//       && EqualityComparer<TId>.Default.Equals(_id, other._id);
//
//   public override int GetHashCode() => HashCode.Combine(GetType(), _id);