package biz.dealnote.messenger.adapter.horizontal;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.base.RecyclerBindableAdapter;
import biz.dealnote.messenger.settings.CurrentTheme;

public class HorizontalOptionsAdapter<T extends Entry> extends RecyclerBindableAdapter<T, HorizontalOptionsAdapter.Holder> {

    public HorizontalOptionsAdapter(List<T> data) {
        super(data);
    }

    @Override
    protected void onBindItemViewHolder(Holder holder, int position, int type) {
        final T item = getItem(position);

        String title = item.getTitle(holder.itemView.getContext());
        String targetTitle = title.startsWith("#") ? title : "#" + title;

        Context context = holder.itemView.getContext();
        holder.chip.setText(targetTitle);
        holder.background.setCardBackgroundColor(item.isActive() ?
                CurrentTheme.getColorPrimary(context) : CurrentTheme.getColorSurface(context));

        holder.itemView.setOnClickListener(v -> {
            listener.onOptionClick(item);
        });
    }

    @Override
    protected Holder viewHolder(View view, int type) {
        return new Holder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_chip;
    }

    static class Holder extends RecyclerView.ViewHolder {

        MaterialCardView background;
        TextView chip;

        Holder(View itemView) {
            super(itemView);
            background = itemView.findViewById(R.id.card_view);
            chip = itemView.findViewById(R.id.chip);
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
