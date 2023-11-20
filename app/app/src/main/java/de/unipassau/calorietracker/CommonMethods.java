package de.unipassau.calorietracker;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CommonMethods {
	/**
	 * Update all widgets on the users screen.
	 *
	 * @param context the application/current context
	 */
	public static void updateWidgets(Context context) {
		AppWidgetManager man = AppWidgetManager.getInstance(context);
		int[] ids = man.getAppWidgetIds(new ComponentName(context, DisplayTodayAppWidget.class));
		Intent updateIntent = new Intent();
		updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		updateIntent.putExtra(DisplayTodayAppWidget.WIDGET_IDS_KEY, ids);
		context.sendBroadcast(updateIntent);
	}

	/**
	 * Calculates the maximum calories that should be consumed by the user depending on the values
	 * stored in Firebase
	 *
	 * @param then gets called when the max calorie value is calculated
	 */
	public static void maxCalorie(Consumer<Long> then) {
		FirestoreHandler.getInstance().getUserData(fsUser -> {
			long height = 170;
			long age = 30;
			if (!(fsUser == null || fsUser.height == 0)) {
				height = fsUser.height;
				age = fsUser.age;
			}

			// Get current weight
			long finalHeight = height;
			long finalAge = age;

			List<Map.Entry<String, Long>> wl = fsUser.weightHistory.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
			long weight = 60L;
			weight = wl.get(wl.size() - 1).getValue();
			then.accept((long) (((13.397 * weight) + (4.799 * finalHeight) - (5.667 * finalAge) + 88.362) * 1.55));
		});
	}
}
