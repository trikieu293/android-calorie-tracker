package de.unipassau.calorietracker.ui.dashboard.recommendAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import de.unipassau.calorietracker.R;
import de.unipassau.calorietracker.data.FSProduct;

public class RecommendAdapter extends ArrayAdapter<FSProduct> {

	private List<FSProduct> itemsList;
	private List<FSProduct> allItemsList;
	private final int itemLayout;


	public RecommendAdapter(Context context, int resource, List<FSProduct> storeDataList) {
		super(context, resource, storeDataList);
		this.itemsList = storeDataList;
		itemLayout = resource;
	}

	@Nullable
	@Override
	public FSProduct getItem(int position) {
		return super.getItem(position);
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		convertView = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
		TextView nameTextView = convertView.findViewById(R.id.recommend_name);
		nameTextView.setText(getItem(position).name);
		TextView calorieTextView = convertView.findViewById(R.id.recommend_calorie);
		calorieTextView.setText(String.valueOf(getItem(position).calorie));
		return convertView;
	}

	public class ListFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			if (allItemsList == null) {
				allItemsList = new ArrayList<>(itemsList);
			}
			if (constraint == null || constraint.length() == 0) {
				results.values = allItemsList;
				results.count = allItemsList.size();
			} else {
				final String searchStrLowerCase = constraint.toString().toLowerCase();
				ArrayList<FSProduct> matchValues = new ArrayList<>();
				for (FSProduct dataItem : allItemsList) {
					if (dataItem.name.startsWith(searchStrLowerCase)) {
						matchValues.add(dataItem);
					}
				}
				results.values = matchValues;
				results.count = matchValues.size();
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			if (results.values != null) {
				itemsList = (ArrayList<FSProduct>) results.values;
			} else {
				itemsList = null;
			}
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}

		@Override
		public CharSequence convertResultToString(Object resultValue) {
			return ((FSProduct) resultValue).name;
		}
	}
}