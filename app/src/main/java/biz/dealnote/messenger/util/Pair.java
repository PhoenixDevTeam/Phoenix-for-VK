package biz.dealnote.messenger.util;

/**
 * Created by ruslan.kolbasa on 01.02.2017.
 * phoenix
 */
public final class Pair<F, S> {

    private F first;

    private S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> Pair<F, S> create(F first, S second) {
        return new Pair<>(first, second);
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    public Pair setFirst(F first) {
        this.first = first;
        return this;
    }

    public Pair setSecond(S second) {
        this.second = second;
        return this;
    }

    @Override
    public String toString() {
        return "[" + first + "]:[" + second + "]";
    }
}
