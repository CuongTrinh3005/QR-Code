package com.example.firstapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.*;
import android.util.SparseArray;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.budiyev.android.codescanner.*;
import com.example.firstapplication.db.DatabaseHandler;
import com.example.firstapplication.entity.Attendance;
import com.example.firstapplication.utils.Helper;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ScanningFragment extends Fragment {
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    TextView txtId, txtName, txtExtra;
    String intentData = "", type = "";
    DatabaseHandler databaseHandler = null;
    MediaPlayer mp = null;
    private CodeScanner mCodeScanner;
    private View view;
    private long startTime = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_scanning, container, false);
        type = Objects.requireNonNull(getActivity()).getIntent().getStringExtra("sheetName");
        databaseHandler = new DatabaseHandler(getActivity());
        mp = MediaPlayer.create(getContext(), R.raw.beep);
        setUpPermission();
        initViews();
        return view;
    }

    private void initViews(){
        final Activity activity = getActivity();
        CodeScannerView scannerView = view.findViewById(R.id.scanner_view);
        assert activity != null;

        txtId = view.findViewById(R.id.txtId);
        txtName = view.findViewById(R.id.txtName);
        txtExtra = view.findViewById(R.id.txtExtra);

        mCodeScanner = new CodeScanner(activity, scannerView);
        mCodeScanner.setFormats(CodeScanner.TWO_DIMENSIONAL_FORMATS);
        mCodeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        mCodeScanner.setScanMode(ScanMode.CONTINUOUS);
        mCodeScanner.setAutoFocusEnabled(true);
        mCodeScanner.setFlashEnabled(false);

        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            intentData = result.getText();
                            if(!Helper.validateQrCode(intentData))
                                return;

                            // Full Name
                            String fullName = intentData.substring(5)
                                    .replace("_", " ").trim();
                            String id = intentData.split("_")[0];

                            txtId.setText(id);
                            txtName.setText(fullName);

                            String date = Helper.getDateTime(new Date());
                            Boolean isExisted = databaseHandler.checkAttendanceExist(type,
                                    id, date);
                            if(!isExisted && intentData.length() > 0 && type.length() > 0){
                                txtExtra.setText("");
                                Attendance attendance = new Attendance(intentData, type);
                                databaseHandler.addAttendance(attendance);
                                Toast.makeText(getContext(), "Lưu thành công", Toast.LENGTH_SHORT).show();
                                mp.start();
                                startTime = System.currentTimeMillis();
                            }
                            else if (isExisted && intentData.length() > 0 && type.length() > 0) {
                                long endTime = System.currentTimeMillis();
                                if(endTime - startTime >= 3000){
                                    txtExtra.setText(
                                            Helper.getStringResources((AppCompatActivity)
                                                            Objects.requireNonNull(getActivity()),
                                                    R.string.exist_notification));
                                    mp.start();
                                    startTime=0;
                                }
                            }
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });

        mCodeScanner.setErrorCallback(new ErrorCallback() {
            @Override
            public void onError(@NonNull Throwable thrown) {
                thrown.printStackTrace();
                Toast.makeText(activity, thrown.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    private void setUpPermission(){
        int permission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        if(permission != PackageManager.PERMISSION_GRANTED){
            makeRequest();
        }
    }

    private void makeRequest(){
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CAMERA_PERMISSION:{
                Toast.makeText(getContext(), "Bạn phải cho phép camera để tiếp tục!", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    public void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}