package com.example.firstapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.firstapplication.db.DatabaseHandler;
import com.example.firstapplication.utils.Helper;

import static com.example.firstapplication.utils.Helper.setActionBarBackGroundColor;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout relativeLayout;
    private PopupWindow popupWindow;
    TextView txtCurrentDate;
    ImageView imageView;
    DatabaseHandler databaseHandler = new DatabaseHandler(this);
    private final String T3T5 = "T3T5";
    private final String giaoLy = "GIAOLY";
    private final String leSom = "LESOM";
    private final String khac = "KHAC";

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
                            Intent intent = new Intent(MainActivity.this, ScannedActivity.class);
                            intent.putExtra("action", "sync");
                            startActivity(intent);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
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
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        setActionBarBackGroundColor(actionBar, "#000000");

        imageView = findViewById(R.id.logo);
        imageView.setBackgroundResource(R.drawable.giuse_church);

        txtCurrentDate = findViewById(R.id.currentDate);
        txtCurrentDate.setText(Helper.getCurrentTimeDisplay());
    }

    @SuppressLint("ResourceType")
    private void setEvents() {
        String sheetName = "";
        Boolean tueThuAllowed = Helper.checkTueAndThuAllowed();
        if(tueThuAllowed){
            sheetName = T3T5;
        }

        Boolean sundayAllowed = Helper.checkSundayAllowed();
        if(sundayAllowed){
            sheetName = giaoLy;
        }

        Boolean sundayEarlyAllowed = Helper.checkSundayEarlyAllowed();
        if(sundayEarlyAllowed){
            sheetName = leSom;
        }

        Boolean otherAllowed = com.example.firstapplication.utils.Helper.checkOtherDaysAllowed();
        if(otherAllowed){
            sheetName = khac;
        }

        if(!"".equals(sheetName)){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String type = "";
            switch (sheetName){
                case T3T5:{
                    type = "Điểm danh thứ 3, thứ 5";
                    break;
                }
                case giaoLy:{
                    type = "Điểm danh học giáo lý";
                    break;
                }
                case leSom:{
                    type = "Điểm danh đi lễ sớm";
                    break;
                }
                case khac:{
                    type = "Điểm danh các trường hợp khác";
                    break;
                }
                default:
                    break;
            }
            Toast.makeText(this, type, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, ScannedActivity.class);
            intent.putExtra("sheetName", sheetName);
            startActivity(intent);
        }
        else{
            showPopUpNotification();
        }
    }

    public void showPopUpNotification() {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);
        // find the view first, then show the popup window
        findViewById(R.id.relativeLayout).post(new Runnable() {
            public void run() {
                popupWindow.showAtLocation(findViewById(R.id.relativeLayout), Gravity.BOTTOM, 0, 300);
            }
        });

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViews();
        processSyncing();
    }
}
