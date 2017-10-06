package biz.dealnote.messenger.longpoll;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.api.model.longpoll.AddMessageUpdate;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;

public class FullAndNonFullUpdates {

    private List<AddMessageUpdate> full;
    private List<Integer> nonFull;

    @NonNull
    public List<AddMessageUpdate> prepareFull(){
        if(Objects.isNull(full)){
            full = new ArrayList<>(1);
        }

        return full;
    }

    @NonNull
    public List<Integer> prepareNonFull(){
        if(Objects.isNull(nonFull)){
            nonFull = new ArrayList<>(1);
        }

        return nonFull;
    }

    public List<AddMessageUpdate> getFullMessages() {
        return full;
    }

    public List<Integer> getNonFull() {
        return nonFull;
    }

    public boolean hasFullMessages(){
        return !Utils.safeIsEmpty(full);
    }

    public boolean hasNonFullMessages(){
        return !Utils.safeIsEmpty(nonFull);
    }

    @Override
    public String toString() {
        return "FullAndNonFullUpdates[" +
                "full=" + (full == null ? 0 : full.size()) +
                ", nonFull=" + (nonFull == null ? 0 : nonFull.size()) + ']';
    }
}
