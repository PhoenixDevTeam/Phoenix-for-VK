package biz.dealnote.messenger.fragment.search;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.SearchOptionsAdapter;
import biz.dealnote.messenger.dialog.SelectChairsDialog;
import biz.dealnote.messenger.dialog.SelectCityDialog;
import biz.dealnote.messenger.dialog.SelectCountryDialog;
import biz.dealnote.messenger.dialog.SelectFacultyDialog;
import biz.dealnote.messenger.dialog.SelectSchoolClassesDialog;
import biz.dealnote.messenger.dialog.SelectSchoolsDialog;
import biz.dealnote.messenger.dialog.SelectUniversityDialog;
import biz.dealnote.messenger.fragment.search.options.BaseOption;
import biz.dealnote.messenger.fragment.search.options.DatabaseOption;
import biz.dealnote.messenger.fragment.search.options.SimpleBooleanOption;
import biz.dealnote.messenger.fragment.search.options.SimpleNumberOption;
import biz.dealnote.messenger.fragment.search.options.SimpleTextOption;
import biz.dealnote.messenger.fragment.search.options.SpinnerOption;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.InputTextDialog;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.disposables.CompositeDisposable;

public class FilterEditFragment extends BottomSheetDialogFragment implements SearchOptionsAdapter.OptionClickListener {

    private ArrayList<BaseOption> mData;
    private SearchOptionsAdapter mAdapter;

    public static FilterEditFragment newInstance(int accountId, ArrayList<BaseOption> options) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelableArrayList(Extra.LIST, options);
        FilterEditFragment fragment = new FilterEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private int mAccountId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mAccountId = getArguments().getInt(Extra.ACCOUNT_ID);
        this.mData = getArguments().getParcelableArrayList(Extra.LIST);
    }

    private TextView mEmptyText;

    private void resolveEmptyTextVisibility(){
        if(Objects.nonNull(mEmptyText)){
            mEmptyText.setVisibility(Utils.isEmpty(mData) ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View root = View.inflate(getActivity(), R.layout.sheet_filter_edirt, null);

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.search_options);

        MenuItem saveItem = toolbar.getMenu().add(R.string.save);
        saveItem.setIcon(CurrentTheme.getDrawableFromAttribute(getActivity(), R.attr.toolbarOKIcon));
        saveItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        saveItem.setOnMenuItemClickListener(menuItem -> {
            onSaveClick();
            return true;
        });

        mEmptyText = root.findViewById(R.id.empty_text);

        RecyclerView mRecyclerView = root.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);

        mAdapter = new SearchOptionsAdapter(mData);
        mAdapter.setOptionClickListener(this);

        mRecyclerView.setAdapter(mAdapter);
        resolveEmptyTextVisibility();

        dialog.setContentView(root);
    }

    private void onSaveClick(){
        Intent data = new Intent();
        data.putParcelableArrayListExtra(Extra.LIST, mData);

        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
        dismiss();
    }

    @Override
    public void onSpinnerOptionClick(final SpinnerOption spinnerOption) {
        new AlertDialog.Builder(getActivity())
                .setTitle(spinnerOption.title)
                .setItems(spinnerOption.createAvailableNames(getActivity()), (dialog, which) -> {
                    spinnerOption.value = spinnerOption.available.get(which);
                    mAdapter.notifyDataSetChanged();
                })
                .setNegativeButton(R.string.clear, (dialog, which) -> {
                    spinnerOption.value = null;
                    mAdapter.notifyDataSetChanged();
                })
                .setPositiveButton(R.string.button_cancel, null)
                .show();
    }

    private static final int REQUEST_CODE_COUTRY = 126;
    private static final int REQUEST_CODE_CITY = 127;
    private static final int REQUEST_CODE_UNIVERSITY = 128;
    private static final int REQUEST_CODE_FACULTY = 129;
    private static final int REQUEST_CODE_CHAIR = 130;
    private static final int REQUEST_CODE_SCHOOL = 131;
    private static final int REQUEST_CODE_SCHOOL_CLASS = 132;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_COUTRY:
            case REQUEST_CODE_CITY:
            case REQUEST_CODE_UNIVERSITY:
            case REQUEST_CODE_FACULTY:
            case REQUEST_CODE_CHAIR:
            case REQUEST_CODE_SCHOOL:
            case REQUEST_CODE_SCHOOL_CLASS:
                Bundle extras = data.getExtras();

                int key = extras.getInt(Extra.KEY);
                Integer id = extras.containsKey(Extra.ID) ? extras.getInt(Extra.ID) : null;
                String title = extras.containsKey(Extra.TITLE) ? extras.getString(Extra.TITLE) : null;

                mergeDatabaseOptionValue(key, id == null ? null : new DatabaseOption.Entry(id, title));
                break;
        }
    }

    private void mergeDatabaseOptionValue(int key, DatabaseOption.Entry value) {
        for (BaseOption option : mData) {
            if (option.key == key && option instanceof DatabaseOption) {
                DatabaseOption databaseOption = (DatabaseOption) option;
                databaseOption.value = value;
                resetChildDependensies(databaseOption.childDependencies);
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void resetChildDependensies(int... childs) {
        if (childs != null) {
            boolean changed = false;
            for (int key : childs) {
                for (BaseOption option : mData) {
                    if (option.key == key) {
                        option.reset();
                        changed = true;
                    }
                }
            }

            if (changed) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDatabaseOptionClick(DatabaseOption databaseOption) {
        BaseOption dependency = findDependencyByKey(databaseOption.parentDependencyKey);

        switch (databaseOption.type) {
            case DatabaseOption.TYPE_COUNTRY:
                SelectCountryDialog selectCountryDialog = new SelectCountryDialog();
                selectCountryDialog.setTargetFragment(this, REQUEST_CODE_COUTRY);

                Bundle args = new Bundle();
                args.putInt(Extra.KEY, databaseOption.key);
                selectCountryDialog.setArguments(args);
                selectCountryDialog.show(getFragmentManager(), "countries");
                break;

            case DatabaseOption.TYPE_CITY:
                if (dependency != null && dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int countryId = ((DatabaseOption) dependency).value.id;
                    showCitiesDialog(databaseOption, countryId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_UNIVERSITY:
                if (dependency != null && dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int countryId = ((DatabaseOption) dependency).value.id;
                    showUniversitiesDialog(databaseOption, countryId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_FACULTY:
                if (dependency != null && dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int universityId = ((DatabaseOption) dependency).value.id;
                    showFacultiesDialog(databaseOption, universityId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_CHAIR:
                if (dependency != null && dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int facultyId = ((DatabaseOption) dependency).value.id;
                    showChairsDialog(databaseOption, facultyId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_SCHOOL:
                if (dependency != null && dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int cityId = ((DatabaseOption) dependency).value.id;
                    showSchoolsDialog(databaseOption, cityId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_SCHOOL_CLASS:
                if (dependency != null && dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int countryId = ((DatabaseOption) dependency).value.id;
                    showSchoolClassesDialog(databaseOption, countryId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    public void onSimpleNumberOptionClick(final SimpleNumberOption option) {
        new InputTextDialog.Builder(getActivity())
                .setTitleRes(option.title)
                .setAllowEmpty(true)
                .setInputType(InputType.TYPE_CLASS_NUMBER)
                .setValue(option.value == null ? null : String.valueOf(option.value))
                .setCallback(newValue -> {
                    option.value = getIntFromEditable(newValue);
                    mAdapter.notifyDataSetChanged();
                })
                .show();
    }

    private Integer getIntFromEditable(String line) {
        if (line == null || TextUtils.getTrimmedLength(line) == 0) {
            return null;
        }

        try {
            return Integer.valueOf(line);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    public void onSimpleTextOptionClick(final SimpleTextOption option) {
        new InputTextDialog.Builder(getActivity())
                .setTitleRes(option.title)
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setValue(option.value)
                .setAllowEmpty(true)
                .setCallback(newValue -> {
                    option.value = newValue;
                    mAdapter.notifyDataSetChanged();
                })
                .show();
    }

    @Override
    public void onSimpleBooleanOptionChanged(SimpleBooleanOption option) {

    }

    @Override
    public void onOptionCleared(BaseOption option) {
        resetChildDependensies(option.childDependencies);
    }

    private void showCitiesDialog(DatabaseOption databaseOption, int countryId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectCityDialog selectCityDialog = SelectCityDialog.newInstance(mAccountId, countryId, args);
        selectCityDialog.setTargetFragment(this, REQUEST_CODE_CITY);
        selectCityDialog.show(getFragmentManager(), "cities");
    }

    private void showUniversitiesDialog(DatabaseOption databaseOption, int countryId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectUniversityDialog dialog = SelectUniversityDialog.newInstance(mAccountId, countryId, args);
        dialog.setTargetFragment(this, REQUEST_CODE_UNIVERSITY);
        dialog.show(getFragmentManager(), "universities");
    }

    private void showSchoolsDialog(DatabaseOption databaseOption, int cityId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectSchoolsDialog dialog = SelectSchoolsDialog.newInstance(mAccountId, cityId, args);
        dialog.setTargetFragment(this, REQUEST_CODE_SCHOOL);
        dialog.show(getFragmentManager(), "schools");
    }

    private void showFacultiesDialog(DatabaseOption databaseOption, int universityId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectFacultyDialog dialog = SelectFacultyDialog.newInstance(mAccountId, universityId, args);
        dialog.setTargetFragment(this, REQUEST_CODE_FACULTY);
        dialog.show(getFragmentManager(), "faculties");
    }

    private void showChairsDialog(DatabaseOption databaseOption, int facultyId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectChairsDialog dialog = SelectChairsDialog.newInstance(mAccountId, facultyId, args);
        dialog.setTargetFragment(this, REQUEST_CODE_CHAIR);
        dialog.show(getFragmentManager(), "chairs");
    }

    private void showSchoolClassesDialog(DatabaseOption databaseOption, int countryId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectSchoolClassesDialog dialog = SelectSchoolClassesDialog.newInstance(mAccountId, countryId, args);
        dialog.setTargetFragment(this, REQUEST_CODE_SCHOOL_CLASS);
        dialog.show(getFragmentManager(), "school-classes");
    }

    private BaseOption findDependencyByKey(int key) {
        if (key == BaseOption.NO_DEPENDENCY) {
            return null;
        }

        for (BaseOption baseOption : mData) {
            if (baseOption.key == key) {
                return baseOption;
            }
        }

        return null;
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }
}