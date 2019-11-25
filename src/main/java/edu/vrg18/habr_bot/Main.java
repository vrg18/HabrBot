package edu.vrg18.habr_bot;

import edu.vrg18.habr_bot.util.HttpJsonReaderWriter;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Main {

//    private static final String API_URL = "http://159.69.208.196:8080/rest/";  // Адрес API
    private static final String API_URL = "http://localhost:8080/rest/";  // Адрес API
    private static final String API_USERNAME = "habrabot";
    private static final String API_PASSWORD = "hik191101";
    private static final String API_FIRSTNAME = "HabrBot";
    private static final int MAX_NUMBER_HABR_ARTICLE = 474000;

    public static void main(String[] args) throws IOException, InterruptedException {

        Pair<Integer, JSONObject> responseO;
        long startTime = System.currentTimeMillis();
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        do {

            responseO = HttpJsonReaderWriter.readJsonObjectFromUrl(API_URL.concat("users/name/").concat(API_USERNAME));

            if (responseO.getKey() == 404) {
                JSONObject habraBot = new JSONObject();
                habraBot.put("userName", API_USERNAME);
                habraBot.put("newPassword", API_PASSWORD);
                habraBot.put("firstName", API_FIRSTNAME);
                habraBot.put("enabled", true);
                responseO = HttpJsonReaderWriter.writeJsonObjectToUrl("POST", habraBot, API_URL.concat("users"));
            }

            Thread.sleep(10000);    // 10 сек

        } while (responseO.getKey() != 200);

        JSONObject userJson = responseO.getValue();
        String userId = responseO.getValue().getString("id");
        System.out.println("user: " + userJson.get("firstName"));

        String uptime = null;
        String responseOkey = null;

        for (; ; ) {

            Pair<Integer, JSONArray> responseA = HttpJsonReaderWriter.readJsonArrayFromUrl(API_URL.concat("messages/user/").concat(userId), API_USERNAME, API_PASSWORD);
            if (responseA.getKey() == 200) {

                for (Object object : responseA.getValue()) {

                    JSONObject inMessage = (JSONObject) object;

                    if (((JSONObject) inMessage.get("author")).get("id").equals(userJson.get("id"))) continue;

                    System.out.println("Получен запрос от " + ((JSONObject) inMessage.get("author")).get("firstName") + ": " + inMessage.get("text"));
                    String textMessage = habrArticle();
                    System.out.print("Статья \"" + textMessage + "\"" + " отправляется... ");

                    JSONObject outMessage = new JSONObject();
                    outMessage.put("author", userJson);
                    outMessage.put("room", inMessage.get("room"));
                    outMessage.put("text", textMessage);
                    responseO = HttpJsonReaderWriter.writeJsonObjectToUrl("POST", outMessage, API_URL.concat("messages"), API_USERNAME, API_PASSWORD);

                    if (responseO.getKey() == 200) {

                        System.out.println("успешно!");

                        JSONObject markInMessageAsRead = new JSONObject();
                        markInMessageAsRead.put("message", inMessage);
                        markInMessageAsRead.put("user", userJson);
                        HttpJsonReaderWriter.writeJsonObjectToUrl("POST", markInMessageAsRead, API_URL.concat("messages/familiarized"), API_USERNAME, API_PASSWORD);

                        JSONObject markOutMessageAsRead = new JSONObject();
                        markOutMessageAsRead.put("message", responseO.getValue());
                        markOutMessageAsRead.put("user", userJson);
                        HttpJsonReaderWriter.writeJsonObjectToUrl("POST", markOutMessageAsRead, API_URL.concat("messages/familiarized"), API_USERNAME, API_PASSWORD);

                    } else if (responseO.getKey() == 422) {
                        System.out.println("хм-м-м... это уже было.");

                    } else {
                        System.out.println("упс-с-с... почему-то не получилось, ошибка " + responseO.getKey().toString() + ".");
                    }
                }

                uptime = format.format(new Date(System.currentTimeMillis() - startTime));
                userJson.put("lastName", " (up " + uptime + ")");
                responseO = HttpJsonReaderWriter.writeJsonObjectToUrl("PUT", userJson, API_URL.concat("users"), API_USERNAME, API_PASSWORD);
                if (responseO.getKey() == 200) {
                    userJson = responseO.getValue();
                }

                responseOkey = responseO.getKey().toString();

            } else {

                uptime = format.format(new Date(System.currentTimeMillis() - startTime));
                responseOkey = "skip";
            }

            System.out.print(uptime + " (" + responseA.getKey() + ", " + responseOkey + ")     \r");
            Thread.sleep(10000);    // 10 сек
        }
    }

    private static String habrArticle() throws IOException, InterruptedException {

        String habrUrl;
        do {
            Thread.sleep(1000);    // 10 сек
            habrUrl = "https://habr.com/post/" + (int) (Math.random() * (MAX_NUMBER_HABR_ARTICLE) + 1) + "/";
            System.out.print("Проверяем \"" + habrUrl + "\"" + " на существование... ");
        } while (!HttpJsonReaderWriter.checkGetRequestFor200(habrUrl));
        System.out.println("существует!");
        return habrUrl;
    }
}
