package de.unipassau.calorietracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import de.unipassau.calorietracker.data.FSItem;
import de.unipassau.calorietracker.data.FSProduct;
import de.unipassau.calorietracker.ui.dashboard.DashboardFragment;

public class BarcodeResultActivity extends AppCompatActivity {

	private LinearLayout ltNew;
	private TextView tvNewEan;
	private EditText etNewName;
	private EditText etNewCalorie;
	private EditText etNewAmount;
	private Button btnCreate;

	private LinearLayout ltExisting;
	private TextView tvExistingEan;
	private TextView tvExistingName;
	private TextView tvExistingCalorie;
	private TextView etExistingAmount;
	private Button btnAdd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode_result);

		// Get ean from bundle
		Bundle bundle = getIntent().getExtras();
		String ean = ""; // or other values
		if (bundle != null) {
			ean = bundle.getString("ean");
		}

		ltNew = findViewById(R.id.lt_new);
		tvNewEan = findViewById(R.id.tv_bc_new_ean);
		etNewName = findViewById(R.id.et_bc_new_name);
		etNewCalorie = findViewById(R.id.et_bc_new_calorie);
		etNewAmount = findViewById(R.id.et_bc_new_amount);
		btnCreate = findViewById(R.id.btn_bc_create);

		ltExisting = findViewById(R.id.lt_existing);
		tvExistingEan = findViewById(R.id.tv_bc_existing_ean);
		tvExistingName = findViewById(R.id.tv_bc_existing_name);
		tvExistingCalorie = findViewById(R.id.tv_bc_existing_calorie);
		etExistingAmount = findViewById(R.id.et_bc_existing_amount);
		btnAdd = findViewById(R.id.btn_bc_add);

		// Check if product exists in database
		String finalEan = ean;
		FirestoreHandler.getInstance().getProductById(ean, fsProduct -> {
			// Product exists
			ltExisting.setVisibility(View.VISIBLE);
			tvExistingEan.setText(fsProduct.id);
			tvExistingCalorie.setText(String.format(Locale.getDefault(), "%d %s", fsProduct.calorie, getString(R.string.unit_cal)));
			tvExistingName.setText(fsProduct.name);
			btnAdd.setOnClickListener(v -> {
				// Check if amount is set
				String amtStr = etExistingAmount.getText().toString();
				if (!amtStr.equals("") && Integer.parseInt(amtStr) > 0) {
					int amt = Integer.parseInt(amtStr);
					// Number is set and bigger than 0
					FSItem item = new FSItem();
					item.name = fsProduct.name;
					item.amount = amt;
					item.calorie = fsProduct.calorie;

					// Set on Firestore
					FirestoreHandler.getInstance().addItemToDate(DashboardFragment.getDayAsString(), item, () -> {
						Intent intent = new Intent(BarcodeResultActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					});
				} else {
					// No valid amount
					etExistingAmount.setError(getString(R.string.error_bc_amount));
				}
			});

		}, () -> {
			// Product doesn't exist
			ltNew.setVisibility(View.VISIBLE);
			tvNewEan.setText(finalEan);
			btnCreate.setOnClickListener(v -> {
				// Check if name is set
				String name = etNewName.getText().toString();
				if (!name.equals("")) {
					// Check if calorie is set
					String calStr = etNewCalorie.getText().toString();
					if (!calStr.equals("") && Integer.parseInt(calStr) > 0) {
						int cal = Integer.parseInt(calStr);
						// Check if amount is set
						String amtStr = etNewAmount.getText().toString();
						if (!amtStr.equals("") && Integer.parseInt(amtStr) > 0) {
							int amt = Integer.parseInt(amtStr);
							// Create new Item on server
							FSProduct fsProduct = new FSProduct();
							fsProduct.name = name;
							fsProduct.id = finalEan;
							fsProduct.calorie = cal;

							FirestoreHandler.getInstance().setProduct(finalEan, fsProduct, () -> {
								// Add item to consumed items
								FSItem item = new FSItem();
								item.name = name;
								item.amount = amt;
								item.calorie = cal;

								// Set on Firestore
								FirestoreHandler.getInstance().addItemToDate(DashboardFragment.getDayAsString(), item, () -> {
									Intent intent = new Intent(BarcodeResultActivity.this, MainActivity.class);
									startActivity(intent);
									finish();
								});
							});

						} else {
							// No valid calorie
							etNewAmount.setError(getString(R.string.error_bc_amount));
						}
					} else {
						// No valid calorie
						etNewCalorie.setError(getString(R.string.error_bc_amount));
					}
				} else {
					// No valid calorie
					etNewName.setError(getString(R.string.error_bc_name));
				}
			});
		});
	}
}