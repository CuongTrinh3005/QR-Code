package com.example.firstapplication;

import android.content.Context;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;

import android.util.SparseArray;
import android.view.SurfaceHolder;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.example.firstapplication.CustomSheetsReading.TOKENS_DIRECTORY_PATH;

public class MainActivity extends AppCompatActivity {
    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    String intentData = "";
    boolean isEmail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        surfaceView = findViewById(R.id.surfaceView);
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
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    }
                    else {
                        ActivityCompat.requestPermissions(MainActivity.this, new
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
                final String spreadsheetId = "1AXCuR1rsi77guOiqX_YyKV-wQv28o99tIrQkspNiU3I";
                final String range = "Custom!A3:C";

                if (barcodes.size() != 0) {
                    txtBarcodeValue.post(new Runnable() {
                        @Override
                        public void run() {
                            intentData = barcodes.valueAt(0).displayValue;
                            txtBarcodeValue.setText(intentData);

                            final NetHttpTransport HTTP_TRANSPORT;
                            try {
//                                HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//                                File dataDirectory = new File(MainActivity.this.getApplicationContext().getFilesDir(), TOKENS_DIRECTORY_PATH);
//                                if(!dataDirectory.exists()){
//                                    dataDirectory.mkdirs();
//                                }
                                HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
//                                File folder = getDir(TOKENS_DIRECTORY_PATH, Context.MODE_PRIVATE);
                                Sheets service = new Sheets.Builder(HTTP_TRANSPORT, CustomSheetsReading.JSON_FACTORY,
                                        CustomSheetsReading.getCredentials(HTTP_TRANSPORT))
                                        .setApplicationName(CustomSheetsReading.APPLICATION_NAME)
                                        .build();
                                // Append data to sheet
                                ValueRange appendBody = new ValueRange()
                                        .setValues(Arrays.asList(
                                                Arrays.asList("Nguyễn Thị", "Linh", "Frontend Engineer")));
                                AppendValuesResponse appendResult = null;

                                appendResult = service.spreadsheets().values()
                                        .append(spreadsheetId, "A5", appendBody)
                                        .setValueInputOption("USER_ENTERED")
                                        .setInsertDataOption("INSERT_ROWS")
                                        .setIncludeValuesInResponse(true)
                                        .execute();

                                if(appendResult != null)
                                    System.out.println("\nAppending data successfully!\n");

                                // Read sheet's data
                                ValueRange response = null;
                                response = service.spreadsheets().values()
                                        .get(spreadsheetId, range)
                                        .execute();

                                List<List<Object>> values = response.getValues();
                                if (values == null || values.isEmpty()) {
                                    System.out.println("No data found.");
                                } else {
                                    for (List row : values) {
                                        // Print columns A and E, which correspond to indices from 0 to 5.
                                        System.out.printf("%s - %s - %s\n",
                                                row.get(0), row.get(1), row.get(2));
                                    }
                                }
                                Toast.makeText(getApplicationContext(), "Send message successfully!",
                                                                                            Toast.LENGTH_SHORT);
                            }
                            catch (Exception exception){
                                exception.printStackTrace();
                            }
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
