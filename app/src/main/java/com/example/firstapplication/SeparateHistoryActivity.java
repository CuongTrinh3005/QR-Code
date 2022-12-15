package com.example.firstapplication;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

import java.util.List;

import static com.example.firstapplication.utils.Helper.setActionBarBackGroundColor;

public class SeparateHistoryActivity extends AppCompatActivity {
    private TextView tvHistory;
    private RecyclerView recyclerView;
    private Button btnSync;
    private ImageView ivDelete;
    private SearchView searchView = null;
    private DatabaseHandler databaseHandler = new DatabaseHandler(this);
    private AttendanceListAdapter attendanceListAdapter = null;
    GoogleSignInClient signInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_separate_history);
        initViews();
        String scannerName = databaseHandler.getScannerName();
        String scannedBy = "Người quét: " + scannerName;
        if(!"".equals(scannerName))
            Toast.makeText(this, scannedBy, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) SeparateHistoryActivity.this.getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(SeparateHistoryActivity.this.getComponentName()));
            searchView.setQueryHint(Helper.getStringResources(this, R.string.search_hint));
        }

        ImageView clearButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        clearButton.setOnClickListener(v -> {
            if(searchView.getQuery().length() == 0) {
                searchView.setIconified(true);
            } else {
                searchView.setQuery("", false);
                renderRecycleView(1);
            }
        });

        final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty())
                    renderRecycleView(1);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                renderRecycleViewInSearching(query);
                return true;
            }
        };

        assert searchView != null;
        searchView.setOnQueryTextListener(queryTextListener);

        return super.onCreateOptionsMenu(menu);
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
        ivDelete = findViewById(R.id.ivDelete);
        recyclerView = findViewById(R.id.recycleViewSeparate);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        renderRecycleView(0);
        controlDeleteAction();
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                databaseHandler.deleteSyncedAttendance();
                                renderRecycleView(1);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(SeparateHistoryActivity.this);
                builder.setMessage("Dữ liệu sẽ xoá vĩnh viễn, không thể khôi phục, vui lòng cân nhắc")
                        .setPositiveButton("Có, xoá", dialogClickListener)
                        .setNegativeButton("Huỷ", dialogClickListener).show();
            }
        });

        btnSync = findViewById(R.id.btnSyncSeparate);
        controlSyncButton();

        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Helper.isNetworkAvailable(SeparateHistoryActivity.this)){
                    Toast.makeText(SeparateHistoryActivity.this, "Vui lòng kiểm tra kết nối mạng...", Toast.LENGTH_LONG).show();
                    return;
                }

                String scannerName = databaseHandler.getScannerName();
                if("".equals(scannerName))
                    authenticate();

                scannerName = databaseHandler.getScannerName();
                if("".equals(scannerName)){
                    Toast.makeText(SeparateHistoryActivity.this, "Vui lòng thực hiện đăng nhập", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(SeparateHistoryActivity.this, "Quá trình đồng bộ bắt đầu ...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SeparateHistoryActivity.this, CircularSyncingActivity.class);
                startActivity(intent);
            }
        });
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

    private void renderRecycleViewInSearching(String queryStr){
        List<Attendance> attendances = databaseHandler.query(queryStr);
        attendanceListAdapter.updateDataset(attendances);
        tvHistory.setText("Tìm được: " + attendances.size() + " kết quả");
    }

    private void controlSyncButton() {
        Boolean notBeSyncedYet = databaseHandler.checkHaveNonSyncedAttendance();
        btnSync.setEnabled(notBeSyncedYet);
    }

    private void controlDeleteAction(){
        ivDelete.setEnabled(databaseHandler.checkHaveSyncedAttendance());
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
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(SeparateHistoryActivity.this);
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
                                    Toast.makeText(SeparateHistoryActivity.this, welcome, Toast.LENGTH_SHORT).show();
                                    btnSync.performClick();
                                    break;
                                }
                            }
                            if(!isMatching){
                                logOut();
                                Toast.makeText(SeparateHistoryActivity.this, "Tài khoản chưa đăng ký với admin!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SeparateHistoryActivity.this, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        renderRecycleView(0);
        controlSyncButton();
        controlDeleteAction();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}