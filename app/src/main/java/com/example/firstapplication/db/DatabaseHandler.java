package com.example.firstapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.firstapplication.entity.Attendance;
import com.example.firstapplication.utils.Helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "attendanceChecking";
    private static final String TABLE_ATTENDANCES = "attendances";
    private static final String KEY_ID = "id";
    private static final String KEY_INFO = "info";
    private static final String KEY_TYPE = "type";
    private static final String KEY_SCANNED_DATE = "scannedDate";
    private static final String KEY_IS_SYNCED = "isSynced";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_ATTENDANCES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_INFO + " TEXT, "
                + KEY_TYPE + " TEXT, " + KEY_SCANNED_DATE + " TEXT, "
                + KEY_IS_SYNCED + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCES);

        // Create tables again
        onCreate(db);
    }

    public void addAttendance(Attendance attendance) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_INFO, attendance.getInfo());
        values.put(KEY_TYPE, attendance.getType());
        values.put(KEY_SCANNED_DATE, Helper.getDateTime(new Date()));

        db.insert(TABLE_ATTENDANCES, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    public List<Attendance> getAllAttendances() {
        List<Attendance> attendanceList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_ATTENDANCES + " ORDER BY " + KEY_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Integer id = Integer.parseInt(cursor.getString(0));
                String info = cursor.getString(1);
                String type = cursor.getString(2);
                String dateStr = cursor.getString(3);
                Boolean isSynced = cursor.getInt(4) > 0;
                Attendance attendance = new Attendance(id, info, type, dateStr, isSynced);

                attendanceList.add(attendance);
            } while (cursor.moveToNext());
        }
        return attendanceList;
    }

    public List<Attendance> getAttendancesHaveNotSyncedYet() {
        List<Attendance> attendanceList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_ATTENDANCES
                + " WHERE " + KEY_IS_SYNCED + " = 0"
                + " ORDER BY " + KEY_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Integer id = Integer.parseInt(cursor.getString(0));
                String info = cursor.getString(1);
                String type = cursor.getString(2);
                String dateStr = cursor.getString(3);
                Boolean isSynced = cursor.getInt(4) > 0;
                Attendance attendance = new Attendance(id, info, type, dateStr, isSynced);

                attendanceList.add(attendance);
            } while (cursor.moveToNext());
        }
        return attendanceList;
    }

    public Boolean checkHaveNonSyncedAttendance(){
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_ATTENDANCES
                + " WHERE " + KEY_IS_SYNCED + " = 0"
                + " ORDER BY " + KEY_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst())
            return true;
        return false;
    }

    public int updateAttendanceStatus(Attendance attendance, Integer status) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_IS_SYNCED, status);

            // updating row
            return db.update(TABLE_ATTENDANCES, values, KEY_ID + " = ?",
                    new String[]{String.valueOf(attendance.getId())});
        } catch (Exception ex) {
            ex.printStackTrace();
            return -9999;
        }
    }

    public void deleteAttendance(Attendance attendance) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ATTENDANCES, KEY_ID + " = ?",
                new String[]{String.valueOf(attendance.getId())});
        db.close();
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_ATTENDANCES);
    }

    public Boolean checkAttendanceExist(String type, String id, String date){
        String selectQuery = "SELECT * FROM " + TABLE_ATTENDANCES
                + " WHERE " + KEY_INFO + " LIKE " + "'" + id.trim() + "%'"
                + " AND " + KEY_TYPE + " = " +  "'"  + type + "'"
                + " AND " + KEY_SCANNED_DATE + " = " + "'" + date + "'"
                + " ORDER BY " + KEY_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst())
            return true;
        return false;
    }
}
