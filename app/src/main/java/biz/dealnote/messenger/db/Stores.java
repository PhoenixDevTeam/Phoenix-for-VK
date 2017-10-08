package biz.dealnote.messenger.db;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.db.interfaces.IStores;

public class Stores {

    public static IStores getInstance(){
        return Injection.provideStores();
    }

}
