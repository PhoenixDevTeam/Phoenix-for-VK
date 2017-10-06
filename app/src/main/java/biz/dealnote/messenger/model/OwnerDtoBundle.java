package biz.dealnote.messenger.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;

import biz.dealnote.messenger.api.model.VKApiOwner;

/**
 * Created by admin on 04.12.2016.
 * phoenix
 */
public interface OwnerDtoBundle {

    @Nullable
    VKApiOwner findById(int id);

    @NonNull
    VKApiOwner getById(int id);

    int size();

    void put(@NonNull VKApiOwner owner);

    Collection<Integer> getMissing(Collection<Integer> ids);
}