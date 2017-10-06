package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

import static biz.dealnote.messenger.util.Utils.firstNonEmptyString;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public class PhotoSizes implements Parcelable {

    private String s;
    private String m;
    private String x;
    private String o;
    private String p;
    private String q;
    private String r;
    private String y;
    private String z;
    private String w;

    public static PhotoSizes empty(){
        return new PhotoSizes();
    }

    public PhotoSizes(){

    }

    public PhotoSizes setS(String s) {
        this.s = s;
        return this;
    }

    public PhotoSizes setM(String m) {
        this.m = m;
        return this;
    }

    public PhotoSizes setX(String x) {
        this.x = x;
        return this;
    }

    public PhotoSizes setO(String o) {
        this.o = o;
        return this;
    }

    public PhotoSizes setP(String p) {
        this.p = p;
        return this;
    }

    public PhotoSizes setQ(String q) {
        this.q = q;
        return this;
    }

    public PhotoSizes setR(String r) {
        this.r = r;
        return this;
    }

    public PhotoSizes setY(String y) {
        this.y = y;
        return this;
    }

    public PhotoSizes setZ(String z) {
        this.z = z;
        return this;
    }

    public PhotoSizes setW(String w) {
        this.w = w;
        return this;
    }

    protected PhotoSizes(Parcel in) {
        s = in.readString();
        m = in.readString();
        x = in.readString();
        o = in.readString();
        p = in.readString();
        q = in.readString();
        r = in.readString();
        y = in.readString();
        z = in.readString();
        w = in.readString();
    }

    public String getS() {
        return s;
    }

    public String getM() {
        return m;
    }

    public String getX() {
        return x;
    }

    public String getO() {
        return o;
    }

    public String getP() {
        return p;
    }

    public String getQ() {
        return q;
    }

    public String getR() {
        return r;
    }

    public String getY() {
        return y;
    }

    public String getZ() {
        return z;
    }

    public String getW() {
        return w;
    }

    public static final Creator<PhotoSizes> CREATOR = new Creator<PhotoSizes>() {
        @Override
        public PhotoSizes createFromParcel(Parcel in) {
            return new PhotoSizes(in);
        }

        @Override
        public PhotoSizes[] newArray(int size) {
            return new PhotoSizes[size];
        }
    };

    public String get3to2url(@PhotoSize int max){
        switch (max){
            case PhotoSize.O:
                return o;
            case PhotoSize.P:
                return firstNonEmptyString(p, o);
            case PhotoSize.Q:
                return firstNonEmptyString(q, p, o);
            case PhotoSize.R:
                return firstNonEmptyString(r, q, p, o);
            default:
                throw new IllegalArgumentException("Invalid photo size: " + max);
        }
    }

    public String getUrlForSize(@PhotoSize int size, boolean excludeNonAspectRatio) {
        switch (size) {
            case PhotoSize.S:
                return s;
            case PhotoSize.M:
                return firstNonEmptyString(m, s);
            case PhotoSize.X:
                return firstNonEmptyString(x, m, s);
            case PhotoSize.O:
                return excludeNonAspectRatio ? firstNonEmptyString(x, m, s)
                        : firstNonEmptyString(o, x, m, s);
            case PhotoSize.P:
                return excludeNonAspectRatio ? firstNonEmptyString(x, m, s)
                        : firstNonEmptyString(p, o, x, m, s);
            case PhotoSize.Q:
                return excludeNonAspectRatio ? firstNonEmptyString(x, m, s)
                        : firstNonEmptyString(q, p, o, x, m, s);
            case PhotoSize.R:
                return excludeNonAspectRatio ? firstNonEmptyString(x, m, s)
                        : firstNonEmptyString(r, q, p, o, x, m, s);
            case PhotoSize.Y:
                return excludeNonAspectRatio ? firstNonEmptyString(y, x, m, s)
                        : firstNonEmptyString(y, r, q, p, o, x, m, s);
            case PhotoSize.Z:
                return excludeNonAspectRatio ? firstNonEmptyString(z, y, x, m, s)
                        : firstNonEmptyString(z, y, r, q, p, o, x, m, s);
            case PhotoSize.W:
                return excludeNonAspectRatio ? firstNonEmptyString(w, z, y, x, m, s)
                        : firstNonEmptyString(w, z, y, r, q, p, o, x, m, s);
            default:
                throw new IllegalArgumentException("Invalid photo size: " + size);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(s);
        parcel.writeString(m);
        parcel.writeString(x);
        parcel.writeString(o);
        parcel.writeString(p);
        parcel.writeString(q);
        parcel.writeString(r);
        parcel.writeString(y);
        parcel.writeString(z);
        parcel.writeString(w);
    }

    public boolean isEmpty() {
        return firstNonEmptyString(s, m, x, o, p, q, r, y, z, w) == null;
    }
}