package biz.dealnote.messenger.model;

public final class LastReadId {

    private int out;

    private int in;

    public LastReadId(int out, int in) {
        this.out = out;
        this.in = in;
    }

    public int getOut() {
        return out;
    }

    public LastReadId setOut(int out) {
        this.out = out;
        return this;
    }

    public int getIn() {
        return in;
    }

    public LastReadId setIn(int in) {
        this.in = in;
        return this;
    }
}