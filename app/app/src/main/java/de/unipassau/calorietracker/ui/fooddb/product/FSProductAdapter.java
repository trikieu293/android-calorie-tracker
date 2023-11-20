package de.unipassau.calorietracker.ui.fooddb.product;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.unipassau.calorietracker.FirestoreHandler;
import de.unipassau.calorietracker.R;
import de.unipassau.calorietracker.data.FSProduct;

public class FSProductAdapter extends RecyclerView.Adapter<FSProductAdapter.ViewHolder> {

	private List<FSProduct> itemList;
	private final Context context;

	public FSProductAdapter(List<FSProduct> itemList, Context context) {
		this.itemList = itemList;
		this.context = context;
	}

	@NonNull
	@Override
	public FSProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View itemView = inflater.inflate(R.layout.db_item, parent, false);
		ViewHolder viewHolder = new ViewHolder(itemView);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(@NonNull FSProductAdapter.ViewHolder holder, int position) {
		//get item
		FSProduct item = itemList.get(position);

		//bind data to viewholder
		holder.tvName.setText(item.name);
		holder.etCalorie.setText(String.valueOf(item.calorie));
		holder.tvId.setText(String.valueOf(item.id));
		holder.etCalorie.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!s.toString().equals("")) {
					item.calorie = Long.parseLong(holder.etCalorie.getText().toString());
					FirestoreHandler.getInstance().setProduct(item.id, item, () -> {
					});
				}
			}
		});
	}

	@Override
	public int getItemCount() {
		return itemList.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView tvName;
		private final TextView tvId;
		private final EditText etCalorie;

		public ViewHolder(View itemView) {
			super(itemView);
			tvName = itemView.findViewById(R.id.db_item_name);
			tvId = itemView.findViewById(R.id.db_item_id);
			etCalorie = itemView.findViewById(R.id.db_item_calorie);
		}
	}


	public void removeItem(int position) {
		itemList.remove(position);
		notifyItemRemoved(position);
	}

	public void restoreItem(FSProduct item, int position) {
		itemList.add(position, item);
		notifyItemInserted(position);
	}


	public List<FSProduct> getData() {
		return itemList;
	}

	public void setItemList(List<FSProduct> list) {
		itemList = list;
		this.notifyDataSetChanged();
	}

}
