package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.EventListener;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.base.RecyclerBindableAdapter;
import biz.dealnote.messenger.model.LogEvent;
import biz.dealnote.messenger.model.LogEventWrapper;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.AppTextUtils;

import static biz.dealnote.messenger.util.Utils.safeLenghtOf;

/**
 * Created by Ruslan Kolbasa on 26.04.2017.
 * phoenix
 */
public class LogsAdapter extends RecyclerBindableAdapter<LogEventWrapper, LogsAdapter.Holder> {

    private final ActionListener actionListener;

    public LogsAdapter(List<LogEventWrapper> data, ActionListener actionListener) {
        super(data);
        this.actionListener = actionListener;
    }

    private static final int MAX_BODY_LENGHT = 400;

    @Override
    protected void onBindItemViewHolder(Holder holder, int position, int type) {
        LogEventWrapper wrapper = getItem(position);
        LogEvent event = wrapper.getEvent();

        holder.body.setText(event.getBody());
        holder.tag.setText(event.getTag());

        long unixtime = event.getDate() / 1000;
        holder.datetime.setText(AppTextUtils.getDateFromUnixTime(unixtime));

        holder.buttonShare.setOnClickListener(v -> actionListener.onShareClick(wrapper));

        holder.bodyRoot.setOnClickListener(v -> {
            if(!canReduce(event.getBody())){
                return;
            }

            wrapper.setExpanded(!wrapper.isExpanded());
            notifyItemChanged(position + getHeadersCount());
            //setupBodyRoot(holder, wrapper);
        });

        setupBodyRoot(holder, wrapper);
    }

    private boolean canReduce(String body){
        return safeLenghtOf(body) > MAX_BODY_LENGHT;
    }

    private void setupBodyRoot(Holder holder, LogEventWrapper wrapper){
        String body = wrapper.getEvent().getBody();

        boolean canReduce = canReduce(body);

        if(!canReduce || wrapper.isExpanded()){
            holder.buttonExpand.setVisibility(View.GONE);
            holder.body.setText(body);
        } else {
            holder.buttonExpand.setVisibility(View.VISIBLE);
            holder.body.setText(AppTextUtils.reduceText(body, MAX_BODY_LENGHT));
        }
    }

    @Override
    protected Holder viewHolder(View view, int type) {
        return new Holder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_log;
    }

    public interface ActionListener extends EventListener {
        void onShareClick(LogEventWrapper wrapper);
    }

    static class Holder extends RecyclerView.ViewHolder {

        TextView tag;
        TextView datetime;
        TextView body;

        View buttonShare;

        View bodyRoot;
        View buttonExpand;

        Holder(View itemView) {
            super(itemView);
            this.tag = (TextView) itemView.findViewById(R.id.log_tag);
            this.datetime = (TextView) itemView.findViewById(R.id.log_datetime);
            this.body = (TextView) itemView.findViewById(R.id.log_body);

            this.buttonShare = itemView.findViewById(R.id.log_button_share);

            this.bodyRoot = itemView.findViewById(R.id.log_body_root);
            this.buttonExpand = itemView.findViewById(R.id.log_button_expand);

            Context context = itemView.getContext();

            ImageView icon = (ImageView) itemView.findViewById(R.id.log_icon);
            icon.getBackground().setColorFilter(CurrentTheme.getColorPrimary(context), PorterDuff.Mode.MULTIPLY);
            icon.setColorFilter(CurrentTheme.getIconColorOnColoredBackgroundCode(context));
        }
    }

}
