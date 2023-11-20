package de.unipassau.calorietracker.ui.dashboard.items;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.unipassau.calorietracker.R;
import de.unipassau.calorietracker.data.FSItem;

public class FSItemAdapter extends RecyclerView.Adapter<FSItemAdapter.ViewHolder> {

	private List<FSItem> itemList;
	private final Context context;

	public FSItemAdapter(List<FSItem> itemList, Context context) {
		this.itemList = itemList;
		this.context = context;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View itemView = inflater.inflate(R.layout.food_item, parent, false);
		ViewHolder viewHolder = new ViewHolder(itemView);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		//get item
		FSItem item = itemList.get(position);

		//bind data to viewholder
		holder.tvName.setText(item.name);
		holder.tvSumCalorie.setText(String.valueOf(item.calorie * item.amount));
		holder.tvAmount.setText(String.valueOf(item.amount));
	}

	@Override
	public int getItemCount() {
		return itemList != null ? itemList.size() : 0;
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView tvName;
		private final TextView tvSumCalorie;
		private final TextView tvAmount;

		public ViewHolder(View itemView) {
			super(itemView);
			tvName = itemView.findViewById(R.id.item_name);
			tvSumCalorie = itemView.findViewById(R.id.item_sum_calorie);
			tvAmount = itemView.findViewById(R.id.item_amount);
		}
	}

	public void removeItem(int position) {
		itemList.remove(position);
		notifyItemRemoved(position);
	}

	public void restoreItem(FSItem item, int position) {
		itemList.add(position, item);
		notifyItemInserted(position);
	}

	public List<FSItem> getData() {
		return itemList;
	}

	public void setItemList(List<FSItem> list) {
		itemList = list;
	}
}

