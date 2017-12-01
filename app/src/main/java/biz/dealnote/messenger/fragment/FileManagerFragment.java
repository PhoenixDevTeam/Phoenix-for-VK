package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.FileManagerAdapter;
import biz.dealnote.messenger.listener.BackPressCallback;
import biz.dealnote.messenger.model.FileItem;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.InputTextDialog;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Utils;

public class FileManagerFragment extends Fragment implements FileManagerAdapter.ClickListener, BackPressCallback {

    public static final String EXTRA_START_DIRECTOTY = "start_directory";
    public static final String EXTRA_SHOW_CANNOT_READ = "show_cannot_read";
    public static final String EXTRA_FILTER_EXTENSION = "filter_extension";

    public static final String returnDirectoryParameter = "ua.com.vassiliev.androidfilebrowser.directoryPathRet";
    public static final String returnFileParameter = "ua.com.vassiliev.androidfilebrowser.filePathRet";

    public static final int SELECT_DIRECTORY = 1;
    public static final int SELECT_FILE = 0;

    private int currentAction;
    // Stores names of traversed directories
    private ArrayList<String> pathDirsList;

    private ArrayList<FileItem> fileList;

    private boolean showHiddenFilesAndDirs = true;
    private boolean directoryShownIsEmpty = false;
    private String filterFileExtension;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private TextView empty;
    private ImageView btnSelectCurrentDir;
    private TextView tvCurrentDir;

    private FileManagerAdapter mAdapter;

    private File path;
    private FilenameFilter filter;
    private DirectoryScrollPositions directoryScrollPositions;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            restoreFromSavedInstanceState(savedInstanceState);
        }

        setHasOptionsMenu(true);

        currentAction = getArguments().getInt(Extra.ACTION);

        showHiddenFilesAndDirs = getArguments().getBoolean(EXTRA_SHOW_CANNOT_READ, true);
        filterFileExtension = getArguments().getString(EXTRA_FILTER_EXTENSION);

        filter = (dir, filename) -> {
            File sel = new File(dir, filename);
            boolean showReadableFile = showHiddenFilesAndDirs || sel.canRead();
            // Filters based on whether the file is hidden or not
            if (currentAction == SELECT_DIRECTORY) {
                return sel.isDirectory() && showReadableFile;
            }

            if (currentAction == SELECT_FILE) {
                // If it is a file check the extension if provided
                if (sel.isFile() && filterFileExtension != null) {
                    return showReadableFile && sel.getName().endsWith(filterFileExtension);
                }

                return (showReadableFile);
            }

            return true;
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_file_explorer, container, false);
        //((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar) root.findViewById(R.id.toolbar));

        mRecyclerView = (RecyclerView) root.findViewById(R.id.list);
        empty = (TextView) root.findViewById(R.id.empty);

        mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setHasFixedSize(Boolean.TRUE);

        btnSelectCurrentDir = (ImageView) root.findViewById(R.id.select_current_directory_button);
        tvCurrentDir = (TextView) root.findViewById(R.id.current_path);

        btnSelectCurrentDir.setVisibility(currentAction == SELECT_DIRECTORY ? View.VISIBLE : View.GONE);

        return root;
    }

    private void resolveEmptyText() {
        empty.setVisibility(Utils.safeIsEmpty(fileList) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (directoryScrollPositions == null) {
            directoryScrollPositions = new DirectoryScrollPositions();
        }

        if (fileList == null) {
            fileList = new ArrayList<>();
            setInitialDirectory();
            loadFileList();
        } else {
            resolveEmptyText();
        }

        mAdapter = new FileManagerAdapter(fileList);
        mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        parseDirectoryPath();
        updateCurrentDirectoryTextView();
    }

    private static final String SAVE_DATA = "save_data";
    private static final String SAVE_PATH = "save_path";
    private static final String SAVE_SCROLL_STATES = "scroll_states";

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_DATA, fileList);
        outState.putSerializable(SAVE_PATH, path);
        outState.putParcelable(SAVE_SCROLL_STATES, directoryScrollPositions);
    }

    private void restoreFromSavedInstanceState(Bundle state) {
        fileList = state.getParcelableArrayList(SAVE_DATA);
        path = (File) state.getSerializable(SAVE_PATH);
        directoryScrollPositions = state.getParcelable(SAVE_SCROLL_STATES);
    }

    private void loadFileList() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
        }

        fileList.clear();

        if (path.exists() && path.canRead()) {
            String[] fList = path.list(filter);
            directoryShownIsEmpty = false;

            for (int i = 0; i < fList.length; i++) {
                // Convert into file path
                File file = new File(path, fList[i]);
                int drawableID = R.drawable.file;
                boolean canRead = file.canRead();
                boolean isDirectory = file.isDirectory();

                // Set drawables
                if (isDirectory) {
                    if (canRead) {
                        drawableID = R.drawable.ic_directory_can_read;
                    } else {
                        drawableID = R.drawable.ic_directory_cant_read;
                    }
                }

                String details = isDirectory ? null : formatBytes(file.length());
                fileList.add(i, new FileItem(isDirectory, fList[i], details, drawableID, canRead));
            }

            if (fileList.size() == 0) {
                directoryShownIsEmpty = true;
            } else {
                Collections.sort(fileList, new ItemFileNameComparator());
            }
        }

        resolveEmptyText();
    }

    private void returnDirectoryFinishActivity() {
        Intent retIntent = new Intent();
        retIntent.putExtra(returnDirectoryParameter, path.getAbsolutePath());
        getActivity().setResult(Activity.RESULT_OK, retIntent);
        getActivity().finish();
    }

    private void returnFileFinishActivity(String filePath) {
        Intent retIntent = new Intent();
        retIntent.putExtra(returnFileParameter, filePath);
        getActivity().setResult(Activity.RESULT_OK, retIntent);
        getActivity().finish();
    }

    private void parseDirectoryPath() {
        pathDirsList = new ArrayList<>();
        pathDirsList.clear();
        String pathString = path.getAbsolutePath();
        String[] parts = pathString.split("/");

        pathDirsList.addAll(Arrays.asList(parts));
    }

    private void updateCurrentDirectoryTextView() {
        int i = 0;
        String curDirString = TextUtils.join("/", pathDirsList) + "/";

        if (pathDirsList.size() == 0) {
            curDirString = "/";
        }

        long freeSpace = getFreeSpace(curDirString);
        String formattedSpaceString = formatBytes(freeSpace);
        if (freeSpace == 0) {
            File currentDir = new File(curDirString);
            if (!currentDir.canWrite()) {
                formattedSpaceString = "NON Writable";
            }
        }

        tvCurrentDir.setText(curDirString);
    }

    @Override
    public void onResume() {
        super.onResume();
        //resolveToolbar();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(R.string.create_dir)
                .setIcon(CurrentTheme.getDrawableFromAttribute(getActivity(), R.attr.toolbarPlusIcon))
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .setOnMenuItemClickListener(item -> {
                    showTextInputDialog();
                    return true;
                });
    }

    /*private void resolveToolbar() {
        if (!isAdded()) return;

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.file_explorer);
            switch (currentAction) {
                case SELECT_DIRECTORY:
                    actionBar.setSubtitle(R.string.select_directory);
                    break;
                case SELECT_FILE:
                    actionBar.setSubtitle(R.string.select_file);
                    break;
            }
        }
    }*/

    private void loadDirectoryUp() {
        mRecyclerView.stopScroll();

        // present directory removed from list
        String s = pathDirsList.remove(pathDirsList.size() - 1);
        // path modified to exclude present directory
        path = new File(path.toString().substring(0, path.toString().lastIndexOf(s)));

        loadFileList();
        mAdapter.notifyDataSetChanged();
        updateCurrentDirectoryTextView();

        Parcelable managerState = directoryScrollPositions.states.get(path.getAbsolutePath());
        if (managerState != null) {
            mLinearLayoutManager.onRestoreInstanceState(managerState);
            directoryScrollPositions.states.remove(path.getAbsolutePath());
        }
    }

    /**
     * Отображение диалога для ввода имени новой папки
     */
    private void showTextInputDialog() {
        new InputTextDialog.Builder(getActivity())
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setTitleRes(R.string.enter_dir_name)
                .setAllowEmpty(false)
                .setCallback(newValue -> {
                    File file = new File(path.getAbsolutePath() + "/" + newValue);
                    if (!file.exists() && file.mkdir()) {
                        loadFileList();
                        mAdapter.notifyDataSetChanged();
                        updateCurrentDirectoryTextView();
                    } else {
                        Toast.makeText(getActivity(), R.string.cannot_create_catalog, Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    private void setInitialDirectory() {
        Intent intent = getActivity().getIntent();
        String requestedStartDir = intent.getStringExtra(EXTRA_START_DIRECTOTY);

        if (!TextUtils.isEmpty(requestedStartDir)) {
            File tempFile = new File(requestedStartDir);
            if (tempFile.isDirectory()) {
                path = tempFile;
            }
        }

        if (path == null) {
            if (Environment.getExternalStorageDirectory().isDirectory() && Environment.getExternalStorageDirectory().canRead()) {
                path = Environment.getExternalStorageDirectory();
            } else {
                path = new File("/");
            }
        }
    }

    @Override
    public void onClick(int position, FileItem item) {
        String chosenFile = fileList.get(position).file;
        File sel = new File(path + "/" + chosenFile);

        if (sel.isDirectory()) {
            if (sel.canRead()) {
                directoryScrollPositions.states.put(path.getAbsolutePath(), mLinearLayoutManager.onSaveInstanceState());

                pathDirsList.add(chosenFile);
                path = new File(sel.getAbsolutePath());
                loadFileList();
                mAdapter.notifyDataSetChanged();

                if(!fileList.isEmpty()){
                    mLinearLayoutManager.scrollToPosition(0);
                }

                updateCurrentDirectoryTextView();
            } else {
                Toast.makeText(getActivity(), R.string.path_not_exist, Toast.LENGTH_LONG).show();
            }
        } else {
            if (!directoryShownIsEmpty) {
                returnFileFinishActivity(sel.getAbsolutePath());
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        Logger.d("FileManager", "onBackPressed");

        boolean root = path.toString().equalsIgnoreCase("/");
        if (!root) {
            loadDirectoryUp();
        }

        return root;
    }

    private class ItemFileNameComparator implements Comparator<FileItem> {
        @Override
        public int compare(FileItem lhs, FileItem rhs) {
            return lhs.file.toLowerCase().compareTo(rhs.file.toLowerCase());
        }
    }

    public static long getFreeSpace(String path) {
        StatFs stat = new StatFs(path);
        long availSize = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        return availSize;
    }

    public static String formatBytes(long bytes) {
        // TODO: add flag to which part is needed (e.g. GB, MB, KB or bytes)

        String retStr = "";

        // One binary gigabyte equals 1,073,741,824 bytes.
        if (bytes > 1073741824) {// Add GB
            long gbs = bytes / 1073741824;
            retStr += (Long.valueOf(gbs)).toString() + "GB ";
            bytes = bytes - (gbs * 1073741824);
        }

        // One MB - 1048576 bytes
        if (bytes > 1048576) {// Add GB
            long mbs = bytes / 1048576;
            retStr += (Long.valueOf(mbs)).toString() + "MB ";
            bytes = bytes - (mbs * 1048576);
        }

        if (bytes > 1024) {
            long kbs = bytes / 1024;
            retStr += (Long.valueOf(kbs)).toString() + "KB";
            bytes = bytes - (kbs * 1024);
        } else {
            retStr += (Long.valueOf(bytes)).toString() + " bytes";
        }

        return retStr;
    }

    private static class DirectoryScrollPositions implements Parcelable {

        private Map<String, Parcelable> states;

        DirectoryScrollPositions() {
            states = new HashMap<>();
        }

        DirectoryScrollPositions(Parcel in) {
            int size = in.readInt();
            states = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                String key = in.readString();
                Parcelable value = in.readParcelable(LinearLayoutManager.SavedState.class.getClassLoader());
                states.put(key, value);
            }
        }

        public static final Creator<DirectoryScrollPositions> CREATOR = new Creator<DirectoryScrollPositions>() {
            @Override
            public DirectoryScrollPositions createFromParcel(Parcel in) {
                return new DirectoryScrollPositions(in);
            }

            @Override
            public DirectoryScrollPositions[] newArray(int size) {
                return new DirectoryScrollPositions[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(states.size());

            for (Map.Entry<String, Parcelable> entry : states.entrySet()) {
                String key = entry.getKey();
                Parcelable value = entry.getValue();
                dest.writeString(key);
                dest.writeParcelable(value, flags);
            }
        }
    }
}
