package biz.dealnote.messenger.api.model;

/**
 * Describes objects that contains an "id" field.
 */
public interface Identificable {

    /**
     * Returns unique identifier of this object(usually it's value of JSON field "id").
     */
    int getId();

}