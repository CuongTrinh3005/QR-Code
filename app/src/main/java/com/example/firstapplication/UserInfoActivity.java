package com.example.firstapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.firstapplication.db.DatabaseHandler;
import com.example.firstapplication.entity.Scanner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.firstapplication.utils.Helper.setActionBarBackGroundColor;

public class UserInfoActivity extends AppCompatActivity {
    private DatabaseHandler databaseHandler = new DatabaseHandler(this);
    private TextView tvDisplayName, tvEmail;
    private ImageView avatar;

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

        Scanner user = databaseHandler.getScannerInfo();
        if(user!=null){
            tvDisplayName.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());
//            URL url = null;
//            try {
//                url = new URL(user.getImageUrl());
//            } catch (MalformedURLException e) {
//                throw new RuntimeException(e);
//            }
//            Bitmap mIcon_val = null;
//            try {
//                mIcon_val = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            avatar.setImageBitmap(mIcon_val);
        }
    }
}