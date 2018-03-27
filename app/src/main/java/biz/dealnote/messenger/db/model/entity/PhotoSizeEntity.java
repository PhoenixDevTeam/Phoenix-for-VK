package biz.dealnote.messenger.db.model.entity;

/**
 * Created by Ruslan Kolbasa on 04.09.2017.
 * phoenix
 */
public class PhotoSizeEntity {

    private Size s;
    private Size m;
    private Size x;
    private Size o;
    private Size p;
    private Size q;
    private Size r;
    private Size y;
    private Size z;
    private Size w;

    public static final class Size {

        private int w;
        private int h;
        private String url;

        public int getH() {
            return h;
        }

        public int getW() {
            return w;
        }

        public String getUrl() {
            return url;
        }

        public Size setH(int h) {
            this.h = h;
            return this;
        }

        public Size setUrl(String url) {
            this.url = url;
            return this;
        }

        public Size setW(int w) {
            this.w = w;
            return this;
        }
    }

    public Size getS() {
        return s;
    }

    public PhotoSizeEntity setS(Size s) {
        this.s = s;
        return this;
    }

    public Size getM() {
        return m;
    }

    public PhotoSizeEntity setM(Size m) {
        this.m = m;
        return this;
    }

    public Size getX() {
        return x;
    }

    public PhotoSizeEntity setX(Size x) {
        this.x = x;
        return this;
    }

    public Size getO() {
        return o;
    }

    public PhotoSizeEntity setO(Size o) {
        this.o = o;
        return this;
    }

    public Size getP() {
        return p;
    }

    public PhotoSizeEntity setP(Size p) {
        this.p = p;
        return this;
    }

    public Size getQ() {
        return q;
    }

    public PhotoSizeEntity setQ(Size q) {
        this.q = q;
        return this;
    }

    public Size getR() {
        return r;
    }

    public PhotoSizeEntity setR(Size r) {
        this.r = r;
        return this;
    }

    public Size getY() {
        return y;
    }

    public PhotoSizeEntity setY(Size y) {
        this.y = y;
        return this;
    }

    public Size getZ() {
        return z;
    }

    public PhotoSizeEntity setZ(Size z) {
        this.z = z;
        return this;
    }

    public Size getW() {
        return w;
    }

    public PhotoSizeEntity setW(Size w) {
        this.w = w;
        return this;
    }
}