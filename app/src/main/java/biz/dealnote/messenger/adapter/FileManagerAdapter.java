package biz.dealnote.messenger.adapter;

import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.FileItem;
import biz.dealnote.messenger.settings.CurrentTheme;

public class FileManagerAdapter extends RecyclerView.Adapter<FileManagerAdapter.Holder> {

    private List<FileItem> data;

    public FileManagerAdapter(List<FileItem> data) {
        this.data = data;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        final FileItem item = data.get(position);
        holder.icon.setImageResource(item.icon);
        holder.fileName.setText(item.file);
        holder.fileDetails.setText(item.details);
        holder.fileDetails.setVisibility(TextUtils.isEmpty(item.details) ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if(clickListener != null){
                clickListener.onClick(holder.getAdapterPosition(), item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView fileName;
        TextView fileDetails;
        ImageView icon;

        public Holder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.item_file_name);
            fileDetails = itemView.findViewById(R.id.item_file_details);
            icon = itemView.findViewById(R.id.item_file_icon);

            icon.getBackground().setColorFilter(CurrentTheme.getIconColorActive(itemView.getContext()), PorterDuff.Mode.MULTIPLY);
        }
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    private ClickListener clickListener;

    public interface ClickListener {
        void onClick(int position, FileItem item);
    }
}
