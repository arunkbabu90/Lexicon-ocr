package arunkbabu90.lexicon.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import arunkbabu90.lexicon.R;

/**
 * Implementation of App Widget functionality.
 */
public class SavedTextsProvider extends AppWidgetProvider
{
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.saved_texts_widget);

        // Intent to navigate to the SavedTextActivity onClick of the widget
        Intent updateIntent = new Intent(context, ListWidgetService.class);
        views.setRemoteAdapter(R.id.lv_widget_saved, updateIntent);
        views.setEmptyView(R.id.lv_widget_saved, R.id.widget_empty_view);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Updates the Text Widget with all the text data from database
     * @param context The context
     * @param appWidgetManager The AppWidgetManager of the widget
     * @param appWidgetIds The widget ids
     */
    public static void updateTextWidgets(Context context, AppWidgetManager appWidgetManager,
                                         int[] appWidgetIds) {
        // Even though we have only one widget for this app. It's always a good idea to check
        //  for multiple widgets
        for (int id : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        UpdateListService.startActionUpdateWidget(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        UpdateListService.startActionUpdateWidget(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}