package biz.dealnote.messenger.crypt.ver;

/**
 * Created by ruslan.kolbasa on 23.11.2016.
 * phoenix
 */
public class Version {

    // aes-128, rsa-512
    public static final int V1 = 1;

    //// aes-256, rsa-2048
    //public static final int V2 = 2;

    public static final int CURRENT = V1;

    public static Attrs ofCurrent(){
        return of(CURRENT);
    }

    private static final Attrs ATTRS1 = new Attrs() {
        @Override
        public int getRsaKeySize() {
            return 512;
        }

        @Override
        public int getAesKeySize() {
            return 128;
        }
    };

    /*private static final Attrs ATTRS2 = new Attrs() {
        @Override
        public int getRsaKeySize() {
            return 2048;
        }

        @Override
        public int getAesKeySize() {
            return 256;
        }
    };*/

    public static Attrs of(int v){
        switch (v){
            case V1:
                return ATTRS1;
            //case V2:
            //    return ATTRS2;
            default:
                throw new IllegalArgumentException("Unsupported crytp version");
        }
    }

    public interface Attrs {
        int getRsaKeySize();

        int getAesKeySize();
    }
}
