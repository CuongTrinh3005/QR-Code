package com.example.firstapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.firstapplication.adapters.AttendanceListAdapter;
import com.example.firstapplication.db.DatabaseHandler;
import com.example.firstapplication.entity.Attendance;
import com.example.firstapplication.entity.Scanner;
import com.example.firstapplication.utils.ApiUtils;
import com.example.firstapplication.utils.Helper;
import com.example.firstapplication.utils.VolleyCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.firstapplication.utils.Helper.setActionBarBackGroundColor;

public class SeparateSyncActivity extends AppCompatActivity {
    String URL = "https://script.google.com/macros/s/AKfycbyWOtmVYxqViQj5ouhKXomrHs-yPYDlnrifE2g0wKYXZdN4_m78ttzzrNt8M7jomE2q/exec";
    private TextView tvHistory;
    private RecyclerView recyclerView;
    private Button btnSync;
    private DatabaseHandler databaseHandler = new DatabaseHandler(this);;
    private AttendanceListAdapter attendanceListAdapter = null;
    private RequestQueue queue;
    MediaPlayer mp = null;
    private Integer startIndex = 0;
    ProgressDialog progressBar;
    GoogleSignInClient signInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_separate_sync);
        initViews();
        databaseHandler.addAttendance(new Attendance("email_testing", "KHAC"));
        mp = MediaPlayer.create(this, R.raw.success);
        String scannerName = databaseHandler.getScannerName();
        String scannedBy = "Người quét: " + scannerName;
        if(!"".equals(scannerName))
            Toast.makeText(this, scannedBy, Toast.LENGTH_SHORT).show();
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

        tvHistory = findViewById(R.id.tvHistorySeparate);
        recyclerView = findViewById(R.id.recycleViewSeparate);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        renderRecycleView(0);

        btnSync = findViewById(R.id.btnSyncSeparate);
        controlSyncButton();

        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Helper.isNetworkAvailable(SeparateSyncActivity.this)){
                    Toast.makeText(SeparateSyncActivity.this, "Vui lòng kiểm tra kết nối mạng...", Toast.LENGTH_LONG).show();
                    return;
                }

                String scannerName = databaseHandler.getScannerName();
                if("".equals(scannerName))
                    authenticate();

                scannerName = databaseHandler.getScannerName();
                if("".equals(scannerName)){
                    Toast.makeText(SeparateSyncActivity.this, "Vui lòng thực hiện đăng nhập", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar = new ProgressDialog(SeparateSyncActivity.this);    //ProgressDialog
                progressBar.setTitle("Đồng bộ dữ liệu");
                progressBar.setMessage("Vui lòng chờ trong ít phút ... ");
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.setCancelable(false);
                progressBar.show();
                btnSync.setEnabled(false);
                Toast.makeText(SeparateSyncActivity.this, "Quá trình đồng bộ bắt đầu ...", Toast.LENGTH_LONG).show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<Attendance> attendancesPreSynced = databaseHandler.getAttendancesHaveNotSyncedYet();
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    databaseHandler.updateAttendanceStatus(attendancesPreSynced.get(startIndex), 1);
                                    if(attendancesPreSynced.size() > 1){
                                        int nextIndex = startIndex+1;
                                        postSpecificRecordInList(attendancesPreSynced, nextIndex, SeparateSyncActivity.this, databaseHandler.getScannerName());
                                    }
                                    else{
                                        renderRecycleView(1);
                                        Toast.makeText(btnSync.getContext(), "Đồng bộ thành công", Toast.LENGTH_SHORT).show();
                                        mp.start();
                                        progressBar.dismiss();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("error", error.toString());
                                    Toast.makeText(SeparateSyncActivity.this,
                                            "Đã có lỗi, vui lòng thử lại: " + error.toString(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }) {
                                @Override
                                protected Map<String, String> getParams() {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("action", "addItem");
                                    params.put("sheetName", attendancesPreSynced.get(startIndex).getType());
                                    params.put("info", attendancesPreSynced.get(startIndex).getInfo());
                                    params.put("scannedDate", attendancesPreSynced.get(startIndex).getScannedDate());
                                    params.put("scannedBy", databaseHandler.getScannerName());

                                    return params;
                                }
                            };
                            int socketTimeout = 300000;

                            RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeout, 0,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                            stringRequest.setRetryPolicy(retryPolicy);
                            queue = Volley.newRequestQueue(SeparateSyncActivity.this);
                            queue.add(stringRequest);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Toast.makeText(btnSync.getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).start();
            }
        });
    }

    private void postSpecificRecordInList(List<Attendance> attendancesPreSynced, int index, Context context, String scannerName){
        Attendance attendancePreSynced = attendancesPreSynced.get(index);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                databaseHandler.updateAttendanceStatus(attendancePreSynced, 1);
                if(index<attendancesPreSynced.size()-1){
                    int nextIndex = index+1;
                    postSpecificRecordInList(attendancesPreSynced, nextIndex, context, scannerName);
                }
                else{
                    renderRecycleView(1);
                    Toast.makeText(btnSync.getContext(), "Đồng bộ thành công", Toast.LENGTH_SHORT).show();
                    mp.start();
                    progressBar.dismiss();
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

    private void renderRecycleView(int option){
        List<Attendance> attendances = databaseHandler.getAllAttendances();
        if(option==0){ // create new adapter
            attendanceListAdapter = new AttendanceListAdapter(attendances);
        } else if (option==1) {
            attendanceListAdapter.updateDataset(attendances);
        }
        recyclerView.setAdapter(attendanceListAdapter);
        List<Attendance> attendanceNotSynced = databaseHandler.getAttendancesHaveNotSyncedYet();

        tvHistory.setText("Tổng số lượng: " + attendances.size() + " - Chưa đồng bộ: " + attendanceNotSynced.size());
    }

    private void controlSyncButton() {
        Boolean notBeSyncedYet = databaseHandler.checkHaveNonSyncedAttendance();
        btnSync.setEnabled(notBeSyncedYet);
    }

    private void authenticate(){
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();
        signInClient = GoogleSignIn.getClient(this, signInOptions);
        signIn(signInClient);
        btnSync.setEnabled(false);
    }

    private void signIn(GoogleSignInClient signInClient){
        Intent signinIntent = signInClient.getSignInIntent();
        startActivityForResult(signinIntent, 1000);
    }

    private void logOut(){
        signInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                finish();
            }
        });
    }

    private Boolean saveScannerInfo(){
        try{
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(SeparateSyncActivity.this);
            if(account!=null){
                String googleId = account.getId();
                String displayName = account.getDisplayName();
                String signedInEmail = account.getEmail();
                String url = "https://script.google.com/macros/s/AKfycbyWOtmVYxqViQj5ouhKXomrHs-yPYDlnrifE2g0wKYXZdN4_m78ttzzrNt8M7jomE2q/exec?action=getItems&sheetName=EMAIL"; //just a string
                ApiUtils utils = new ApiUtils();
                utils.getAllowedEmails(url, this, new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Boolean isMatching = false;
                        try {
                            JSONObject items = new JSONObject(result);
                            JSONArray permittedEmailListJson = items.getJSONArray("items");
                            for(int index=0; index<permittedEmailListJson.length(); index++){
                                JSONObject allowedEntity = permittedEmailListJson.getJSONObject(index);
                                String email = allowedEntity.getString("email").trim();
                                if(email.equalsIgnoreCase(signedInEmail)) {
                                    btnSync.setEnabled(false);
                                    isMatching = true;
                                    Scanner scanner = new Scanner(googleId, displayName, signedInEmail);
                                    databaseHandler.addScanner(scanner);

                                    String welcome = "Xin chào, " + displayName;
                                    Toast.makeText(SeparateSyncActivity.this, welcome, Toast.LENGTH_SHORT).show();
                                    btnSync.performClick();
                                    break;
                                }
                            }
                            if(!isMatching){
                                logOut();
                                Toast.makeText(SeparateSyncActivity.this, "Tài khoản chưa đăng ký với admin!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                return true;
            }
            return false;
        }
        catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1000){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);
                if(!saveScannerInfo()){
                    throw new RuntimeException("Đã có lỗi xảy ra!");
                }
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(SeparateSyncActivity.this, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        renderRecycleView(0);
        controlSyncButton();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}