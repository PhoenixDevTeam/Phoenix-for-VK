package biz.dealnote.messenger.db.impl;

import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import biz.dealnote.messenger.db.MapFunction;
import biz.dealnote.messenger.db.interfaces.Cancelable;
import biz.dealnote.messenger.db.interfaces.IStorage;
import biz.dealnote.messenger.db.interfaces.IStorages;
import biz.dealnote.messenger.db.model.entity.AttachmentsEntity;
import biz.dealnote.messenger.db.model.entity.EntitiesWrapper;
import biz.dealnote.messenger.db.model.entity.EntityWrapper;
import biz.dealnote.messenger.db.serialize.AttachmentsDboAdapter;
import biz.dealnote.messenger.db.serialize.EntitiesWrapperAdapter;
import biz.dealnote.messenger.db.serialize.EntityWrapperAdapter;
import biz.dealnote.messenger.db.serialize.UriSerializer;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

public class AbsStorage implements IStorage {

    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Uri.class, new UriSerializer())
            .registerTypeAdapter(AttachmentsEntity.class, new AttachmentsDboAdapter())
            .registerTypeAdapter(EntityWrapper.class, new EntityWrapperAdapter())
            .registerTypeAdapter(EntitiesWrapper.class, new EntitiesWrapperAdapter())
            .serializeSpecialFloatingPointValues() // for test
            .create();

    private final AppStorages mRepositoryContext;

    public AbsStorage(@NonNull AppStorages base) {
        this.mRepositoryContext = base;
    }

    @Override
    public IStorages getStores() {
        return mRepositoryContext;
    }

    @NonNull
    public Context getContext() {
        return mRepositoryContext.getApplicationContext();
    }

    @Nullable
    static String serializeJson(@Nullable Object o){
        return isNull(o) ? null : GSON.toJson(o);
    }

    @Nullable
    static <T> T deserializeJson(Cursor cursor, String column, Class<T> clazz){
        String json = cursor.getString(cursor.getColumnIndex(column));
        if(nonEmpty(json)){
            return GSON.fromJson(json, clazz);
        } else {
            return null;
        }
    }

    static <T> List<T> mapAll(Cursor cursor, MapFunction<T> function, boolean close){
        List<T> data = new ArrayList<>(safeCountOf(cursor));
        if (nonNull(cursor)) {
            while (cursor.moveToNext()) {
                data.add(function.map(cursor));
            }

            if(close){
                cursor.close();
            }
        }

        return data;
    }

    static <T> List<T> mapAll(Cancelable cancelable, Cursor cursor, MapFunction<T> function, boolean close){
        List<T> data = new ArrayList<>(safeCountOf(cursor));
        if (nonNull(cursor)) {
            while (cursor.moveToNext()) {
                if (cancelable.isOperationCancelled()) {
                    break;
                }

                data.add(function.map(cursor));
            }

            if(close){
                cursor.close();
            }
        }

        return data;
    }

    static int extractId(ContentProviderResult result) {
        return Integer.parseInt(result.uri.getPathSegments().get(1));
    }

    protected ContentResolver getContentResolver(){
        return mRepositoryContext.getContentResolver();
    }

    static <T> int addToListAndReturnIndex(@NonNull List<T> target, @NonNull T item) {
        target.add(item);
        return target.size() - 1;
    }
}