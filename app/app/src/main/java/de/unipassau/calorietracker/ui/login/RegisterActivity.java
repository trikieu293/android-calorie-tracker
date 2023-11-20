package de.unipassau.calorietracker.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import de.unipassau.calorietracker.R;
import de.unipassau.calorietracker.SetupActivity;
import de.unipassau.calorietracker.databinding.ActivityRegisterBinding;

/**
 * @author Nick Kelldorfner
 */
public class RegisterActivity extends AppCompatActivity {

	private FirebaseAuth mAuth;

	private Button registerButton;

	private EditText username;
	private EditText password;
	private EditText passwordRepeat;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		de.unipassau.calorietracker.databinding.ActivityRegisterBinding binding = ActivityRegisterBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		// Initialize Firebase Auth
		mAuth = FirebaseAuth.getInstance();

		// Get view references
		registerButton = findViewById(R.id.register);
		username = findViewById(R.id.username);
		password = findViewById(R.id.password);
		passwordRepeat = findViewById(R.id.passwordRepeat);

		// Register Handlers
		registerButton.setOnClickListener(v -> registerUser());
		username.addTextChangedListener(validationTextWatcher);
		password.addTextChangedListener(validationTextWatcher);
		passwordRepeat.addTextChangedListener(validationTextWatcher);
	}

	/**
	 * Register a new user to Firebase with the EditText values
	 */
	private void registerUser() {
		String pw = password.getText().toString();
		String em = username.getText().toString();

		mAuth.createUserWithEmailAndPassword(em, pw)
				.addOnCompleteListener(this, task -> {
					if (task.isSuccessful()) {
						// Sign in success, update UI with the signed-in user's information
						Log.d("FIREBASE", "createUserWithEmail:success");

						// Switch to SetupActivity
						Intent intent = new Intent(RegisterActivity.this, SetupActivity.class);
						finish();
						startActivity(intent);
					} else {
						// If sign in fails, display a message to the user.
						Log.w("FIREBASE", "createUserWithEmail:failure", task.getException());
						Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
					}
				});
	}

    /*
    VALIDATING INPUT
     */

	/**
	 * Use a TextWatcher to watch for changes in the login EditTexts
	 */
	private final TextWatcher validationTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (username.getText().toString().length() == 0 || !Patterns.EMAIL_ADDRESS.matcher(username.getText().toString()).matches()) {
				username.setError(getString(R.string.invalid_username));
				registerButton.setEnabled(false);
				return;
			}

			if (password.getText().toString().length() <= 5) {
				password.setError(getString(R.string.invalid_password));
				registerButton.setEnabled(false);
				return;
			}

			if (!passwordRepeat.getText().toString().equals(password.getText().toString())) {
				passwordRepeat.setError(getString(R.string.invalid_repeated_password));
				registerButton.setEnabled(false);
				return;
			}

			registerButton.setEnabled(true);
		}
	};
}