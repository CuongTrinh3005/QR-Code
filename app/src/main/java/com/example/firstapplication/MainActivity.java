package com.example.firstapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.firstapplication.db.DatabaseHandler;

import static com.example.firstapplication.utils.Helper.setActionBarBackGroundColor;

public class MainActivity extends AppCompatActivity {
    TextView txtTitle;
    Button btnTueThu;
    Button btnSunday;
    Button btnEarly;
    Button btnOther;
    DatabaseHandler databaseHandler = new DatabaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setEvents();
        processSyncing();
    }

    private void processSyncing() {
        Boolean haveNonSyncedAttendance = databaseHandler.checkHaveNonSyncedAttendance();
        if(haveNonSyncedAttendance){
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            Toast.makeText(MainActivity.this, "Going to sync data", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, ScannedActivity.class);
                            intent.putExtra("action", "sync");
                            startActivity(intent);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            Toast.makeText(MainActivity.this, "Syncing data later", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Bạn có muốn đồng bộ dữ liệu?").setPositiveButton("Có, đồng bộ", dialogClickListener)
                    .setNegativeButton("Để sau", dialogClickListener).show();
        }
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

    private void initViews() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setActionBarBackGroundColor(actionBar, "#000000");
        txtTitle = findViewById(R.id.textTitle);
        btnTueThu = findViewById(R.id.btnTueThu);
        btnSunday = findViewById(R.id.btnSunday);
        btnEarly = findViewById(R.id.btnEarly);
        btnOther = findViewById(R.id.btnOther);

//        Boolean tueThuAllowed = com.example.firstapplication.utils.Helper.checkTueAndThuAllowed();
//        btnTueThu.setEnabled(tueThuAllowed);
//
//        Boolean sundayAllowed = com.example.firstapplication.utils.Helper.checkSundayAllowed();
//        btnSunday.setEnabled(sundayAllowed);
//
//        Boolean sundayEarlyAllowed = com.example.firstapplication.utils.Helper.checkSundayEarlyAllowed();
//        btnEarly.setEnabled(sundayEarlyAllowed);
    }

    private void setEvents() {
        btnTueThu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScannedActivity.class);
                intent.putExtra("sheetName", "T3T5");
                startActivity(intent);
            }
        });

        btnSunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScannedActivity.class);
                intent.putExtra("sheetName", "GIAOLY");
                startActivity(intent);
            }
        });
        btnEarly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScannedActivity.class);
                intent.putExtra("sheetName", "LESOM");
                startActivity(intent);
            }
        });

        btnOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScannedActivity.class);
                intent.putExtra("sheetName", "KHAC");
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        processSyncing();
    }
}
