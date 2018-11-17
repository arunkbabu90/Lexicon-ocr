package arunkbabu90.lexicon.tutorials;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import arunkbabu90.lexicon.Constants;
import arunkbabu90.lexicon.R;
import arunkbabu90.lexicon.activities.SettingsActivity;
import arunkbabu90.lexicon.services.EdgeScreenService;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class Page2Fragment extends Fragment
{
    @BindView(R.id.btn_tr_open_settings) Button mOpenSettingsButton;
    @BindView(R.id.tv_tutorial_title2) TextView mTitleView;
    @BindView(R.id.tv_tutorial_desc2) TextView mDescView;

    public Page2Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_page2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.btn_tr_open_settings)
    public void onSettingsButtonClick(View view) {
        Intent settingsIntent = new Intent(getContext(), SettingsActivity.class);
        settingsIntent.putExtra(Constants.IS_TUTORIAL_MODE_KEY, true);
        startActivity(settingsIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            if (EdgeScreenService.EdgeScreenServiceRunning) {
                mTitleView.setText(R.string.success);
                mDescView.setText(R.string.edge_activation_success);
            }
        }
    }
}
