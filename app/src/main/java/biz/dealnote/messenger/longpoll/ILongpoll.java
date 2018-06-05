package biz.dealnote.messenger.longpoll;

public interface ILongpoll {
    int getAccountId();
    void connect();
    void shutdown();
}