package biz.dealnote.messenger.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.vkdatabase.CountriesAdapter;
import biz.dealnote.messenger.fragment.base.BasePresenterDialogFragment;
import biz.dealnote.messenger.listener.TextWatcherAdapter;
import biz.dealnote.messenger.model.database.Country;
import biz.dealnote.messenger.mvp.presenter.CountriesPresenter;
import biz.dealnote.messenger.mvp.view.ICountriesView;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.mvp.core.IPresenterFactory;

public class SelectCountryDialog extends BasePresenterDialogFragment<CountriesPresenter, ICountriesView>
        implements CountriesAdapter.Listener, ICountriesView {

    @Override
    protected String tag() {
        return SelectCountryDialog.class.getSimpleName();
    }

    private CountriesAdapter mAdapter;
    private View mLoadingView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_countries, null);

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.countries_title)
                .setView(view)
                .setNegativeButton(R.string.button_cancel, null)
                .create();

        EditText filterView = view.findViewById(R.id.input);
        filterView.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().fireFilterEdit(s);
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new CountriesAdapter(getActivity(), Collections.emptyList());
        mAdapter.setListener(this);

        recyclerView.setAdapter(mAdapter);

        mLoadingView = view.findViewById(R.id.progress_root);
        return dialog;
    }

    @Override
    public void onClick(Country country) {
        getPresenter().fireCountryClick(country);
    }

    @Override
    public void displayData(List<Country> countries) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.setData(countries);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void displayLoading(boolean loading) {
        if(Objects.nonNull(mLoadingView)){
            mLoadingView.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void returnSelection(Country country) {
        Intent intent = new Intent();
        intent.putExtra(Extra.COUNTRY, country);
        intent.putExtra(Extra.ID, country.getId());
        intent.putExtra(Extra.TITLE, country.getTitle());

        if (getArguments() != null) {
            intent.putExtras(getArguments());
        }

        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }

    @Override
    public IPresenterFactory<CountriesPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CountriesPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }
}