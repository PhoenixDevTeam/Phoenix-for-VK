package biz.dealnote.messenger.domain;

import android.support.annotation.NonNull;

import java.util.Map;

import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Privacy;
import biz.dealnote.messenger.model.SimplePrivacy;
import biz.dealnote.messenger.util.Optional;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 18.09.2017.
 * phoenix
 */
public interface IUtilsInteractor {
    Single<Map<Integer, Privacy>> createFullPrivacies(int accountId, @NonNull Map<Integer, SimplePrivacy> orig);
    Single<Optional<Owner>> resolveDomain(final int accountId, String domain);
}