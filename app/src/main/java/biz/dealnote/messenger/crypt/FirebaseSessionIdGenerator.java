package biz.dealnote.messenger.crypt;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import biz.dealnote.messenger.util.Objects;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 12.07.2017.
 * phoenix
 */
public class FirebaseSessionIdGenerator implements ISessionIdGenerator {

    @Override
    public Single<Long> generateNextId() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance("https://phoenix-for-vk.firebaseio.com/");
        final DatabaseReference ref = database.getReference();
        final DatabaseReference databaseCounter = ref.child("key_exchange_session_counter");

        //https://stackoverflow.com/questions/28915706/auto-increment-a-value-in-firebase
        return Single.create(emitter -> databaseCounter.runTransaction(new Transaction.Handler() {

            long nextValue;

            @Override
            public Transaction.Result doTransaction(final MutableData currentData) {
                if (currentData.getValue() == null) {
                    nextValue = 1;
                } else {
                    nextValue = (Long) currentData.getValue() + 1;
                }

                currentData.setValue(nextValue);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError e, boolean committed, DataSnapshot currentData) {
                if (Objects.nonNull(e)) {
                    emitter.onError(new SessionIdGenerationException(e.getMessage()));
                } else {
                    emitter.onSuccess(nextValue);
                }
            }
        }));
    }
}