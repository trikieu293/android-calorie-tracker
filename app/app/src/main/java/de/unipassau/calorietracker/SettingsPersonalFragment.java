package de.unipassau.calorietracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsPersonalFragment extends Fragment {

	public static SettingsPersonalFragment newInstance() {
		return new SettingsPersonalFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {

		Fragment fragFirstname = SettingsKeyValueFragment.newInstance("firstname", getString(R.string.settings_firstname), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
		Fragment fragSurname = SettingsKeyValueFragment.newInstance("surname", getString(R.string.settings_lastname), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
		Fragment fragAge = SettingsKeyValueFragment.newInstance("age", getString(R.string.settings_age), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		Fragment fragHeight = SettingsKeyValueFragment.newInstance("height", getString(R.string.settings_height), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

		transaction.add(R.id.fragmentParent, fragFirstname);
		transaction.add(R.id.fragmentParent, fragSurname);
		transaction.add(R.id.fragmentParent, fragAge);
		transaction.add(R.id.fragmentParent, fragHeight);
		transaction.commit();

		View view = inflater.inflate(R.layout.settings_personal_fragment, container, false);

		view.findViewById(R.id.button_logout).setOnClickListener(v -> {
			FirebaseAuth.getInstance().signOut();
			startActivity(new Intent(getActivity(), LauncherActivity.class));
		});

		return view;
	}

}