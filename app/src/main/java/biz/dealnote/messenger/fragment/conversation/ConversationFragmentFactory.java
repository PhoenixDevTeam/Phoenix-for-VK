package biz.dealnote.messenger.fragment.conversation;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.model.VKApiAttachment;

public class ConversationFragmentFactory {

    public static Fragment newInstance(Bundle args){
        String type = args.getString(Extra.TYPE);
        if(type == null){
            throw new IllegalArgumentException("Type cant bee null");
        }

        Fragment fragment = null;
        switch (type){
            case VKApiAttachment.TYPE_PHOTO:
                fragment = new ConversationPhotosFragment();
                break;
            case VKApiAttachment.TYPE_VIDEO:
                fragment = new ConversationVideosFragment();
                break;
            case VKApiAttachment.TYPE_DOC:
                fragment = new ConversationDocsFragment();
                break;
            case VKApiAttachment.TYPE_AUDIO:
                fragment = new ConversationAudiosFragment();
                break;
        }

        if(fragment != null){
            fragment.setArguments(args);
        }

        return fragment;
    }

    public static Bundle buildArgs(int accountId, int peerId, String type){
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        bundle.putInt(Extra.PEER_ID, peerId);
        bundle.putString(Extra.TYPE, type);
        return bundle;
    }
}
