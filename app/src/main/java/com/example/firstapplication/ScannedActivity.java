package com.example.firstapplication;

import android.content.Context;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;

import android.util.SparseArray;
import android.view.SurfaceHolder;
import androidx.core.app.ActivityCompat;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.firstapplication.db.DatabaseHandler;
import com.example.firstapplication.entity.Attendace;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.*;

import static com.example.firstapplication.Helper.setActionBarBackGroundColor;

public class ScannedActivity extends AppCompatActivity {
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    Button btnAction;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static Context mContext;
    private RequestQueue queue;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    String intentData = "", sheetName = "";
    String URL = "https://script.google.com/macros/s/AKfycbyWOtmVYxqViQj5ouhKXomrHs-yPYDlnrifE2g0wKYXZdN4_m78ttzzrNt8M7jomE2q/exec";

    public static Context getContext() {
        return mContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_scanned_tue_thu);
        initViews();
        sheetName = getIntent().getStringExtra("sheetName");
        DatabaseHandler db = new DatabaseHandler(this);

//        Log.d("Insert: ", "Inserting ..");
//        db.addAttendance(new Attendace("Test1",
//                com.example.firstapplication.utils.Helper.convertDateToString(new Date())));
//        db.addAttendance(new Attendace("Test2",
//                com.example.firstapplication.utils.Helper.convertDateToString(new Date())));
//        db.addAttendance(new Attendace("Test3",
//                com.example.firstapplication.utils.Helper.convertDateToString(new Date())));
//
//        // Reading all contacts
//        Log.d("Reading: ", "Reading all contacts..");
//        List<Attendace> attendances = db.getAllAttendances();
//
//        for (Attendace attendace : attendances) {
//            String log = "Id: " + attendace.getId() + " ,Info: " + attendace.getInfo() + " ,Scanned Date: " +
//                    attendace.getScannedDate();
//            // Writing Contacts to log
//            Log.d("Record: ", log);
//        }
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
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        surfaceView = findViewById(R.id.surfaceView);
        btnAction = findViewById(R.id.btnAction);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intentData.length() > 0 & sheetName.length() > 0) {
                    btnAction.setEnabled(false);
//                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
//                        @Override
//                        public void onResponse(String response) {
//                            txtBarcodeValue.setText(response.toString());
//                            Toast.makeText(ScannedActivity.this, response.toString(), Toast.LENGTH_LONG).show();
//                            intentData = "";
//                            btnAction.setEnabled(true);
//                        }
//                    }, new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            Log.d("error", error.toString());
//                        }
//                    }) {
//                        @Override
//                        protected Map<String, String> getParams() {
//                            Map<String, String> params = new HashMap<>();
//                            params.put("action", "addItem");
//                            params.put("sheetName", sheetName);
//                            params.put("info", intentData);
//
//                            return params;
//                        }
//                    };
//                    int socketTimeout = 50000;
//
//                    RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//                    stringRequest.setRetryPolicy(retryPolicy);
//                    queue = Volley.newRequestQueue(ScannedActivity.this);
//                    queue.add(stringRequest);


                }
            }
        });
    }

    private void initialiseDetectorsAndSources() {

        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScannedActivity.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(ScannedActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
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
                Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    txtBarcodeValue.post(new Runnable() {
                        @Override
                        public void run() {
                            intentData = barcodes.valueAt(0).displayValue;
                            txtBarcodeValue.setText(intentData);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }
}
