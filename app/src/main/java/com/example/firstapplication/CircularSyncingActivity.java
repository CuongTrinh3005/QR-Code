package com.example.firstapplication;

import android.media.MediaPlayer;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import java.util.List;

import com.example.firstapplication.db.DatabaseHandler;
import com.example.firstapplication.entity.Attendance;

import static com.example.firstapplication.utils.Helper.setActionBarBackGroundColor;

public class CircularSyncingActivity extends AppCompatActivity {
    public ProgressBar progressBar;
    private TextView progressText;
    MediaPlayer mp = null;
    private DatabaseHandler databaseHandler = new DatabaseHandler(this);
    int percentage =0;
    String URL = "https://script.google.com/macros/s/AKfycbyWOtmVYxqViQj5ouhKXomrHs-yPYDlnrifE2g0wKYXZdN4_m78ttzzrNt8M7jomE2q/exec";

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

        List<Attendance> attendancesPreSynced = databaseHandler.getAttendancesHaveNotSyncedYet();
        int noNeedToSyncedRecords = attendancesPreSynced.size();
        if(noNeedToSyncedRecords <= 0){
            Toast.makeText(CircularSyncingActivity.this, "Đã đồng bộ đầy đủ!",
                    Toast.LENGTH_SHORT).show();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.finish();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                int syncedRecords = 0, responseCode;
                try{
                    do{
                        Attendance attendanceNeedToBeSynced = attendancesPreSynced.get(syncedRecords);
                        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                                .addFormDataPart("action", "addItem")
                                .addFormDataPart("sheetName", attendanceNeedToBeSynced.getType())
                                .addFormDataPart("info", attendanceNeedToBeSynced.getInfo())
                                .addFormDataPart("scannedDate", attendanceNeedToBeSynced.getScannedDate())
                                .addFormDataPart("scannedBy", databaseHandler.getScannerName())
                                .build();
                        okhttp3.Request request = new okhttp3.Request.Builder()
                                .url(URL)
                                .method("POST", body)
                                .build();

                        okhttp3.Response response = client.newCall(request).execute();
                        responseCode = response.code();
                        if(responseCode == 200){
                            databaseHandler.updateAttendanceStatus(attendancesPreSynced.get(syncedRecords), 1);
                            syncedRecords++;
                            percentage = syncedRecords * 100 / noNeedToSyncedRecords;
                            progressText.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(percentage);
                                    progressText.setText("" + percentage + "%");
                                }
                            });
                        }
                    }while ((syncedRecords < noNeedToSyncedRecords) && responseCode == 200);
                    if (syncedRecords == noNeedToSyncedRecords) {
                        progressText.removeCallbacks(this);
                        mp.start();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = Toast.makeText(CircularSyncingActivity.this,
                                        "Đồng bộ thành công", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                    Toast.makeText(CircularSyncingActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }
}