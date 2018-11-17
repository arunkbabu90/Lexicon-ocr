package arunkbabu90.lexicon.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import arunkbabu90.lexicon.Constants;
import arunkbabu90.lexicon.R;
import arunkbabu90.lexicon.activities.ExtractActivity;
import arunkbabu90.lexicon.dialogs.ErrorDialog;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements View.OnClickListener
{
    @BindView(R.id.open_image_card) CardView mOpenImageCard;
    @BindView(R.id.capture_camera_card) CardView mCaptureCameraCard;

    private int mDisplayWidth;
    private Uri mCurrentPhotoPath;
    private String mAbstractPhotoPath;

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

        // Find the device's screen width for setting the dialog's size
        if (getActivity() != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            mDisplayWidth = displayMetrics.widthPixels;
        }

        mCaptureCameraCard.setOnClickListener(this);
        mOpenImageCard.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (getActivity() == null) return;

        switch (v.getId())
        {
            case R.id.open_image_card:
                openImage();
                break;
            case R.id.capture_camera_card:
                launchCamera();
                break;
        }
    }

    /**
     * Opens the Storage Access Framework for picking an image file
     */
    private void openImage() {
        Intent pickFile = new Intent(Intent.ACTION_GET_CONTENT);
        pickFile.setType("image/*");
        startActivityForResult(pickFile, Constants.REQUEST_CODE_SAF);
    }

    /**
     * Opens an external Camera App
     */
    private void launchCamera() {
        // Intent to open the external camera app
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check whether the device has a camera
        if (getActivity() != null && takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File imageFile;
            try {
                imageFile = createImageCacheFile();
                if (imageFile != null) {
                    Uri imageURI = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                            Constants.FILE_PROVIDER_AUTHORITY, imageFile);
                    mCurrentPhotoPath = imageURI;
                    mAbstractPhotoPath = imageFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                    startActivityForResult(takePictureIntent, Constants.REQUEST_CODE_CAMERA_MAIN);
                }
            } catch (IOException e) {
                Toast.makeText(getContext(), R.string.file_err, Toast.LENGTH_LONG).show();
            }
        } else {
            // The device has no camera
            DialogFragment noCameraDialog = ErrorDialog.newInstance(getString(R.string.camera_err), mDisplayWidth);
            noCameraDialog.show(getActivity().getSupportFragmentManager(), Constants.CAMERA_ERROR_DIALOG_TAG);
        }
    }

    /**
     * Creates a *.jpg file in this App's private directory (ie, InternalStorage/Android/data/)
     * @return The image file object
     * @throws IOException Thrown if it is unable to create the file
     */
    private File createImageCacheFile() throws IOException {
        if (getActivity() != null) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(new Date());
            String prefix = "PNG_" + timeStamp + "_";
            File directory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile(prefix, Constants.IMAGE_FORMAT_PNG, directory);
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_SAF && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
            }
            if (uri != null) {
                Intent i = new Intent(getActivity(), ExtractActivity.class);
                i.putExtra(Constants.OPENED_FROM_SAF_KEY, true);
                i.putExtra(Constants.IMAGE_URI_KEY, uri.toString());
                startActivity(i);
            }
        }

        if (requestCode == Constants.REQUEST_CODE_CAMERA_MAIN && resultCode == Activity.RESULT_OK) {
            Intent c = new Intent(getActivity(), ExtractActivity.class);
            c.putExtra(Constants.CAPTURED_FROM_CAMERA_KEY, true);
            c.putExtra(Constants.PHOTO_URI_KEY, mCurrentPhotoPath.toString());
            c.putExtra(Constants.CAPTURED_FILE_PATH_KEY, mAbstractPhotoPath);
            startActivity(c);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}