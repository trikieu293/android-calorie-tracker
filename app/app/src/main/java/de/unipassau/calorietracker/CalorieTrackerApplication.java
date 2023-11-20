package de.unipassau.calorietracker;

import android.app.Application;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import com.google.android.material.color.DynamicColors;

public class CalorieTrackerApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		DynamicColors.applyToActivitiesIfAvailable(this);
	}

	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

}
