package de.unipassau.calorietracker.data;

import java.util.List;

public class FSDay {
	public List<FSItem> items;
	public long sumCalorie;
	public String stage;

	public long getSumCalorie() {
		int sum = 0;
		for (FSItem i : items) {
			sum += i.amount * i.calorie;
		}
		sumCalorie = sum;
		return sumCalorie;
	}
}
