package biz.dealnote.messenger.db.model.entity;

import java.util.List;

/**
 * Created by Ruslan Kolbasa on 04.09.2017.
 * phoenix
 */
public class AttachmentsEntity {

    private final List<Entity> entities;

    public AttachmentsEntity(List<Entity> entities) {
        this.entities = entities;
    }

    public List<Entity> getEntities() {
        return entities;
    }
}