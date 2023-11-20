package de.unipassau.calorietracker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.widget.RemoteViews;

import java.util.Locale;

import de.unipassau.calorietracker.data.FSDay;
import de.unipassau.calorietracker.ui.dashboard.DashboardFragment;

/**
 * Implementation of App Widget functionality.
 */
public class DisplayTodayAppWidget extends AppWidgetProvider {

	public static final String WIDGET_IDS_KEY = "displaytodaywidgetids";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.hasExtra(WIDGET_IDS_KEY)) {
			int[] ids = intent.getExtras().getIntArray(WIDGET_IDS_KEY);
			this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
		} else super.onReceive(context, intent);
	}

	public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
								int appWidgetId) {

		// Construct the RemoteViews object
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.display_today_app_widget);

		// Update when clicked
		Intent intent = new Intent();
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		int[] arr = {appWidgetId};
		intent.putExtra(DisplayTodayAppWidget.WIDGET_IDS_KEY, arr);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				context, 0, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

		views.setOnClickPendingIntent(R.id.appwidget_vc, pendingIntent);

		getCurrentWidgetData(views, context, () -> appWidgetManager.updateAppWidget(appWidgetId, views));
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		for (int appWidgetId : appWidgetIds) {
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}

	@Override
	public void onEnabled(Context context) {
		// Widget is created
		CommonMethods.updateWidgets(context);
	}

	@Override
	public void onDisabled(Context context) {
		// Widget is disabled
	}

	public static void getCurrentWidgetData(RemoteViews views, Context context, Runnable runnable) {
		FirestoreHandler.getInstance().getDay(DashboardFragment.getDayAsString(), fsDay -> {
			FSDay today = fsDay;
			CommonMethods.maxCalorie(maxcal -> {
				int maxCalorie = maxcal.intValue();
				Long sumCalorie = today.getSumCalorie();

				if (Build.VERSION.SDK_INT >= 31) {
					if (sumCalorie <= maxcal) {
						views.setColorStateList(R.id.appwidget_progressbar, "setProgressTintList", ColorStateList.valueOf(context.getColor(R.color.progressbar_good)));
					} else {
						views.setColorStateList(R.id.appwidget_progressbar, "setProgressTintList", ColorStateList.valueOf(context.getColor(R.color.progressbar_exceed)));
					}
				}

				views.setProgressBar(R.id.appwidget_progressbar, maxCalorie, sumCalorie.intValue(), false);
				views.setTextViewText(R.id.appwidget_text, String.format(Locale.GERMANY, "%d / %d %s", sumCalorie, maxCalorie, context.getString(R.string.unit_cal)));

				runnable.run();
			});
		});

	}
}