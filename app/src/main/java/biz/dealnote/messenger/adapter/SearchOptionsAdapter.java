package biz.dealnote.messenger.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.base.RecyclerBindableAdapter;
import biz.dealnote.messenger.fragment.search.options.BaseOption;
import biz.dealnote.messenger.fragment.search.options.DatabaseOption;
import biz.dealnote.messenger.fragment.search.options.SimpleBooleanOption;
import biz.dealnote.messenger.fragment.search.options.SimpleNumberOption;
import biz.dealnote.messenger.fragment.search.options.SimpleTextOption;
import biz.dealnote.messenger.fragment.search.options.SpinnerOption;

public class SearchOptionsAdapter extends RecyclerBindableAdapter<BaseOption, RecyclerView.ViewHolder> {

    public SearchOptionsAdapter(List<BaseOption> items) {
        super(items);
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position, int type) {
        BaseOption option = getItem(position);

        switch (type){
            case TYPE_NORMAL:
                NormalHolder normalHolder = (NormalHolder) viewHolder;

                if(option instanceof SimpleNumberOption){
                    bindSimpleNumberHolder((SimpleNumberOption) option, normalHolder);
                }

                if(option instanceof SpinnerOption){
                    bindSpinnerHolder((SpinnerOption)option, normalHolder);

                }

                if(option instanceof SimpleTextOption){
                    bindSimpleTextHolder((SimpleTextOption) option, normalHolder);
                }

                if(option instanceof DatabaseOption){
                    bindDatabaseHolder((DatabaseOption)option, normalHolder);
                }

                break;
            case TYPE_BOOLEAN:
                SimpleBooleanHolder simpleBooleanHolder = (SimpleBooleanHolder) viewHolder;
                bindSimpleBooleanHolder((SimpleBooleanOption)option, simpleBooleanHolder);
                break;
        }
    }

    private void bindDatabaseHolder(final DatabaseOption option, final NormalHolder holder){
        holder.title.setText(option.title);
        holder.value.setText(option.value == null ? null : option.value.title);
        holder.delete.setVisibility(option.value == null ? View.INVISIBLE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if(mOptionClickListener != null){
                mOptionClickListener.onDatabaseOptionClick(option);
            }
        });

        holder.delete.setOnClickListener(v -> {
            holder.value.setText(null);
            holder.delete.setVisibility(View.INVISIBLE);
            option.value = null;

            if(mOptionClickListener != null){
                mOptionClickListener.onOptionCleared(option);
            }
        });
    }

    private void bindSimpleBooleanHolder(final SimpleBooleanOption option, SimpleBooleanHolder holder){
        holder.checkableView.setText(option.title);

        holder.checkableView.setOnCheckedChangeListener(null);
        holder.checkableView.setChecked(option.checked);

        holder.checkableView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            option.checked = isChecked;
            if(mOptionClickListener != null){
                mOptionClickListener.onSimpleBooleanOptionChanged(option);
            }
        });
    }

    private void bindSpinnerHolder(final SpinnerOption option, final NormalHolder holder){
        if(option.value == null){
            holder.value.setText(null);
        } else {
            holder.value.setText(option.value.name);
        }

        holder.delete.setVisibility(option.value == null ? View.INVISIBLE : View.VISIBLE);

        holder.title.setText(option.title);
        holder.itemView.setOnClickListener(v -> {
            if(mOptionClickListener != null){
                mOptionClickListener.onSpinnerOptionClick(option);
            }
        });

        holder.delete.setOnClickListener(v -> {
            holder.value.setText(null);
            holder.delete.setVisibility(View.INVISIBLE);
            option.value = null;

            if(mOptionClickListener != null){
                mOptionClickListener.onOptionCleared(option);
            }
        });
    }

    private void bindSimpleNumberHolder(final SimpleNumberOption option, final NormalHolder holder){
        holder.value.setText(option.value == null ? null : String.valueOf(option.value));
        holder.title.setText(option.title);
        holder.delete.setVisibility(option.value == null ? View.INVISIBLE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if(mOptionClickListener != null){
                mOptionClickListener.onSimpleNumberOptionClick(option);
            }
        });

        holder.delete.setOnClickListener(v -> {
            holder.value.setText(null);
            holder.delete.setVisibility(View.INVISIBLE);
            option.value = null;

            if(mOptionClickListener != null){
                mOptionClickListener.onOptionCleared(option);
            }
        });
    }

    private void bindSimpleTextHolder(final SimpleTextOption option, final NormalHolder holder){
        holder.value.setText(option.value == null ? null : String.valueOf(option.value));
        holder.title.setText(option.title);

        holder.delete.setVisibility(TextUtils.isEmpty(option.value) ? View.INVISIBLE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if(mOptionClickListener != null){
                mOptionClickListener.onSimpleTextOptionClick(option);
            }
        });

        holder.delete.setOnClickListener(v -> {
            holder.value.setText(null);
            holder.delete.setVisibility(View.INVISIBLE);
            option.value = null;

            if(mOptionClickListener != null){
                mOptionClickListener.onOptionCleared(option);
            }
        });
    }

    @Override
    protected RecyclerView.ViewHolder viewHolder(View view, int type) {
        switch (type){
            case TYPE_NORMAL:
                return new NormalHolder(view);
            case TYPE_BOOLEAN:
                return new SimpleBooleanHolder(view);
        }

        return null;
    }

    public class NormalHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView value;
        ImageView delete;

        NormalHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            value = itemView.findViewById(R.id.value);
            delete = itemView.findViewById(R.id.delete);
        }
    }

    public class SimpleBooleanHolder extends RecyclerView.ViewHolder {

        SwitchCompat checkableView;

        SimpleBooleanHolder(View itemView) {
            super(itemView);
            checkableView = itemView.findViewById(R.id.switchcompat);
        }
    }

    @Override
    protected int layoutId(int type) {
        switch (type){
            case TYPE_NORMAL:
                return R.layout.item_search_option_text;
            case TYPE_BOOLEAN:
                return R.layout.item_search_option_checkbox;
        }

        return 0;
    }

    @Override
    protected int getItemType(int position) {
        BaseOption option = getItem(position - getHeadersCount());

        if(option instanceof SimpleNumberOption
                || option instanceof SimpleTextOption
                || option instanceof SpinnerOption
                || option instanceof DatabaseOption){
            return TYPE_NORMAL;
        }

        if(option instanceof SimpleBooleanOption){
            return TYPE_BOOLEAN;
        }

        return -1;
    }

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_BOOLEAN = 1;

    private OptionClickListener mOptionClickListener;

    public void setOptionClickListener(OptionClickListener optionClickListener) {
        this.mOptionClickListener = optionClickListener;
    }

    public interface OptionClickListener {
        void onSpinnerOptionClick(SpinnerOption spinnerOption);
        void onDatabaseOptionClick(DatabaseOption databaseOption);
        void onSimpleNumberOptionClick(SimpleNumberOption option);
        void onSimpleTextOptionClick(SimpleTextOption option);
        void onSimpleBooleanOptionChanged(SimpleBooleanOption option);
        void onOptionCleared(BaseOption option);
    }
}