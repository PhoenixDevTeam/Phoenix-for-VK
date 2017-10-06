package biz.dealnote.messenger.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.DrawerCategory;

public class DrawerCategotiesAdapter extends RecyclerView.Adapter<DrawerCategotiesAdapter.ViewHolder> {

    private List<DrawerCategory> data;

    public DrawerCategotiesAdapter(List<DrawerCategory> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drawer_category, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DrawerCategory category = data.get(position);

        holder.checkBox.setText(category.getTitle());
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(category.isChecked());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> category.setChecked(isChecked));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<DrawerCategory> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.item_drawer_category_check);
        }
    }
}