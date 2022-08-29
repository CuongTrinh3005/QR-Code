package com.example.firstapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.firstapplication.entity.Attendace;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "attendanceChecking";
    private static final String TABLE_ATTENDANCES = "attendances";
    private static final String KEY_ID = "id";
    private static final String KEY_INFO = "info";
    private static final String KEY_SCANNED_DATE = "scannedDate";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_ATTENDANCES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_INFO + " TEXT,"
                + KEY_SCANNED_DATE + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCES);

        // Create tables again
        onCreate(db);
    }

    public void addAttendance(Attendace attendace) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_INFO, attendace.getInfo()); // Contact Name
        values.put(KEY_SCANNED_DATE, attendace.getScannedDate().toString());

        // Inserting Row
        db.insert(TABLE_ATTENDANCES, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    public List<Attendace> getAllAttendances() {
        List<Attendace> attendanceList = new ArrayList<Attendace>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ATTENDANCES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Integer id = Integer.parseInt(cursor.getString(0));
                String info = cursor.getString(1);
                String dateStr = cursor.getString(2);
                Attendace attendace = new Attendace(id, info, dateStr);

                attendanceList.add(attendace);
            } while (cursor.moveToNext());
        }
        return attendanceList;
    }
}
