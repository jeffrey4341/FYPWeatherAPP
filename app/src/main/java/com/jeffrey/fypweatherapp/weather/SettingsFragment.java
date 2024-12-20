package com.jeffrey.fypweatherapp.weather;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeffrey.fypweatherapp.R;
import com.jeffrey.fypweatherapp.dslv.DragSortListView;
import com.jeffrey.fypweatherapp.dynamicweathertype.BaseDrawer.Type;
import com.jeffrey.fypweatherapp.util.LocationAdapter;
import com.jeffrey.fypweatherapp.util.LocationManager;
import com.jeffrey.fypweatherapp.widget.support.LabelSpinner;
import com.jeffrey.fypweatherapp.widget.support.SmoothSwitch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends BaseFragment {
	private View mRootView;

	//private View dialogView;
	private TextView mGpsTextView;
//	private ArrayList<Area> mSelectedAreas;// = new
											// ArrayList<ApiManager.Area>();
	private Type type = Type.DEFAULT;

//	private static final String BUNDLE_EXTRA_SELECTED_AREAS = "BUNDLE_EXTRA_SELECTED_AREAS";

	private static final int RC_SIGN_IN = 123;
	private TextView loginLogoutTextView;
	private FirebaseAuth mAuth;
	private GoogleSignInClient googleSignInClient;
	private static List<String> documentIds = new ArrayList<>();
	private List<String> originalLocations = new ArrayList<>();

	public static SettingsFragment makeInstance() {
		SettingsFragment fragment = new SettingsFragment();
		Bundle bundle = new Bundle();
		fragment.setArguments(bundle);
		return fragment;
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		this.mSelectedAreas = (ArrayList<Area>) getArguments().getSerializable(BUNDLE_EXTRA_SELECTED_AREAS);
//		if(mSelectedAreas == null){
//			mSelectedAreas = new ArrayList<ApiManager.Area>();
//		}
		final int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if (hourOfDay >= 7 && hourOfDay <= 18) {
			type = Type.UNKNOWN_D;
		} else {
			type = Type.UNKNOWN_N;
		}
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mRootView == null) {
			mRootView = inflater.inflate(R.layout.fragment_settings, null);

			mAuth = FirebaseAuth.getInstance();

			// Configure Google Sign-In
			GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
					.requestIdToken(getString(R.string.default_web_client_id)) // Replace with your web client ID from Firebase
					.requestEmail()
					.build();
			googleSignInClient = GoogleSignIn.getClient(getContext(), gso);

			loginLogoutTextView = mRootView.findViewById(R.id.settings_login_logout);
			updateLoginStatus();

			// Click Listener for Login/Logout
			loginLogoutTextView.setOnClickListener(v -> {
				if (mAuth.getCurrentUser() == null) {
					signIn();
				} else {
					signOut();
				}
			});

			final Context context = mRootView.getContext();
			mRootView.findViewById(R.id.settings_github).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String url = "https://github.com/jeffrey4341/FYPWeatherAPP";
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					context.startActivity(Intent.createChooser(intent, url));
				}
			});
			SmoothSwitch switchNotification = (SmoothSwitch) mRootView.findViewById(R.id.settings_switch_notification);
			final LabelSpinner smallIconSpinner = (LabelSpinner) mRootView.findViewById(R.id.settings_spinner_smallicon);
			final LabelSpinner textColorSpinner = (LabelSpinner) mRootView.findViewById(R.id.settings_spinner_textcolor);
//			switchNotification.setChecked(WeatherNotificationService.Config.isShowNotification(getActivity()), false);
//			smallIconSpinner.setVisibility(switchNotification.isChecked() ? View.VISIBLE : View.GONE);
			textColorSpinner.setVisibility(switchNotification.isChecked() ? View.VISIBLE : View.GONE);
			switchNotification.setOnCheckedChangeListener(new SmoothSwitch.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					smallIconSpinner.setVisibility(isChecked ? View.VISIBLE : View.GONE);
					textColorSpinner.setVisibility(isChecked ? View.VISIBLE : View.GONE);
//					WeatherNotificationService.Config.setShowNotification(getActivity(), isChecked);
//					WeatherNotificationService.startServiceCheckConfig(getActivity());
				}
			});
			
			
//			smallIconSpinner.setSelection(WeatherNotificationService.Config.getSmallIconType(smallIconSpinner.getContext()), false);
			smallIconSpinner.setSelection(0, false);
			smallIconSpinner.setOnSelectionChangedListener(new LabelSpinner.OnSelectionChangedListener() {
				@Override
				public void OnSelectionChanged(int position) {
//					WeatherNotificationService.Config.setSmallIconType(context, position);
//					WeatherNotificationService.startServiceCheckConfig(context);
				}
			});
//			textColorSpinner.setSelection(WeatherNotificationService.Config.getRemoteViewTextColor(context), false);
			textColorSpinner.setSelection(0, false);
			textColorSpinner.setOnSelectionChangedListener(new LabelSpinner.OnSelectionChangedListener() {
				@Override
				public void OnSelectionChanged(int position) {
//					WeatherNotificationService.Config.setRemoteViewTextColor(context, position);
//					WeatherNotificationService.startServiceCheckConfig(context);
				}
			});
		}
		return mRootView;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Reference to the Manage Area button
		TextView manageAreaButton = view.findViewById(R.id.settings_manage_area);

		// Set click listener to show the Manage Area dialog
		manageAreaButton.setOnClickListener(v -> showManageAreaDialog());
	}

	private void signIn() {
		Intent signInIntent = googleSignInClient.getSignInIntent();
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}

	private void signOut() {
		mAuth.signOut();
		googleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
			Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
			// Delay execution to allow the toast to be visible
			new android.os.Handler().postDelayed(() -> {
				clearAppData();
				restartApplication();
			}, 1000); // 1 second delay
		});
	}

	// Method to clear app data and cache
	private void clearAppData() {
		try {
			Runtime runtime = Runtime.getRuntime();
			runtime.exec("pm clear " + requireContext().getPackageName());
		} catch (Exception e) {
			Log.e("SettingsFragment", "Failed to clear app data: ", e);
		}
	}

	// Method to restart the application
	private void restartApplication() {
		Intent intent = new Intent(requireContext(), MainActivity.class); // Replace 'MainActivity' with your launcher activity
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		requireActivity().finish(); // Finish the current activity
		System.exit(0); // Optional: Forcefully close the app process
	}

	private void updateLoginStatus() {
		if (mAuth.getCurrentUser() != null) {
			loginLogoutTextView.setText("Logout");
		} else {
			loginLogoutTextView.setText("Login");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		notifyActivityUpdate();
		mRootView.findViewById(R.id.settings_manage_area).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAuth.getCurrentUser() != null) {
					showManageAreaDialog();
				} else {
					Toast.makeText(getContext(), "Please login to manage locations.", Toast.LENGTH_SHORT).show();
				}
			}
		});
		mGpsTextView = (TextView) mRootView.findViewById(R.id.settings_gps_location);
		
	}

	// Show Manage Area Dialog
	private void showManageAreaDialog() {
		// Inflate the dialog layout
		AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
		View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_manage_area, null);
		builder.setView(dialogView);

		AlertDialog alertDialog = builder.create();

		// Initialize ViewSwitcher and Buttons
		ViewSwitcher viewSwitcher = dialogView.findViewById(R.id.manage_areas_viewswitcher);
		Button addButton = dialogView.findViewById(R.id.manage_areas_add);
		Button cancelButton = dialogView.findViewById(R.id.manage_areas_cancle_button);
		Button saveButton = dialogView.findViewById(R.id.manage_areas_ok_button);
		Button returnButton = dialogView.findViewById(R.id.manage_areas_return_button);
		Button deleteButton = viewSwitcher.findViewById(R.id.listitem_manage_area_delete_button);
		EditText searchEditText = dialogView.findViewById(R.id.manage_areas_search_edittext);
		ListView searchListView = dialogView.findViewById(R.id.manage_areas_search_listview);
		DragSortListView dragSortListView = dialogView.findViewById(R.id.manage_areas_dragSortListView);

		// Adapter to manage locations
		List<String> locations = new ArrayList<>();
		List<String> documentIds = new ArrayList<>(); // Document IDs from Firestore
		LocationAdapter adapter = new LocationAdapter(requireContext(), locations, documentIds);
		dragSortListView.setAdapter(adapter);

		// Load saved locations into the list
		loadSavedLocations(dragSortListView, adapter);

//		// Retrofit setup
//		Retrofit retrofit = new Retrofit.Builder()
//				.baseUrl("https://maps.googleapis.com/maps/api/")
//				.addConverterFactory(ocConverterFactory.create())
//				.build();
//		ApiManager.GeocodingApiService apiService = retrofit.create(ApiManager.GeocodingApiService.class);

		// Add Button: Switch to the search layout
		addButton.setOnClickListener(v -> {
			if (viewSwitcher.getDisplayedChild() == 0) {
				viewSwitcher.showNext(); // Show the search layout
			}
		});


		// Return Button: Switch back to the main layout
		returnButton.setOnClickListener(v -> {
			if (viewSwitcher.getDisplayedChild() == 1) {
				viewSwitcher.showPrevious(); // Show the main layout
			}
		});

		// Cancel Button: Close the dialog
		cancelButton.setOnClickListener(v -> {
			alertDialog.dismiss(); // Use the AlertDialog reference to dismiss
		});

		// Save Button: Save changes (placeholder logic)
		saveButton.setOnClickListener(v -> {
			FirebaseFirestore db = FirebaseFirestore.getInstance();
			String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
			List<String> locationsToDelete = adapter.getLocationsToDelete();

			// Current location details
			String locationName = LocationManager.getInstance().getCityname();
			double latitude = LocationManager.getInstance().getLatitude();
			double longitude = LocationManager.getInstance().getLongitude();

			// Store existing locations to check for duplicates
			Map<String, Boolean> existingLocations = new HashMap<>();

			// Step 1: Fetch all existing locations to avoid duplicates
			db.collection("users")
					.document(userId)
					.collection("locations")
					.get()
					.addOnSuccessListener(queryDocumentSnapshots -> {
						for (DocumentSnapshot document : queryDocumentSnapshots) {
							String name = document.getString("name");
							Double lat = document.getDouble("latitude");
							Double lon = document.getDouble("longitude");

							// Add location to the map as a key
							if (name != null && lat != null && lon != null) {
								String key = name + "_" + lat + "_" + lon;
								existingLocations.put(key, true);
							}
						}

						// Step 2: Save the current location if it's not a duplicate
						String currentKey = locationName + "_" + latitude + "_" + longitude;
						if (!existingLocations.containsKey(currentKey)) {
							saveLocationToFirebase(locationName, latitude, longitude);
						} else {
							Log.d("DAMN", "Location already exists: " + locationName);
						}

						// Step 3: Delete marked locations
						for (String docId : locationsToDelete) {
							db.collection("users")
									.document(userId)
									.collection("locations")
									.document(docId)
									.delete()
									.addOnSuccessListener(aVoid -> Log.d("DAMN", "Deleted location: " + docId))
									.addOnFailureListener(e -> Log.e("DAMN", "Error deleting location", e));
						}

						// Notify the user
						Toast.makeText(requireContext(), "Changes saved!", Toast.LENGTH_SHORT).show();
						alertDialog.dismiss(); // Close the dialog
					})
					.addOnFailureListener(e -> Log.e("DAMN", "Error fetching existing locations", e));
		});



//		// Add TextWatcher to fetch search results
//		searchEditText.addTextChangedListener(new TextWatcher() {
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				if (s.length() > 2) {
//					fetchGeocodingResults(apiService, s.toString(), searchResults, adapter);
//				}
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {}
//		});

		// Show the dialog
		alertDialog.show();
	}

//	private void fetchGeocodingResults(ApiManager.GeocodingApiService apiService, String query, List<String> searchResults, ArrayAdapter<String> adapter) {
//		Call<ApiManager.GeocodingResponse> call = apiService.getGeocodingResults(query, "YOUR_API_KEY");
//		call.enqueue(new Callback<ApiManager.GeocodingResponse>() {
//			@Override
//			public void onResponse(Call<ApiManager.GeocodingResponse> call, Response<ApiManager.GeocodingResponse> response) {
//				if (response.isSuccessful() && response.body() != null) {
//					searchResults.clear();
//					for (ApiManager.GeocodingResult result : response.body().getResults()) {
//						searchResults.add(result.getFormattedAddress());
//					}
//					adapter.notifyDataSetChanged(); // Update ListView
//				}
//			}
//
//			@Override
//			public void onFailure(Call<ApiManager.GeocodingResponse> call, Throwable t) {
//				Toast.makeText(requireContext(), "Failed to fetch results", Toast.LENGTH_SHORT).show();
//			}
//
//		});
//	}

	// Save Location to Firebase
	private void saveLocationToFirebase(String name, double latitude, double longitude) {
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

		Map<String, Object> locationData = new HashMap<>();
		locationData.put("name", name);
		locationData.put("latitude", latitude);
		locationData.put("longitude", longitude);

		db.collection("users")
				.document(userId)
				.collection("locations")
				.add(locationData)
				.addOnSuccessListener(documentReference -> Log.d("DAMN", "Location saved: " + name))
				.addOnFailureListener(e -> Log.e("DAMN", "Error saving location", e));
	}


	// Load Saved Locations
	private void loadSavedLocations(DragSortListView dragSortListView, LocationAdapter adapter) {
		String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
		FirebaseFirestore db = FirebaseFirestore.getInstance();

		db.collection("users")
				.document(userId)
				.collection("locations")
				.get()
				.addOnSuccessListener(queryDocumentSnapshots -> {
					List<String> locations = new ArrayList<>();
					List<String> documentIds = new ArrayList<>();

					for (DocumentSnapshot document : queryDocumentSnapshots) {
						locations.add(document.getString("name"));
						documentIds.add(document.getId());
					}

					adapter.updateData(locations, documentIds); // Update adapter data
				})
				.addOnFailureListener(e -> {
					Log.e("SettingsFragment", "Error loading locations", e);
					Toast.makeText(requireContext(), "Failed to load locations.", Toast.LENGTH_SHORT).show();
				});
	}





	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
			GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult();
			if (account != null) {
				firebaseAuthWithGoogle(account);
			}
		}
	}

	private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
		AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
		mAuth.signInWithCredential(credential)
				.addOnCompleteListener(requireActivity(), task -> {
					if (task.isSuccessful()) {
						Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
						updateLoginStatus();
					} else {
						Log.w("FUCK", "signInWithCredential:failure", task.getException());
						Toast.makeText(getContext(), "Login failed", Toast.LENGTH_SHORT).show();
					}
				});
	}
	

	@Override
	public String getTitle() {
		return "MyWeather";
	}

	@Override
	public void onSelected() {
	}

	@Override
	public Type getDrawerType() {
		return type;
	}
	
}








