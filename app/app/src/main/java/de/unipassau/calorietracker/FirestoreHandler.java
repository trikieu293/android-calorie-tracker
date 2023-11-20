package de.unipassau.calorietracker;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import de.unipassau.calorietracker.data.FSDay;
import de.unipassau.calorietracker.data.FSItem;
import de.unipassau.calorietracker.data.FSProduct;
import de.unipassau.calorietracker.data.FSUser;
import de.unipassau.calorietracker.data.HandleUserData;

public class FirestoreHandler {
	private static FirestoreHandler _inst;

	private final FirebaseAuth mAuth;
	private final FirebaseFirestore db;

	// DB vars
	private FSUser user = null;

	public FSUser getUser() {
		return user;
	}

	public void setUser(FSUser mUser) {
		this.user = mUser;
	}

	public FirestoreHandler() {
		// Initialize Firebase Auth
		mAuth = FirebaseAuth.getInstance();

		// Initialize Firestore object
		db = FirebaseFirestore.getInstance();
	}

	public static FirestoreHandler getInstance() {
		if (_inst == null)
			_inst = new FirestoreHandler();
		return _inst;
	}


	/**
	 * Method for getting user data from the server
	 *
	 * @param handleFunction A functional interface that handles the returned data
	 */
	public void getUserData(HandleUserData handleFunction) {
		// Get the document with the UID of the user
		DocumentReference docRef = db.collection("users").document(mAuth.getUid());
		docRef.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				DocumentSnapshot document = task.getResult();
				if (document != null && document.exists()) {
					Log.d("FIRESTORE", "DocumentSnapshot data: " + document.getData());

					FSUser user = new FSUser();
					// Mapping values to a FDUser Object
					user.firstname = document.getString("firstname");
					user.surname = document.getString("surname");
					user.gender = document.getString("gender");
					user.age = document.contains("age") ? document.getLong("age") : 0;
					user.height = document.contains("height") ? document.getLong("height") : 0;
					user.weightHistory = document.contains("weightHistory") ? (Map<String, Long>) document.get("weightHistory") : new HashMap<>();

					// Set the user var to the result
					setUser(user);

					// Call functional interface
					handleFunction.handleUserData(user);
				} else {
					Log.d("FIRESTORE", "No such document");

					// Call functional interface
					handleFunction.handleUserData(null);
				}
			} else {
				Log.e("FIRESTORE", "get failed with ", task.getException());

				// Call functional interface
				handleFunction.handleUserData(user);
			}
		});
	}

	/**
	 * Method for setting a user document on the server. Existing documents will be merged and the server-data will only be overwritten by non-null attributes.
	 *
	 * @param user is the user object that should be stored on the server
	 */
	public void setUserData(FSUser user, Runnable then) {
		Map<String, Object> data = new HashMap<>();

		for (Field f : user.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			try {
				if (f.get(user) != null && !f.get(user).equals(0L)) {
					data.put(f.getName(), f.get(user));
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		db.collection("users").document(mAuth.getUid())
				.set(data, SetOptions.merge())
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						Log.d("FIRESTORE", "DocumentSnapshot successfully written!");
						then.run();
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Log.w("FIRESTORE", "Error writing document", e);
					}
				});
	}

	public void getDays(Consumer<Map<String, FSDay>> result) {
		db.collection("users").document(mAuth.getUid()).collection("days").get()
				.addOnCompleteListener(task -> {
					if (task.isSuccessful()) {
						Map<String, FSDay> ret = new HashMap<>();
						for (QueryDocumentSnapshot document : task.getResult()) {
							ret.put(document.getId(), document.toObject(FSDay.class));
						}
						result.accept(ret);
					} else {
						Log.d("FIRESTORE", "Error getting documents: ", task.getException());
					}
				});
	}

	public void getDay(String date, Consumer<FSDay> result) {
		DocumentReference docRef = db.collection("users").document(mAuth.getUid()).collection("days").document(date);
		docRef.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				DocumentSnapshot document = task.getResult();
				if (document != null && document.exists()) {
					result.accept(document.toObject(FSDay.class));
				} else {
					Log.d("FIRESTORE", "No such document");

					FSDay d = new FSDay();
					d.items = new ArrayList<>();
					d.stage = "NORMAL";
					d.sumCalorie = 0L;
					result.accept(d);
				}
			} else {
				Log.e("FIRESTORE", "get failed with ", task.getException());
			}
		});
	}

	public void addItemToDate(String date, FSItem item, Runnable then) {
		db.collection("users").document(mAuth.getUid()).collection("days")
				.document(date).get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				if (task.getResult().exists()) {
					// Get the current list of consumed goods. This workaround is necessary because simply updating an array with FieldValue.arrayUnion() would remove duplicates.
					db.collection("users").document(mAuth.getUid()).collection("days").document(date).get().addOnCompleteListener(t2 -> {
						if (t2.isSuccessful()) {
							DocumentSnapshot document = t2.getResult();
							if (document != null && document.exists()) {
								List<FSItem> itemsInDb = (List<FSItem>) document.get("items");
								// Add item to new list
								itemsInDb.add(item);
								// Set day array on firestore
								db.collection("users").document(mAuth.getUid()).collection("days")
										.document(date).update("items", itemsInDb)
										.addOnSuccessListener(aVoid -> {
											Log.d("FIRESTORE", "DocumentSnapshot successfully written!");
											then.run();
										})
										.addOnFailureListener(e -> Log.w("FIRESTORE", "Error writing document", e));
							} else {
								Log.d("FIRESTORE", "No such document");
							}
						} else {
							Log.e("FIRESTORE", "get failed with ", t2.getException());
						}
					});
				} else {
					// Create a new Day
					FSDay fd = new FSDay();
					fd.sumCalorie = 0;
					fd.stage = "NORMAL";
					fd.items = new ArrayList<>();
					fd.items.add(item);
					db.collection("users").document(mAuth.getUid()).collection("days")
							.document(date).set(fd)
							.addOnSuccessListener(aVoid -> {
								Log.d("FIRESTORE", "DocumentSnapshot successfully initially written!");
								then.run();
							})
							.addOnFailureListener(e -> Log.w("FIRESTORE", "Error writing document", e));
				}
			}
		}).addOnFailureListener(e -> Log.w("FIRESTORE", "Error writing document", e));
	}

	public void deleteItemFromDate(String date, FSItem item, Runnable then) {
		db.collection("users").document(mAuth.getUid()).collection("days")
				.document(date).update("items", FieldValue.arrayRemove(item))
				.addOnSuccessListener(aVoid -> {
					Log.d("FIRESTORE", "DocumentSnapshot successfully written!");
					then.run();
				})
				.addOnFailureListener(e -> Log.w("FIRESTORE", "Error writing document", e));
	}


	/**
	 * Method for getting a product from the server
	 *
	 * @param id             The id of the product
	 * @param handleFunction A functional interface that handles the returned data
	 * @param notExistent    A functional interface that handles what to do if document is not existent
	 */
	public void getProductById(String id, Consumer<FSProduct> handleFunction, Runnable notExistent) {
		DocumentReference docRef = db.collection("products").document(id);
		docRef.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				DocumentSnapshot document = task.getResult();
				if (document != null && document.exists()) {
					Log.d("FIRESTORE", "DocumentSnapshot data: " + document.getData());

					FSProduct _p = document.toObject(FSProduct.class);
					_p.id = id;
					handleFunction.accept(_p);
				} else {
					Log.d("FIRESTORE", "No such document");
					notExistent.run();
				}
			} else {
				Log.e("FIRESTORE", "get failed with ", task.getException());
			}
		});
	}

	/**
	 * Method for getting a product from the server
	 *
	 * @param handleFunction A functional interface that handles the returned data
	 */
	public void getProducts(Consumer<List<FSProduct>> handleFunction) {
		db.collection("products").get()
				.addOnCompleteListener(task -> {
					if (task.isSuccessful()) {
						List<FSProduct> ret = new ArrayList<>();
						for (QueryDocumentSnapshot document : task.getResult()) {
							FSProduct _p = document.toObject(FSProduct.class);
							_p.id = document.getId();
							ret.add(_p);
						}
						handleFunction.accept(ret);
					} else {
						Log.d("FIRESTORE", "Error getting documents: ", task.getException());
					}
				});
	}

	/**
	 * Method for setting a product document on the server. Existing documents will be merged and the server-data will only be overwritten by non-null attributes.
	 *
	 * @param id      is the user object that should be stored on the server
	 * @param product is the user object that should be stored on the server
	 */
	public void setProduct(String id, FSProduct product, Runnable then) {
		db.collection("products").document(id)
				.set(product, SetOptions.merge())
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						Log.d("FIRESTORE", "DocumentSnapshot successfully written!");
						then.run();
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Log.w("FIRESTORE", "Error writing document", e);
					}
				});
	}

	/**
	 * Method for removing a product document on the server.
	 *
	 * @param id   is the user object that should be removed from the server
	 * @param then Run after removing complete
	 */
	public void removeProduct(String id, Runnable then) {
		db.collection("products").document(id)
				.delete()
				.addOnSuccessListener(aVoid -> {
					Log.d("FIRESTORE", "DocumentSnapshot successfully removed!");
					then.run();
				})
				.addOnFailureListener(e -> Log.w("FIRESTORE", "Error removing document", e));
	}

}
