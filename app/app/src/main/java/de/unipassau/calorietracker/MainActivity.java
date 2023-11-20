package de.unipassau.calorietracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import de.unipassau.calorietracker.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

		// Set Action Bar
		Toolbar tb = findViewById(R.id.toolbar);
		setSupportActionBar(tb);

		BottomNavigationView navView = findViewById(R.id.nav_view);
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
				R.id.navigation_dashboard, R.id.navigation_history, R.id.navigation_fooddb)
				.build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
		NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
		NavigationUI.setupWithNavController(binding.navView, navController);

		// Refresh the widget
		CommonMethods.updateWidgets(getApplicationContext());

		// Init the Notification Component
		initNotifications();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_profile:
				// Show profile activity
				Intent set = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(set);

				return true;
			case R.id.action_scan:
				// Show profile activity
				Intent bcs = new Intent(MainActivity.this, BarcodeScannerActivity.class);
				startActivity(bcs);

				return true;
			default:
				// Invoke the superclass to handle it.
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.top_appbar_menu, menu);
		return true;
	}

	private void initNotifications() {
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
		WorkManager.getInstance(getApplicationContext()).enqueue(reminderMorning);
	}
}