package com.jeffrey.fypweatherapp.weather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.jeffrey.fypweatherapp.R;
import com.jeffrey.fypweatherapp.dynamicweathertype.BaseDrawer.Type;
import com.jeffrey.fypweatherapp.widget.support.LabelSpinner;
import com.jeffrey.fypweatherapp.widget.support.SmoothSwitch;
import com.jeffrey.fypweatherapp.weather.api.ApiManager;

import java.util.ArrayList;
import java.util.Calendar;

public class SettingsFragment extends BaseFragment {
	private View mRootView;
	private TextView mGpsTextView;
//	private ArrayList<Area> mSelectedAreas;// = new
											// ArrayList<ApiManager.Area>();
	private Type type = Type.DEFAULT;

//	private static final String BUNDLE_EXTRA_SELECTED_AREAS = "BUNDLE_EXTRA_SELECTED_AREAS";

	private static final int RC_SIGN_IN = 123;
	private TextView loginLogoutTextView;
	private FirebaseAuth mAuth;
	private GoogleSignInClient googleSignInClient;

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
		//System.exit(0); // Optional: Forcefully close the app process
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
				
			}
		});
		mGpsTextView = (TextView) mRootView.findViewById(R.id.settings_gps_location);
		
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










