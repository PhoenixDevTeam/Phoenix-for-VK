package biz.dealnote.messenger.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Ruslan Kolbasa on 11.09.2017.
 * phoenix
 */
public class SparseArrayOwnersBundle implements IOwnersBundle {

    private final SparseArray<Owner> data;

    public SparseArrayOwnersBundle(int capacity) {
        this.data = new SparseArray<>(capacity);
    }

    @Nullable
    @Override
    public Owner findById(int id) {
        return data.get(id);
    }

    @NonNull
    @Override
    public Owner getById(int id) {
        Owner owner = findById(id);

        if (owner == null) {
            if (id > 0) {
                owner = new User(id);
            } else if (id < 0) {
                owner = new Community(-id);
            } else {
                throw new IllegalArgumentException("Zero owner id!!!");
            }
        }

        return owner;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public void putAll(@NonNull Collection<? extends Owner> owners) {
        for (Owner owner : owners) {
            put(owner);
        }
    }

    @Override
    public void put(@NonNull Owner owner) {
        switch (owner.getOwnerType()) {
            case OwnerType.USER:
                this.data.put(((User) owner).getId(), owner);
                break;
            case OwnerType.COMMUNITY:
                this.data.put(-((Community) owner).getId(), owner);
                break;
        }
    }

    @Override
    public Collection<Integer> getMissing(Collection<Integer> ids) {
        Collection<Integer> missing = new ArrayList<>();

        for(Integer id : ids){
            if(data.get(id) == null){
                missing.add(id);
            }
        }

        return missing;
    }
}