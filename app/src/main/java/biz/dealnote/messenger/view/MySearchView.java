package biz.dealnote.messenger.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.listener.TextWatcherAdapter;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.Logger;

public class MySearchView extends LinearLayout {

    private static final String TAG = MySearchView.class.getSimpleName();

    private String mQuery;

    private EditText mInput;
    private ImageView mButtonBack;
    private ImageView mButtonClear;
    private ImageView mButtonAdditional;

    public MySearchView(Context context) {
        super(context);
        init();
    }

    public MySearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MySearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.custom_searchview, this);

        mInput = (EditText) findViewById(R.id.input);
        mInput.setOnEditorActionListener(mOnEditorActionListener);

        mButtonBack = (ImageView) findViewById(R.id.button_back);
        mButtonClear = (ImageView) findViewById(R.id.clear);
        mButtonAdditional = (ImageView) findViewById(R.id.additional);

        mButtonBack.setColorFilter(CurrentTheme.getIconColorStatic(getContext()));
        mButtonClear.setColorFilter(CurrentTheme.getIconColorStatic(getContext()));
        mButtonAdditional.setColorFilter(CurrentTheme.getIconColorStatic(getContext()));

        mInput.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                mQuery = s.toString();

                if (mOnQueryChangeListener != null) {
                    mOnQueryChangeListener.onQueryTextChange(s.toString());
                }

                resolveCloseButton();
            }
        });

        mButtonClear.setOnClickListener(v -> clear());

        mButtonBack.setOnClickListener(v -> {
            if (mOnBackButtonClickListener != null) {
                mOnBackButtonClickListener.onBackButtonClick();
            }
        });

        mButtonAdditional.setOnClickListener(v -> {
            if (mOnAdditionalButtonClickListener != null) {
                mOnAdditionalButtonClickListener.onAdditionalButtonClick();
            }
        });

        resolveCloseButton();
    }

    public Editable getText(){
        return mInput.getText();
    }

    public void clear(){
        mInput.getText().clear();
    }

    private final TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {

        /**
         * Called when the input method default action key is pressed.
         */
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            Logger.d(TAG, "onEditorAction, actionId: " + actionId + ", event: " + event);
            onSubmitQuery();
            return true;
        }
    };

    private void onSubmitQuery() {
        CharSequence query = mInput.getText();
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            if (mOnQueryChangeListener != null && mOnQueryChangeListener.onQueryTextSubmit(query.toString())) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(getWindowToken(), 0);
                }
            }
        }
    }

    public void setRightButtonVisibility(boolean visible){
        mButtonAdditional.setVisibility(visible ? VISIBLE : GONE);
    }

    private void resolveCloseButton() {
        boolean empty = TextUtils.isEmpty(mQuery);
        Logger.d(TAG, "resolveCloseButton, empty: " + empty);
        mButtonClear.setVisibility(TextUtils.isEmpty(mQuery) ? GONE : VISIBLE);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle state = new Bundle();
        state.putParcelable("PARENT", superState);
        state.putString("query", mQuery);

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable("PARENT");
        super.onRestoreInstanceState(superState);

        mQuery = savedState.getString("query");
        mInput.setText(mQuery);
    }

    private OnQueryTextListener mOnQueryChangeListener;

    public void setOnQueryTextListener(OnQueryTextListener onQueryChangeListener) {
        this.mOnQueryChangeListener = onQueryChangeListener;
    }

    public void setOnBackButtonClickListener(OnBackButtonClickListener onBackButtonClickListener) {
        this.mOnBackButtonClickListener = onBackButtonClickListener;
    }

    /**
     * Callbacks for changes to the query text.
     */
    public interface OnQueryTextListener {

        /**
         * Called when the user submits the query. This could be due to a key press on the
         * keyboard or due to pressing a submit button.
         * The listener can override the standard behavior by returning true
         * to indicate that it has handled the submit request. Otherwise return false to
         * let the SearchView handle the submission by launching any associated intent.
         *
         * @param query the query text that is to be submitted
         * @return true if the query has been handled by the listener, false to let the
         * SearchView perform the default action.
         */
        boolean onQueryTextSubmit(String query);

        /**
         * Called when the query text is changed by the user.
         *
         * @param newText the new content of the query text field.
         * @return false if the SearchView should perform the default action of showing any
         * suggestions if available, true if the action was handled by the listener.
         */
        boolean onQueryTextChange(String newText);
    }

    public interface OnBackButtonClickListener {
        void onBackButtonClick();
    }

    private OnBackButtonClickListener mOnBackButtonClickListener;

    public interface OnAdditionalButtonClickListener {
        void onAdditionalButtonClick();
    }

    private OnAdditionalButtonClickListener mOnAdditionalButtonClickListener;

    public void setOnAdditionalButtonClickListener(OnAdditionalButtonClickListener onAdditionalButtonClickListener) {
        this.mOnAdditionalButtonClickListener = onAdditionalButtonClickListener;
    }

    public void setQuery(String query, boolean quetly){
        OnQueryTextListener tmp = mOnQueryChangeListener;
        if(quetly) {
            mOnQueryChangeListener = null;
        }

        setQuery(query);

        if(quetly){
            mOnQueryChangeListener = tmp;
        }
    }

    public void setQuery(String query) {
        mInput.setText(query);
    }

    public void setSelection(int start, int end) {
        mInput.setSelection(start, end);
    }

    public void setSelection(int position) {
        mInput.setSelection(position);
    }

    public void setLeftIcon(int drawable) {
        mButtonBack.setImageResource(drawable);
    }
}
