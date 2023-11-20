package de.unipassau.calorietracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import de.unipassau.calorietracker.data.FSUser;

public class SettingsWheightFragment extends Fragment {

	private SettingsWheightViewModel mViewModel;

	private Button mButtonMinus;
	private Button mButtonPlus;
	private Button mButtonConfirm;
	private TextView mTextWeight;
	private RecyclerView mRecyclerView;

	public static SettingsWheightFragment newInstance() {
		return new SettingsWheightFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.settings_wheight_fragment, container, false);
		mViewModel = new ViewModelProvider(this).get(SettingsWheightViewModel.class);

		// Set view references
		mButtonMinus = view.findViewById(R.id.buttonMinus);
		mButtonPlus = view.findViewById(R.id.buttonPlus);
		mButtonConfirm = view.findViewById(R.id.confirm);
		mTextWeight = view.findViewById(R.id.textWeight);
		mRecyclerView = view.findViewById(R.id.recyclerView);

		// Set button handlers
		mButtonPlus.setOnClickListener(v -> {
			mViewModel.currentWeight++;
			mTextWeight.setText(String.format("%s%s", mViewModel.currentWeight, getString(R.string.unit_weight)));
			mButtonConfirm.setVisibility(View.VISIBLE);
		});
		mButtonMinus.setOnClickListener(v -> {
			mViewModel.currentWeight--;
			mTextWeight.setText(String.format("%s%s", mViewModel.currentWeight, getString(R.string.unit_weight)));
			mButtonConfirm.setVisibility(View.VISIBLE);
		});
		mButtonConfirm.setOnClickListener(v -> {
			// Store data on server
			FSUser usr = new FSUser();
			@SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			usr.weightHistory = mViewModel.weightHistory.entrySet().stream().collect(Collectors.toMap(e -> sdf.format(e.getKey()), Map.Entry::getValue));

			// Add today to map
			Date today = new Date();
			usr.weightHistory.put(sdf.format(today), mViewModel.currentWeight);

			//Set user data
			FirestoreHandler.getInstance().setUserData(usr, () -> {
				// Refresh the RecyclerView
				WeightEntryAdapter weightEntryAdapter = new WeightEntryAdapter(usr.weightHistory.entrySet().stream().collect(Collectors.toMap(e -> {
					try {
						return sdf.parse(e.getKey());
					} catch (ParseException parseException) {
						parseException.printStackTrace();
					}
					return null;
				}, Map.Entry::getValue)));
				mRecyclerView.setAdapter(weightEntryAdapter);

				mButtonConfirm.setVisibility(View.GONE);
			});
		});

		mButtonConfirm.setVisibility(View.GONE);

		// Get loaded user data
		FSUser user = FirestoreHandler.getInstance().getUser();
		if (user != null && user.weightHistory != null) {
			@SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			// Map the weight dates to actual Date objects
			mViewModel.weightHistory = user.weightHistory.entrySet().stream().collect(Collectors.toMap(e -> {
				try {
					Date date = sdf.parse(e.getKey());
					date.setHours(0);
					date.setMinutes(0);
					date.setSeconds(0);
					return date;
				} catch (ParseException parseException) {
					parseException.printStackTrace();
				}
				return null;
			}, Map.Entry::getValue));

			// Find latest entry and set current weight to this
			Comparator<Map.Entry<Date, Long>> comparator = Map.Entry.comparingByKey();
			mViewModel.currentWeight = Collections.max(mViewModel.weightHistory.entrySet(), comparator).getValue();

		} else {
			mViewModel.weightHistory = new HashMap<>();
			mViewModel.currentWeight = 50L;
		}
		// Set the text to the loaded weight
		mTextWeight.setText(String.format("%s%s", mViewModel.currentWeight, getString(R.string.unit_weight)));

		// Use the recycler view to display the data
		WeightEntryAdapter weightEntryAdapter = new WeightEntryAdapter(mViewModel.weightHistory);
		mRecyclerView.setAdapter(weightEntryAdapter);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

		// Tri - Save current weight for calculation
		SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putLong("weight", mViewModel.currentWeight);
		editor.apply();

		return view;
	}

}