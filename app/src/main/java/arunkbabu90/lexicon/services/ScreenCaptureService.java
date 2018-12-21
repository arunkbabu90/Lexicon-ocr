package arunkbabu90.lexicon.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import arunkbabu90.lexicon.Constants;
import arunkbabu90.lexicon.LexUtils;
import arunkbabu90.lexicon.RemoveBlackBands;
import arunkbabu90.lexicon.activities.EdgeCropActivity;
import arunkbabu90.lexicon.activities.EdgeScreenActivity;
import arunkbabu90.lexicon.activities.ExtractActivity;

/**
 * Takes a screenshot of the current screen using MediaProjection API (Screen Cast)
 */
@SuppressLint("NewApi")
public class ScreenCaptureService extends Service
{
    private final int CAPTURE_FLAGS =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                    | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mDisplayDensity;
    private MediaProjection mProjection;
    private ImageReader mImageReader;
    private VirtualDisplay mVirtualDisplay;
    private Handler mHandler;
    private final HandlerThread mHandlerThread = new HandlerThread(getClass().getSimpleName(), Process.THREAD_PRIORITY_BACKGROUND);

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mDisplayDensity = intent.getIntExtra(Constants.SCR_DENSITY_KEY, 0);
        mDisplayHeight = intent.getIntExtra(Constants.SCR_HEIGHT_KEY, 0);
        mDisplayWidth = intent.getIntExtra(Constants.SCR_WIDTH_KEY, 0);

        MediaProjectionManager mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        getSystemService(Context.WINDOW_SERVICE);

        // Fully initialize the MediaProjection API to start the screen capture
        if (mProjectionManager != null) {
            mProjection = mProjectionManager.getMediaProjection(intent.getIntExtra(Constants.RESULT_CODE_MEDIA_PROJECTION,
                    -1), (Intent) intent.getParcelableExtra(Constants.RESULT_INTENT_MEDIA_PROJECTION));
        }

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Initialize the ImageReader to read image pixels from the VirtualDisplay
                mImageReader = ImageReader.newInstance(mDisplayWidth, mDisplayHeight,
                        PixelFormat.RGBA_8888, 1);

                // Create a Virtual Display in which the image is rendered
                mVirtualDisplay = mProjection.createVirtualDisplay(Constants.VIRTUAL_DISPLAY_NAME, mDisplayWidth,
                        mDisplayHeight, mDisplayDensity, CAPTURE_FLAGS,
                        mImageReader.getSurface(), null, mHandler);

                mImageReader.setOnImageAvailableListener(new ImageResultListener(), mHandler);
            }
        }, 1000);

        return super.onStartCommand(intent, flags, startId);
    }

    public class ImageResultListener implements ImageReader.OnImageAvailableListener
    {
        @Override
        public void onImageAvailable(ImageReader imageReader) {

            //Extract the image from the ImageReader Surface
            Image image = imageReader.acquireLatestImage();

            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * mDisplayWidth;
            int bitmapWidth = mDisplayWidth + rowPadding / pixelStride;

            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, mDisplayHeight, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);

            mProjection.stop();
            image.close();

            // Trim off the bitmap to remove the black Bands
            // The black margins/bands seem to be captured along with the screen
            // So it needs to be removed
            Bitmap cropped = RemoveBlackBands.trim(bitmap);
            bitmap.recycle();

            Uri imageURI = null;
            File imageFile = null;
            OutputStream outputStream = null;
            try {
                imageFile = createImageCacheFile();
                imageURI = FileProvider.getUriForFile(getApplicationContext(),
                        Constants.FILE_PROVIDER_AUTHORITY, imageFile);
                outputStream = getApplicationContext().getContentResolver().openOutputStream(imageURI);
                cropped.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                LexUtils.closeQuietly(outputStream);
            }

            if (imageURI == null) return;

            // Launch the CropImageActivity if the mode is not FullScreen & launch ExtractActivity
            //  directly otherwise
            Intent imageCropIntent;
            if (EdgeScreenActivity.getCropType() == Constants.FULLSCREEN_MODE) {
                imageCropIntent = new Intent(ScreenCaptureService.this, ExtractActivity.class);
                imageCropIntent.putExtra(Constants.SCR_CAPTURED_IMG_ABS_PATH_KEY, imageFile.getAbsolutePath());
            } else {
                imageCropIntent = new Intent(ScreenCaptureService.this, EdgeCropActivity.class);
            }
            imageCropIntent.putExtra(Constants.SCR_CAPTURED_IMG_URI_KEY, imageURI.toString());
            imageCropIntent.putExtra(Constants.CAPTURED_FROM_SCREEN_KEY, true);
            imageCropIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(imageCropIntent);

            // Stop the service
            stopSelf();
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
    public void onDestroy() {
        super.onDestroy();

        // Free up resources
        mProjection.stop();
        mVirtualDisplay.release();
        mHandlerThread.quit();
    }
}
