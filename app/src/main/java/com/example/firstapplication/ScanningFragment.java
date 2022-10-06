package com.example.firstapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.firstapplication.db.DatabaseHandler;
import com.example.firstapplication.entity.Attendance;
import com.example.firstapplication.utils.Helper;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public class ScanningFragment extends Fragment {
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    String intentData = "", type = "";
    private View view;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private Boolean cameraSourceStarted=false;
    DatabaseHandler databaseHandler = null;
    MediaPlayer mp = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_scanning, container, false);
        initViews(view);
        initialiseDetectorsAndSources();
        type = Objects.requireNonNull(getActivity()).getIntent().getStringExtra("sheetName");
        databaseHandler = new DatabaseHandler(getActivity());
        mp = MediaPlayer.create(getContext(), R.raw.beep);
        return view;
    }

    public void initViews(View view) {
        txtBarcodeValue = view.findViewById(R.id.txtBarcodeValue);
        surfaceView = view.findViewById(R.id.surfaceView);
    }

    private void initialiseDetectorsAndSources() {
        barcodeDetector = new BarcodeDetector.Builder(getContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(getContext(), barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                    cameraSourceStarted = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    txtBarcodeValue.post(new Runnable() {
                        @Override
                        public void run() {
                            intentData = barcodes.valueAt(0).displayValue;
                            if(!Helper.validateQrCode(intentData))
                                return;

                            // Get 2 last names
                            String[] names = intentData.split("_");
                            String displayName = names[names.length-2] + " " + names[names.length-1];

                            txtBarcodeValue.setText(displayName);
                            String id = intentData.split("_")[0];
                            String date = Helper.getDateTime(new Date());
                            Boolean isExisted = databaseHandler.checkAttendanceExist(type,
                                    id, date);
                            if(!isExisted && intentData.length() > 0 && type.length() > 0){
                                Attendance attendance = new Attendance(intentData, type);
                                databaseHandler.addAttendance(attendance);
                                Toast.makeText(getContext(), "Lưu mới thành công", Toast.LENGTH_SHORT).show();
                                mp.start();
                            }
                            else if(isExisted && intentData.length() > 0 && type.length() > 0){
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                mp.start();
                            }
                        }
                    });
                }
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!cameraSourceStarted) {
                    try {
                        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            cameraSource.start(surfaceView.getHolder());
                        } else {
                            ActivityCompat.requestPermissions(getActivity(), new
                                    String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 500);
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraSource.release();
        cameraSourceStarted = false;
    }
}