package biz.dealnote.messenger.db.interfaces;

import java.util.List;

import biz.dealnote.messenger.model.LogEvent;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 26.04.2017.
 * phoenix
 */
public interface ILogsStore {

    Single<LogEvent> add(int type, String tag, String body);

    Single<List<LogEvent>> getAll(int type);
}
