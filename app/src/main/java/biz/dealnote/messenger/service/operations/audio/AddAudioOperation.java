package biz.dealnote.messenger.service.operations.audio;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class AddAudioOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        Audio audio = (Audio) request.getParcelable(EXTRA_AUDIO);

        Integer groupId = request.contains(Extra.GROUP_ID) ? request.getInt(Extra.GROUP_ID) : null;
        Integer albumId = request.contains(Extra.ALBUM_ID) ? request.getInt(Extra.ALBUM_ID) : null;

        int resultId = Apis.get()
                .vkDefault(accountId)
                .audio()
                .add(audio.getId(), audio.getOwnerId(), groupId, albumId)
                .blockingGet();

        /*try {
            VKApiAudio clone = audio.clone();
            clone.id = resultId;
            clone.owner_id = accountId;
            clone.album_id = albumId == null ? 0 : albumId;
            if (groupId != null) {
                clone.owner_id = -Math.abs(groupId);
            }

            ContentValues cv = AudiosColumns.getCV(clone);

            context.getContentResolver().insert(MessengerContentProvider.getAudiosContentUriFor(accountId), cv);
        } catch (CloneNotSupportedException ignore) {
        }*/

        return buildSimpleSuccessResult(resultId != 0);
    }
}
