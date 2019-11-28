package com.papei.thanos.speedometer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents a wrapper around SQLite android helpers.
 */
public final class DatabaseHelper extends SQLiteOpenHelper {
    /**
     * The file name of the SQLite database.
     */
    private static final String DB_NAME = "app.db";

    /**
     * The database version used for updates.
     */
    private static final int DB_VERSION = 1;

    /**
     * Default constructor.
     *
     * @param context The context reference.
     */
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Adds a new speeding violation to the database asynchronously.
     *
     * @param longitude The longitude of the violation.
     * @param latitude The latitude of the violation.
     * @param speed The current speed of the violation.
     * @return A completable future that returns the ID of the new record once created.
     */
    public CompletableFuture<Long> addViolation(double longitude, double latitude, float speed) {
        return CompletableFuture.supplyAsync(() -> {
            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(ViolationRecordColumns.LONGITUDE, longitude);
            values.put(ViolationRecordColumns.LATITUDE, latitude);
            values.put(ViolationRecordColumns.SPEED, speed);
            values.put(ViolationRecordColumns.TIMESTAMP, System.currentTimeMillis());

            return db.insert(ViolationRecordColumns.TABLE_NAME, null, values);
        });
    }

    /**
     * Gets the list of all violations in the database.
     *
     * @return A completable future that returns a list of all violations once fetched.
     */
    public CompletableFuture<List<ViolationRecord>> getViolations() {
        return CompletableFuture.supplyAsync(() -> {
            SQLiteDatabase db = getReadableDatabase();

            Cursor cursor = db.query(
                    ViolationRecordColumns.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    ViolationRecordColumns.TIMESTAMP + " DESC"
            );

            List<ViolationRecord> records = new ArrayList<>();

            while (cursor.moveToNext()) {
                records.add(this.bindCursorToViolation(cursor));
            }

            cursor.close();

            return records;
        });
    }

    /**
     * Gets the list of all violations recorded during the week.
     *
     * @return A completable future that returns a list of violations once fetched.
     */
    public CompletableFuture<List<ViolationRecord>> getLastWeekViolations() {
        return CompletableFuture.supplyAsync(() -> {
            SQLiteDatabase db = getReadableDatabase();

            long time = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7);

            Cursor cursor = db.query(
                    ViolationRecordColumns.TABLE_NAME,
                    null,
                    ViolationRecordColumns.TIMESTAMP + " >= ?",
                    new String[]{Long.toString(time)},
                    null,
                    null,
                    ViolationRecordColumns.TIMESTAMP + " DESC"
            );

            List<ViolationRecord> records = new ArrayList<>();

            while (cursor.moveToNext()) {
                ViolationRecord record = this.bindCursorToViolation(cursor);
                records.add(record);
            }

            cursor.close();

            return records;
        });
    }

    /**
     * Creates a violation record mapping from a cursor reference.
     *
     * @param cursor The cursor created from a query.
     * @return A new violation record that holds the fetched data.
     */
    private ViolationRecord bindCursorToViolation(Cursor cursor) {
        return new ViolationRecord(
                cursor.getLong(cursor.getColumnIndex(ViolationRecordColumns._ID)),
                cursor.getDouble(cursor.getColumnIndex(ViolationRecordColumns.LONGITUDE)),
                cursor.getDouble(cursor.getColumnIndex(ViolationRecordColumns.LATITUDE)),
                cursor.getFloat(cursor.getColumnIndex(ViolationRecordColumns.SPEED)),
                cursor.getLong(cursor.getColumnIndex(ViolationRecordColumns.TIMESTAMP))
        );
    }

    /**
     * The list of actions to execute when the database must be created.
     *
     * @param db The database reference.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ViolationRecordColumns.TABLE_NAME + "(" +
                ViolationRecordColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ViolationRecordColumns.LONGITUDE + " DOUBLE, " +
                ViolationRecordColumns.LATITUDE + " DOUBLE, " +
                ViolationRecordColumns.SPEED + " FLOAT, " +
                ViolationRecordColumns.TIMESTAMP + " LONG)"
        );
    }

    /**
     * The list of actions to execute when the database must be updated.
     *
     * @param db The database reference.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ViolationRecordColumns.TABLE_NAME);
        onCreate(db);
    }

    /**
     * The list of actions to execute when the database must be downgraded.
     *
     * @param db The database reference.
     * @param oldVersion The old version of the database.
     * @param newVersion The new version of the database.
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * A model that represents all violation records on the database.
     */
    public final class ViolationRecord {
        /**
         * The numerical ID of the violation in the database.
         */
        private long id;

        /**
         * The longitude of the violation.
         */
        private double longitude;

        /**
         * The latitude of the violation.
         */
        private double latitude;

        /**
         * The recorded speed of the violation.
         */
        private float speed;

        /**
         * The recorded timestamp of the violation.
         */
        private long timestamp;

        /**
         * Default constructor.
         *
         * @param id The numerical ID of the violation in the database.
         * @param longitude The longitude of the violation.
         * @param latitude The latitude of the violation.
         * @param speed The recorded speed of the violation.
         * @param timestamp The recorded timestamp of the violation.
         */
        public ViolationRecord(long id, double longitude, double latitude, float speed, long timestamp) {
            this.id = id;
            this.longitude = longitude;
            this.latitude = latitude;
            this.speed = speed;
            this.timestamp = timestamp;
        }

        /**
         * Gets the numerical ID of the violation in the database.
         * @return The numerical ID of the violation in the database.
         */
        public long getId() {
            return id;
        }

        /**
         * Gets the longitude of the violation.
         *
         * @return The longitude of the violation.
         */
        public double getLongitude() {
            return longitude;
        }

        /**
         * Gets the latitude of the violation.
         *
         * @return The latitude of the violation.
         */
        public double getLatitude() {
            return latitude;
        }

        /**
         * Gets the recorded speed of the violation.
         *
         * @return The recorded speed of the violation.
         */
        public float getSpeed() {
            return speed;
        }

        /**
         * Gets the recorded timestamp of the violation.
         *
         * @return The recorded timestamp of the violation.
         */
        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * A class that holds violation column names, used in the database.
     */
    private static class ViolationRecordColumns implements BaseColumns {
        /**
         * The name of the table that holds all violations.
         */
        public static final String TABLE_NAME = "violations";

        /**
         * The longitude column name.
         */
        public static final String LONGITUDE = "longitude";

        /**
         * The latitude column name.
         */
        public static final String LATITUDE = "latitude";

        /**
         * The speed column name.
         */
        public static final String SPEED = "speed";

        /**
         * The timestamp column name.
         */
        public static final String TIMESTAMP = "timestamp";
    }
}
