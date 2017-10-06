package biz.dealnote.messenger.adapter.vkdatabase;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.City;

public class CitiesAdapter extends RecyclerView.Adapter<CitiesAdapter.Holder> {

    private Context mContext;
    private List<City> mData;

    public CitiesAdapter(Context mContext, List<City> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_city, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        final City city = mData.get(position);
        holder.title.setText(city.getTitle());
        holder.title.setTypeface(null, city.isImportant() ? Typeface.BOLD : Typeface.NORMAL);

        holder.region.setText(city.getRegion());
        holder.region.setVisibility(TextUtils.isEmpty(city.getRegion()) ? View.GONE : View.VISIBLE);

        holder.area.setText(city.getArea());
        holder.area.setVisibility(TextUtils.isEmpty(city.getArea()) ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if(mListener != null){
                mListener.onClick(city);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView title;
        TextView area;
        TextView region;

        public Holder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            area = itemView.findViewById(R.id.area);
            region = itemView.findViewById(R.id.region);
        }
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    private Listener mListener;

    public interface Listener {
        void onClick(City country);
    }
}
