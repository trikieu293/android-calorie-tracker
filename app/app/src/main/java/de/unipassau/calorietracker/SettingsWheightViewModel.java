package de.unipassau.calorietracker;

import androidx.lifecycle.ViewModel;

import java.util.Date;
import java.util.Map;

public class SettingsWheightViewModel extends ViewModel {
	public Map<Date, Long> weightHistory;
	public long currentWeight;
}