package de.unipassau.calorietracker.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.unipassau.calorietracker.CommonMethods;
import de.unipassau.calorietracker.FirestoreHandler;
import de.unipassau.calorietracker.R;
import de.unipassau.calorietracker.data.FSItem;
import de.unipassau.calorietracker.databinding.FragmentHistoryBinding;
import de.unipassau.calorietracker.ui.dashboard.items.FSItemAdapter;

public class HistoryFragment extends Fragment {

	private FragmentHistoryBinding binding;

	private String chose_day;
	private RecyclerView list;
	private FSItemAdapter adapter;
	private List<FSItem> items;
	private TextView sumCalorie;
	private TextView result;
	private int sum;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentHistoryBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		// Initialize basic-value
		chose_day = getDayAsString(Calendar.getInstance().getTime());
		initCalendar(root);
		initRecycleView(root);
		return root;
	}

	private void initCalendar(View root) {
		CalendarView calendarView = root.findViewById(R.id.calendar_layout);
		long today = Calendar.getInstance().getTime().getTime();
		calendarView.setDate(today);
		calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
			month += 1;
			String d = String.valueOf(dayOfMonth);
			String m = String.valueOf(month);
			if (dayOfMonth < 10) {
				d = "0" + dayOfMonth;
			}
			if (month < 10) {
				m = "0" + month;
			}
			chose_day = d + "-" + m + "-" + year;

			FirestoreHandler.getInstance().getDay(chose_day, fsDay -> {
				items = fsDay.items;
				refreshScreen();
			});
		});
	}

	private void initRecycleView(View root) {
		FirestoreHandler.getInstance().getDay(chose_day, fsDay -> {
			items = fsDay.items;
			list = root.findViewById(R.id.list_of_date_layout);
			adapter = new FSItemAdapter(items, requireContext().getApplicationContext());
			list.setAdapter(adapter);
			LinearLayoutManager linearLayoutManager =
					new LinearLayoutManager(requireContext().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
			list.setLayoutManager(linearLayoutManager);

			sumCalorie = root.findViewById(R.id.sum_chose_day);
			result = root.findViewById(R.id.result_chose_day);
			refreshScreen();
		});

	}


	private void refreshScreen() {
		adapter.setItemList(items);
		adapter.notifyDataSetChanged();

		sum = 0;
		for (FSItem i : items) {
			sum += i.amount * i.calorie;
		}
		sumCalorie.setText(String.format(Locale.getDefault(), "%s %s", sum, getText(R.string.unit_cal)));

		CommonMethods.maxCalorie(maxCalorie -> {
			if (sum > maxCalorie) {
				result.setText(getText(R.string.text_too_much));
				result.setTextColor(getContext().getColor(R.color.progressbar_exceed));
			} else if (sum < 0.8 * maxCalorie) {
				result.setText(getText(R.string.text_too_low));
				result.setTextColor(getContext().getColor(R.color.common_google_signin_btn_text_dark_focused));
			} else {
				result.setText(getText(R.string.text_good));
				result.setTextColor(getContext().getColor(R.color.progressbar_good));
			}
		});
	}

	public static String getDayAsString(Date date) {
		Format formatter = new SimpleDateFormat("dd-MM-yyyy");
		return formatter.format(date);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}