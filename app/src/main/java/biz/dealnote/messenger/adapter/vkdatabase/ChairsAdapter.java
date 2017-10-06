package biz.dealnote.messenger.adapter.vkdatabase;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.database.Chair;

public class ChairsAdapter extends RecyclerView.Adapter<ChairsAdapter.Holder> {

    private Context mContext;
    private List<Chair> mData;

    public ChairsAdapter(Context mContext, List<Chair> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_country, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        final Chair chair = mData.get(position);

        holder.name.setText(chair.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if(mListener != null){
                mListener.onClick(chair);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView name;

        public Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
        }
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    private Listener mListener;

    public interface Listener {
        void onClick(Chair chair);
    }
}