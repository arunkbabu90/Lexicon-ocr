package arunkbabu90.lexicon.services;

import android.app.ActivityOptions;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import arunkbabu90.lexicon.Constants;
import arunkbabu90.lexicon.R;
import arunkbabu90.lexicon.activities.EdgeScreenActivity;
import arunkbabu90.lexicon.activities.SettingsActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class EdgeScreenService extends Service
{
    @BindView(R.id.edge_screen_activator) View mEdgeScreenActivator;

    private View mActivatorFrameLayout;
    private Handler mHandler;
    private WindowManager mWindowManager;
    private boolean mIsBlinking = false;

    public static boolean EdgeScreenServiceRunning = false;  // Used to determine whether the service is running for other classes

    // Required empty public constructor for initialization
    public EdgeScreenService() { }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EdgeScreenServiceRunning = true;

        //Inflate the Edge Screen Activator
        mActivatorFrameLayout = LayoutInflater.from(this).inflate(R.layout.activator_edge_screen, null);

        ButterKnife.bind(this, mActivatorFrameLayout);

        // If the device is running on Android Oreo or greater; push a foreground notification
        //  to prevent the system from killing the Edge-Screen service unexpectedly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    Constants.REQUEST_CODE_EDGE_SERVICE_PENDING_INTENT, settingsIntent, 0);

            String channelId = createNotificationChannel();

            Notification.Builder notificationBuilder = new Notification.Builder(this, channelId)
                    .setColor(getColor(android.R.color.holo_purple))
                    .setSmallIcon(R.drawable.ic_edge_notification)
                    .setContentTitle(getString(R.string.edge_notification_title))
                    .setContentText(getString(R.string.edge_notification_desc))
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setStyle(new Notification.BigTextStyle())
                    .setContentIntent(pendingIntent);
            startForeground(Constants.EDGE_FOREGROUND_SERVICE_ID, notificationBuilder.build());
        }

        // Initialize Shared Preferences
        SharedPreferences mSharedPreferences = getApplicationContext().getSharedPreferences(Constants.PREF_FILE_NAME, 0);
        boolean blinkEdge = mSharedPreferences.getBoolean(Constants.PREF_EDGE_BLINK, true);

        // Add the view to the window
        WindowManager.LayoutParams layoutParams;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        // Specify the EdgeActivator Position
        layoutParams.gravity = Gravity.TOP | Gravity.END;
        layoutParams.x = 0;
        layoutParams.y = 100;

        // Add view to the window
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }
        if (mWindowManager != null) {
            mWindowManager.addView(mActivatorFrameLayout, layoutParams);
        }

        // If the Edge Screen is activated for the first time. Then blink it to get the attention of the user
        if (blinkEdge) {
            SharedPreferences.Editor mPreferenceEditor = mSharedPreferences.edit();
            mPreferenceEditor.putBoolean(Constants.PREF_EDGE_BLINK, false);
            mPreferenceEditor.apply();

            startBlinking(500);
        }

        mEdgeScreenActivator.setOnTouchListener(new View.OnTouchListener() {
            private float initialPosition, finalPosition;
            private final int MIN_DISTANCE = 40;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // This check identifies whether the user swiped from Right to Left over the
                //  Edge-Screen Activator
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Stop the blink if the user touches the Edge Activator
                        if (mIsBlinking) {
                            stopBlinking();
                        }
                        initialPosition = motionEvent.getX();
                        // Glow the Activator when user presses it (Change the color to White)
                        mEdgeScreenActivator.setBackgroundColor(getResources().getColor(R.color.colorActivatorPressed));
                        return true;
                    case MotionEvent.ACTION_UP:
                        view.performClick();
                        finalPosition = motionEvent.getX();
                        float deltaX = initialPosition - finalPosition;
                        // Remove the Activator Glow when user releases it (Revert the color)
                        mEdgeScreenActivator.setBackgroundColor(getResources().getColor(R.color.colorActivatorTransparent));

                        if ((Math.abs(deltaX) > MIN_DISTANCE) && (finalPosition < initialPosition)) {
                            // Right to Left Swipe (#EdgeSwipe)
                            //  So launch the edge screen activity
                            Intent edgeIntent = new Intent(getApplicationContext(), EdgeScreenActivity.class);
                            Bundle slideAnimationBundle = ActivityOptions.makeCustomAnimation(
                                    getApplicationContext(), 0, 0).toBundle();
                            edgeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(edgeIntent, slideAnimationBundle);
                            return true;
                        }
                }
                return false;
            }
        });
    }

    /**
     * Apply the change in position of the view when a configuration change occurs
     */
    private void applyConfigurationChange() {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (mWindowManager != null && mActivatorFrameLayout.isAttachedToWindow()) {
            mWindowManager.removeView(mActivatorFrameLayout);
        }

        // Make the activator visible only if device is in portrait mode or is a tablet
        Resources r = getResources();
        if (!r.getBoolean(R.bool.isLandscape) || r.getBoolean(R.bool.isSW600)) {
            onCreate();
        }
    }


    /**
     * Creates a notification channel for the notification for Android Oreo+ devices
     * @return Channel id of this notification channel
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        String id = Constants.EDGE_CHANNEL_ID;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(id,
                Constants.EDGE_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MIN);
        channel.setDescription(getString(R.string.edge_notification_channel_desc));
        channel.setShowBadge(false);
        channel.enableVibration(false);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        } else {
            Toast.makeText(this, R.string.err_edge_notification_channel, Toast.LENGTH_LONG).show();
            return Constants.NULL;
        }

        return id;
    }

    /**
     * Blink the Edge Screen activator until #stopBlinking() is called
     * @param blinkDelay The delay in milliseconds between subsequent blinks
     */
    private void startBlinking(int blinkDelay) {
        final AnimationDrawable drawable = new AnimationDrawable();
        mHandler = new Handler();

        drawable.addFrame(new ColorDrawable(getResources().getColor(R.color.colorActivatorPressed)), blinkDelay);
        drawable.addFrame(new ColorDrawable(getResources().getColor(R.color.colorActivatorTransparent)), blinkDelay);
        drawable.setOneShot(false);

        mEdgeScreenActivator.setBackground(drawable);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawable.start();
            }
        }, 100);
        mIsBlinking = true;
    }

    /**
     * Stop Blinking the Edge Screen Activator
     */
    private void stopBlinking() {
        mHandler.removeCallbacks(null);
        mIsBlinking = false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        applyConfigurationChange();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EdgeScreenServiceRunning = false;

        if (mActivatorFrameLayout != null) mWindowManager.removeView(mActivatorFrameLayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) stopForeground(true);

        stopSelf();
        System.gc();
    }
}