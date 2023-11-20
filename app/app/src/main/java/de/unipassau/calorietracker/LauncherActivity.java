package de.unipassau.calorietracker;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.unipassau.calorietracker.ui.login.LoginActivity;

public class LauncherActivity extends AppCompatActivity {

	private FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Init the FirebaseAuth Object
		mAuth = FirebaseAuth.getInstance();

		// Check whether the user is logged in
		FirebaseUser currentUser = mAuth.getCurrentUser();
		if (currentUser != null) {
			// User data found
			// Check if name is set to decide whether to show the setup activity
			FirestoreHandler.getInstance().getUserData(fsUser -> {
				Intent intent;
				if (fsUser != null && fsUser.firstname != null && !fsUser.firstname.equals("")) {
					intent = new Intent(this, MainActivity.class);
				} else {
					intent = new Intent(this, SetupActivity.class);
				}
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				finish();
				startActivity(intent);
			});

		} else {
			// No user data found, show LoginActivity
			Intent intent = new Intent(this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			finish();
			startActivity(intent);
		}
	}
}