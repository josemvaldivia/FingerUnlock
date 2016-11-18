package com.example.jose.fingerunlock;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class ApiRequest extends AsyncTask<String, Void, String> {

    private String process(String requestUrl) {
        String response = "";
        URL url = null;
        try {
            url = new URL(requestUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            InputStream in = null;
            try {
                in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader r = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = "";
            try {
                line = r.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            response += line;
        } finally {
            urlConnection.disconnect();
        }
        return response;
    }
    @Override
    protected String doInBackground(String... urls) {
        String response = "";
        for (String url : urls) {
            response += process(url);
        }
        return response;
    }

}
