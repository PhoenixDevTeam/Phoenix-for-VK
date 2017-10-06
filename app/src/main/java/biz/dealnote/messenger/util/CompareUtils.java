package biz.dealnote.messenger.util;

/**
 * Created by Ruslan Kolbasa on 07.09.2017.
 * phoenix
 */
public class CompareUtils {
    public static int compareInts(int lhs, int rhs){
        return (lhs < rhs) ? -1 : ((lhs > rhs) ? 1 : 0);
    }

    public static int compareBoolean(boolean lhs, boolean rhs){
        if(lhs == rhs){
            return 0;
        }

        if (lhs) {
            return 1;
        } else {
            return -1;
        }
    }
}