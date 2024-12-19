package com.jeffrey.fypweatherapp.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jeffrey.fypweatherapp.R;

import java.util.ArrayList;
import java.util.List;

public class LocationAdapter extends ArrayAdapter<String> {
	private final List<String> locations;
	private final List<String> documentIds;
	private final List<String> locationsToDelete = new ArrayList<>();

	public LocationAdapter(Context context, List<String> locations, List<String> documentIds) {
		super(context, R.layout.listitem_manage_area, locations);
		this.locations = locations;
		this.documentIds = documentIds;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_manage_area, parent, false);
		}

		TextView locationText = convertView.findViewById(R.id.listitem_manage_area_text);
		ImageView deleteButton = convertView.findViewById(R.id.listitem_manage_area_delete_button);

		// Set the location name
		locationText.setText(locations.get(position));

		// Handle delete button click
		deleteButton.setOnClickListener(v -> {
			// Add the document ID to the delete list
			locationsToDelete.add(documentIds.get(position));

			// Remove the location from the list
			locations.remove(position);
			documentIds.remove(position);
			notifyDataSetChanged(); // Refresh the ListView
		});

		return convertView;
	}

	public List<String> getLocationsToDelete() {
		return locationsToDelete;
	}

	public void updateData(List<String> newLocations, List<String> newDocumentIds) {
		locations.clear();
		locations.addAll(newLocations);
		documentIds.clear();
		documentIds.addAll(newDocumentIds);
		notifyDataSetChanged(); // Refresh the ListView
	}
}

