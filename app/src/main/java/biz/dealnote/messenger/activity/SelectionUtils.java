package biz.dealnote.messenger.activity;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.SelectProfileCriteria;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Utils;

public class SelectionUtils {

    private static final String TAG = SelectionUtils.class.getSimpleName();
    private static final String VIEW_TAG = "SelectionUtils.SelectionView";

    public static void addSelectionProfileSupport(Context context, ViewGroup root, final Object mayBeUser) {
        if (!(context instanceof ProfileSelectable) || root == null) return;

        SelectProfileCriteria criteria = ((ProfileSelectable) context).getAcceptableCriteria();

        boolean canSelect = mayBeUser instanceof User;

        if (canSelect && criteria.isFriendsOnly()) {
            canSelect = ((User) mayBeUser).isFriend();
        }

        final ProfileSelectable callack = (ProfileSelectable) context;
        ImageView selectionView = root.findViewWithTag(VIEW_TAG);

        if (!canSelect && selectionView == null) return;

        if (canSelect && selectionView == null) {
            selectionView = new ImageView(context);
            selectionView.setImageResource(R.drawable.plus);
            selectionView.setTag(VIEW_TAG);
            selectionView.setBackgroundResource(R.drawable.circle_back);
            selectionView.getBackground().setAlpha(150);
            selectionView.setLayoutParams(createLayoutParams(root));

            int dp4px = (int) Utils.dpToPx(4, context);
            selectionView.setPadding(dp4px, dp4px, dp4px, dp4px);

            Logger.d(TAG, "Added new selectionView");
            root.addView(selectionView);
        } else {
            Logger.d(TAG, "Re-use selectionView");
        }

        selectionView.setVisibility(canSelect ? View.VISIBLE : View.GONE);

        if (!canSelect) {
            selectionView.setOnClickListener(null);
        } else {
            selectionView.setOnClickListener(v -> callack.select((User) mayBeUser));
        }
    }

    private static ViewGroup.LayoutParams createLayoutParams(ViewGroup parent) {
        if (parent instanceof RelativeLayout) {
            int margin = (int) Utils.dpToPx(6, parent.getContext());

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            params.bottomMargin = margin;
            params.leftMargin = margin;
            params.rightMargin = margin;
            params.topMargin = margin;
            return params;
        } else {
            throw new IllegalArgumentException("Not yet impl for parent: " + parent.getClass().getSimpleName());
        }
    }
}
