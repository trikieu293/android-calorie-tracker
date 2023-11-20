package de.unipassau.calorietracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;

public class BarcodeScannerActivity extends AppCompatActivity {

	private CodeScanner mCodeScanner;

	private final ActivityResultLauncher<String> requestPermissionLauncher =
			registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
				if (isGranted) {
					// Permission is granted
					setUpScanner();
				} else {
					// Permission not granted
					Toast.makeText(BarcodeScannerActivity.this, getString(R.string.error_cam_access_denied), Toast.LENGTH_LONG).show();
				}
			});


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode_scanner);

		// Check for permission
		if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
			// Permission already given
			setUpScanner();
		} else {
			// Ask for permission
			requestPermissionLauncher.launch(
					Manifest.permission.CAMERA
			);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mCodeScanner != null)
			mCodeScanner.startPreview();
	}

	@Override
	protected void onPause() {
		if (mCodeScanner != null)
			mCodeScanner.releaseResources();
		super.onPause();
	}

	private void setUpScanner() {
		CodeScannerView scannerView = findViewById(R.id.scanner_view);
		if (scannerView != null) {
			mCodeScanner = new CodeScanner(this, scannerView);
			mCodeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
				// Start the result activity with the result as extra
				Intent intent = new Intent(BarcodeScannerActivity.this, BarcodeResultActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("ean", result.getText());
				// Put the bundle into intent
				intent.putExtras(bundle);
				// Start activity and close this
				startActivity(intent);
				finish();
			}));
			scannerView.setOnClickListener(view -> mCodeScanner.startPreview());
		}
	}
}