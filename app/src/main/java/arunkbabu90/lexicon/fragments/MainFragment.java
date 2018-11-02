package arunkbabu90.lexicon.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arunkbabu90.lexicon.R;
import arunkbabu90.lexicon.activities.ExtractActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements View.OnClickListener
{
    @BindView(R.id.open_image_card) CardView mOpenImageCard;
    @BindView(R.id.capture_camera_card) CardView mCaptureCameraCard;

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
    }

    @Override
    public void onClick(View v) {
        if (getActivity() == null) return;

        switch (v.getId())
        {
            case R.id.open_image_card:
                startActivity(new Intent(getActivity(), ExtractActivity.class));
                break;
            case R.id.capture_camera_card:
                break;
        }
    }
}