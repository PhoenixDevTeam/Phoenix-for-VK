package biz.dealnote.messenger.domain;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.db.AttachToType;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 05.09.2017.
 * phoenix
 */
public interface IAttachmentsRepository {

    @CheckResult
    Completable remove(int accountId, @AttachToType int type, int attachToId, int generatedAttachmentId);

    @CheckResult
    Completable attach(int accountId, @AttachToType int attachToType, int attachToDbid, @NonNull List<? extends AbsModel> models);

    Single<List<Pair<Integer, AbsModel>>> getAttachmentsWithIds(int accountId, @AttachToType int attachToType, int attachToDbid);

    interface IBaseEvent {
        int getAccountId();

        @AttachToType
        int getAttachToType();

        int getAttachToId();
    }

    interface IRemoveEvent extends IBaseEvent {
        int getGeneratedId();
    }

    interface IAddEvent extends IBaseEvent {
        List<Pair<Integer, AbsModel>> getAttachments();
    }

    Observable<IAddEvent> observeAdding();

    Observable<IRemoveEvent> observeRemoving();
}