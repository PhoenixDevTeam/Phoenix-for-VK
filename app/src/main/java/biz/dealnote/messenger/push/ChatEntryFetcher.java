package biz.dealnote.messenger.push;

import android.content.Context;
import android.graphics.Bitmap;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.util.Optional;
import io.reactivex.Single;

public class ChatEntryFetcher {

    public static Single<DialogInfo> getRx(Context context, int accountId, int peerId) {
        final Context app = context.getApplicationContext();

        switch (Peer.getType(peerId)) {
            case Peer.USER:
            case Peer.GROUP:
                int ownerId = Peer.toOwnerId(peerId);

                return OwnerInfo.getRx(app, accountId, ownerId)
                        .map(info -> {
                            Owner owner = info.getOwner();

                            DialogInfo response = new DialogInfo();
                            response.title = owner.getFullName();
                            response.img = owner.get100photoOrSmaller();
                            response.icon = info.getAvatar();
                            response.last_seen = owner instanceof User ? ((User) owner).getLastSeen() : 0L;
                            return response;
                        });
                case Peer.CHAT:
                    return InteractorFactory.createDialogsInteractor()
                            .getChatById(accountId, peerId)
                            .flatMap(chat -> NotificationUtils.loadRoundedImageRx(app, chat.get100orSmallerAvatar(), R.drawable.ic_group_chat)
                                    .map(Optional::wrap)
                                    .onErrorReturnItem(Optional.empty())
                                    .map(optional -> {
                                        DialogInfo response = new DialogInfo();
                                        response.title = chat.getTitle();
                                        response.img = chat.get100orSmallerAvatar();
                                        response.icon = optional.get();
                                        return response;
                                    }));
        }

        throw new UnsupportedOperationException();
    }

    public static class DialogInfo {
        public String title;
        public String img;
        public Bitmap icon;
        public long last_seen;
    }
}