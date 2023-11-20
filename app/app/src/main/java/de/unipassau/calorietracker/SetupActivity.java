package de.unipassau.calorietracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.unipassau.calorietracker.data.FSUser;

public class SetupActivity extends AppCompatActivity {
	private EditText etFirstname;
	private EditText etLastname;
	private EditText etAge;
	private EditText etHeight;
	private EditText etWeight;
	private Button btnStart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);

		etFirstname = findViewById(R.id.etFirstname);
		etLastname = findViewById(R.id.etLastname);
		etAge = findViewById(R.id.etAge);
		etHeight = findViewById(R.id.etHeight);
		etWeight = findViewById(R.id.etWeight);

		btnStart = findViewById(R.id.btnStart);

		btnStart.setOnClickListener(v -> {
			// Check all values for validity
			if (etFirstname.getText().toString().length() == 0) {
				etFirstname.setError(getString(R.string.invalid_string));
				return;
			}
			if (etLastname.getText().toString().length() == 0) {
				etLastname.setError(getString(R.string.invalid_string));
				return;
			}
			if (etAge.getText().toString().equals("") || Integer.parseInt(etAge.getText().toString()) <= 0) {
				etAge.setError(getString(R.string.error_bc_amount));
				return;
			}
			if (etHeight.getText().toString().equals("") || Integer.parseInt(etHeight.getText().toString()) <= 0) {
				etHeight.setError(getString(R.string.error_bc_amount));
				return;
			}
			if (etWeight.getText().toString().equals("") || Integer.parseInt(etWeight.getText().toString()) <= 0) {
				etWeight.setError(getString(R.string.error_bc_amount));
				return;
			}

			// Disable button to avoid double clicks
			btnStart.setEnabled(false);

			// Set values in Firestore db
			FSUser userData = new FSUser();
			userData.firstname = etFirstname.getText().toString();
			userData.surname = etLastname.getText().toString();
			userData.age = Integer.parseInt(etAge.getText().toString());
			userData.height = Integer.parseInt(etHeight.getText().toString());
			Map<String, Long> wh = new HashMap<>();
			@SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date today = new Date();
			wh.put(sdf.format(today), Long.parseLong(etWeight.getText().toString()));
			userData.weightHistory = wh;
			FirestoreHandler.getInstance().setUserData(userData, () -> {
				// Switch to MainActivity
				Intent intent = new Intent(SetupActivity.this, MainActivity.class);
				finish();
				startActivity(intent);
			});
		});
	}
}