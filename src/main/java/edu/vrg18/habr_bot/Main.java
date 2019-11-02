package edu.vrg18.habr_bot;

import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

public class Main {

    //    private static final String API_URL = "http://159.69.208.196:8080/rest/";  // Адрес API
    private static final String API_URL = "http://localhost:8080/rest/";  // Адрес API
    private static final String API_USERNAME = "habrabot";
    private static final String API_PASSWORD = "hik191101";
    private static final String API_FIRSTNAME = "HabrBot";

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

            Thread.sleep(1000); // 1 сек

        } while (responseO.getKey() != 200);

        String userId = responseO.getValue().getString("id");
        System.out.println("userId: " + userId);

//        for (;;) {

        Pair<Integer, JSONArray> responseA = HttpJsonReaderWriter.readJsonArrayFromUrl(API_URL.concat("messages/user/").concat(userId), API_USERNAME, API_PASSWORD);
        for (Object inMessage : responseA.getValue()) {

            System.out.println("Получен запрос от: " + ((JSONObject) ((JSONObject) inMessage).get("author")).get("firstName"));
            String textMessage = habrArticle();
            System.out.print("Статья \"" + textMessage + "\"" + " отправляется... ");

            JSONObject outMessage = new JSONObject();
            outMessage.put("author", userId);
            outMessage.put("room", ((JSONObject) ((JSONObject) inMessage).get("room")).get("id"));
            outMessage.put("text", textMessage);
            responseO = HttpJsonReaderWriter.writeJsonObjectToUrl(outMessage, API_URL.concat("messages"));//API_USERNAME, API_PASSWORD);

            if (responseO.getKey() == 200) {

                System.out.println("успешно!");

                JSONObject markMessageAsRead = new JSONObject();
                outMessage.put("message", ((JSONObject) inMessage).get("id"));
                outMessage.put("user", userId);
                responseO = HttpJsonReaderWriter.writeJsonObjectToUrl(markMessageAsRead, API_URL.concat("messages/familiarized"), API_USERNAME, API_PASSWORD);

            } else {
                System.out.println("упс-с-с... не получилось.");
            }
        }

        Thread.sleep(60000); // 1 мин
        //        }

        System.out.println(responseO.getValue());
    }

    private static String habrArticle() {

        return "post472686";
    }

}
