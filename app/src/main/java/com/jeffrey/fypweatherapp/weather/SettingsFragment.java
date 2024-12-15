package com.jeffrey.fypweatherapp.weather;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.jeffrey.fypweatherapp.R;
import com.jeffrey.fypweatherapp.dynamicweathertype.BaseDrawer.Type;
import com.jeffrey.fypweatherapp.widget.support.LabelSpinner;
import com.jeffrey.fypweatherapp.widget.support.SmoothSwitch;
import com.jeffrey.fypweatherapp.weather.api.ApiManager;
//import com.jeffrey.fypweatherapp.weather.api.ApiManager.Area;

import java.util.ArrayList;
import java.util.Calendar;

public class SettingsFragment extends BaseFragment {
	private View mRootView;
	private TextView mGpsTextView;
//	private ArrayList<Area> mSelectedAreas;// = new
											// ArrayList<ApiManager.Area>();
	private Type type = Type.DEFAULT;

//	private static final String BUNDLE_EXTRA_SELECTED_AREAS = "BUNDLE_EXTRA_SELECTED_AREAS";

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
			final Context context = mRootView.getContext();
			mRootView.findViewById(R.id.settings_github).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String url = "https://github.com/Mixiaoxiao/Weather";
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










