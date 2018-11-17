package arunkbabu90.lexicon.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import arunkbabu90.lexicon.R;
import arunkbabu90.lexicon.database.AppExecutor;
import arunkbabu90.lexicon.database.TextDatabase;

public class UpdateListService extends IntentService
{
    public static final String ACTION_UPDATE_WIDGET = "android.appwidget.action.APPWIDGET_UPDATE";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public UpdateListService() {
        super("UpdateListService");
    }

    /**
     * Starts this service to update the saved texts to the widget. If
     * the service is already performing a task this action will be queued
     * @param context The Context
     */
    public static void startActionUpdateWidget(Context context) {
        Intent updateIntent = new Intent(context, UpdateListService.class);
        updateIntent.setAction(ACTION_UPDATE_WIDGET);
        context.startService(updateIntent);
    }

    /**
     * Updates the saved text list from the database to this widget
     */
    private void handleActionUpdateSavedTextList() {
        AppExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                TextDatabase db = TextDatabase.getInstance(getApplicationContext());
                db.textDao().loadTexts();
            }
        });

        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = widgetManager.getAppWidgetIds(new ComponentName(this, SavedTextsProvider.class));
        widgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_widget_saved);
        // Now Update all the widgets
        SavedTextsProvider.updateTextWidgets(this, widgetManager, appWidgetIds);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if (ACTION_UPDATE_WIDGET.equals(action)) {
            handleActionUpdateSavedTextList();
        }
    }
}
