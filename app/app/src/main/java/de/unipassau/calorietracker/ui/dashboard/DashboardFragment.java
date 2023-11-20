package de.unipassau.calorietracker.ui.dashboard;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.unipassau.calorietracker.CommonMethods;
import de.unipassau.calorietracker.FirestoreHandler;
import de.unipassau.calorietracker.R;
import de.unipassau.calorietracker.data.FSDay;
import de.unipassau.calorietracker.data.FSItem;
import de.unipassau.calorietracker.databinding.FragmentDashboardBinding;
import de.unipassau.calorietracker.ui.dashboard.items.FSItemAdapter;
import de.unipassau.calorietracker.ui.dashboard.recommendAdapter.RecommendAdapter;

public class DashboardFragment extends Fragment {

	private FSDay today;
	private View root;
	private CircularProgressBar progressBar;
	private TextView progressText;
	private AutoCompleteTextView itemNameEdit;
	private EditText itemCalorieEdit;
	private EditText itemAmountEdit;
	private RecyclerView list;
	private FSItemAdapter adapter;
	private ConstraintLayout home_layout;

	private FragmentDashboardBinding binding;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = FragmentDashboardBinding.inflate(inflater, container, false);
		root = binding.getRoot();

		// Parent-Layout and Date
		home_layout = root.findViewById(R.id.dashboard_layout);
		TextView todayText = root.findViewById(R.id.today_text);
		todayText.setText(getDayAsString());
		list = root.findViewById(R.id.list_layout);

		updateData();

		return root;
	}

	private void updateData() {
		// Database for Item-list
		FirestoreHandler.getInstance().getDay(getDayAsString(), fsDay -> {
			today = fsDay;

			// Initialize Layout
			adapter = new FSItemAdapter(today.items, requireContext().getApplicationContext());
			list.setAdapter(adapter);
			LinearLayoutManager linearLayoutManager =
					new LinearLayoutManager(requireContext().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
			list.setLayoutManager(linearLayoutManager);

			initProgressBar(root);
			initAddLayout(root);
			enableSwipeToDeleteAndUndo();
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		updateData();
	}

	public void initProgressBar(View root) {
		CommonMethods.maxCalorie(maxcal -> {
			float maxCalorie = (float) maxcal;
			float sumCalorie = today.getSumCalorie();
			progressBar = root.findViewById(R.id.progress_circular);
			progressBar.setProgressMax(maxCalorie);
			progressBar.setProgress(sumCalorie);

			progressText = root.findViewById(R.id.progress_text);
			int progress = (int) progressBar.getProgress();
			int max_progress = (int) progressBar.getProgressMax();
			progressText.setText(String.format(Locale.GERMANY, "%d / %d", progress, max_progress));
			updateProgress();
		});
	}


	public void initAddLayout(View root) {
		// AutoCompleteTextView with Recommend-Function
		FirestoreHandler.getInstance().getProducts(fsProducts -> {
			itemNameEdit = root.findViewById(R.id.item_name_edit_text);
			itemNameEdit.setAdapter(new RecommendAdapter(getContext(), R.layout.recommend_item, fsProducts));
			itemNameEdit.setThreshold(1);
			itemNameEdit.setOnItemClickListener((parent, view, position, id) -> {
				TextView textView = view.findViewById(R.id.recommend_calorie);
				String calorie = textView.getText().toString();
				itemCalorieEdit.setText(calorie);
			});
		});


		itemCalorieEdit = root.findViewById(R.id.item_calorie_edit_text);
		itemAmountEdit = root.findViewById(R.id.item_amount_edit_text);
		Button addButton = root.findViewById(R.id.add_button);
		addButton.setOnClickListener(v -> {
			String name = itemNameEdit.getText().toString();
			if (name.equals("")) name = getString(R.string.text_undefined_item);
			if (itemCalorieEdit.getText().toString().equals("")) return;
			if (itemAmountEdit.getText().toString().equals("")) return;
			int calorie = Integer.parseInt(itemCalorieEdit.getText().toString());
			int amount = Integer.parseInt(itemAmountEdit.getText().toString());
			FSItem item = new FSItem();
			item.name = name;
			item.calorie = calorie;
			item.amount = amount;

			// Add item
			today.items.add(item);
			FirestoreHandler.getInstance().addItemToDate(getDayAsString(), item, this::refreshScreen);
		});
	}

	/**
	 * Refreshes the current data and notifies all widgets to update
	 */
	@SuppressLint("NotifyDataSetChanged")
	public void refreshScreen() {
		adapter.notifyDataSetChanged();
		itemNameEdit.setText("");
		itemCalorieEdit.setText("");
		itemAmountEdit.setText("");
		updateProgress();
		CommonMethods.updateWidgets(this.getContext());
	}

	/**
	 * Updates the ProgressBar and ProgressText
	 */
	public void updateProgress() {
		long sum = today.getSumCalorie();
		CommonMethods.maxCalorie(maxcal -> {
			long maxCalorie = maxcal;
			if (sum > maxCalorie) {
				progressBar.setProgressBarColor(getContext().getColor(R.color.progressbar_exceed));
			} else if (sum < 0.8 * maxCalorie) {
				progressBar.setProgressBarColor(getContext().getColor(R.color.progressbar_default));
			} else {
				progressBar.setProgressBarColor(getContext().getColor(R.color.progressbar_good));
			}

			progressBar.setProgress(today.getSumCalorie());
			progressText.setText(String.format(Locale.GERMANY, "%d / %d", sum, maxCalorie));
		});
	}


	/**
	 * Get the current day as String
	 *
	 * @return the current day in the format dd-MM-yyyy
	 */
	// Convert current day to String for saving
	public static String getDayAsString() {
		Format formatter = new SimpleDateFormat("dd-MM-yyyy");
		Date date = Calendar.getInstance().getTime();
		return formatter.format(date);
	}

	/**
	 * Enables Swipe to Delete
	 */
	private void enableSwipeToDeleteAndUndo() {
		SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(requireContext().getApplicationContext()) {
			@Override
			public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

				final int position = viewHolder.getAbsoluteAdapterPosition();
				final FSItem item = adapter.getData().get(position);

				// Remove item from database, list and adapter
				FirestoreHandler.getInstance().deleteItemFromDate(getDayAsString(), today.items.get(position), () -> {
				});
				adapter.removeItem(position);
				refreshScreen();

				Snackbar snackbar = Snackbar
						.make(home_layout, getText(R.string.toast_item_removed), Snackbar.LENGTH_LONG);
				snackbar.setAction(getText(R.string.toast_undo), view -> {
					FirestoreHandler.getInstance().addItemToDate(getDayAsString(), item, () -> {
					});
					adapter.restoreItem(item, position);
					refreshScreen();
					list.scrollToPosition(position);
				});
				snackbar.setActionTextColor(Color.YELLOW);
				snackbar.show();
			}
		};

		ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
		itemTouchhelper.attachToRecyclerView(list);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}