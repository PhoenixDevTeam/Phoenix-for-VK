package biz.dealnote.messenger.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.base.RecyclerBindableAdapter;
import biz.dealnote.messenger.model.ProxyConfig;
import biz.dealnote.messenger.util.Utils;

/**
 * Created by admin on 10.07.2017.
 * phoenix
 */
public class ProxiesAdapter extends RecyclerBindableAdapter<ProxyConfig, ProxiesAdapter.Holder> {

    private final ActionListener actionListener;

    public ProxiesAdapter(List<ProxyConfig> data, ActionListener actionListener) {
        super(data);
        this.actionListener = actionListener;
    }

    @Override
    protected void onBindItemViewHolder(Holder holder, int position, int type) {
        ProxyConfig config = getItem(position);

        boolean isActive = config.equals(active);

        holder.address.setText(config.getAddress());
        holder.port.setText(String.valueOf(config.getPort()));
        holder.username.setText(config.getUser());

        StringBuilder pass = new StringBuilder();
        if(Utils.nonEmpty(config.getPass())){
            for(int i = 0; i < config.getPass().length(); i++){
                pass.append("*");
            }
        }

        holder.pass.setText(pass.toString());

        holder.setAsActive.setOnClickListener(v -> actionListener.onSetAtiveClick(config));
        holder.delete.setOnClickListener(v -> actionListener.onDeleteClick(config));
        holder.disable.setOnClickListener(v -> actionListener.onDisableClick(config));

        holder.disable.setVisibility(isActive ? View.VISIBLE : View.GONE);
        holder.setAsActive.setVisibility(isActive ? View.GONE : View.VISIBLE);
        holder.delete.setVisibility(isActive ? View.GONE : View.VISIBLE);
    }

    private ProxyConfig active;

    public void setActive(ProxyConfig active) {
        this.active = active;
        notifyDataSetChanged();
    }

    public void setData(List<ProxyConfig> data, ProxyConfig config){
        super.setItems(data, false);
        this.active = config;
        notifyDataSetChanged();
    }

    public interface ActionListener {
        void onDeleteClick(ProxyConfig config);
        void onSetAtiveClick(ProxyConfig config);
        void onDisableClick(ProxyConfig config);
    }

    @Override
    protected Holder viewHolder(View view, int type) {
        return new Holder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_proxy;
    }

    static class Holder extends RecyclerView.ViewHolder {

        TextView address;
        TextView port;
        TextView username;
        TextView pass;

        View delete;
        View setAsActive;
        View disable;

        Holder(View itemView) {
            super(itemView);
            address = (TextView) itemView.findViewById(R.id.address);
            port = (TextView) itemView.findViewById(R.id.port);
            username = (TextView) itemView.findViewById(R.id.username);
            pass = (TextView) itemView.findViewById(R.id.password);

            delete = itemView.findViewById(R.id.button_delete);
            setAsActive = itemView.findViewById(R.id.button_set_as_active);
            disable = itemView.findViewById(R.id.button_disable);
        }
    }
}