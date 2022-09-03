package com.example.firstapplication;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.firstapplication.adapters.AttendanceListAdapter;
import com.example.firstapplication.db.DatabaseHandler;
import com.example.firstapplication.entity.Attendance;
import com.example.firstapplication.utils.Helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryFragment extends Fragment {
    String URL = "https://script.google.com/macros/s/AKfycbyWOtmVYxqViQj5ouhKXomrHs-yPYDlnrifE2g0wKYXZdN4_m78ttzzrNt8M7jomE2q/exec";
    private View view;
    private TextView tvHistory;
    private RecyclerView recyclerView;
    private Button btnSync;
    private DatabaseHandler databaseHandler = null;
    private AttendanceListAdapter attendanceListAdapter = null;
    private RequestQueue queue;
    MediaPlayer mp = null;
    private Integer noOfRecord = 0;
    ProgressDialog progressBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_history, container, false);
        databaseHandler = new DatabaseHandler(getActivity());
        initView(view);
        mp = MediaPlayer.create(getContext(), R.raw.success);

        return view;
    }

    private void initView(View view) {
        tvHistory = view.findViewById(R.id.tvHistory);
        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        renderRecycleView(0);

        btnSync = view.findViewById(R.id.btnSync);
        controlSyncButton();

        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Helper.isNetworkAvailable(getContext())){
                    Toast.makeText(getContext(), "Vui lòng kiểm tra kết nối mạng...", Toast.LENGTH_LONG).show();
                    return;
                }
                progressBar = new ProgressDialog(getContext());    //ProgressDialog
                progressBar.setTitle("Đồng bộ dữ liệu");
                progressBar.setMessage("Vui lòng chờ trong ít phút ... ");
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.setCancelable(false);
                progressBar.show();
                btnSync.setEnabled(false);
                Toast.makeText(getContext(), "Quá trình đồng bộ bắt đầu ...", Toast.LENGTH_LONG).show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<Attendance> attendancesPreSynced = databaseHandler.getAttendancesHaveNotSyncedYet();
                            for (Attendance attendancePreSynced : attendancesPreSynced) {
                                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        databaseHandler.updateAttendanceStatus(attendancePreSynced, 1);
                                        noOfRecord++;
                                        if (noOfRecord == attendancesPreSynced.size()) {
                                            renderRecycleView(1);
                                            Toast.makeText(btnSync.getContext(), "Đồng bộ thành công", Toast.LENGTH_SHORT).show();
                                            btnSync.setEnabled(true);
                                            noOfRecord = 0; // reset counter
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

                                        return params;
                                    }
                                };
                                int socketTimeout = 50000;

                                RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                                stringRequest.setRetryPolicy(retryPolicy);
                                queue = Volley.newRequestQueue(getContext());
                                queue.add(stringRequest);
                                Thread.sleep(2000);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Toast.makeText(btnSync.getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).start();
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

    private void controlSyncButton() {
        Boolean notBeSyncedYet = databaseHandler.checkHaveNonSyncedAttendance();
        btnSync.setEnabled(notBeSyncedYet);
    }

    @Override
    public void onResume() {
        super.onResume();
        renderRecycleView(1);
        controlSyncButton();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}