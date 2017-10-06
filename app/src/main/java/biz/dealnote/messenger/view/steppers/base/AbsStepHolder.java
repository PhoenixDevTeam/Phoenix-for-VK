package biz.dealnote.messenger.view.steppers.base;

import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.holder.IdentificableHolder;

/**
 * Created by ruslan.kolbasa on 09.08.2016.
 * mobilebankingandroid
 */
public abstract class AbsStepHolder<T extends AbsStepsHost> extends RecyclerView.ViewHolder implements IdentificableHolder {

    private static int nextHolderId;

    public final int index;
    public View counterRoot;
    public TextView counterText;
    public TextView titleText;
    public View line;
    public View contentRoot;
    public ViewGroup content;
    public AppCompatButton buttonNext;
    public Button buttonCancel;
    protected View mContentView;

    public AbsStepHolder(ViewGroup parent, int internalLayoutRes, int stepIndex) {
        super(createVerticalMainHolderView(parent));

        this.index = stepIndex;
        this.counterRoot = itemView.findViewById(R.id.counter_root);
        this.counterText = (TextView) itemView.findViewById(R.id.counter);
        this.titleText = (TextView) itemView.findViewById(R.id.title);
        this.line = itemView.findViewById(R.id.step_line);
        this.buttonNext = (AppCompatButton) itemView.findViewById(R.id.buttonNext);
        this.buttonCancel = (Button) itemView.findViewById(R.id.buttonCancel);
        this.content = (ViewGroup) itemView.findViewById(R.id.content);
        this.contentRoot = itemView.findViewById(R.id.content_root);

        this.mContentView = LayoutInflater.from(itemView.getContext()).inflate(internalLayoutRes, parent, false);
        this.content.addView(mContentView);

        initInternalView(mContentView);
    }

    private static View createVerticalMainHolderView(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_step, parent, false);
        itemView.setTag(generateHolderId());
        return itemView;
    }

    @Override
    public int getHolderId() {
        return (int) itemView.getTag();
    }

    public abstract void initInternalView(View contentView);

    public final void bindInternalStepViews(T host) {
        bindViews(host);
    }

    protected abstract void bindViews(T host);

    public void setNextButtonAvailable(boolean enable) {
        buttonNext.setEnabled(enable);
    }

    private static int generateHolderId(){
        nextHolderId++;
        return nextHolderId;
    }
}