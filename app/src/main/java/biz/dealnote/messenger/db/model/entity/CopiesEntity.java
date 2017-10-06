package biz.dealnote.messenger.db.model.entity;

import java.util.List;

import biz.dealnote.messenger.db.model.IdPairEntity;

/**
 * Created by Ruslan Kolbasa on 11.09.2017.
 * phoenix
 */
public class CopiesEntity {

    private int count;

    private List<IdPairEntity> pairDbos;

    public CopiesEntity setCount(int count) {
        this.count = count;
        return this;
    }

    public CopiesEntity setPairDbos(List<IdPairEntity> pairDbos) {
        this.pairDbos = pairDbos;
        return this;
    }

    public int getCount() {
        return count;
    }

    public List<IdPairEntity> getPairDbos() {
        return pairDbos;
    }
}