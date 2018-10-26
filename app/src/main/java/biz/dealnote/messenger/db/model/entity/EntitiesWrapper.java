package biz.dealnote.messenger.db.model.entity;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;

public final class EntitiesWrapper implements Iterable<Entity> {

    private final List<Entity> entities;

    public EntitiesWrapper(List<Entity> entities) {
        this.entities = entities;
    }

    public List<Entity> get() {
        return entities;
    }

    public static final EntitiesWrapper EMPTY = new EntitiesWrapper(Collections.emptyList());

    public static EntitiesWrapper wrap(List<Entity> entities){
        return entities == null ? EMPTY : new EntitiesWrapper(entities);
    }

    @NonNull
    @Override
    public Iterator<Entity> iterator() {
        return entities.iterator();
    }
}