package com.example.firstapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.firstapplication.Helper.setActionBarBackGroundColor;

public class MainActivity extends AppCompatActivity {
    TextView txtTitle;
    Button btnTueThu;
    Button btnSunday;
    Button btnEarly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setEvents();
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
                intent.putExtra("sheetName", "CN");
                startActivity(intent);
            }
        });
        btnEarly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScannedActivity.class);
                intent.putExtra("sheetName", "DILESOM");
                startActivity(intent);
            }
        });
    }
}
