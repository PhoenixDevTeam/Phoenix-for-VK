package biz.dealnote.messenger.view;

import android.support.annotation.StringRes;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.LoadMoreState;

public class LoadMoreFooterHelper {

    public static LoadMoreFooterHelper createFrom(View view, final Callback callback){
        LoadMoreFooterHelper helper = new LoadMoreFooterHelper();
        helper.holder = new Holder(view);
        helper.callback = callback;
        helper.holder.bLoadMore.setOnClickListener(v -> {
            if(callback != null){
                callback.onLoadMoreClick();
            }
        });
        return helper;
    }

    public Callback callback;
    public Holder holder;
    public int state;

    public static class Holder {

        public View root;
        public View container;
        public ProgressBar progress;
        public View bLoadMore;
        public TextView tvEndOfList;

        public Holder(View root){
            this.root = root;
            container = root.findViewById(R.id.footer_load_more_root);
            progress = (ProgressBar) root.findViewById(R.id.footer_load_more_progress);
            bLoadMore = root.findViewById(R.id.footer_load_more_run);
            tvEndOfList = (TextView) root.findViewById(R.id.footer_load_more_end_of_list);
        }
    }

    public void setEndOfListTextRes(@StringRes int res){
        holder.tvEndOfList.setText(res);
    }

    public void setEndOfListText(String text){
        holder.tvEndOfList.setText(text);
    }

    public void switchToState(@LoadMoreState int state){
        this.state = state;
        holder.container.setVisibility(state == LoadMoreState.INVISIBLE ? View.GONE : View.VISIBLE);

        switch (state){
            case LoadMoreState.LOADING:
                holder.tvEndOfList.setVisibility(View.INVISIBLE);
                holder.bLoadMore.setVisibility(View.INVISIBLE);
                holder.progress.setVisibility(View.VISIBLE);
                break;
            case LoadMoreState.END_OF_LIST:
                holder.progress.setVisibility(View.INVISIBLE);
                holder.bLoadMore.setVisibility(View.INVISIBLE);
                holder.tvEndOfList.setVisibility(View.VISIBLE);
                break;
            case LoadMoreState.CAN_LOAD_MORE:
                holder.tvEndOfList.setVisibility(View.INVISIBLE);
                holder.progress.setVisibility(View.INVISIBLE);
                holder.bLoadMore.setVisibility(View.VISIBLE);
                break;
        }
    }

    public interface Callback {
        void onLoadMoreClick();
    }
}
