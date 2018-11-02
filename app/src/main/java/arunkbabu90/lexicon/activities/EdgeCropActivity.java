package arunkbabu90.lexicon.activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.theartofdev.edmodo.cropper.CropImageView;

import arunkbabu90.lexicon.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class EdgeCropActivity extends AppCompatActivity
{
    @BindView(R.id.edge_crop_view) CropImageView mCropImageView;
    @BindView(R.id.fab_edge_crop) FloatingActionButton mFabCropDone;
    @BindView(R.id.crop_coordinator_layout) CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        ButterKnife.bind(this);

        // Enable Exclusive FullScreen mode
        mCoordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
