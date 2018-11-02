package arunkbabu90.lexicon.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Switch;

import arunkbabu90.lexicon.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener
{
    @BindView(R.id.about_developer_view) LinearLayout mAboutView;
    @BindView(R.id.licenses_view) LinearLayout mLicensesView;
    @BindView(R.id.launch_tutorial_view) LinearLayout mLaunchTutorialView;
    @BindView(R.id.sw_enable_edge) Switch mEnableEdgeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        // Set ClickListeners
        mAboutView.setOnClickListener(this);
        mLicensesView.setOnClickListener(this);

        // Make the navigation bar white and icons grey on Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                    | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            getWindow().setNavigationBarColor(getColor(R.color.colorPrimaryDark));
        }

        // Make the status bar white and icons grey on Marshmallow and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getColor(R.color.colorPrimaryDark));
        }

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.about_developer_view:
                startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                break;
            case R.id.licenses_view:
                startActivity(new Intent(SettingsActivity.this, LicenseActivity.class));
                break;
        }
    }
}
