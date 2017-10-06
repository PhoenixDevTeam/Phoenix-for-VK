package biz.dealnote.messenger.settings;

import biz.dealnote.messenger.Injection;

/**
 * Created by admin on 01.12.2016.
 * phoenix
 */
public class Settings {

    public static ISettings get(){
        return Injection.provideSettings();
    }

}
