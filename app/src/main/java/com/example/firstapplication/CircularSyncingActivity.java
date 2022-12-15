package com.example.firstapplication;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.firstapplication.db.DatabaseHandler;
import com.example.firstapplication.entity.Attendance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.firstapplication.utils.Helper.setActionBarBackGroundColor;

public class CircularSyncingActivity extends AppCompatActivity {
    public ProgressBar progressBar;
    private TextView progressText;
    private Integer index = 0;
    private RequestQueue queue;
    MediaPlayer mp = null;
    private DatabaseHandler databaseHandler = new DatabaseHandler(this);
    int syncedRecords = 0;
    String URL = "https://script.google.com/macros/s/AKfycbyWOtmVYxqViQj5ouhKXomrHs-yPYDlnrifE2g0wKYXZdN4_m78ttzzrNt8M7jomE2q/exec";

    public void setSyncedRecords(int num){
        syncedRecords = num;
    }

    public int getSyncedRecords(){
        return syncedRecords;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circular_syncing);

        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progress_text);
        mp = MediaPlayer.create(this, R.raw.success);

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);
        setActionBarBackGroundColor(actionBar, "#000000");

        try {
            List<Attendance> attendancesPreSynced = databaseHandler.getAttendancesHaveNotSyncedYet();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    databaseHandler.updateAttendanceStatus(attendancesPreSynced.get(index), 1);
                    int currentSyncedRecord = getSyncedRecords();
                    setSyncedRecords(currentSyncedRecord+1);
                    if (attendancesPreSynced.size() > 1) {
                        int nextIndex = index + 1;
                        postSpecificRecordInList(attendancesPreSynced, nextIndex,
                                CircularSyncingActivity.this, databaseHandler.getScannerName());
                    } else {
                        progressText.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(100);
                                progressText.setText("100%");
                                if (syncedRecords == attendancesPreSynced.size()) {
                                    progressText.removeCallbacks(this);
                                    Toast.makeText(CircularSyncingActivity.this, "Đồng bộ thành công",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        mp.start();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("error", error.toString());
                    Toast.makeText(CircularSyncingActivity.this,
                            "Đã có lỗi, vui lòng thử lại: " + error.toString(),
                            Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("action", "addItem");
                    params.put("sheetName", attendancesPreSynced.get(index).getType());
                    params.put("info", attendancesPreSynced.get(index).getInfo());
                    params.put("scannedDate", attendancesPreSynced.get(index).getScannedDate());
                    params.put("scannedBy", databaseHandler.getScannerName());

                    return params;
                }
            };
            int socketTimeout = 300000;

            RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeout, 0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            stringRequest.setRetryPolicy(retryPolicy);
            queue = Volley.newRequestQueue(CircularSyncingActivity.this);
            queue.add(stringRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(CircularSyncingActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void postSpecificRecordInList(List<Attendance> attendancesPreSynced, int indexArg, Context context, String scannerName){
        Attendance attendancePreSynced = attendancesPreSynced.get(indexArg);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    databaseHandler.updateAttendanceStatus(attendancePreSynced, 1);
                    int currentSyncedRecord = getSyncedRecords();
                    currentSyncedRecord += 1;
                    setSyncedRecords(currentSyncedRecord);
                    int percentage = currentSyncedRecord * 100 / attendancesPreSynced.size();
                    progressText.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(percentage);
                            progressText.setText("" + percentage + "%");
                            if (syncedRecords == attendancesPreSynced.size()) {
                                progressText.removeCallbacks(this);
                                Toast.makeText(CircularSyncingActivity.this, "Đồng bộ thành công",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    if(indexArg < attendancesPreSynced.size()-1){
                        int nextIndex = indexArg+1;
                        postSpecificRecordInList(attendancesPreSynced, nextIndex, context, scannerName);
                    }
                    else{
                        mp.start();
                    }
                }catch (Exception ex){
                    Toast.makeText(CircularSyncingActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "addItem");
                params.put("sheetName", attendancePreSynced.getType());
                params.put("info", attendancePreSynced.getInfo());
                params.put("scannedDate", attendancePreSynced.getScannedDate());
                params.put("scannedBy", scannerName);

                return params;
            }
        };
        int socketTimeout = 300000;

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeout, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);
        queue = Volley.newRequestQueue(context);
        queue.add(stringRequest);
    }
}