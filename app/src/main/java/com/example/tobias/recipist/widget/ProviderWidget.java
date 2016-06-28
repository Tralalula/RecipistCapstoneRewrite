package com.example.tobias.recipist.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.activity.MainActivity;

/**
 * Created by Tobias on 26-06-2016.
 */
public class ProviderWidget extends AppWidgetProvider {
    private static int[] sAppWidgetIds;
    private static AppWidgetManager sAppWidgetManager;

    private static final String ACTION_REFRESH = "com.example.tobias.recipist.widget.REFRESH";

    @SuppressLint("NewApi")
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = initViews(context, appWidgetManager, appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private static RemoteViews initViews(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main);

        Intent data = new Intent(context, ServiceWidget.class);
        data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        data.setData(Uri.parse(data.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(widgetId, R.id.widget_main_list_view, data);

        Intent launchIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_main_linear_layout_header, pendingIntent);

        manager.updateAppWidget(widgetId, views);

        return views;
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction())) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            this.onUpdate(context, manager, manager.getAppWidgetIds(new ComponentName(context, ProviderWidget.class)));
        }
    }

    public static Intent receiveBroadcast(Context context) {
        return new Intent(ACTION_REFRESH).setComponent(new ComponentName(context, ProviderWidget.class));
    }
}
