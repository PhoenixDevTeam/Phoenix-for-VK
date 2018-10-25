package biz.dealnote.messenger.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

@IntDef({LoadMoreState.LOADING, LoadMoreState.INVISIBLE, LoadMoreState.CAN_LOAD_MORE, LoadMoreState.END_OF_LIST})
@Retention(RetentionPolicy.SOURCE)
public @interface LoadMoreState {
    int LOADING = 1;
    int INVISIBLE = 2;
    int CAN_LOAD_MORE = 3;
    int END_OF_LIST = 4;
}
