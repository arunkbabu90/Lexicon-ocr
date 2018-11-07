package arunkbabu90.lexicon.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.TooltipCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import arunkbabu90.lexicon.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    @BindView(R.id.tbtn_settings) ImageButton mSettingsButton;
    @BindView(R.id.tbtn_saved_text) ImageButton mSavedTextButton;
    @BindView(R.id.tbtn_about) ImageButton mAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mAbout.setOnClickListener(this);
        mSettingsButton.setOnClickListener(this);
        mSavedTextButton.setOnClickListener(this);

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
