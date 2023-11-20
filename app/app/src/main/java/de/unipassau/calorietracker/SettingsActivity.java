package de.unipassau.calorietracker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

	TabLayout tabLayout;
	FrameLayout frameLayout;
	TextView textName;
	TextView textMail;

	private FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		// Initialize Firebase Auth
		mAuth = FirebaseAuth.getInstance();

		// Get view references
		tabLayout = findViewById(R.id.tabLayout);
		frameLayout = findViewById(R.id.tabFrameLayout);
		textName = findViewById(R.id.profileName);
		textMail = findViewById(R.id.profileMail);

		// Set tab bar to invisible to avoid editing while data is unloaded
		tabLayout.setVisibility(View.INVISIBLE);

		// Show user data
		FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) {
			try {
				textMail.setText(user.getEmail());

				FirestoreHandler.getInstance().getUserData(fsUser -> {
					// Set name to name
					if (fsUser != null)
						textName.setText(String.format("%s %s", fsUser.firstname, fsUser.surname));

					// Show tab bar
					tabLayout.setVisibility(View.VISIBLE);

					// Show initial tab fragment
					FragmentManager fragmentManager = getSupportFragmentManager();
					FragmentTransaction transaction = fragmentManager.beginTransaction();
					transaction.replace(R.id.tabFrameLayout, new SettingsWheightFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();

					tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
						@Override
						public void onTabSelected(TabLayout.Tab tab) {
							Fragment tabFrag = null;
							switch (tab.getPosition()) {
								case 0:
									tabFrag = new SettingsWheightFragment();
									break;
								case 1:
									tabFrag = new SettingsPersonalFragment();
							}

							FragmentManager fragmentManager = getSupportFragmentManager();
							FragmentTransaction transaction = fragmentManager.beginTransaction();
							transaction.replace(R.id.tabFrameLayout, tabFrag).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
						}

						@Override
						public void onTabUnselected(TabLayout.Tab tab) {
						}

						@Override
						public void onTabReselected(TabLayout.Tab tab) {
						}
					});
				});

			} catch (NullPointerException e) {
				Log.e("FIREBASE", e.getMessage());
			}
		}
	}
}