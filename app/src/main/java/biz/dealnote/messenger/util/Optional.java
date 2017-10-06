package biz.dealnote.messenger.util;

/**
 * Created by Ruslan Kolbasa on 03.07.2017.
 * phoenix
 */
public class Optional<T> {

    private final T value;

    private Optional(T value) {
        this.value = value;
    }

    public static <T> Optional<T> wrap(T value){
        return new Optional<>(value);
    }

    public static <T> Optional<T> empty(){
        return new Optional<>(null);
    }

    public T get() {
        return value;
    }

    public boolean isEmpty(){
        return value == null;
    }

    public boolean nonEmpty(){
        return value != null;
    }
}