package arunkbabu90.lexicon.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.TooltipCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import arunkbabu90.lexicon.Constants;
import arunkbabu90.lexicon.R;
import arunkbabu90.lexicon.dialogs.ErrorDialog;
import arunkbabu90.lexicon.dialogs.ExtractDialog;
import arunkbabu90.lexicon.dialogs.LoadingDialog;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ExtractActivity extends AppCompatActivity implements View.OnClickListener
{
    @BindView(R.id.iv_current_image) ImageView mImageView;
    @BindView(R.id.pb_image_loading) ProgressBar mLoadingCircle;
    @BindView(R.id.crop_view) CropImageView mCropView;
    @BindView(R.id.open_button) Button mOpenButton;
    @BindView(R.id.crop_button) ImageButton mCropButton;
    @BindView(R.id.extract_button) Button mExtractButton;

    private boolean mIsOpenedFromSAF;
    private boolean mIsCapturedFromCamera;
    private boolean mIsCapturedFromScreen;
    private boolean mIsSharedFromGallery;
    private boolean mIsCropButtonPressedBefore;
    private int mDisplayWidth;
    private Target mTarget;
    private StringBuilder mOcrStringBuilder;
    private DialogFragment mExtractingDialog;
    private DialogFragment mExtractedTextDialog;
    private DialogFragment mErrorDialog;
    private Bitmap mLoadedBitmap;
    private String mCameraFilePath;
    private String mExtractedText;
    private Uri mCurrentImageUri;

    private String cTextCache;
    private String cOpenButtonText;
    private int cShortAnimationDuration;
    private int cMediumAnimationDuration;
    private int cPreviousRotation = 0;
    private int cClicks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extract);

        ButterKnife.bind(this);

        mLoadingCircle.setVisibility(View.VISIBLE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mDisplayWidth = displayMetrics.widthPixels;

        // Cache the short/medium animation time for animating views
        cShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        cMediumAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        // Set the navigation bar color to grey
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorDeepGrey));

        mIsOpenedFromSAF = getIntent().getBooleanExtra(Constants.OPENED_FROM_SAF_KEY, false);
        mIsCapturedFromCamera = getIntent().getBooleanExtra(Constants.CAPTURED_FROM_CAMERA_KEY, false);

        // If the image is opened from Storage Access Framework
        if (mIsOpenedFromSAF) {
            mCurrentImageUri = Uri.parse(getIntent().getStringExtra(Constants.IMAGE_URI_KEY));
            mCameraFilePath = Constants.NULL;
            mOpenButton.setText(getString(R.string.open));
        }

        // If the image is captured from camera
        if (mIsCapturedFromCamera) {
            mCurrentImageUri = Uri.parse(getIntent().getStringExtra(Constants.PHOTO_URI_KEY));
            mCameraFilePath = getIntent().getStringExtra(Constants.CAPTURED_FILE_PATH_KEY);
            mOpenButton.setText(R.string.capture);
        }

        // If the image is captured using Edge Screen
        if (mIsCapturedFromScreen) {
            mOpenButton.setText(R.string.close);
        }

        // If the image is shared from external gallery applications
        if (mIsSharedFromGallery) {
            mOpenButton.setText(R.string.close);
        }

        // Load the image using Picasso
        loadImage(mCurrentImageUri);

        mOpenButton.setOnClickListener(this);
        mCropButton.setOnClickListener(this);
        mExtractButton.setOnClickListener(this);
    }


    /**
     * Loads the image into the ImageView
     * @param imageUri The Uri of the image
     */
    private void loadImage(Uri imageUri) {
        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mLoadedBitmap = Bitmap.createBitmap(bitmap);
                mLoadingCircle.setVisibility(View.GONE);
                mImageView.setImageBitmap(bitmap);
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                mLoadingCircle.setVisibility(View.GONE);
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };
        Picasso.get().load(imageUri).into(mTarget);
    }

    /**
     * Extract the text from the image
     */
    private void extractText() {
        if (mLoadedBitmap == null) {
            Toast.makeText(this, R.string.extract_failed, Toast.LENGTH_LONG).show();
            return;
        }

        // If there is text inside the text cache; display it
        if (cTextCache != null && !cTextCache.matches("")) {
            mExtractedText = cTextCache;
            showExtractedTextDialog(mExtractedText);
            return;
        }

        // Disable the extract button to prevent the use from pressing it again. Otherwise two text
        // extraction instances will be created which eventually slows down both
        mExtractButton.setClickable(false);
        mExtractButton.setEnabled(false);

        // Show the loading text dialog
        mExtractingDialog = LoadingDialog.newInstance(getString(R.string.extracting_text));
        mExtractingDialog.show(getSupportFragmentManager(), Constants.EXTRACTING_TEXT_DIALOG_TAG);

        FirebaseVisionImage visionImage = FirebaseVisionImage.fromBitmap(mLoadedBitmap);
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        mOcrStringBuilder = new StringBuilder();

        textRecognizer.processImage(visionImage)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        if (mExtractingDialog != null) mExtractingDialog.dismiss();
                        List<FirebaseVisionText.TextBlock> textBlocks = firebaseVisionText.getTextBlocks();

                        boolean isFirstRun = true;
                        for (int i = 0; i < textBlocks.size(); i++) {
                            List<FirebaseVisionText.Line> lines = textBlocks.get(i).getLines();
                            if (!isFirstRun) mOcrStringBuilder.append("\n\n");
                            isFirstRun = false;
                            for (int j = 0; j < lines.size(); j++) {
                                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                                for (int k = 0; k < elements.size(); k++) {
                                    mOcrStringBuilder.append(elements.get(k).getText());
                                    mOcrStringBuilder.append(" ");
                                }
                            }
                        }
                        mExtractedText = mOcrStringBuilder.toString();
                        cTextCache = mExtractedText;
                        mExtractButton.setClickable(true);
                        mExtractButton.setEnabled(true);
                        showExtractedTextDialog(mExtractedText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (mExtractingDialog != null) mExtractingDialog.dismiss();
                        mExtractButton.setClickable(true);
                        mExtractButton.setEnabled(true);
                        mExtractedText = "";
                        cTextCache = "";
                        e.printStackTrace();
                        showErrorDialog(getString(R.string.err_dependency_unavailable));
                    }
                });
    }


    /**
     * Decides whether to open the camera or Storage Access Framework based on the user selection
     */
    private void openCameraOrFileOrClose() {
        if (mIsOpenedFromSAF) {
            Intent safIntent = new Intent(Intent.ACTION_GET_CONTENT);
            safIntent.setType("image/*");
            startActivityForResult(safIntent, Constants.REQUEST_CODE_SAF_EXTRACT);
        } else if (mIsCapturedFromCamera) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File imageFile;
                try {
                    imageFile = createImageCacheFile();
                    Uri imageUri = FileProvider.getUriForFile(this, Constants.FILE_PROVIDER_AUTHORITY, imageFile);
                    mCurrentImageUri = imageUri;
                    mCameraFilePath = imageFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(takePictureIntent, Constants.REQUEST_CODE_CAMERA_EXTRACT);
                } catch (IOException e) {
                    Toast.makeText(this, getString(R.string.file_err), Toast.LENGTH_LONG).show();
                }
            } else {
                // The device has no camera
                DialogFragment noCameraDialog = ErrorDialog.newInstance(getString(R.string.camera_err), mDisplayWidth);
                noCameraDialog.show(getSupportFragmentManager(), Constants.CAMERA_ERROR_DIALOG_TAG);
            }
        } else if (mIsCapturedFromScreen || mIsSharedFromGallery) {
            finish();
        }
    }

    /**
     * Show the image crop view so that the user can crop or rotate the image with cross fade animation
     */
    private void activateCropMode() {
        mIsCropButtonPressedBefore = true;

        mImageView.setVisibility(View.GONE);
        mCropView.setVisibility(View.VISIBLE);
        mCropView.animate().rotation(0).setDuration(0);

        cOpenButtonText = mOpenButton.getText().toString();

        // Animate the open button with fade-in-out animation
        mOpenButton.animate().alpha(0f).setDuration(cShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mOpenButton.setText(R.string.cancel);
                        mOpenButton.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                        mOpenButton.animate().alpha(1f).setDuration(cShortAnimationDuration);
                    }
                });

        // Animate the crop button
        mCropButton.animate().alpha(0f).setDuration(cShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TooltipCompat.setTooltipText(mCropButton, getString(R.string.rotate));
                        mCropButton.setContentDescription(getString(R.string.rotate));
                        mCropButton.setImageResource(R.drawable.ic_action_rotate);
                        mCropButton.animate().alpha(1f).setDuration(cShortAnimationDuration);
                    }
                });


        // Animate the extract button
        mExtractButton.animate().alpha(0f).setDuration(cShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mExtractButton.setText(R.string.apply);
                        mExtractButton.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        mExtractButton.animate().alpha(1f).setDuration(cShortAnimationDuration);
                    }
                });

        mCropView.setImageBitmap(mLoadedBitmap);
        mCropView.setGuidelines(CropImageView.Guidelines.ON_TOUCH);
        mCropView.setScaleType(CropImageView.ScaleType.FIT_CENTER);
        mCropView.setCropShape(CropImageView.CropShape.RECTANGLE);
    }

    /**
     * Hides the image crop view and reverts all the view modifications with cross fade animation
     */
    private void deactivateCropMode() {
        mIsCropButtonPressedBefore = false;

        cPreviousRotation = 0;

        mImageView.setVisibility(View.VISIBLE);
        mCropView.setVisibility(View.GONE);
        mCropView.setRotatedDegrees(0);

        // Animate the open button
        mOpenButton.setEnabled(true);
        mOpenButton.setClickable(true);
        mOpenButton.animate().alpha(0f).setDuration(cShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mOpenButton.setText(cOpenButtonText);
                        mOpenButton.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        mOpenButton.animate().alpha(1f).setDuration(cShortAnimationDuration);
                    }
                });

        // Animate the crop button
        mCropButton.setEnabled(true);
        mCropButton.setClickable(true);
        mCropButton.animate().alpha(0f).setDuration(cShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TooltipCompat.setTooltipText(mCropButton, getString(R.string.crop_or_rotate));
                        mCropButton.setContentDescription(getString(R.string.crop_or_rotate));
                        mCropButton.setImageResource(R.drawable.ic_action_crop_rotate);
                        mCropButton.animate().alpha(1f).setDuration(cShortAnimationDuration);
                    }
                });

        // Animate the extract button
        mExtractButton.setEnabled(true);
        mExtractButton.setClickable(true);
        mExtractButton.animate().alpha(0f).setDuration(cShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mExtractButton.setText(R.string.extract);
                        mExtractButton.setTextColor(getResources().getColor(android.R.color.holo_purple));
                        mExtractButton.animate().alpha(1f).setDuration(cShortAnimationDuration);
                    }
                });
    }

    /**
     * Simulates the image rotation by 90 degrees. This won't actually rotate the image
     * but instead it animates the rotation and returns the actual value of rotation so that
     * it can be used to apply the rotation to image after confirmation
     */
    private void rotateImage() {
        mCropView.animate().rotation(cPreviousRotation -= 90).setDuration(cShortAnimationDuration);
    }

    /**
     * Applies the cropped image back to image view
     */
    private void applyCrop() {
        mCropView.setRotatedDegrees(cPreviousRotation);
        mLoadedBitmap = Bitmap.createBitmap(mCropView.getCroppedImage());
        mImageView.setImageBitmap(mLoadedBitmap);
        deactivateCropMode();
    }


    /**
     * Displays the ExtractDialog
     * @param extractedText The text to be displayed in the dialog fragment
     */
    private void showExtractedTextDialog(String extractedText) {
        mExtractedTextDialog = ExtractDialog.newInstance(extractedText, mDisplayWidth);
        mExtractedTextDialog.show(getSupportFragmentManager(), Constants.EXTRACTED_TEXT_DIALOG_TAG);
    }

    /**
     * Displays the error dialog
     * @param errorMessage The error message to be displayed
     */
    private void showErrorDialog(String errorMessage) {
        mErrorDialog = ErrorDialog.newInstance(errorMessage, mDisplayWidth);
        mErrorDialog.show(getSupportFragmentManager(), Constants.ERROR_DIALOG_TAG);
    }


    /**
     * Creates a *.jpg file in this App's private directory (ie, InternalStorage/Android/data/)
     * @return The image file object
     * @throws IOException Thrown if it is unable to create the file
     */
    private File createImageCacheFile() throws IOException {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(new Date());
            String prefix = "JPEG_" + timeStamp + "_";
            File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            return File.createTempFile(prefix, ".jpg", directory);
    }

    /**
     * Clears the text in text cache and extracted text variables
     */
    private void clearGarbage() {
        mExtractedText = null;
        cTextCache = null;
    }


    /**
     * Deletes all the files in the directory of the specified file's path
     * @param filePath The path of the file in which all files in the directory needs to be deleted (can include path separators)
     */
    private void deleteAllFiles(String filePath) {
        if (mIsCapturedFromCamera && !mCameraFilePath.equals(Constants.NULL)) {
            int lastIndex = mCameraFilePath.lastIndexOf("/");
            filePath = filePath.substring(0, lastIndex);
            File file = new File(filePath);
            boolean isDeleted = false;

            /*
             * Code adapted from https://stackoverflow.com/questions/4943629/how-to-delete-a-whole-folder-and-content
             */
            if (file.isDirectory()) {
                String[] children = file.list();

                for (String child: children) {
                    isDeleted = new File(file, child).delete();
                }
            }

            // TODO: Should be deleted after the application is complete
            if (isDeleted) {
                Toast.makeText(this, "Files Successfully Deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "File Deletion Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Constants.REQUEST_CODE_SAF_EXTRACT && resultCode == Activity.RESULT_OK) {
            mIsOpenedFromSAF = true;
            mOpenButton.setText(R.string.open);

            if (data != null) {
                mCurrentImageUri = data.getData();
            }

            if (mCurrentImageUri != null) {
                clearGarbage();
            }

            loadImage(mCurrentImageUri);
        }

        if (requestCode == Constants.REQUEST_CODE_CAMERA_EXTRACT && resultCode == Activity.RESULT_OK) {
            mIsCapturedFromCamera = true;
            mOpenButton.setText(R.string.capture);

            if (data != null) {
                mCurrentImageUri = data.getData();
            }
            clearGarbage();
            loadImage(mCurrentImageUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (cTextCache != null && !cTextCache.equals("")) outState.putString(Constants.TEXT_CACHE_KEY, cTextCache);

        if (cOpenButtonText != null && !cOpenButtonText.equals(""))
            outState.putString(Constants.OPEN_BUTTON_TEXT_CACHE_KEY, cOpenButtonText);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState == null) return;

        cTextCache = savedInstanceState.getString(Constants.TEXT_CACHE_KEY);
        cOpenButtonText = savedInstanceState.getString(Constants.OPEN_BUTTON_TEXT_CACHE_KEY);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.open_button:
                if (!mIsCropButtonPressedBefore) {
                    // Crop mode is deactivated; so normal button behaviours will be enabled
                    openCameraOrFileOrClose();
                } else {
                    // Crop Mode is activated so this button becomes "CANCEL" Button
                    // So deactivate crop mode on click
                    deactivateCropMode();
                }
                break;
            case R.id.crop_button:
                if (!mIsCropButtonPressedBefore) {
                    // Crop mode is OFF; This button is "CROP OR ROTATE" Button
                    // So activate crop mode on click
                    activateCropMode();
                } else {
                    // Crop mode is ON; This button becomes "ROTATE" Button
                    // So rotate the image on click
                    rotateImage();
                }
                break;
            case R.id.extract_button:
                if (!mIsCropButtonPressedBefore) {
                    // Crop mode is OFF; This button is "EXTRACT" Button
                    // So extract the text on click
                    extractText();
                } else {
                    // Crop mode is ON; This button becomes "APPLY" Button
                    // So apply the cropped image to ImageView on click
                    applyCrop();
                }
                break;
        }
    }


    @Override
    protected void onPause() {
        if (mExtractingDialog != null) mExtractingDialog.dismiss();
        if (mExtractedTextDialog != null) mExtractedTextDialog.dismiss();
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        if (mTarget != null) Picasso.get().cancelRequest(mTarget);
        if(mImageView != null) Picasso.get().cancelRequest(mImageView);

        if (mLoadedBitmap != null) {
            mLoadedBitmap = null;
        }

        // Delete the file captured from Camera on Exit.
        deleteAllFiles(mCameraFilePath);

        mExtractedText = null;
        cTextCache = null;

        System.gc();
        super.onDestroy();
    }
}
