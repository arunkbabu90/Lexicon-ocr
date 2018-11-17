package arunkbabu90.lexicon.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import arunkbabu90.lexicon.Constants;
import arunkbabu90.lexicon.R;
import arunkbabu90.lexicon.dialogs.ErrorDialog;
import arunkbabu90.lexicon.services.EdgeScreenService;
import arunkbabu90.lexicon.services.ScreenCaptureService;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

public class EdgeScreenActivity extends AppCompatActivity implements View.OnClickListener
{
    @BindView(R.id.edge_screen_layout) ConstraintLayout mEdgeScreenLayout;
    @BindView(R.id.rect_tv) TextView mRectangleTextView;
    @BindView(R.id.oval_tv) TextView mOvalTextView;
    @BindView(R.id.fullscreen_tv) TextView mFullScreenTextView;
    @BindView(R.id.camera_tv) TextView mCameraTextView;
    @BindView(R.id.fab_rectangle) FloatingActionButton mRectangleCrop;
    @BindView(R.id.fab_oval) FloatingActionButton mOvalCrop;
    @BindView(R.id.fab_fullscreen) FloatingActionButton mFullScreenCrop;
    @BindView(R.id.fab_camera) FloatingActionButton mLaunchCameraButton;

    private String mAbstractPhotoPath;
    private Uri mCurrentPhotoPath;
    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mDisplayDensity;
    private static int mCropType = -1;

    public static boolean EdgeScreenLaunched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edge_screen);
        ButterKnife.bind(this);

        EdgeScreenLaunched = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mOvalTextView.setVisibility(View.GONE);
            mOvalCrop.hide();
        }

        // Make Edge Screen fully transparent
        getWindow().setFlags(FLAG_LAYOUT_NO_LIMITS, FLAG_LAYOUT_NO_LIMITS);
        getWindow().setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS);

        // In case if the user revokes the permission at any instant; kill the Edge-Screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)
                && EdgeScreenService.EdgeScreenServiceRunning) {
            stopService(new Intent(this, EdgeScreenService.class));

            finish();
        }

        // Find the Height, Width & Density of the device
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mDisplayHeight = displayMetrics.heightPixels;
        mDisplayWidth = displayMetrics.widthPixels;
        mDisplayDensity = displayMetrics.densityDpi;

        // Animate the TextViews in sync with FAB when edge screen is launched
        Animation fabOpenAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        mRectangleCrop.setAnimation(fabOpenAnim);
        mRectangleTextView.setAnimation(fabOpenAnim);
        mOvalCrop.setAnimation(fabOpenAnim);
        mOvalTextView.setAnimation(fabOpenAnim);
        mFullScreenCrop.setAnimation(fabOpenAnim);
        mFullScreenTextView.setAnimation(fabOpenAnim);
        mLaunchCameraButton.setAnimation(fabOpenAnim);
        mCameraTextView.setAnimation(fabOpenAnim);

        // Setup button click listeners
        mRectangleCrop.setOnClickListener(this);
        mOvalCrop.setOnClickListener(this);
        mFullScreenCrop.setOnClickListener(this);
        mLaunchCameraButton.setOnClickListener(this);
        mEdgeScreenLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.fab_rectangle:
                mCropType = Constants.RECTANGLE_CROP_MODE;
                startCapture();
                break;
            case R.id.fab_oval:
                mCropType = Constants.OVAL_CROP_MODE;
                startCapture();
                break;
            case R.id.fab_fullscreen:
                mCropType = Constants.FULLSCREEN_MODE;
                startCapture();
                break;
            case R.id.fab_camera:
                openExternalCamera();
                break;
            case R.id.edge_screen_layout:
                finish();
                break;
        }
    }


    /**
     * Opens an External Camera App
     */
    private void openExternalCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Checking whether the device have a Camera
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            // Create a file to save the image
            File imageFile;
            try {
                imageFile = createImageCacheFile();
                Uri imageURI = FileProvider.getUriForFile(this,
                        Constants.FILE_PROVIDER_AUTHORITY, imageFile);
                mCurrentPhotoPath = imageURI;
                mAbstractPhotoPath = imageFile.getAbsolutePath();
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                startActivityForResult(takePicture, Constants.REQUEST_CODE_CAMERA_EDGE);
            } catch (IOException e) {
                Toast.makeText(EdgeScreenActivity.this, R.string.file_err, Toast.LENGTH_LONG).show();
            }
        } else {
            // The device has no camera
            ErrorDialog.newInstance(getString(R.string.camera_err), mDisplayWidth)
                    .show(getSupportFragmentManager(), Constants.CAMERA_ERROR_DIALOG_TAG);
        }
    }


    /**
     * Initiates the screen capture
     */
    private void startCapture() {
        // Request for media projection permission to the user
        MediaProjectionManager projectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        if (projectionManager != null) {
            // Start the permission request
            startActivityForResult(projectionManager.createScreenCaptureIntent(),
                    Constants.REQUEST_CODE_CAST_PERMISSION);
        }
    }

    /**
     * Returns the currently selected crop type
     * @return The crop type
     */
    public static int getCropType() {
        return mCropType;
    }

    /**
     * Create an image file. This image file will be private to this app
     * @return File The file used for storing the image
     * @throws IOException If the file fails to create
     */
    private File createImageCacheFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(imageFileName, Constants.IMAGE_FORMAT_PNG, storageDirectory);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CAST_PERMISSION) {
            // The Capture Permission result will be received here on whether the Capture permission is
            // denied or not
            if (resultCode == Activity.RESULT_OK) {
                // Start the ScreenShot Service to Capture the Screen
                Intent scrCaptureIntent = new Intent(this, ScreenCaptureService.class);
                scrCaptureIntent.putExtra(Constants.RESULT_CODE_MEDIA_PROJECTION, resultCode);
                scrCaptureIntent.putExtra(Constants.RESULT_INTENT_MEDIA_PROJECTION, data);
                scrCaptureIntent.putExtra(Constants.SCR_DENSITY_KEY, mDisplayDensity);
                scrCaptureIntent.putExtra(Constants.SCR_HEIGHT_KEY, mDisplayHeight);
                scrCaptureIntent.putExtra(Constants.SCR_WIDTH_KEY, mDisplayWidth);
                startService(scrCaptureIntent);
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == Constants.REQUEST_CODE_CAMERA_EDGE && resultCode == Activity.RESULT_OK) {
            // Intent containing the captured image file location & passed to the DisplayActivity
            Intent extractIntent = new Intent(this, ExtractActivity.class);
            extractIntent.putExtra(Constants.CAPTURED_FROM_CAMERA_KEY, true);
            extractIntent.putExtra(Constants.PHOTO_URI_KEY, mCurrentPhotoPath.toString());
            extractIntent.putExtra(Constants.CAPTURED_FILE_PATH_KEY, mAbstractPhotoPath);
            startActivity(extractIntent);
        }

        finish();
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Dim this Activity's Background to focus the user on edge screen contents
        WindowManager.LayoutParams windowManager = getWindow().getAttributes();
        windowManager.dimAmount = 0.70f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Slide out the Edge Screen
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EdgeScreenLaunched = false;
    }

    @Override
    public void finish() {
        super.finish();
        // Slide out the Edge Screen
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);

        // Un-dim the background
        WindowManager.LayoutParams windowManager = getWindow().getAttributes();
        windowManager.dimAmount = 0.0f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }
}
