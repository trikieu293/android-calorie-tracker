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

import de.unipassau.calorietracker.MainActivity;
import de.unipassau.calorietracker.R;
import de.unipassau.calorietracker.databinding.ActivityLoginBinding;

/**
 * @author Nick Kelldorfner
 */
public class LoginActivity extends AppCompatActivity {

	private FirebaseAuth mAuth;

	private Button loginButton;

	private EditText username;
	private EditText password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		de.unipassau.calorietracker.databinding.ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		// Initialize Firebase Auth
		mAuth = FirebaseAuth.getInstance();

		// Get view references
		Button registerButton = findViewById(R.id.register);
		loginButton = findViewById(R.id.login);
		username = findViewById(R.id.username);
		password = findViewById(R.id.password);

		// Register Handlers
		registerButton.setOnClickListener(v -> showRegistrationActivity());
		loginButton.setOnClickListener(v -> login());
		username.addTextChangedListener(validationTextWatcher);
		password.addTextChangedListener(validationTextWatcher);
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
				loginButton.setEnabled(false);
				return;
			}

			if (password.getText().toString().length() <= 5) {
				password.setError(getString(R.string.invalid_password));
				loginButton.setEnabled(false);
				return;
			}
			loginButton.setEnabled(true);
		}
	};


	/**
	 * Show the registration form Activity
	 */
	private void showRegistrationActivity() {
		Intent intent = new Intent(this, RegisterActivity.class);
		startActivity(intent);
	}

	/**
	 * Try to login the user with the credentials in the EditTexts
	 */
	private void login() {
		String pw = password.getText().toString();
		String em = username.getText().toString();

		mAuth.signInWithEmailAndPassword(em, pw).addOnCompleteListener(this, task -> {
			if (task.isSuccessful()) {
				// Sign in success, update UI with the signed-in user's information
				Log.d("FIREBASE", "signInWithEmail:success");

				// Switch to MainActivity
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
				finish();
				startActivity(intent);
			} else {
				// If sign in fails, display a message to the user.
				Log.w("FIREBASE", "signInWithEmail:failure", task.getException());
				Toast.makeText(LoginActivity.this, "Authentication failed.",
						Toast.LENGTH_LONG).show();
			}
		});
	}
}