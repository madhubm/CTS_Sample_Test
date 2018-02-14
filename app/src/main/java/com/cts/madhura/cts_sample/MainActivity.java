package com.cts.madhura.cts_sample;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Activity;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    private RecyclerView mRecyclerView;
    private TestAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        new HttpRequestTask().execute();
    }

    private class HttpRequestTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            //this method will be running on UI thread

            progressDialog.setMessage("\tLoading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                url = new URL("https://dl.dropboxusercontent.com/s/2iodh4vg0eortkl/facts.json");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("Madhura", "malformed exception");
                return e.toString();
            }

            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");

                // setDoOutput to true as we recieve data from json file
                conn.setDoOutput(true);

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return e1.toString();
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {
                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String res) {
            //this method will be running on UI thread

            progressDialog.dismiss();
            ArrayList<Test> data = new ArrayList<>();
            progressDialog.dismiss();

            try {

                /*JSONArray jArray = new JSONArray(res);

                // Extract data from json and store into ArrayList as class objects
                for(int i=0;i<jArray.length();i++){
                    JSONObject json_data = jArray.getJSONObject(i);
                    Test testData = new Test();
                    testData.imageHref= json_data.getString("imageHref");
                    testData.description= json_data.getString("description");
                    testData.title= json_data.getString("title");
                    data.add(testData);
                }
                */

                JSONObject results = new JSONObject(res);

                String title = results.getString("title");
                Log.d("Madhura", "title from JSON: " + title);
                getSupportActionBar().setTitle(title); // for set actionbar title
                JSONArray rows = results.getJSONArray("rows");

                Log.d("Madhura", "   rows length: " + rows.length());

                for (int i = 0; i < rows.length(); i++) {
                    JSONObject json_data = rows.getJSONObject(i);
                    Test testData = new Test();
                    testData.imageHref = json_data.getString("imageHref");
                    testData.description = json_data.getString("description");
                    testData.title = json_data.getString("title");
                    data.add(testData);
                }

                // Setup and Handover data to recyclerview
                mRecyclerView = (RecyclerView) findViewById(R.id.testListView);
                mAdapter = new TestAdapter(MainActivity.this, data);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
}