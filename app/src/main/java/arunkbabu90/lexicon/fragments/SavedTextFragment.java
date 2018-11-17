package arunkbabu90.lexicon.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import arunkbabu90.lexicon.Constants;
import arunkbabu90.lexicon.R;
import arunkbabu90.lexicon.adapters.SavedTextAdapter;
import arunkbabu90.lexicon.database.AppExecutor;
import arunkbabu90.lexicon.database.Text;
import arunkbabu90.lexicon.database.TextDatabase;
import arunkbabu90.lexicon.database.TextViewModel;
import arunkbabu90.lexicon.dialogs.ExtractDialog;
import arunkbabu90.lexicon.widget.UpdateListService;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class SavedTextFragment extends Fragment implements SavedTextAdapter.ItemClickListener
{
    @BindView(R.id.tv_fav_no_internet) TextView mNoInternetTextView;
    @BindView(R.id.rv_saved_texts) RecyclerView mRecyclerView;

    private boolean mShowLayoutAnimation = true;
    private int mDisplayWidth;
    private SavedTextAdapter mAdapter;
    private TextDatabase mDb;
    private Text cUndoCache;

    public SavedTextFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved_text, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mDb = TextDatabase.getInstance(getContext());

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mDisplayWidth = displayMetrics.widthPixels;

        int noOfCols;
        if (getResources().getBoolean(R.bool.isSW600)) {
            noOfCols = 3;
        } else if (getResources().getBoolean(R.bool.isLandscape)) {
            noOfCols = 2;
        } else {
            noOfCols = 1;
        }

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), noOfCols));
        mAdapter = new SavedTextAdapter();
        mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        // Swipe the card to delete the text from database
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                final View foreground = ((SavedTextAdapter.SavedTextAdapterViewHolder) viewHolder).viewForeground;
                getDefaultUIUtil().clearView(foreground);
            }

            @Override
            public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder, float dX,
                                        float dY, int actionState, boolean isCurrentlyActive) {
                final View foreground = ((SavedTextAdapter.SavedTextAdapterViewHolder) viewHolder).viewForeground;
                getDefaultUIUtil().onDrawOver(c, recyclerView, foreground, dX, dY,
                        actionState, isCurrentlyActive);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX,
                                    float dY, int actionState, boolean isCurrentlyActive) {
                final View foreground = ((SavedTextAdapter.SavedTextAdapterViewHolder) viewHolder).viewForeground;
                getDefaultUIUtil().onDraw(c, recyclerView, foreground, dX, dY,
                        actionState, isCurrentlyActive);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final Text text = mAdapter.getSavedTextList().get(position);
                cUndoCache = text;
                mShowLayoutAnimation = false;

                AppExecutor.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.textDao().deleteText(text);

                        UpdateListService.startActionUpdateWidget(getContext());
                    }
                });

                // Undo functionality in-case if the text was accidentally deleted
                Snackbar.make(view.findViewById(R.id.saved_text_fragment), R.string.text_deleted, Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AppExecutor.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        mDb.textDao().insertText(cUndoCache);
                                        cUndoCache = null;
                                    }
                                });
                            }
                        })
                        .setActionTextColor(getResources().getColor(android.R.color.holo_green_light))
                        .show();
            }
        }).attachToRecyclerView(mRecyclerView);

        setupViewModel();

        // Intent to update the widget data
        Intent updateWidgetIntent = new Intent(getContext(), UpdateListService.class);
        updateWidgetIntent.setAction(UpdateListService.ACTION_UPDATE_WIDGET);
        updateWidgetIntent.putExtra(Constants.WIDGET_SAVED_TEXT_LIST_KEY, mAdapter.getSavedTextList());
        getActivity().startService(updateWidgetIntent);
    }

    private void setupViewModel() {
        TextViewModel viewModel = ViewModelProviders.of(this).get(TextViewModel.class);
        viewModel.getSavedTexts().observe(SavedTextFragment.this, new Observer<List<Text>>() {
            @Override
            public void onChanged(@Nullable List<Text> texts) {
                // If the database is empty then show there is NO TEXT; else show the saved texts
                if (texts == null || texts.isEmpty()) {
                    mNoInternetTextView.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                } else {
                    mNoInternetTextView.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mAdapter.setSavedTexts(toArrayList(texts));
                    if (mShowLayoutAnimation) {
                        runLayoutAnimation(mRecyclerView);
                        mShowLayoutAnimation = true;
                    }
                }
            }
        });
    }

    /**
     * Convert List<> to ArrayList<>
     * @return The ArrayList
     */
    private ArrayList<Text> toArrayList(List<Text> list) {
        return new ArrayList<>(list);
    }

    @Override
    public void onItemClick(View v, String savedText) {
        switch (v.getId())
        {
            case R.id.btn_saved_text_share:
                // Share the text
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, savedText);
                if (shareIntent.resolveActivity(getActivity().getApplicationContext().getPackageManager()) != null) {
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)));
                } else {
                    Toast.makeText(getContext(), R.string.err_no_apps_found, Toast.LENGTH_LONG).show();
                }
                return;
        }

        ExtractDialog.newInstance(savedText, mDisplayWidth, ExtractDialog.BUTTON_VISIBILITY_NO_SAVE)
                .show(getActivity().getSupportFragmentManager(), Constants.SAVED_TEXT_DIALOG_TAG);
    }

    /**
     * Starts the layout animation
     */
    private void runLayoutAnimation(RecyclerView recyclerView) {
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getContext(),
                R.anim.layout_animation_saved_text);
        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
    }
}
