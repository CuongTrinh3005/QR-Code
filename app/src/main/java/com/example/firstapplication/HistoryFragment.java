package com.example.firstapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
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
import java.util.Objects;

public class HistoryFragment extends Fragment {
    private View view;
    private TextView tvHistory;
    private RecyclerView recyclerView;
    private Button btnSync;
    private DatabaseHandler databaseHandler = null;
    private AttendanceListAdapter attendanceListAdapter = null;
    GoogleSignInClient signInClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_history, container, false);
        databaseHandler = new DatabaseHandler(getActivity());
        initView(view);

        String scannerName = databaseHandler.getScannerName();
        String scannedBy = "Ng?????i qu??t: " + scannerName;
        if(!"".equals(scannerName))
            Toast.makeText(getContext(), scannedBy, Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(getContext(), "Vui l??ng ki???m tra k???t n???i m???ng...", Toast.LENGTH_LONG).show();
                    return;
                }

                String scannerName = databaseHandler.getScannerName();
                if("".equals(scannerName))
                    authenticate();

                scannerName = databaseHandler.getScannerName();
                if("".equals(scannerName)){
                    Toast.makeText(getContext(), "Vui l??ng th???c hi???n ????ng nh???p", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(getContext(), "Qu?? tr??nh ?????ng b??? b???t ?????u ...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getContext(), CircularSyncingActivity.class);
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

        tvHistory.setText("T???ng s??? l?????ng: " + attendances.size() + " - Ch??a ?????ng b???: " + attendanceNotSynced.size());
    }

    private void controlSyncButton() {
        Boolean notBeSyncedYet = databaseHandler.checkHaveNonSyncedAttendance();
        btnSync.setEnabled(notBeSyncedYet);
    }

    private void authenticate(){
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();
        signInClient = GoogleSignIn.getClient(Objects.requireNonNull(getContext()), signInOptions);
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
                getActivity().finish();
            }
        });
    }

    private Boolean saveScannerInfo(){
        try{
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
            if(account!=null){
                String googleId = account.getId();
                String displayName = account.getDisplayName();
                String signedInEmail = account.getEmail();
                String url = "https://script.google.com/macros/s/AKfycbyWOtmVYxqViQj5ouhKXomrHs-yPYDlnrifE2g0wKYXZdN4_m78ttzzrNt8M7jomE2q/exec?action=getItems&sheetName=EMAIL"; //just a string
                ApiUtils utils = new ApiUtils();
                utils.getAllowedEmails(url, (AppCompatActivity) getActivity(), new VolleyCallback() {
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

                                    String welcome = "Xin ch??o, " + displayName;
                                    Toast.makeText(getContext(), welcome, Toast.LENGTH_SHORT).show();
                                    btnSync.performClick();
                                    break;
                                }
                            }
                            if(!isMatching){
                                logOut();
                                Toast.makeText(getContext(), "T??i kho???n ch??a ????ng k?? v???i admin!", Toast.LENGTH_SHORT).show();
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
                    throw new RuntimeException("???? c?? l???i x???y ra!");
                }
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "????ng nh???p th???t b???i!", Toast.LENGTH_SHORT).show();
            }
        }
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