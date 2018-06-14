package biz.dealnote.messenger.db;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.db.interfaces.IStorages;

public class Stores {

    public static IStorages getInstance(){
        return Injection.provideStores();
    }

}
