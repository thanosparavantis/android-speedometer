package com.papei.thanos.speedometer;

import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * The activity that holds all violation records.
 */
public class RecordsActivity extends AppCompatActivity {
    /**
     * The list view reference.
     */
    private ListView violationsListView;

    /**
     * The database helper reference.
     */
    private DatabaseHelper dbHelper;

    /**
     * The violations adapter reference.
     */
    private ViolationsAdapter violationsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        // Grab the violation list view reference by ID.
        this.violationsListView = findViewById(R.id.violationsListView);

        // Create a new connection with the database through the helper.
        this.dbHelper = new DatabaseHelper(this);

        // Display all records with all time sorting.
        sortByAllTime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close the database connection.
        this.dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        // Add the standard menu and the extra sorting menu.
        inflater.inflate(R.menu.menu_sorting, menu);
        inflater.inflate(R.menu.menu_options, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sort_as) {
            // When the sorting option is clicked, display the sorting options dialog.
            viewSortDialog();

            return true;
        } else {
            return Utilities.handleNavigation(this, item.getItemId());
        }
    }

    private void viewSortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.sort_by);

        builder.setItems(new String[]{
                getString(R.string.last_week),
                getString(R.string.all_time)
        }, (dialog, which) -> {
            switch (which) {
                case 0:
                    sortByLastWeek();
                    Toast.makeText(getApplicationContext(), R.string.last_week_message, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    sortByAllTime();
                    Toast.makeText(getApplicationContext(), R.string.all_time_message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void sortByLastWeek() {
        this.dbHelper.getLastWeekViolations().thenAcceptAsync(
                list -> {
                    // Fetch last week records asynchronously and then populate the list view.

                    getSupportActionBar().setSubtitle(R.string.last_week_detailed);
                    this.updateListViewData(list);
                }, getMainExecutor());
    }

    private void sortByAllTime() {
        this.dbHelper.getViolations().thenAcceptAsync(
                list -> {
                    // Fetch all time records asynchronously and then populate the list view.

                    getSupportActionBar().setSubtitle(R.string.all_time_detailed);
                    this.updateListViewData(list);
                }, getMainExecutor());
    }

    private void updateListViewData(List<DatabaseHelper.ViolationRecord> list) {
        // If the adapter doesn't exist create a new one,
        // otherwise reset the existing adapter and notify for changes.

        if (this.violationsAdapter == null) {
            this.violationsAdapter = new ViolationsAdapter(this, list);
            this.violationsListView.setAdapter(this.violationsAdapter);
        } else {
            this.violationsAdapter.clear();
            this.violationsAdapter.addAll(list);
            this.violationsAdapter.notifyDataSetChanged();
        }
    }
}
