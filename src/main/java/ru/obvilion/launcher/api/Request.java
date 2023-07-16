package ru.obvilion.launcher.api;

import ru.obvilion.json.JSONException;
import ru.obvilion.json.JSONObject;
import ru.obvilion.launcher.config.Global;
import ru.obvilion.launcher.utils.Log;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;

public class Request {
    static {
        loadCerts();
    }

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

    public JSONObject connectAndGetJSON_THROWS() throws IOException {
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


    public JSONObject connectAndGetJSON()  {
        try {
            String json = requestType == RequestType.GET
                    ? this.createGetRequest()
                    : this.createPostRequest();

            if (json == null) {
                return null;
            }

            return new JSONObject(json);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String connect() throws IOException {
        URL obj = new URL(this.link);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        connection.setRequestMethod(this.requestType.toString());

        InputStream is = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //Log.custom("N", "GET > {2} > {1}, {0}", connection.getResponseCode(), connection.getResponseMessage(), this.link);

        return response.toString();
    }

    private String createGetRequest() throws IOException {
        return connect();
    }

    public String createHeadRequest(String key)  {
        try {
            URL obj = new URL(this.link);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod(this.requestType.toString());
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return connection.getHeaderField(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public String createPostRequest() throws IOException {
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
        http.setRequestProperty("From", "Launcher-" + Global.VERSION);

        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
        }

        InputStream is = http.getResponseCode() >= 400 ? http.getErrorStream() : http.getInputStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //Log.custom("N", "POST > {2} > {1}, {0}", http.getResponseCode(), http.getResponseMessage(), this.link);

        return response.toString();
    }

    public static void loadCerts() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

            try (InputStream in = Request.class.getClassLoader().getResourceAsStream("cacerts")) {
                keyStore.load(in, Global.SSL_CERTS_PASSWORD.toCharArray());
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            kmf.init(keyStore, Global.SSL_CERTS_PASSWORD.toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();

            tmf.init(keyStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);

            SSLSocketFactory factory = sslContext.getSocketFactory();
            HttpsURLConnection.setDefaultSSLSocketFactory(factory);

        } catch (Exception e) {
            Log.err("Error on certs initialization:");
            e.printStackTrace();
        }
    }
}

