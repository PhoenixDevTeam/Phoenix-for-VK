package biz.dealnote.messenger.adapter.horizontal;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.base.RecyclerBindableAdapter;
import biz.dealnote.messenger.view.RoundedButton;

public class HorizontalOptionsAdapter<T extends Entry> extends RecyclerBindableAdapter<T, HorizontalOptionsAdapter.Holder> {

    public HorizontalOptionsAdapter(List<T> data) {
        super(data);
    }

    @Override
    protected void onBindItemViewHolder(Holder holder, int position, int type) {
        final T item = getItem(position);

        String title = item.getTitle(holder.itemView.getContext());
        String targetTitle = title.startsWith("#") ? title : "#" + title;

        holder.button.setText(targetTitle);
        holder.button.setActive(item.isActive());

        holder.itemView.setOnClickListener(v -> listener.onOptionClick(item));
    }

    @Override
    protected Holder viewHolder(View view, int type) {
        return new Holder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_round_button;
    }

    static class Holder extends RecyclerView.ViewHolder {

        RoundedButton button;

        Holder(View itemView) {
            super(itemView);
            button = (RoundedButton) itemView.findViewById(R.id.button);
        }
    }

    private Listener<T> listener;

    public void setListener(Listener<T> listener) {
        this.listener = listener;
    }

    public interface Listener<T extends Entry> {
        void onOptionClick(T entry);
    }
}
