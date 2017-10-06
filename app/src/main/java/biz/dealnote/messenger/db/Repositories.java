package biz.dealnote.messenger.db;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.db.interfaces.IRepositories;

public class Repositories  {

    public static IRepositories getInstance(){
        return Injection.provideRepositories();
    }

}
