package arunkbabu90.lexicon.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import arunkbabu90.lexicon.Constants;
import arunkbabu90.lexicon.R;
import arunkbabu90.lexicon.dialogs.ErrorDialog;
import arunkbabu90.lexicon.services.EdgeScreenService;
import arunkbabu90.lexicon.tutorials.TutorialActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener
{
    @BindView(R.id.about_developer_view) LinearLayout mAboutView;
    @BindView(R.id.licenses_view) LinearLayout mLicensesView;
    @BindView(R.id.launch_tutorial_view) LinearLayout mLaunchTutorialView;
    @BindView(R.id.sw_enable_edge) Switch mEdgeSwitch;
    @BindView(R.id.tv_launch_tr_title) TextView mLaunchTrTitle;
    @BindView(R.id.tv_launch_tr_desc) TextView mLaunchTrDesc;
    @BindView(R.id.tv_license_title) TextView mLicenseTrTitle;
    @BindView(R.id.tv_license_desc) TextView mLicenseTrDesc;
    @BindView(R.id.tv_about_title) TextView mAboutTitle;
    @BindView(R.id.tv_about_desc) TextView mAboutDesc;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mPrefEditor;
    private boolean mIsPermissionDenied;
    private int mDisplayWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        // Get an instance of the shared preferences
        mSharedPreferences = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mDisplayWidth = displayMetrics.widthPixels;

        // Set ClickListeners
        mAboutView.setOnClickListener(this);
        mLicensesView.setOnClickListener(this);
        mEdgeSwitch.setOnClickListener(this);
        mLaunchTutorialView.setOnClickListener(this);

        // Disable all views other than the Enable Edge Switch in tutorial mode
        boolean tutorialMode = getIntent().getBooleanExtra(Constants.IS_TUTORIAL_MODE_KEY, false);
        if (tutorialMode) {
            mLaunchTrTitle.setTextColor(getResources().getColor(R.color.colorDisabled));
            mLaunchTrDesc.setTextColor(getResources().getColor(R.color.colorDisabled));

            mLicenseTrTitle.setTextColor(getResources().getColor(R.color.colorDisabled));
            mLicenseTrDesc.setTextColor(getResources().getColor(R.color.colorDisabled));

            mAboutTitle.setTextColor(getResources().getColor(R.color.colorDisabled));
            mAboutDesc.setTextColor(getResources().getColor(R.color.colorDisabled));

            mLaunchTutorialView.setEnabled(false);
            mLaunchTutorialView.setClickable(false);

            mLicensesView.setEnabled(false);
            mLicensesView.setClickable(false);

            mAboutView.setEnabled(false);
            mAboutView.setClickable(false);
        }

        // Only show the up action if it's not in tutorial mode
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            if (!tutorialMode)
                ab.setDisplayHomeAsUpEnabled(true);
            else
                ab.setDisplayHomeAsUpEnabled(false);
        }

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

        boolean edgeSwitchON = mSharedPreferences.getBoolean(Constants.PREF_EDGE_SWITCH_STATE, false);
        if (edgeSwitchON) {
            mEdgeSwitch.setChecked(true);
        } else {
            mEdgeSwitch.setChecked(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.sw_enable_edge:
                boolean edgeSwitchON = mEdgeSwitch.isChecked();
                mPrefEditor = mSharedPreferences.edit();
                mPrefEditor.putBoolean(Constants.PREF_EDGE_SWITCH_STATE, edgeSwitchON);
                mPrefEditor.apply();
                if (edgeSwitchON) {
                    enableEdge();
                } else {
                    disableEdge();
                }
                break;
            case R.id.about_developer_view:
                startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                break;
            case R.id.licenses_view:
                startActivity(new Intent(SettingsActivity.this, LicenseActivity.class));
                break;
            case R.id.launch_tutorial_view:
                startActivity(new Intent(SettingsActivity.this, TutorialActivity.class));
                break;
        }
    }


    /**
     * Activates the Edge Screen
     */
    private void enableEdge() {
        mIsPermissionDenied = false;
        mPrefEditor = mSharedPreferences.edit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {
            // Overlay Permission is Denied so start Permissions Activity to ask user
            //  the required permissions
            mIsPermissionDenied = true;
            mEdgeSwitch.setChecked(false);
            startActivity(new Intent(this, PermissionsActivity.class));
        }

        // If the permission is denied and edge screen service is running; Stop it
        if (mIsPermissionDenied && EdgeScreenService.EdgeScreenServiceRunning) {
            stopService(new Intent(this, EdgeScreenService.class));
        }

        // If the overlay permission is granted; start the edge screen
        // Only start Edge Screen Service if it isn't running
        if (!mIsPermissionDenied && !EdgeScreenService.EdgeScreenServiceRunning) {
            Intent edgeServiceIntent = new Intent(SettingsActivity.this, EdgeScreenService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(edgeServiceIntent);
            } else {
                startService(edgeServiceIntent);
            }

            // Show a warning to the user that; In case if the screen appears distorted when capturing
            // screen. Then the device's gpu isn't supported
            boolean show = mSharedPreferences.getBoolean(Constants.PREF_SHOW_CAST_IMG_DISTORTION_WARNING, true);
            if (show) {
                ErrorDialog.newInstance(getString(R.string.err_gpu_incompatible), mDisplayWidth);
                mPrefEditor.putBoolean(Constants.PREF_SHOW_CAST_IMG_DISTORTION_WARNING, false);
            }
        }
        mPrefEditor.putBoolean(Constants.PREF_EDGE_SWITCH_STATE, mEdgeSwitch.isChecked());
        mPrefEditor.apply();
    }

    /**
     * Deactivates the Edge Screen
     */
    private void disableEdge() {
        if (EdgeScreenService.EdgeScreenServiceRunning) {
            stopService(new Intent(this, EdgeScreenService.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Checks to see if the permission is granted or denied
        if (mEdgeSwitch.isChecked()) {
            enableEdge();
        }
    }
}
