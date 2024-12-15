package com.jeffrey.fypweatherapp.weather;

import android.app.Activity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.widget.Toast;

import com.jeffrey.fypweatherapp.dynamicweathertype.BaseDrawer;

public abstract class BaseFragment extends Fragment{

	public abstract String getTitle();
	public abstract void onSelected();
	public abstract BaseDrawer.Type getDrawerType();

	protected void notifyActivityUpdate() {
		if (getUserVisibleHint()) {
			Activity activity = getActivity();
			if (activity != null && activity instanceof MainActivity) {
				BaseDrawer.Type drawerType = getDrawerType();
				String drawerTypeString = (drawerType != null) ? drawerType.toString() : "UNKNOWN";
				((MainActivity) activity).updateCurDrawerType();
				// Toast.makeText(activity, getTitle() + " notifyActivityUpdate->" + drawerTypeString, Toast.LENGTH_SHORT).show();
			} else if (activity == null) {
				Log.w("BaseFragment", "notifyActivityUpdate getActivity() is NULL! Fragment might not be attached.");
			} else {
				Log.w("BaseFragment", "Activity is not an instance of MainActivity.");
			}
		}
	}

	protected void toast(String msg) {
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}
}
