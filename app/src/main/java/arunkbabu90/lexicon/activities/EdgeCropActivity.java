package arunkbabu90.lexicon.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import arunkbabu90.lexicon.Constants;
import arunkbabu90.lexicon.LexUtils;
import arunkbabu90.lexicon.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class EdgeCropActivity extends AppCompatActivity
{
    @BindView(R.id.edge_crop_view) CropImageView mCropView;
    @BindView(R.id.fab_edge_crop) FloatingActionButton mFabCropDone;
    @BindView(R.id.crop_coordinator_layout) CoordinatorLayout mCoordinatorLayout;

    private Target mTarget;
    private Bitmap mCroppedImage;
    private Bitmap mLoadedBitmap;
    private Uri mSourcePath;
    private String mAbsolutePath;
    private boolean mAvoidErrorToast = false;

    public static boolean CropImageActivityLaunched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        ButterKnife.bind(this);

        CropImageActivityLaunched = true;

        // Enable Exclusive FullScreen mode
        mCoordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mSourcePath = Uri.parse(getIntent().getStringExtra(Constants.SCR_CAPTURED_IMG_URI_KEY));

        // Load the bitmap into the crop view
        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mLoadedBitmap = bitmap;
                mCropView.setImageBitmap(bitmap);
                setCropMode();
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Toast.makeText(EdgeCropActivity.this, getString(R.string.err_default), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };
        Picasso.get().load(mSourcePath).into(mTarget);

        // Turns ON Guidelines in the preferred crop shape
        mCropView.setGuidelines(CropImageView.Guidelines.ON_TOUCH);
    }

    /**
     * Set the crop mode based on the which button the user clicked in EDGE SCREEN
     */
    private void setCropMode() {
        int cropModeID = EdgeScreenActivity.getCropType();
        switch (cropModeID)
        {
            case Constants.RECTANGLE_CROP_MODE:
                mCropView.setCropShape(CropImageView.CropShape.RECTANGLE);
                mFabCropDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCroppedImage = mCropView.getCroppedImage();
                        saveCropImage();
                    }
                });
                break;
            case Constants.OVAL_CROP_MODE:
                mCropView.setCropShape(CropImageView.CropShape.OVAL);
                mFabCropDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // This step is to convert the Bitmap to oval shape otherwise it will still be
                        //  rectangular in shape. The bitmap is converted to oval at runtime
                        mCroppedImage = CropImage.toOvalBitmap(mLoadedBitmap);
                        saveCropImage();
                    }
                });
                break;
            case Constants.FULLSCREEN_MODE:
                // If FullScreen then there's no need to crop so just Directly save it
                mCroppedImage = mLoadedBitmap;
                // While Capturing in Full Screen Mode it is found that an Error Toast below "Something went wrong..."
                //  is automatically getting thrown even on Success so this field can be used to prevent it from showing
                mAvoidErrorToast = true;
                saveCropImage();
                finish();
                break;
        }
    }

    /**
     *  Save the cropped image to storage and launch the ExtractActivity
     */
    private void saveCropImage() {
        if (mCroppedImage != null) {
            // Create a file
            Uri fileUri = null;
            try {
                File imageFile = createImageCacheFile();
                fileUri = FileProvider.getUriForFile(this,
                        Constants.FILE_PROVIDER_AUTHORITY, imageFile);
                mAbsolutePath = imageFile.getAbsolutePath();
            } catch (IOException e) {
                Toast.makeText(EdgeCropActivity.this, R.string.file_err, Toast.LENGTH_LONG).show();
            }

            if (fileUri == null) return;

            // Write the image pixels to the file
            OutputStream fileStream = null;
            try {
                fileStream = getApplicationContext().getContentResolver().openOutputStream(fileUri);
                mCroppedImage.compress(Bitmap.CompressFormat.PNG, 100, fileStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                LexUtils.closeQuietly(fileStream);
            }

            Intent intent = new Intent(EdgeCropActivity.this, ExtractActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.SCR_CAPTURED_IMG_URI_KEY, fileUri.toString());
            intent.putExtra(Constants.SCR_CAPTURED_IMG_ABS_PATH_KEY, mAbsolutePath);
            intent.putExtra(Constants.CAPTURED_FROM_SCREEN_KEY, true);
            startActivity(intent);

            finish();
        } else {
            if (!mAvoidErrorToast){
                Toast.makeText(this, R.string.err_default, Toast.LENGTH_LONG).show();
            }
            mAvoidErrorToast = false;
            finish();
        }
    }


    /**
     * Create a file for the image to be taken from camera. This image file will be private to this app
     * @return File The file used for storing the image
     * @throws IOException If the file was not created
     */
    private File createImageCacheFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(imageFileName, Constants.IMAGE_FORMAT_PNG, storageDirectory);
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CropImageActivityLaunched = false;
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear the Cropped image
        if (mCroppedImage != null) {
            mCroppedImage.recycle();
        }
        mCropView.clearImage();
    }
}