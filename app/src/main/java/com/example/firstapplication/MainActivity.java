package com.example.firstapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.firstapplication.db.DatabaseHandler;
import com.example.firstapplication.utils.Helper;
import com.google.android.material.navigation.NavigationView;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import static com.example.firstapplication.utils.Helper.setActionBarBackGroundColor;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener{
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    private PopupWindow popupWindow;
    ImageView imageView;
    DatabaseHandler databaseHandler = new DatabaseHandler(this);
    private final String T3T5 = "T3T5";
    private final String giaoLy = "GIAOLY";
    private final String leSom = "LESOM";
    private final String khac = "KHAC";
    private final String ZONE_ID = "Asia/Ho_Chi_Minh";

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
                            Intent intent = new Intent(MainActivity.this, SeparateHistoryActivity.class);
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
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initViews() {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        setActionBarBackGroundColor(actionBar, "#000000");

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.navigationView);
        TextView syncRecordNumber = (TextView) MenuItemCompat.getActionView(mNavigationView.getMenu()
                .findItem(R.id.nav_sync));

        initializeCountDrawer(syncRecordNumber);

        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(this);
        }

        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.logo);
        imageView.setBackgroundResource(R.drawable.giuse_church);
    }

    private void initializeCountDrawer(TextView syncRecordNumber) {
        syncRecordNumber.setGravity(Gravity.CENTER_VERTICAL);
        syncRecordNumber.setTypeface(null, Typeface.BOLD);
        syncRecordNumber.setTextColor(getResources().getColor(R.color.red));

        Integer noNonSyncedRecords = databaseHandler.getAttendancesHaveNotSyncedYet().size();
        if(noNonSyncedRecords > 0)
            syncRecordNumber.setText(String.valueOf(noNonSyncedRecords));
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_account) {
            Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_scan) {
            checkToScan();
        }
        else if (id == R.id.nav_sync) {
            Intent intent = new Intent(MainActivity.this, SeparateHistoryActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_exit) {
            this.finish();
        }

        return true;
    }

    private void checkToScan(){
        Date date = new Date();
        LocalTime localTime = LocalTime.now(ZoneId.of(ZONE_ID));
        String sheetName = "";
        Boolean tueThuAllowed = Helper.checkTueAndThuAllowed(date, localTime);
        if(tueThuAllowed){
            sheetName = T3T5;
        }

        Boolean sundayAllowed = Helper.checkSundayAllowed(date, localTime);
        if(sundayAllowed){
            sheetName = giaoLy;
        }

        Boolean sundayEarlyAllowed = Helper.checkSundayEarlyAllowed(date, localTime);
        if(sundayEarlyAllowed){
            sheetName = leSom;
        }

        Boolean otherAllowed = Helper.checkOtherDaysAllowed(date, localTime);
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

    private void setEvents() {
        checkToScan();
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
