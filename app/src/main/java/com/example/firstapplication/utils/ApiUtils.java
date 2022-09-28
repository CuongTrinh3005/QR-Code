package com.example.firstapplication.utils;

import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class ApiUtils {
    String info="";
    public static String getService(String resource){
        String info = "";
        try{
            URL url = new URL(resource);
            // make connection
            URLConnection urlc = url.openConnection();
            urlc.setRequestProperty("Content-Type", "application/json");
            BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String l = null;
            while ((l=br.readLine())!=null) {
                info += l;
            }
            br.close();
        }
        catch (IOException io) {
            Log.d("exception", "IO exception");
        }
        catch (Exception ex){
            Log.d("exception", ex.toString());
        }
        return info;
    }

    public void getAllowedEmails(String url, AppCompatActivity activity, final VolleyCallback callback){
        RequestQueue queue;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                info = response;
                callback.onSuccess(response);
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
                params.put("action", "getItems");
                params.put("sheetName", "EMAIL");

                return params;
            }
        };
        int socketTimeout = 50000;

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);
        queue = Volley.newRequestQueue(activity.getBaseContext());
        queue.add(stringRequest);
    }
}
