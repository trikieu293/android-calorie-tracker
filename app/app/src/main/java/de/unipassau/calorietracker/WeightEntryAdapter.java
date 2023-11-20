package de.unipassau.calorietracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WeightEntryAdapter extends RecyclerView.Adapter<WeightEntryAdapter.ViewHolder> {
	public class ViewHolder extends RecyclerView.ViewHolder {
		public final TextView dateTextView;
		public final TextView valueTextView;

		public ViewHolder(View itemView) {
			super(itemView);

			dateTextView = itemView.findViewById(R.id.date);
			valueTextView = itemView.findViewById(R.id.value);
		}
	}

	class WeightData implements Comparable<WeightData> {
		public final Date date;
		public final long weight;

		public WeightData(Date date, long weight) {
			this.date = date;
			this.weight = weight;
		}

		@Override
		public int compareTo(WeightData o) {
			return date.compareTo(o.date);
		}
	}

	private final List<WeightData> mWeights;

	public WeightEntryAdapter(Map<Date, Long> _weights) {
		mWeights = new ArrayList<>();
		for (Map.Entry<Date, Long> ent : _weights.entrySet()) {
			mWeights.add(new WeightData(ent.getKey(), ent.getValue()));
		}
		mWeights.sort(Collections.reverseOrder(Comparator.comparing(o -> o.date)));
	}

	@Override
	public WeightEntryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Context context = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);

		// Inflate the layout
		View contactView = inflater.inflate(R.layout.item_weight, parent, false);

		ViewHolder viewHolder = new ViewHolder(contactView);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(WeightEntryAdapter.ViewHolder holder, int position) {
		WeightData w = mWeights.get(position);
		if (w != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
			holder.dateTextView.setText(sdf.format(w.date));
			holder.valueTextView.setText(String.format(Locale.GERMANY, "%dkg", w.weight));
		}
	}

	@Override
	public int getItemCount() {
		return mWeights.size();
	}
}
