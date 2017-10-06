package biz.dealnote.messenger.settings;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import biz.dealnote.messenger.model.drawer.AbsDrawerItem;
import biz.dealnote.messenger.model.drawer.RecentChat;

import static biz.dealnote.messenger.util.Utils.safeIsEmpty;

/**
 * Created by admin on 01.12.2016.
 * phoenix
 */
class RecentChatsSettings implements ISettings.IRecentChats {

    private final Context app;

    private Gson gson;

    RecentChatsSettings(Context app) {
        this.app = app.getApplicationContext();
        this.gson = new Gson();
    }

    @Override
    public List<RecentChat> get(int acountid) {
        List<RecentChat> recentChats = new ArrayList<>();

        Set<String> stringSet = PreferenceManager.getDefaultSharedPreferences(app)
                .getStringSet(recentChatKeyFor(acountid), null);

        if(!safeIsEmpty(stringSet)){
            for(String s : stringSet){
                try {
                    RecentChat recentChat = gson.fromJson(s, RecentChat.class);
                    recentChats.add(recentChat);
                } catch (Exception ignored){}
            }
        }

        return recentChats;
    }

    @Override
    public void store(int accountid, List<RecentChat> chats) {
        Set<String> target = new LinkedHashSet<>();
        for(AbsDrawerItem item : chats){
            if(item instanceof RecentChat){

                if(((RecentChat) item).getAid() != accountid) continue;

                target.add(gson.toJson(item));
            }
        }

        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putStringSet(recentChatKeyFor(accountid), target)
                .apply();
    }

    private static String recentChatKeyFor(int aid) {
        return "recent" + aid;
    }
}
