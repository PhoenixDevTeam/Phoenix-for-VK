package biz.dealnote.messenger.util;

/**
 * Created by Ruslan Kolbasa on 07.09.2017.
 * phoenix
 */
public class CompareUtils {
    public static int compareInts(int lhs, int rhs){
        return Integer.compare(lhs, rhs);
    }

    public static int compareBoolean(boolean lhs, boolean rhs){
        return Boolean.compare(lhs, rhs);
    }
}