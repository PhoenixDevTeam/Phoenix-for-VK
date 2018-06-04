package biz.dealnote.messenger.longpoll;

import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class LongpollEntry {

    private final Longpoll longpoll;
    private final SocketHandler handler;
    private boolean released;
    private final WeakReference<ILongpollManager> managerReference;
    private final int accountId;

    LongpollEntry(Longpoll longpoll, ILongpollManager manager) {
        this.longpoll = longpoll;
        this.accountId = longpoll.getAccountId();
        this.managerReference = new WeakReference<>(manager);
        this.handler = new SocketHandler(this);
    }

    public void connect(){
        longpoll.connect();
        handler.restartPreDestroy();
    }

    public void destroy(){
        handler.release();
        longpoll.shutdown();
        released = true;

        ILongpollManager manager = managerReference.get();
        if(nonNull(manager)){
            manager.notifyDestroy(this);
        }
    }

    public void deferDestroy(){
        handler.restartPreDestroy();
    }

    public int getAccountId() {
        return accountId;
    }

    private void firePreDestroy() {
        ILongpollManager manager = managerReference.get();
        if(nonNull(manager)){
            manager.notifyPreDestroy(this);
        }
    }

    private static final class SocketHandler extends android.os.Handler {

        final static int PRE_DESTROY = 2;
        final static int DESTROY = 3;

        final WeakReference<LongpollEntry> reference;

        SocketHandler(LongpollEntry holder) {
            super(Looper.getMainLooper());
            this.reference = new WeakReference<>(holder);
        }

        void restartPreDestroy() {
            removeMessages(PRE_DESTROY);
            removeMessages(DESTROY);
            sendEmptyMessageDelayed(PRE_DESTROY, 30_000L);
        }

        void postDestroy() {
            sendEmptyMessageDelayed(DESTROY, 30_000L);
        }

        void release(){
            removeMessages(PRE_DESTROY);
            removeMessages(DESTROY);
        }

        @Override
        public void handleMessage(Message msg) {
            LongpollEntry holder = reference.get();
            if (holder != null && !holder.released) {
                switch (msg.what) {
                    case PRE_DESTROY:
                        postDestroy();
                        holder.firePreDestroy();
                        break;

                    case DESTROY:
                        holder.destroy();
                        break;
                }
            }
        }
    }
}