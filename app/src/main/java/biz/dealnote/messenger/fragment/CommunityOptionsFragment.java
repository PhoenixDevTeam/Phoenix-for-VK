package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.Day;
import biz.dealnote.messenger.model.GroupSettings;
import biz.dealnote.messenger.model.IdOption;
import biz.dealnote.messenger.mvp.presenter.CommunityOptionsPresenter;
import biz.dealnote.messenger.mvp.view.ICommunityOptionsView;
import biz.dealnote.messenger.util.Month;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.view.MySpinnerView;
import biz.dealnote.mvp.core.IPresenterFactory;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public class CommunityOptionsFragment extends BasePresenterFragment<CommunityOptionsPresenter, ICommunityOptionsView>
        implements ICommunityOptionsView {

    public static CommunityOptionsFragment newInstance(int accountId, Community community, GroupSettings settings) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.GROUP, community);
        args.putParcelable(Extra.SETTINGS, settings);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        CommunityOptionsFragment fragment = new CommunityOptionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private EditText mName;
    private EditText mDescription;
    private View mCommunityTypeRoot;
    private EditText mAddress;

    private View mCategoryRoot;
    private MySpinnerView mCategory;

    private View mSubjectRoot;
    private MySpinnerView[] mSubjects;

    private EditText mWebsite;

    private View mPublicDateRoot;
    private TextView mDay;
    private TextView mMonth;
    private TextView mYear;

    private View mFeedbackCommentsRoot;
    private CheckBox mFeedbackComments;

    private CheckBox mObsceneFilter;
    private CheckBox mObsceneStopWords;
    private EditText mObsceneStopWordsEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_options, container, false);

        mName = root.findViewById(R.id.name);
        mDescription = root.findViewById(R.id.description);
        mCommunityTypeRoot = root.findViewById(R.id.community_type_root);
        mAddress = root.findViewById(R.id.link);

        mCategoryRoot = root.findViewById(R.id.category_root);
        mCategory = root.findViewById(R.id.spinner_category);
        mCategory.setIconOnClickListener(v -> getPresenter().onCategoryClick());

        mSubjectRoot = root.findViewById(R.id.subject_root);
        mSubjects = new MySpinnerView[2];
        mSubjects[0] = root.findViewById(R.id.subject_0);
        mSubjects[1] = root.findViewById(R.id.subject_1);

        mWebsite = root.findViewById(R.id.website);

        mPublicDateRoot = root.findViewById(R.id.public_date_root);
        mDay = root.findViewById(R.id.day);
        mDay.setOnClickListener(v -> getPresenter().fireDayClick());

        mMonth = root.findViewById(R.id.month);
        mMonth.setOnClickListener(v -> getPresenter().fireMonthClick());

        mYear = root.findViewById(R.id.year);
        mYear.setOnClickListener(v -> getPresenter().fireYearClick());

        mFeedbackCommentsRoot = root.findViewById(R.id.feedback_comments_root);
        mFeedbackComments = root.findViewById(R.id.feedback_comments);

        mObsceneFilter = root.findViewById(R.id.obscene_filter);
        mObsceneStopWords = root.findViewById(R.id.obscene_stopwords);
        mObsceneStopWordsEditText = root.findViewById(R.id.obscene_stopwords_values);
        return root;
    }

    @Override
    public IPresenterFactory<CommunityOptionsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            VKApiCommunity community = getArguments().getParcelable(Extra.GROUP);
            GroupSettings settings = getArguments().getParcelable(Extra.SETTINGS);
            return new CommunityOptionsPresenter(accountId, community, settings, saveInstanceState);
        };
    }

    @Override
    protected String tag() {
        return CommunityOptionsFragment.class.getSimpleName();
    }

    @Override
    public void displayName(String title) {
        safelySetText(mName, title);
    }

    @Override
    public void displayDescription(String description) {
        safelySetText(mDescription, description);
    }

    @Override
    public void setCommunityTypeVisible(boolean visible) {
        safelySetVisibleOrGone(mCommunityTypeRoot, visible);
    }

    @Override
    public void displayAddress(String address) {
        safelySetText(mAddress, address);
    }

    @Override
    public void setCategoryVisible(boolean visible) {
        safelySetVisibleOrGone(mCategoryRoot, visible);
    }

    @Override
    public void displayCategory(String categoryText) {
        if(Objects.nonNull(mCategory)){
            mCategory.setValue(categoryText);
        }
    }

    @Override
    public void showSelectOptionDialog(int requestCode, List<IdOption> data) {
        String[] strings = new String[data.size()];
        for(int i = 0; i < data.size(); i++){
            strings[i] = data.get(i).getTitle();
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.select_from_list_title)
                .setItems(strings, (dialog, which) -> getPresenter().fireOptionSelected(requestCode, data.get(which)))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public void setSubjectRootVisible(boolean visible) {
        safelySetVisibleOrGone(mSubjectRoot, visible);
    }

    @Override
    public void setSubjectVisible(int index, boolean visible) {
        if(Objects.nonNull(mSubjects)){
            safelySetVisibleOrGone(mSubjects[index], visible);
        }
    }

    @Override
    public void displaySubjectValue(int index, String value) {
        if(Objects.nonNull(mSubjects)){
            mSubjects[index].setValue(value);
        }
    }

    @Override
    public void displayWebsite(String website) {
        safelySetText(mWebsite, website);
    }

    @Override
    public void setPublicDateVisible(boolean visible) {
        safelySetVisibleOrGone(mPublicDateRoot, visible);
    }

    @Override
    public void dislayPublicDate(Day day) {
        if(day.getDay() > 0){
            safelySetText(mDay, String.valueOf(day.getDay()));
        } else {
            safelySetText(mDay, R.string.day);
        }

        if(day.getYear() > 0){
            safelySetText(mYear, String.valueOf(day.getYear()));
        } else {
            safelySetText(mYear, R.string.year);
        }

        if(day.getMonth() > 0){
            safelySetText(mMonth, Month.getMonthTitle(day.getMonth()));
        } else {
            safelySetText(mMonth, R.string.month);
        }
    }

    @Override
    public void setFeedbackCommentsRootVisible(boolean visible) {
        safelySetVisibleOrGone(mFeedbackCommentsRoot, visible);
    }

    @Override
    public void setFeedbackCommentsChecked(boolean checked) {
        safelySetCheched(mFeedbackComments, checked);
    }

    @Override
    public void setObsceneFilterChecked(boolean checked) {
        safelySetCheched(mObsceneFilter, checked);
    }

    @Override
    public void setObsceneStopWordsChecked(boolean checked) {
        safelySetCheched(mObsceneStopWords, checked);
    }

    @Override
    public void setObsceneStopWordsVisible(boolean visible) {
        safelySetVisibleOrGone(mObsceneStopWordsEditText, visible);
    }

    @Override
    public void displayObsceneStopWords(String words) {
        safelySetText(mObsceneStopWordsEditText, words);
    }
}