package biz.dealnote.messenger.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.model.Identificable;
import biz.dealnote.messenger.model.ISelectable;
import biz.dealnote.messenger.model.ISomeones;
import io.reactivex.disposables.Disposable;

import static biz.dealnote.messenger.util.Objects.isNull;

public class Utils {

    public static String stringEmptyIfNull(String orig) {
        return orig == null ? "" : orig;
    }

    public static <T> List<T> listEmptyIfNull(List<T> orig) {
        return orig == null ? Collections.emptyList() : orig;
    }

    public static <T> ArrayList<T> singletonArrayList(T data) {
        ArrayList<T> list = new ArrayList<>(1);
        list.add(data);
        return list;
    }

    public static <T> int findIndexByPredicate(List<T> data, Predicate<T> predicate) {
        for (int i = 0; i < data.size(); i++) {
            if (predicate.test(data.get(i))) {
                return i;
            }
        }

        return -1;
    }

    public static <T> Pair<Integer, T> findInfoByPredicate(List<T> data, Predicate<T> predicate) {
        for (int i = 0; i < data.size(); i++) {
            T t = data.get(i);
            if (predicate.test(t)) {
                return Pair.create(i, t);
            }
        }

        return null;
    }

    public static <T extends Identificable> Pair<Integer, T> findInfoById(List<T> data, int id) {
        for (int i = 0; i < data.size(); i++) {
            T t = data.get(i);
            if (t.getId() == id) {
                return Pair.create(i, t);
            }
        }

        return null;
    }

    public static <T extends Identificable> List<Integer> collectIds(Collection<T> data, Predicate<T> predicate) {
        int count = countOf(data, predicate);
        if (count == 0) {
            return Collections.emptyList();
        }

        List<Integer> ids = new ArrayList<>(count);
        for (T t : data) {
            if (predicate.test(t)) {
                ids.add(t.getId());
            }
        }

        return ids;
    }

    public static <T extends Identificable> int countOf(Collection<T> data, Predicate<T> predicate) {
        int count = 0;
        for (T t : data) {
            if (predicate.test(t)) {
                count++;
            }
        }

        return count;
    }

    public static boolean nonEmpty(Collection<?> data) {
        return data != null && !data.isEmpty();
    }

    public static Throwable getCauseIfRuntime(Throwable throwable) {
        Throwable target = throwable;
        while (target instanceof RuntimeException) {
            if (Objects.isNull(target.getCause())) {
                break;
            }

            target = target.getCause();
        }

        return target;
    }

    public static <T> ArrayList<T> cloneListAsArrayList(List<T> original) {
        if (original == null) {
            return null;
        }

        ArrayList<T> clone = new ArrayList<>(original.size());
        clone.addAll(original);
        return clone;
    }

    public static int countOfPositive(Collection<Integer> values) {
        int count = 0;
        for (Integer value : values) {
            if (value > 0) {
                count++;
            }
        }

        return count;
    }

    public static int countOfNegative(Collection<Integer> values) {
        int count = 0;
        for (Integer value : values) {
            if (value < 0) {
                count++;
            }
        }

        return count;
    }

    public static void trimListToSize(List<?> data, int maxsize) {
        if (data.size() > maxsize) {
            data.remove(data.size() - 1);
            trimListToSize(data, maxsize);
        }
    }

    public static <T> ArrayList<T> copyToArrayListWithPredicate(final List<T> orig, Predicate<T> predicate) {
        final ArrayList<T> data = new ArrayList<>(orig.size());
        for (T t : orig) {
            if (predicate.test(t)) {
                data.add(t);
            }
        }

        return data;
    }

    public static <T> List<T> copyListWithPredicate(final List<T> orig, Predicate<T> predicate) {
        final List<T> data = new ArrayList<>(orig.size());
        for (T t : orig) {
            if (predicate.test(t)) {
                data.add(t);
            }
        }

        return data;
    }

    public static boolean isEmpty(CharSequence body) {
        return body == null || body.length() == 0;
    }

    public static boolean nonEmpty(CharSequence text) {
        return text != null && text.length() > 0;
    }

    public static boolean isEmpty(Collection<?> data) {
        return data == null || data.size() == 0;
    }

    public static boolean isEmpty(Map<?, ?> data) {
        return data == null || data.size() == 0;
    }

    public interface SimpleFunction<F, S> {
        S apply(F orig);
    }

    public static <T> String join(T[] tokens, String delimiter, SimpleFunction<T, String> function) {
        if (isNull(tokens)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (T token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }

            sb.append(function.apply(token));
        }

        return sb.toString();
    }

    public static <T> String join(Iterable<T> tokens, String delimiter, SimpleFunction<T, String> function) {
        if (isNull(tokens)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (T token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }

            sb.append(function.apply(token));
        }

        return sb.toString();
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param tokens an array objects to be joined. Strings will be formed from
     *               the objects by calling object.toString().
     */
    public static String join(CharSequence delimiter, Iterable tokens) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> it = tokens.iterator();
        if (it.hasNext()) {
            sb.append(it.next());
            while (it.hasNext()) {
                sb.append(delimiter);
                sb.append(it.next());
            }
        }
        return sb.toString();
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param tokens an array strings to be joined
     */
    public static String stringJoin(CharSequence delimiter, String ... tokens) {
        StringBuilder sb = new StringBuilder();

        boolean firstTime = true;
        for (String token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }

            sb.append(token);
        }

        return sb.toString();
    }

    public static boolean safeIsEmpty(int[] mids) {
        return mids == null || mids.length == 0;
    }

    public static int safeLenghtOf(CharSequence text) {
        return Objects.isNull(text) ? 0 : text.length();
    }

    public static <T> int indexOf(@NonNull List<T> data, Predicate<T> predicate) {
        for (int i = 0; i < data.size(); i++) {
            T t = data.get(i);
            if (predicate.test(t)) {
                return i;
            }
        }

        return -1;
    }

    public static <T> boolean removeIf(@NonNull Collection<T> data, @NonNull Predicate<T> predicate) {
        boolean hasChanges = false;
        Iterator<T> iterator = data.iterator();
        while (iterator.hasNext()) {
            if (predicate.test(iterator.next())) {
                iterator.remove();
                hasChanges = true;
            }
        }

        return hasChanges;
    }

    public static void safelyDispose(Disposable disposable) {
        if (Objects.nonNull(disposable) && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public static void safelyCloseCursor(Cursor cursor) {
        if (Objects.nonNull(cursor)) {
            cursor.close();
        }
    }

    public static void safelyRecycle(Bitmap bitmap) {
        if (Objects.nonNull(bitmap)) {
            try {
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (Exception ignored) {

            }
        }
    }

    public static void safelyClose(Closeable closeable) {
        if (Objects.nonNull(closeable)) {
            try {
                closeable.close();
            } catch (IOException ignored) {

            }
        }
    }

    public static void showRedTopToast(@NonNull Activity activity, String text) {
        View view = View.inflate(activity, R.layout.toast_error, null);
        ((TextView) view.findViewById(R.id.text)).setText(text);

        Toast toast = Toast.makeText(activity, text, Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
        toast.show();
    }

    public static void showRedTopToast(@NonNull Activity activity, @StringRes int text, Object... params) {
        View view = View.inflate(activity, R.layout.toast_error, null);
        ((TextView) view.findViewById(R.id.text)).setText(activity.getString(text, params));

        Toast toast = Toast.makeText(activity, text, Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
        toast.show();
    }

    public static int safeCountOf(SparseArray sparseArray) {
        return sparseArray == null ? 0 : sparseArray.size();
    }

    public static int safeCountOf(Map map) {
        return map == null ? 0 : map.size();
    }

    public static int safeCountOf(Cursor cursor) {
        return cursor == null ? 0 : cursor.getCount();
    }

    public static long startOfTodayMillis() {
        return startOfToday().getTimeInMillis();
    }

    public static Calendar startOfToday() {
        Calendar current = Calendar.getInstance();
        current.set(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DATE), 0, 0, 0);
        return current;
    }

    @NonNull
    public static List<Integer> idsListOf(@NonNull Collection<? extends Identificable> data) {
        List<Integer> ids = new ArrayList<>(data.size());
        for (Identificable identifiable : data) {
            ids.add(identifiable.getId());
        }

        return ids;
    }

    @Nullable
    public static <T extends Identificable> T findById(@NonNull Collection<T> data, int id) {
        for (T element : data) {
            if (element.getId() == id) {
                return element;
            }
        }

        return null;
    }

    public static <T extends Identificable> int findIndexById(@NonNull List<T> data, int id) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getId() == id) {
                return i;
            }
        }

        return -1;
    }

    public static <T extends ISomeones> int findIndexById(@NonNull List<T> data, int id, int ownerId) {
        for (int i = 0; i < data.size(); i++) {
            T t = data.get(i);
            if (t.getId() == id && t.getOwnerId() == ownerId) {
                return i;
            }
        }

        return -1;
    }

    @NonNull
    public static <T extends Identificable> SparseArray<T> convertToSparseArray(@NonNull Collection<T> data) {
        SparseArray<T> map = new SparseArray<>(data.size());
        for (T item : data) {
            map.put(item.getId(), item);
        }

        return map;
    }

    @NonNull
    public static <T extends ISelectable> ArrayList<T> getSelected(@NonNull List<T> fullData) {
        return getSelected(fullData, false);
    }

    @NonNull
    public static <T extends ISelectable> ArrayList<T> getSelected(@NonNull List<T> fullData, boolean reverse) {
        ArrayList<T> result = new ArrayList<>();

        if (reverse) {
            for (int i = fullData.size() - 1; i >= 0; i--) {
                T m = fullData.get(i);
                if (m.isSelected()) {
                    result.add(m);
                }
            }
        } else {
            for (T item : fullData) {
                if (item.isSelected()) {
                    result.add(item);
                }
            }
        }

        return result;
    }

    public static int countOfSelection(List<? extends ISelectable> data) {
        int count = 0;
        for (ISelectable selectable : data) {
            if (selectable.isSelected()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Проверка, содержит ли маска флаг
     *
     * @param mask маска
     * @param flag флаг
     * @return если содержит - true
     */
    public static boolean hasFlag(int mask, int flag) {
        return (mask & flag) != 0;
    }

    /**
     * Проверка, содержит ли маска флаги
     *
     * @param mask  маска
     * @param flags флаги
     * @return если содержит - true
     */
    public static boolean hasFlags(int mask, int... flags) {
        for (int flag : flags) {
            if (!hasFlag(mask, flag)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Проверка, содержит ли маска какой нибудь из флагов
     *
     * @param mask  маска
     * @param flags флаги
     * @return если содержит - true
     */
    public static boolean hasSomeFlag(int mask, int... flags) {
        for (int flag : flags) {
            if (hasFlag(mask, flag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>Adds an object to the list. The object will be inserted in the correct
     * place so that the objects in the list are sorted. When the list already
     * contains objects that are equal according to the comparator, the new
     * object will be inserted immediately after these other objects.</p>
     *
     * @param o the object to be added
     */
    public static <T> int addElementToList(final T o, List<T> data, Comparator<T> comparator) {
        int i = 0;
        boolean found = false;
        while (!found && (i < data.size())) {
            found = comparator.compare(o, data.get(i)) < 0;
            if (!found) {
                i++;
            }
        }

        data.add(i, o);
        return i;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean isKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isKitkatWear() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT_WATCH;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean hasNougat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean hasNougatMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
    }

    public static boolean hasOreo(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static int indexOf(List<? extends Identificable> data, int id) {
        if (data == null) {
            return -1;
        }

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getId() == id) {
                return i;
            }
        }

        return -1;
    }

    public static boolean safeIsEmpty(CharSequence text) {
        return Objects.isNull(text) || text.length() == 0;
    }

    public static boolean safeTrimmedIsEmpty(String value) {
        return value == null || TextUtils.getTrimmedLength(value) == 0;
    }

    public static String firstNonEmptyString(String... array) {
        for (String s : array) {
            if (!TextUtils.isEmpty(s)) {
                return s;
            }
        }

        return null;
    }

    /**
     * Округление числа
     *
     * @param value  число
     * @param digits количество знаков после запятой
     * @return округленное число
     */
    public static BigDecimal roundUp(double value, int digits) {
        return new BigDecimal("" + value).setScale(digits, BigDecimal.ROUND_HALF_UP);
    }

    public static <T> ArrayList<T> createSingleElementList(T element) {
        ArrayList<T> list = new ArrayList<>();
        list.add(element);
        return list;
    }

    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean trimmedIsEmpty(String text) {
        return text == null || text.trim().length() == 0;
    }

    public static boolean trimmedNonEmpty(String text) {
        return text != null && text.trim().length() > 0;
    }

    public static boolean is600dp(Context context) {
        return context.getResources().getBoolean(R.bool.is_tablet);
    }

    public static boolean safeIsEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean safeIsEmpty(SparseArray<?> array) {
        return array == null || array.size() == 0;
    }

    public static boolean safeAllIsEmpty(Collection<?>... collections) {
        for (Collection collection : collections) {
            if (!safeIsEmpty(collection)) {
                return false;
            }
        }

        return true;
    }

    public static boolean intValueNotIn(int value, int... variants) {
        for (int variant : variants) {
            if (value == variant) {
                return false;
            }
        }

        return true;
    }

    public static boolean intValueIn(int value, int... variants) {
        for (int variant : variants) {
            if (value == variant) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasOneElement(Collection<?> collection) {
        return safeCountOf(collection) == 1;
    }

    public static int safeCountOf(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    public static int safeCountOfMultiple(Collection<?>... collections) {
        if (collections == null) {
            return 0;
        }

        int count = 0;
        for (Collection<?> collection : collections) {
            count = count + safeCountOf(collection);
        }

        return count;
    }

    public static float getActionBarHeight(Activity activity) {
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        }
        return 0;
    }

    /**
     * Добавляет прозрачность к цвету
     *
     * @param color  цвет
     * @param factor степень прозрачности
     * @return прозрачный цвет
     */
    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release + ")";
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static String getDiviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static String getAppVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return String.valueOf(packageInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static float dpToPx(float dp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static float spToPx(float dp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, context.getResources().getDisplayMetrics());
    }

    private Utils() {
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /***
     * Android L (lollipop, API 21) introduced a new problem when trying to invoke implicit intent,
     * "java.lang.IllegalArgumentException: Service Intent must be explicit"
     * <p>
     * If you are using an implicit intent, and know only 1 target would answer this intent,
     * This method will help you turn the implicit intent into the explicit form.
     * <p>
     * Inspired from SO answer: http://stackoverflow.com/a/26318757/1446466
     *
     * @param context
     * @param implicitIntent - The original implicit intent
     * @return Explicit Intent created from the implicit original intent
     */
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    public static void shareLink(Activity activity, String link, String subject) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, link);
        activity.startActivity(Intent.createChooser(sharingIntent, activity.getResources().getString(R.string.share_using)));
    }
}
