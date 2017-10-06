package biz.dealnote.messenger.view.steppers.base;

import java.util.EventListener;

/**
 * Created by ruslan.kolbasa on 29.08.2016.
 * mobilebankingandroid
 */
public interface BaseHolderListener extends EventListener {
    void onNextButtonClick(int step);
    void onCancelButtonClick(int step);
}