package com.papei.thanos.speedometer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * A custom array adapter that populates list views.
 */
public class ViolationsAdapter extends ArrayAdapter<DatabaseHelper.ViolationRecord> {
    /**
     * Default constructor.
     *
     * @param context The current context.
     * @param objects The list of violation records.
     */
    public ViolationsAdapter(Context context, List<DatabaseHelper.ViolationRecord> objects) {
        super(context, R.layout.violation_item, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the view or a new reference from the layout inflater.
        View view = convertView != null ? convertView : LayoutInflater.from(getContext()).inflate(R.layout.violation_item, parent, false);

        // Get all text views from the violation item template.
        TextView speedTextView = view.findViewById(R.id.violationSpeedTextView);
        TextView longitudeTextView = view.findViewById(R.id.violationLongitudeTextView);
        TextView latitudeTextView = view.findViewById(R.id.violationLatitudeTextView);
        TextView timestampTextView = view.findViewById(R.id.violationTimestampTextView);

        // Get the current violation record reference.
        DatabaseHelper.ViolationRecord record = getItem(position);

        // Setup a click listener so every time the user clicks on a
        // record the map activity is started with propagated intent data.
        // We do this so when the map is loaded, it will zoom in on the violation marker.
        view.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MapActivity.class);
            intent.putExtra("longitude", record.getLongitude());
            intent.putExtra("latitude", record.getLatitude());
            getContext().startActivity(intent);
        });

        // Update text views with data from the record.
        speedTextView.setText(Utilities.formatSpeed(getContext(), record.getSpeed()));
        longitudeTextView.setText(Utilities.formatLongitude(getContext(), record.getLongitude()));
        latitudeTextView.setText(Utilities.formatLatitude(getContext(), record.getLatitude()));
        timestampTextView.setText(Utilities.formatTimestamp(record.getTimestamp()));

        return view;
    }
}
