package de.unipassau.calorietracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Field;

import de.unipassau.calorietracker.data.FSUser;
import de.unipassau.calorietracker.databinding.FragmentSettingsKeyValueBinding;

/**
 * A SettingsKeyValueFragment
 * Use the {@link SettingsKeyValueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsKeyValueFragment extends Fragment {

	private static final String LABEL = "label";
	private static final String INPUT_TYPE = "inputType";
	private static final String DB_KEY = "dbKey";

	private String mLabel;
	private int mInputType;
	private String mDbKey;

	private EditText propertyEdit;

	public SettingsKeyValueFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param dbKey     Database key of this property.
	 * @param label     Label for the Setting.
	 * @param inputType Type of the EditText field.
	 * @return A new instance of fragment SettingsKeyValueFragment.
	 */
	public static SettingsKeyValueFragment newInstance(String dbKey, String label, int inputType) {
		SettingsKeyValueFragment fragment = new SettingsKeyValueFragment();
		Bundle args = new Bundle();
		args.putString(DB_KEY, dbKey);
		args.putString(LABEL, label);
		args.putInt(INPUT_TYPE, inputType);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mDbKey = getArguments().getString(DB_KEY);
			mLabel = getArguments().getString(LABEL);
			mInputType = getArguments().getInt(INPUT_TYPE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		FragmentSettingsKeyValueBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings_key_value, container, false);
		View view = binding.getRoot();

		propertyEdit = view.findViewById(R.id.propertyEdit);

		// Get current value by reflection
		FSUser user = FirestoreHandler.getInstance().getUser();
		if (user != null) {
			try {
				Field field = user.getClass().getDeclaredField(mDbKey);
				Object obj = field.get(user);
				binding.setCurrentValue(obj != null ? obj.toString() : "");
			} catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		binding.setEditMode(false);
		binding.setLabel(mLabel);
		binding.setInputType(mInputType);
		binding.editButton.setOnClickListener(v -> binding.setEditMode(true));
		binding.confirmButton.setOnClickListener(v -> {
			// Handle confirm edit
			FSUser changedUserData = new FSUser();
			try {
				final Field declaredField = changedUserData.getClass().getDeclaredField(mDbKey);

				if (String.class.equals(declaredField.getType()))
					declaredField.set(changedUserData, propertyEdit.getText().toString());
				else if (long.class.equals(declaredField.getType()))
					declaredField.set(changedUserData, Long.parseLong(propertyEdit.getText().toString()));

				FirestoreHandler.getInstance().setUserData(changedUserData, () -> {
					binding.setCurrentValue(propertyEdit.getText().toString());
				});
			} catch (IllegalAccessException | NoSuchFieldException e) {
				e.printStackTrace();
			}

			binding.setEditMode(false);
		});

		return view;
	}
}