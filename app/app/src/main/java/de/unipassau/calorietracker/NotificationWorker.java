package de.unipassau.calorietracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationWorker extends Worker {
	private static final int NOTIFICATION_ID = 1;
	private static final String NOTIFICATION_CHANNEL_ID = "CALORIE_REMINDER_CHANNEL";

	public NotificationWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
		super(appContext, workerParams);
	}

	@NonNull
	@Override
	public Result doWork() {
		Context context = getApplicationContext();

		// Set the open app intent
		Intent i = new Intent(context, LauncherActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE);

		// Build the Notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
		builder.setContentTitle(context.getString(R.string.notification_title));
		builder.setContentText(context.getString(R.string.notification_text));
		builder.setSmallIcon(R.drawable.ic_baseline_local_fire_department_24);
		builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
		builder.setContentIntent(pendingIntent);

		// Create a notification channel
		NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
		channel.setDescription(context.getString(R.string.notification_channel_decription));
		NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
		notificationManager.createNotificationChannel(channel);

		// Send notification
		notificationManager.notify(NOTIFICATION_ID, builder.build());


		// Reschedule task
		Calendar current = Calendar.getInstance();

		// Morning notification
		Calendar calendarMorning = Calendar.getInstance();
		calendarMorning.setTimeInMillis(System.currentTimeMillis());
		calendarMorning.set(Calendar.HOUR_OF_DAY, 8);
		calendarMorning.set(Calendar.MINUTE, 0);
		calendarMorning.set(Calendar.SECOND, 0);
		if (calendarMorning.before(current))
			calendarMorning.add(Calendar.HOUR_OF_DAY, 24);

		long delay = calendarMorning.getTimeInMillis() - current.getTimeInMillis();

		OneTimeWorkRequest reminderMorning = new OneTimeWorkRequest.Builder(NotificationWorker.class).setInitialDelay(delay, TimeUnit.MILLISECONDS).build();
		WorkManager.getInstance(context).enqueue(reminderMorning);

		return Result.success();
	}
}