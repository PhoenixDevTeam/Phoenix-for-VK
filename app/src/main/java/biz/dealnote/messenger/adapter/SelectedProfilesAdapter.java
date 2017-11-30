package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.util.EventListener;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.settings.CurrentTheme;

public class SelectedProfilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_CHECK = 0;
    private static final int VIEW_TYPE_USER = 1;

    private Context mContext;
    private List<User> mData;
    private Transformation mTransformation;

    public SelectedProfilesAdapter(Context context, List<User> data) {
        this.mContext = context;
        this.mData = data;
        this.mTransformation = CurrentTheme.createTransformationForAvatar(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_CHECK:
                return new CheckViewHolder(LayoutInflater.from(mContext)
                        .inflate(R.layout.item_selection_check, parent, false));
            case VIEW_TYPE_USER:
                return new ProfileViewHolder(LayoutInflater.from(mContext)
                        .inflate(R.layout.item_selected_user, parent, false));

        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position == 0){
            bindCheckViewHolder((CheckViewHolder) holder);
        } else {
            bindProfileViewHolder((ProfileViewHolder) holder, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_CHECK : VIEW_TYPE_USER;
    }

    private void bindCheckViewHolder(CheckViewHolder holder){
        if(mData.isEmpty()){
            holder.counter.setText(R.string.press_plus_for_add);
        } else {
            holder.counter.setText(String.valueOf(mData.size()));
        }

        holder.root.setOnClickListener(v -> {
            if(mActionListener != null){
                mActionListener.onCheckClick();
            }
        });
    }

    private void bindProfileViewHolder(final ProfileViewHolder holder, int adapterPosition) {
        final User user = mData.get(toDataPosition(adapterPosition));
        holder.name.setText(user.getFirstName());

        PicassoInstance.with()
                .load(user.getPhoto50())
                .transform(mTransformation)
                .into(holder.avatar);

        holder.buttonRemove.setOnClickListener(v -> {
            if (mActionListener != null) {
                mActionListener.onClick(holder.getAdapterPosition(), user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    private ActionListener mActionListener;

    public void setActionListener(ActionListener actionListener) {
        this.mActionListener = actionListener;
    }

    public interface ActionListener extends EventListener {
        void onClick(int adapterPosition, User user);
        void onCheckClick();
    }

    private class CheckViewHolder extends RecyclerView.ViewHolder {

        TextView counter;
        View root;

        CheckViewHolder(View itemView) {
            super(itemView);
            this.counter = itemView.findViewById(R.id.counter);
            this.root = itemView.findViewById(R.id.root);
        }
    }

    public int toAdapterPosition(int dataPosition){
        return dataPosition + 1;
    }

    public int toDataPosition(int adapterPosition){
        return adapterPosition - 1;
    }

    public void notifyHeaderChange(){
        notifyItemChanged(0);
    }

    private class ProfileViewHolder extends RecyclerView.ViewHolder {

        ImageView avatar;
        TextView name;
        ImageView buttonRemove;

        ProfileViewHolder(View itemView) {
            super(itemView);
            this.avatar = itemView.findViewById(R.id.avatar);
            this.name = itemView.findViewById(R.id.name);
            this.buttonRemove = itemView.findViewById(R.id.button_remove);
            this.buttonRemove.setColorFilter(CurrentTheme.getIconColorStatic(mContext), PorterDuff.Mode.MULTIPLY);

            View root = itemView.findViewById(R.id.root);
            root.getBackground().setColorFilter(CurrentTheme.getMessageBackgroundSquare(mContext), PorterDuff.Mode.MULTIPLY);
            //root.getBackground().setAlpha(180);
        }
    }
}
