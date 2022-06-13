package ru.obvilion.launcher.api;

import ru.obvilion.json.JSONException;
import ru.obvilion.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Request {
    public RequestType requestType;
    public String link;

    public ArrayList<String> headerNames = new ArrayList<>();
    public ArrayList<String> headerValues = new ArrayList<>();

    public JSONObject body;

    public Request(String link) {
        this(RequestType.GET, link);
    }

    public Request(RequestType type, String link) {
        this.requestType = type;
        this.link = link;
    }

    public void addHeader(String type, String value) {
        headerNames.add(type);
        headerValues.add(value);
    }

    public void setBody(JSONObject json) {
        this.body = json;
    }

    public JSONObject connectAndGetJSON() {
        String json = requestType == RequestType.GET
                ? this.createGetRequest()
                : this.createPostRequest();

        if (json == null) {
            return null;
        }

        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String connect() {
        try {
            URL obj = new URL(this.link);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod(this.requestType.toString());

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String createGetRequest() {
        return connect();
    }

    public String createPostRequest() {
        try {
            URL url = new URL(link);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);

            byte[] out = body.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setRequestProperty("Accept-Charset", "UTF-8");
            http.connect();
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            }

            InputStream is = http.getResponseCode() >= 400 ? http.getErrorStream() : http.getInputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}

