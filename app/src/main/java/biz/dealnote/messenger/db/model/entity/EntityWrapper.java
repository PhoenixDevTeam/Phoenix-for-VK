package biz.dealnote.messenger.db.model.entity;

/**
 * Created by Ruslan Kolbasa on 21.09.2017.
 * phoenix
 */
public class EntityWrapper {

    private final Entity entity;

    public EntityWrapper(Entity entity) {
        this.entity = entity;
    }

    public Entity get() {
        return entity;
    }

    public static EntityWrapper empty(){
        return new EntityWrapper(null);
    }
}