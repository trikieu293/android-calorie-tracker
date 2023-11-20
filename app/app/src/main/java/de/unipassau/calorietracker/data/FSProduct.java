package de.unipassau.calorietracker.data;

public class FSProduct {
	public String id;
	public String name;
	public long calorie;

	@Override
	public String toString() {
		return name;
	}
}
