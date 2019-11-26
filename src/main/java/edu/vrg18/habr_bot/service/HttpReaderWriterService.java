package edu.vrg18.habr_bot.service;

import com.alibaba.fastjson.JSON;
import javafx.util.Pair;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;


public class HttpReaderWriterService {

    public static Pair<Integer, Object> readObjectFromUrl(Class<?> cls, String... args) throws IOException {

        Pair<Integer, String> response = readJsonTextFromUrl(args);
        return new Pair<>(
                response.getKey(),
                response.getKey() == 200 ? JSON.parseObject(response.getValue(), cls) : null);
    }

    public static Pair<Integer, List<?>> readListFromUrl(Class<?> cls, String... args) throws IOException {

        Pair<Integer, String> response = readJsonTextFromUrl(args);
        return new Pair<>(
                response.getKey(),
                response.getKey() == 200 ? JSON.parseArray(response.getValue(), cls) : null);
    }

    public static Pair<Integer, Object> writeObjectToUrl(String httpMethod, Object object, String... args) throws IOException {

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
        os.write(JSON.toJSONString(object).getBytes("UTF-8"));
        os.close();

        int response = conn.getResponseCode();
        if (response == 200) {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String result = IOUtils.toString(in, "UTF-8");
            in.close();
            object = JSON.parseObject(result, object.getClass());
        }
        conn.disconnect();
        return new Pair<>(response, object);
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

    public static boolean checkGetRequestFor200(String url) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(10000);
        conn.setDoOutput(true);
        return conn.getResponseCode() == 200;
    }
}
