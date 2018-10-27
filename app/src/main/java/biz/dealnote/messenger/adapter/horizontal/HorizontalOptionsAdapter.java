package biz.dealnote.messenger.adapter.horizontal;

import android.view.View;

import com.google.android.material.chip.Chip;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.base.RecyclerBindableAdapter;

public class HorizontalOptionsAdapter<T extends Entry> extends RecyclerBindableAdapter<T, HorizontalOptionsAdapter.Holder> {

    public HorizontalOptionsAdapter(List<T> data) {
        super(data);
    }

    @Override
    protected void onBindItemViewHolder(Holder holder, int position, int type) {
        final T item = getItem(position);

        String title = item.getTitle(holder.itemView.getContext());
        String targetTitle = title.startsWith("#") ? title : "#" + title;

        holder.chip.setText(targetTitle);
        holder.chip.setChecked(true);

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

        Chip chip;

        Holder(View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.progress_button);
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
