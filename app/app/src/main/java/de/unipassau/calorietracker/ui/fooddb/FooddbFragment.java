package de.unipassau.calorietracker.ui.fooddb;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.unipassau.calorietracker.FirestoreHandler;
import de.unipassau.calorietracker.R;
import de.unipassau.calorietracker.data.FSProduct;
import de.unipassau.calorietracker.databinding.FragmentFooddbBinding;
import de.unipassau.calorietracker.ui.fooddb.product.FSProductAdapter;

public class FooddbFragment extends Fragment {

	private FragmentFooddbBinding binding;

	private EditText editName;
	private EditText editCalorie;
	private EditText editEAN;
	private Button addButton;
	private List<FSProduct> itemList;
	private FSProductAdapter adapter;
	private RecyclerView list;
	private ConstraintLayout layout;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentFooddbBinding.inflate(inflater, container, false);
		View root = binding.getRoot();
		initRecycleView(root);
		enableSwipeToDeleteAndUndo();
		return root;
	}

	private void initRecycleView(View root) {
		FirestoreHandler.getInstance().getProducts(fsProducts -> {
			itemList = fsProducts;

			Log.d("test", " tao array");
			list = root.findViewById(R.id.list_of_db_layout);
			adapter = new FSProductAdapter(itemList, requireContext().getApplicationContext());
			list.setAdapter(adapter);
			LinearLayoutManager linearLayoutManager =
					new LinearLayoutManager(requireContext().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
			list.setLayoutManager(linearLayoutManager);
			initAddLayout(root);
		});
	}

	private void initAddLayout(View root) {
		layout = root.findViewById(R.id.database_layout);
		editName = root.findViewById(R.id.db_add_name);

		editName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (editName.getText().toString().equals("")) {
					adapter.setItemList(itemList);
				} else {
					adapter.setItemList(filter(itemList, editName.getText().toString()));
				}
			}
		});

		editCalorie = root.findViewById(R.id.db_add_calorie);
		editEAN = root.findViewById(R.id.db_add_ean);
		addButton = root.findViewById(R.id.db_add_button);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = editName.getText().toString();
				if (name.equals("")) {
					name = "Undefined Item " + Math.random() * 1000;
				}
				int calorie = Integer.parseInt(editCalorie.getText().toString());
				String id = editEAN.getText().toString();
				if (id.equals("")) {
					id = UUID.randomUUID().toString();
				}

				FSProduct item = new FSProduct();
				item.calorie = calorie;
				item.name = name;
				item.id = id;
				itemList.add(item);
				adapter.setItemList(itemList);
				FirestoreHandler.getInstance().setProduct(id, item, () -> {
					refreshScreen();
				});
			}
		});
	}

	private void refreshScreen() {
		editName.setText("");
		editCalorie.setText("");
	}


	private void enableSwipeToDeleteAndUndo() {
		de.unipassau.calorietracker.ui.fooddb.SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(requireContext().getApplicationContext()) {
			@Override
			public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

				final int position = viewHolder.getAbsoluteAdapterPosition();
				final FSProduct item = adapter.getData().get(position);

				//remove item from database, list and adapter

				FirestoreHandler.getInstance().removeProduct(itemList.get(position).id, () -> {
					adapter.removeItem(position);
					adapter.notifyDataSetChanged();
					refreshScreen();

					Snackbar snackbar = Snackbar
							.make(layout, "Item was removed from the list.", Snackbar.LENGTH_LONG);
					snackbar.setAction("UNDO", view -> FirestoreHandler.getInstance().setProduct(item.id, item, () -> {
						adapter.restoreItem(item, position);
						refreshScreen();
						list.scrollToPosition(position);
					}));
					snackbar.setActionTextColor(Color.YELLOW);
					snackbar.show();
				});
			}
		};

		ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
		itemTouchhelper.attachToRecyclerView(list);
	}

	private List<FSProduct> filter(List<FSProduct> products, String query) {
		query = query.toLowerCase();

		final ArrayList<FSProduct> filteredModelList = new ArrayList<>();
		for (FSProduct product : products) {
			final String text = product.name.toLowerCase();
			if (text.contains(query)) {
				filteredModelList.add(product);
			}
		}
		return filteredModelList;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}