package arunkbabu90.lexicon.fragments;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import arunkbabu90.lexicon.R;
import arunkbabu90.lexicon.activities.SavedTextActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements View.OnClickListener
{
    @BindView(R.id.open_image_card) CardView mOpenImageCard;
    @BindView(R.id.capture_camera_card) CardView mCaptureCameraCard;
    @BindView(R.id.tbtn_settings) ImageButton mSettingsButton;
    @BindView(R.id.tbtn_saved_text) ImageButton mSavedTextButton;
    @BindView(R.id.tbtn_about) ImageButton mAbout;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mCaptureCameraCard.setOnClickListener(this);
        mOpenImageCard.setOnClickListener(this);
        mAbout.setOnClickListener(this);
        mSettingsButton.setOnClickListener(this);
        mSavedTextButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.open_image_card:
                break;
            case R.id.capture_camera_card:
                break;
            case R.id.tbtn_about:
                break;
            case R.id.tbtn_settings:
                break;
            case R.id.tbtn_saved_text:
                if (getActivity() != null)
                    startActivity(new Intent(getActivity().getApplicationContext(), SavedTextActivity.class));
                break;
        }
    }
}
