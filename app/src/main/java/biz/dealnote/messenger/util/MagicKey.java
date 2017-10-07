package biz.dealnote.messenger.util;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MagicKey implements Parcelable {

    private static final int TYPE_BOOLEAN = 1;
    private static final int TYPE_BYTE = 2;
    private static final int TYPE_CHAR = 3;
    private static final int TYPE_SHORT = 4;
    private static final int TYPE_INT = 5;
    private static final int TYPE_LONG = 6;
    private static final int TYPE_FLOAT = 7;
    private static final int TYPE_DOUBLE = 8;
    private static final int TYPE_STRING = 9;
    private static final int TYPE_CHARSEQUENCE = 10;
    private static final int TYPE_PARCELABLE = 11;

    private final ArrayList<String> mParamList = new ArrayList<>();
    private final ArrayList<Integer> mTypeList = new ArrayList<>();
    private final Bundle mBundle;

    public MagicKey() {
        this.mBundle = new Bundle();
    }

    public MagicKey(Bundle bundle) {
        this.mBundle = bundle;
    }

    /**
     * Add a boolean parameter to the key, replacing any existing value for the given name.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @return This instance
     */
    public MagicKey put(String name, boolean value) {
        removeFromRequestData(name);
        mParamList.add(name);
        mTypeList.add(TYPE_BOOLEAN);
        mBundle.putBoolean(name, value);
        return this;
    }

    /**
     * Add a byte parameter to the key, replacing any existing value for the given name.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @return This instance
     */
    public MagicKey put(String name, byte value) {
        removeFromRequestData(name);
        mParamList.add(name);
        mTypeList.add(TYPE_BYTE);
        mBundle.putByte(name, value);
        return this;
    }

    /**
     * Add a char parameter to the key, replacing any existing value for the given name.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @return This instance
     */
    public MagicKey put(String name, char value) {
        removeFromRequestData(name);
        mParamList.add(name);
        mTypeList.add(TYPE_CHAR);
        mBundle.putChar(name, value);
        return this;
    }

    /**
     * Add a short parameter to the key, replacing any existing value for the given name.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @return This instance
     */
    public MagicKey put(String name, short value) {
        removeFromRequestData(name);
        mParamList.add(name);
        mTypeList.add(TYPE_SHORT);
        mBundle.putShort(name, value);
        return this;
    }

    /**
     * Add a int parameter to the key, replacing any existing value for the given name.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @return This instance
     */
    public MagicKey put(String name, int value) {
        removeFromRequestData(name);
        mParamList.add(name);
        mTypeList.add(TYPE_INT);
        mBundle.putInt(name, value);
        return this;
    }

    /**
     * Add a long parameter to the key, replacing any existing value for the given name.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @return This instance
     */
    public MagicKey put(String name, long value) {
        removeFromRequestData(name);
        mParamList.add(name);
        mTypeList.add(TYPE_LONG);
        mBundle.putLong(name, value);
        return this;
    }

    /**
     * Add a float parameter to the key, replacing any existing value for the given name.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @return This instance
     */
    public MagicKey put(String name, float value) {
        removeFromRequestData(name);
        mParamList.add(name);
        mTypeList.add(TYPE_FLOAT);
        mBundle.putFloat(name, value);
        return this;
    }

    /**
     * Add a double parameter to the key, replacing any existing value for the given name.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @return This instance
     */
    public MagicKey put(String name, double value) {
        removeFromRequestData(name);
        mParamList.add(name);
        mTypeList.add(TYPE_DOUBLE);
        mBundle.putDouble(name, value);
        return this;
    }

    /**
     * Add a String parameter to the key, replacing any existing value for the given name.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @return This instance
     */
    public MagicKey put(String name, String value) {
        removeFromRequestData(name);
        mParamList.add(name);
        mTypeList.add(TYPE_STRING);
        mBundle.putString(name, value);
        return this;
    }

    /**
     * Add a CharSequence parameter to the key, replacing any existing value for the given name.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @return This instance
     */
    public MagicKey put(String name, CharSequence value) {
        removeFromRequestData(name);
        mParamList.add(name);
        mTypeList.add(TYPE_CHARSEQUENCE);
        mBundle.putCharSequence(name, value);
        return this;
    }

    /**
     * Add a Parcelable parameter to the key, replacing any existing value for the given name.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @return This instance
     */
    public MagicKey put(String name, Parcelable value) {
        removeFromRequestData(name);
        mParamList.add(name);
        mTypeList.add(TYPE_PARCELABLE);
        mBundle.putParcelable(name, value);
        return this;
    }

    private void removeFromRequestData(String name) {
        if (mParamList.contains(name)) {
            final int index = mParamList.indexOf(name);
            mParamList.remove(index);
            mTypeList.remove(index);
            mBundle.remove(name);
        }
    }

    /**
     * Check whether the key has an existing value for the given name.
     *
     * @param name The parameter name.
     * @return Whether the key has an existing value for the given name.
     */
    public boolean contains(String name) {
        return mParamList.contains(name);
    }

    /**
     * Returns the value associated with the given name, or false if no mapping of the desired type
     * exists for the given name.
     *
     * @param name The parameter name.
     * @return A boolean value.
     */
    public boolean getBoolean(String name) {
        return mBundle.getBoolean(name);
    }

    @Nullable
    public Boolean optBoolean(String name) {
        return contains(name) ? getBoolean(name) : null;
    }

    /**
     * Returns the value associated with the given name, or (byte) 0 if no mapping of the desired
     * type exists for the given name.
     *
     * @param name The parameter name.
     * @return A byte value.
     */
    public byte getByte(String name) {
        return mBundle.getByte(name);
    }

    /**
     * Returns the value associated with the given name, or (char) 0 if no mapping of the desired
     * type exists for the given name.
     *
     * @param name The parameter name.
     * @return A char value.
     */
    public char getChar(String name) {
        return mBundle.getChar(name);
    }

    /**
     * Returns the value associated with the given name, or (short) 0 if no mapping of the desired
     * type exists for the given name.
     *
     * @param name The parameter name.
     * @return A short value.
     */
    public short getShort(String name) {
        return mBundle.getShort(name);
    }

    /**
     * Returns the value associated with the given name, or 0 if no mapping of the desired type
     * exists for the given name.
     *
     * @param name The parameter name.
     * @return An int value.
     */
    public int getInt(String name) {
        return mBundle.getInt(name);
    }

    public Integer optInt(String name) {
        return contains(name) ? getInt(name) : null;
    }

    /**
     * Returns the value associated with the given name, or 0L if no mapping of the desired type
     * exists for the given name.
     *
     * @param name The parameter name.
     * @return A long value.
     */
    public long getLong(String name) {
        return mBundle.getLong(name);
    }

    @Nullable
    public Long optLong(String name) {
        return contains(name) ? getLong(name) : null;
    }

    /**
     * Returns the value associated with the given name, or 0.0f if no mapping of the desired type
     * exists for the given name.
     *
     * @param name The parameter name.
     * @return A float value.
     */
    public float getFloat(String name) {
        return mBundle.getFloat(name);
    }

    /**
     * Returns the value associated with the given name, or 0.0 if no mapping of the desired type
     * exists for the given name.
     *
     * @param name The parameter name.
     * @return A double value.
     */
    public double getDouble(String name) {
        return mBundle.getDouble(name);
    }

    /**
     * Returns the value associated with the given name, or null if no mapping of the desired type
     * exists for the given name.
     *
     * @param name The parameter name.
     * @return A String value.
     */
    public String getString(String name) {
        return mBundle.getString(name);
    }

    /**
     * Returns the value associated with the given name, or null if no mapping of the desired type
     * exists for the given name.
     *
     * @param name The parameter name.
     * @return A CharSequence value.
     */
    public CharSequence getCharSequence(String name) {
        return mBundle.getCharSequence(name);
    }

    /**
     * Returns the value associated with the given name, or null if no mapping of the desired type
     * exists for the given name.
     *
     * @param name The parameter name.
     * @return A Parcelable value.
     */
    public Parcelable getParcelable(String name) {
        return mBundle.getParcelable(name);
    }

    /**
     * Sets the ClassLoader to use by the underlying Bundle when getting Parcelable objects.
     *
     * @param classLoader The ClassLoader to use by the underlying Bundle when getting Parcelable
     *                    objects.
     */
    public void setClassLoader(ClassLoader classLoader) {
        mBundle.setClassLoader(classLoader);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MagicKey)) return false;

        MagicKey oParams = (MagicKey) o;

        if (mParamList.size() != oParams.mParamList.size()) {
            return false;
        }

        for (int i = 0, length = mParamList.size(); i < length; i++) {
            String param = mParamList.get(i);
            if (!oParams.mParamList.contains(param)) {
                return false;
            }

            int type = mTypeList.get(i);
            if (oParams.mTypeList.get(i) != type) {
                return false;
            }

            switch (mTypeList.get(i)) {
                case TYPE_BOOLEAN:
                    if (mBundle.getBoolean(param) != oParams.mBundle.getBoolean(param)) {
                        return false;
                    }
                    break;
                case TYPE_BYTE:
                    if (mBundle.getByte(param) != oParams.mBundle.getByte(param)) {
                        return false;
                    }
                    break;
                case TYPE_CHAR:
                    if (mBundle.getChar(param) != oParams.mBundle.getChar(param)) {
                        return false;
                    }
                    break;
                case TYPE_SHORT:
                    if (mBundle.getShort(param) != oParams.mBundle.getShort(param)) {
                        return false;
                    }
                    break;
                case TYPE_INT:
                    if (mBundle.getInt(param) != oParams.mBundle.getInt(param)) {
                        return false;
                    }
                    break;
                case TYPE_LONG:
                    if (mBundle.getLong(param) != oParams.mBundle.getLong(param)) {
                        return false;
                    }
                    break;
                case TYPE_FLOAT:
                    if (mBundle.getFloat(param) != oParams.mBundle.getFloat(param)) {
                        return false;
                    }
                    break;
                case TYPE_DOUBLE:
                    if (mBundle.getDouble(param) != oParams.mBundle.getDouble(param)) {
                        return false;
                    }
                    break;
                case TYPE_STRING:
                    if (!Objects.safeEquals(mBundle.getString(param),
                            oParams.mBundle.getString(param))) {
                        return false;
                    }
                    break;
                case TYPE_CHARSEQUENCE:
                    if (!Objects.safeEquals(mBundle.getCharSequence(param),
                            oParams.mBundle.getCharSequence(param))) {
                        return false;
                    }
                    break;
                case TYPE_PARCELABLE:
                    if (!Objects.safeEquals(mBundle.getParcelable(param),
                            oParams.mBundle.getParcelable(param))) {
                        return false;
                    }
                    break;
                default:
                    // We should never arrive here normally.
                    throw new IllegalArgumentException("The type of the field is not a valid one");
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        ArrayList<Object> objectList = new ArrayList<>();
        for (int i = 0, length = mParamList.size(); i < length; i++) {
            objectList.add(mBundle.get(mParamList.get(i)));
        }

        return objectList.hashCode();
    }

    public MagicKey(final Parcel in) {
        in.readStringList(mParamList);

        for (int i = 0, n = in.readInt(); i < n; i++) {
            mTypeList.add(in.readInt());
        }

        mBundle = in.readBundle(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(mParamList);
        dest.writeInt(mTypeList.size());

        for (int i = 0, length = mTypeList.size(); i < length; i++) {
            dest.writeInt(mTypeList.get(i));
        }

        dest.writeBundle(mBundle);
    }

    public static final Parcelable.Creator<MagicKey> CREATOR = new Parcelable.Creator<MagicKey>() {
        public MagicKey createFromParcel(final Parcel in) {
            return new MagicKey(in);
        }

        public MagicKey[] newArray(final int size) {
            return new MagicKey[size];
        }
    };

    @Override
    public String toString() {
        List<String> params = new ArrayList<>();

        for (String key : mBundle.keySet()) {
            try {
                Object value = mBundle.get(key);
                params.add(key + ":" + String.valueOf(value));
            } catch (Exception ignored) {
            }
        }

        return params.toString();
    }
}
