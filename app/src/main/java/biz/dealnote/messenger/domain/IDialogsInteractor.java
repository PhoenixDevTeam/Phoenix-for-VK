package biz.dealnote.messenger.domain;

import biz.dealnote.messenger.model.Chat;
import io.reactivex.Single;

/**
 * Created by admin on 19.03.2017.
 * phoenix
 */
public interface IDialogsInteractor {
    Single<Chat> getChatById(int accountId, int peerId);
}