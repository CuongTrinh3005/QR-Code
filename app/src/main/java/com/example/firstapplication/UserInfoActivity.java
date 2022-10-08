package com.example.firstapplication;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.firstapplication.db.DatabaseHandler;
import com.example.firstapplication.entity.Scanner;
import com.example.firstapplication.utils.ApiUtils;
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

import static com.example.firstapplication.utils.Helper.setActionBarBackGroundColor;

public class UserInfoActivity extends AppCompatActivity {
    private DatabaseHandler databaseHandler = new DatabaseHandler(this);
    private TextView tvDisplayName, tvEmail;
    private ImageView avatar;
    private ImageButton btnSignIn;
    GoogleSignInClient signInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        initViews();
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

    public void initViews(){
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        setActionBarBackGroundColor(actionBar, "#000000");
        tvDisplayName = findViewById(R.id.displayName);
        tvEmail = findViewById(R.id.email);
        avatar = findViewById(R.id.avatar);
        avatar.setBackgroundResource(R.drawable.user_avatar);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate();
            }
        });

        Scanner user = databaseHandler.getScannerInfo();
        if(user!=null){
            tvDisplayName.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());
            btnSignIn.setVisibility(View.GONE);
        }
    }

    private void authenticate(){
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();
        signInClient = GoogleSignIn.getClient(this, signInOptions);
        signIn(signInClient);
        btnSignIn.setEnabled(false);
    }

    private void signIn(GoogleSignInClient signInClient){
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1000);
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
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
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
                                    btnSignIn.setEnabled(false);
                                    isMatching = true;
                                    Scanner scanner = new Scanner(googleId, displayName, signedInEmail);
                                    databaseHandler.addScanner(scanner);

                                    String welcome = "Xin chào, " + displayName;
                                    Toast.makeText(UserInfoActivity.this, welcome, Toast.LENGTH_SHORT).show();
                                    tvDisplayName.setText(displayName);
                                    tvEmail.setText(email);
                                    btnSignIn.setVisibility(View.GONE);
                                    break;
                                }
                            }
                            if(!isMatching){
                                logOut();
                                Toast.makeText(UserInfoActivity.this, "Tài khoản chưa đăng ký với admin!"
                                        , Toast.LENGTH_SHORT).show();
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
                Toast.makeText(UserInfoActivity.this, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}