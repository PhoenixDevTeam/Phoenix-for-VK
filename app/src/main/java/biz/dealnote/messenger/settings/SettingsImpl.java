package biz.dealnote.messenger.settings;

import android.content.Context;

import static biz.dealnote.messenger.util.Objects.isNull;

/**
 * Created by admin on 01.12.2016.
 * phoenix
 */
public class SettingsImpl implements ISettings {

    private static volatile SettingsImpl instance;
    private IRecentChats recentChats;
    private IDrawerSettings drawerSettings;
    private IPushSettings pushSettings;
    private ISecuritySettings securitySettings;
    private IUISettings iuiSettings;
    private INotificationSettings notificationSettings;
    private IMainSettings mainSettings;
    private IAccountsSettings accountsSettings;
    private IOtherSettings otherSettings;

    private SettingsImpl(Context app){
        this.notificationSettings = new NotificationsPrefs(app);
        this.recentChats = new RecentChatsSettings(app);
        this.drawerSettings = new DrawerSettings(app);
        this.pushSettings = new PushSettings(app);
        this.securitySettings = new SecuritySettings(app);
        this.iuiSettings = new UISettings(app);
        this.mainSettings = new MainSettings(app);
        this.accountsSettings = new AccountsSettings(app);
        this.otherSettings = new OtherSettings(app);
    }

    public static SettingsImpl getInstance(Context context) {
        if(isNull(instance)){
            synchronized (SettingsImpl.class){
                if(isNull(instance)){
                    instance = new SettingsImpl(context.getApplicationContext());
                }
            }
        }

        return instance;
    }

    @Override
    public IRecentChats recentChats() {
        return recentChats;
    }

    @Override
    public IDrawerSettings drawerSettings() {
        return drawerSettings;
    }

    @Override
    public IPushSettings pushSettings() {
        return pushSettings;
    }

    @Override
    public ISecuritySettings security() {
        return securitySettings;
    }

    @Override
    public IUISettings ui() {
        return iuiSettings;
    }

    @Override
    public INotificationSettings notifications() {
        return notificationSettings;
    }

    @Override
    public IMainSettings main() {
        return mainSettings;
    }

    @Override
    public IAccountsSettings accounts() {
        return accountsSettings;
    }

    @Override
    public IOtherSettings other() {
        return otherSettings;
    }
}
