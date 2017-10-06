package biz.dealnote.messenger.view.steppers.base;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import biz.dealnote.messenger.adapter.holder.SharedHolders;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;

/**
 * Created by ruslan.kolbasa on 09.08.2016.
 * mobilebankingandroid
 */
public abstract class AbsSteppersVerticalAdapter<H extends AbsStepsHost> extends RecyclerView.Adapter<AbsStepHolder<H>> {

    private final H mHost;
    private SharedHolders<AbsStepHolder<H>> mSharedHolders;

    public AbsSteppersVerticalAdapter(@NonNull H host, @NonNull BaseHolderListener actionListener) {
        this.mHost = host;
        this.mSharedHolders = new SharedHolders<>(false);
        this.mActionListener = actionListener;
    }

    @Override
    public AbsStepHolder<H> onCreateViewHolder(ViewGroup parent, int viewType) {
        return createHolderForStep(parent, mHost, viewType);
    }

    @Nullable
    private AbsStepHolder<H> findHolderByStepIndex(int step){
        return mSharedHolders.findOneByEntityId(step);
    }

    public abstract AbsStepHolder<H> createHolderForStep(ViewGroup parent, H host, int step);

    @Override
    public void onBindViewHolder(final AbsStepHolder<H> holder, final int position) {
        mSharedHolders.put(position, holder);

        holder.counterText.setText(String.valueOf(position + 1));

        boolean isCurrent = mHost.getCurrentStep() == position;
        boolean isLast = position == getItemCount() - 1;
        boolean isActive = position <= mHost.getCurrentStep();

        int activeColor = CurrentTheme.getIconColorActive(holder.itemView.getContext());
        int inactiveColor = Color.parseColor("#2cb1b1b1");
        int tintColor = isActive ? activeColor : inactiveColor;

        holder.counterRoot.setEnabled(isCurrent);
        holder.counterRoot.getBackground().setColorFilter(tintColor, PorterDuff.Mode.MULTIPLY);
        holder.contentRoot.setVisibility(isCurrent ? View.VISIBLE : View.GONE);

        holder.line.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);

        holder.buttonNext.setText(mHost.getNextButtonText(position));
        holder.buttonNext.setOnClickListener(v -> mActionListener.onNextButtonClick(holder.getAdapterPosition()));

        holder.buttonCancel.setText(mHost.getCancelButtonText(position));
        holder.buttonCancel.setOnClickListener(v -> mActionListener.onCancelButtonClick(holder.getAdapterPosition()));

        holder.titleText.setText(mHost.getStepTitle(position));
        holder.titleText.setTypeface(Typeface.create(isCurrent ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));

        holder.bindInternalStepViews(mHost);
        holder.setNextButtonAvailable(mHost.canMoveNext(position));

        int px16dp = (int) Utils.dpToPx(16f, holder.itemView.getContext());
        holder.itemView.setPadding(0, 0, 0, position == getItemCount() - 1 ? px16dp : 0);
    }

    public void updateNextButtonAvailability(int step){
        AbsStepHolder<H> holder = findHolderByStepIndex(step);
        if(Objects.nonNull(holder)){
            holder.setNextButtonAvailable(mHost.canMoveNext(step));
        }
    }

    @Override
    public int getItemCount() {
        return mHost.getStepsCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private BaseHolderListener mActionListener;
}