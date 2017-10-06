package biz.dealnote.messenger.model;

import java.util.ArrayList;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public class UsersPart {

    public ArrayList<User> users;
    public int titleResId;
    public boolean enable;
    public Integer displayCount;

    public UsersPart(int titleResId, ArrayList<User> users, boolean enable) {
        this.titleResId = titleResId;
        this.users = users;
        this.enable = enable;
    }
}