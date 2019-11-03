package edu.vrg18.habr_bot;

import javafx.util.Pair;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;


class HttpJsonReaderWriter {

    static Pair<Integer, JSONArray> readJsonArrayFromUrl(String... args) throws IOException {

        Pair<Integer, String> response = readJsonTextFromUrl(args);
        return new Pair<>(response.getKey(), response.getKey() == 200 ? new JSONArray(response.getValue()) : null);
    }

    static Pair<Integer, JSONObject> readJsonObjectFromUrl(String... args) throws IOException {

        Pair<Integer, String> response = readJsonTextFromUrl(args);
        return new Pair<>(response.getKey(), response.getKey() == 200 ? new JSONObject(response.getValue()) : null);
    }

    private static Pair<Integer, String> readJsonTextFromUrl(String[] args) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) new URL(args[0]).openConnection();
        conn.setConnectTimeout(10000);

        if (args.length > 1) {
            Base64.Encoder enc = Base64.getEncoder();
            byte[] encbytes = enc.encode((args[1] + ":" + args[2]).getBytes());
            conn.setRequestProperty("Authorization", "Basic " + new String(encbytes));
        }

        conn.setDoOutput(true);

        String result = null;
        int response = conn.getResponseCode();
        if (response == 200) {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            result = IOUtils.toString(in, "UTF-8");
            in.close();
        }

        conn.disconnect();
        return new Pair<>(response, result);
    }

    static Pair<Integer, JSONObject> writeJsonObjectToUrl(String httpMethod, JSONObject jsonObject, String... args) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) new URL(args[0]).openConnection();
        conn.setConnectTimeout(10000);
        conn.setRequestMethod(httpMethod);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        if (args.length > 1) {
            Base64.Encoder enc = Base64.getEncoder();
            byte[] encbytes = enc.encode((args[1] + ":" + args[2]).getBytes());
            conn.setRequestProperty("Authorization", "Basic " + new String(encbytes));
        }

        conn.setDoOutput(true);
        conn.setDoInput(true);

        OutputStream os = conn.getOutputStream();
        os.write(jsonObject.toString().getBytes("UTF-8"));
        os.close();

        int response = conn.getResponseCode();
        if (response == 200) {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String result = IOUtils.toString(in, "UTF-8");
            in.close();
            jsonObject = new JSONObject(result);
        }
        conn.disconnect();
        return new Pair<>(response, jsonObject);
    }

    static boolean checkGetRequestFor200(String url) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(10000);
        conn.setDoOutput(true);
        return conn.getResponseCode() == 200;
    }

    private static String readAll(Reader rd) throws IOException {

        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
