package biz.dealnote.messenger.adapter.multidata;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import biz.dealnote.messenger.model.DataWrapper;

public abstract class MultyDataAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private List<DataWrapper<T>> fullData;
    private int[] titles;

    public MultyDataAdapter(List<DataWrapper<T>> dataWrappers, int[] titles) {
        this.fullData = dataWrappers;
        this.titles = titles;
    }

    public void setData(List<DataWrapper<T>> wrappers, int[] titles) {
        this.fullData = wrappers;
        this.titles = titles;
        notifyDataSetChanged();
    }

    public static class ItemInfo<T> {

        public T item;

        public int internalPosition;

        public int sectionTitleRes;
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (DataWrapper pair : fullData) {
            if (!pair.isEnabled()) {
                continue;
            }

            count = count + pair.size();
        }

        return count;
    }

    protected void get(int adapterPosition, @NonNull ItemInfo<T> info) {
        int offset = 0;

        for (int i = 0; i < fullData.size(); i++) {
            DataWrapper<T> wrapper = fullData.get(i);

            if (!wrapper.isEnabled()) {
                continue;
            }

            int newOffset = offset + wrapper.size();

            if (adapterPosition < newOffset) {
                int internalPosition = adapterPosition - offset;

                info.item = wrapper.get().get(internalPosition);
                info.internalPosition = internalPosition;
                info.sectionTitleRes = this.titles[i];
                return;
            }

            offset = newOffset;
        }

        throw new IllegalArgumentException("Invalid adapter position");
    }

    public void notifyItemRangeInserted(int dataIndex, int internalPosition, int count){
        super.notifyItemRangeInserted(getAdapterPosition(dataIndex, internalPosition), count);
    }

    public void notifyItemRemoved(int dataIndex, int internalPosition){
        super.notifyItemRemoved(getAdapterPosition(dataIndex, internalPosition));
    }

    public void notifyItemChanged(int dataIndex, int internalPosition){
        super.notifyItemChanged(getAdapterPosition(dataIndex, internalPosition));
    }

    public int getAdapterPosition(int dataIndex, int internalPosition){
        int offset = 0;

        for(int i = 0; i < fullData.size(); i++){
            if(i < dataIndex){
                offset = offset + fullData.get(i).size();
            } else {
                break;
            }
        }

        return offset + internalPosition;
    }

    @Deprecated
    protected ItemInfo get(int adapterPosition) {
        ItemInfo<T> info = new ItemInfo<>();
        this.get(adapterPosition, info);
        return info;
    }

    public T getItemAt(int adapterPosition) {
        int offset = 0;

        for (DataWrapper<T> dataWrapper : fullData) {
            if (!dataWrapper.isEnabled()) {
                continue;
            }

            int newOffset = offset + dataWrapper.size();

            if (adapterPosition < newOffset) {
                int internalPosition = adapterPosition - offset;
                return dataWrapper.get().get(internalPosition);
            }

            offset = newOffset;
        }

        throw new IllegalArgumentException("Invalid position");
    }
}