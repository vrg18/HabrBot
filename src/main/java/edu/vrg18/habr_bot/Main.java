package edu.vrg18.habr_bot;

import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String API_URL = "http://159.69.208.196:8080/chat/rest/";  // Адрес API
    //    private static final String API_URL = "http://localhost:8080/rest/";  // Адрес API
    private static final String API_USERNAME = "habrabot";
    private static final String API_PASSWORD = "hik191101";
    private static final String API_FIRSTNAME = "HabrBot";
    private static final int MAX_NUMBER_HABR_ARTICLE = 474000;

    public static void main(String[] args) throws IOException, InterruptedException {

        Pair<Integer, JSONObject> responseO;

        do {

            responseO = HttpJsonReaderWriter.readJsonObjectFromUrl(API_URL.concat("users/name/").concat(API_USERNAME));

            if (responseO.getKey() == 404) {
                JSONObject habraBot = new JSONObject();
                habraBot.put("userName", API_USERNAME);
                habraBot.put("newPassword", API_PASSWORD);
                habraBot.put("firstName", API_FIRSTNAME);
                habraBot.put("enabled", true);
                responseO = HttpJsonReaderWriter.writeJsonObjectToUrl(habraBot, API_URL.concat("users"));
            }

            try {
                Thread.sleep(10000);    // 10 сек
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } while (responseO.getKey() != 200);

        JSONObject userJson = responseO.getValue();
        String userId = responseO.getValue().getString("id");
        System.out.println("user: " + userJson.get("firstName"));

        for (; ; ) {

            Pair<Integer, JSONArray> responseA = HttpJsonReaderWriter.readJsonArrayFromUrl(API_URL.concat("messages/user/").concat(userId), API_USERNAME, API_PASSWORD);
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
                responseO = HttpJsonReaderWriter.writeJsonObjectToUrl(outMessage, API_URL.concat("messages"), API_USERNAME, API_PASSWORD);

                if (responseO.getKey() == 200) {

                    System.out.println("успешно!");

                    JSONObject markInMessageAsRead = new JSONObject();
                    markInMessageAsRead.put("message", inMessage);
                    markInMessageAsRead.put("user", userJson);
                    HttpJsonReaderWriter.writeJsonObjectToUrl(markInMessageAsRead, API_URL.concat("messages/familiarized"), API_USERNAME, API_PASSWORD);

                    JSONObject markOutMessageAsRead = new JSONObject();
                    markOutMessageAsRead.put("message", responseO.getValue());
                    markOutMessageAsRead.put("user", userJson);
                    HttpJsonReaderWriter.writeJsonObjectToUrl(markOutMessageAsRead, API_URL.concat("messages/familiarized"), API_USERNAME, API_PASSWORD);

                } else if (responseO.getKey() == 422) {
                    System.out.println("хм-м-м... это уже было.");

                } else {
                    System.out.println("упс-с-с... почему-то не получилось, ошибка " + responseO.getKey().toString() + ".");
                }
            }

//            try {
//                Thread.sleep(10000);    // 10 сек
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }

    private static String habrArticle() throws IOException, InterruptedException {

        String habrUrl;
        do {
//            try {
//                Thread.sleep(10000);    // 10 сек
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            habrUrl = "https://habr.com/post/" + (int) (Math.random() * (MAX_NUMBER_HABR_ARTICLE) + 1) + "/";
            System.out.print("Проверяем \"" + habrUrl + "\"" + " на существование... ");
        } while (!HttpJsonReaderWriter.checkGetRequestFor200(habrUrl));
        System.out.println("существует!");
        return habrUrl;
    }

}
