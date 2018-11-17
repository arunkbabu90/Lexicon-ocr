package arunkbabu90.lexicon.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.TooltipCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import arunkbabu90.lexicon.Constants;
import arunkbabu90.lexicon.R;
import arunkbabu90.lexicon.services.EdgeScreenService;
import arunkbabu90.lexicon.tutorials.TutorialActivity;
import arunkbabu90.lexicon.widget.UpdateListService;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    @BindView(R.id.tbtn_settings) ImageButton mSettingsButton;
    @BindView(R.id.tbtn_saved_text) ImageButton mSavedTextButton;
    @BindView(R.id.tbtn_about) ImageButton mAbout;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAbout.setOnClickListener(this);
        mSettingsButton.setOnClickListener(this);
        mSavedTextButton.setOnClickListener(this);

        mSharedPreferences = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
        boolean isEdgeSwitchEnabled = mSharedPreferences.getBoolean(Constants.PREF_EDGE_SWITCH_STATE, false);

        boolean isTutorialViewed = mSharedPreferences.getBoolean(Constants.PREF_TUTORIAL_COMPLETED, false);
        if (!isTutorialViewed) {
            startActivity(new Intent(this, TutorialActivity.class));
        }

        // If edge screen is enabled in settings & the permission is granted; start it
        //  otherwise stop it
        if (isEdgeSwitchEnabled && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this))) {
            Intent edgeIntent = new Intent(this, EdgeScreenService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(edgeIntent);
            } else {
                startService(edgeIntent);
            }
        } else {
            if (EdgeScreenService.EdgeScreenServiceRunning) {
                stopService(new Intent(this, EdgeScreenService.class));
            }
        }

        // Make the navigation bar white and icons grey on Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setSystemUiVisibility(
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            getWindow().setNavigationBarColor(getColor(R.color.colorPrimaryDark));
        }

        // Make the status bar white and icons grey on Marshmallow and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getColor(R.color.colorPrimaryDark));
        }

        TooltipCompat.setTooltipText(mSettingsButton, getString(R.string.settings));
        TooltipCompat.setTooltipText(mAbout, getString(R.string.about));
        TooltipCompat.setTooltipText(mSavedTextButton, getString(R.string.saved_text));

        // Update the widget with data
        UpdateListService.startActionUpdateWidget(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.tbtn_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.tbtn_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.tbtn_saved_text:
                startActivity(new Intent(MainActivity.this, SavedTextActivity.class));
                break;
        }
    }
}
