package edu.vrg18.habr_bot;

import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class Main {

    //    private static final String API_URL = "http://159.69.208.196:8080/rest/";  // Адрес API
    private static final String API_URL = "http://localhost:8080/rest/";  // Адрес API
    private static final String API_USERNAME = "habrabot";
    private static final String API_PASSWORD = "hik191101";
    private static final String API_FIRSTNAME = "HabrBot";

    public static void main(String[] args) throws IOException {


        Pair <Integer, JSONObject> responseO = HttpJsonReaderWriter.readJsonObjectFromUrl(API_URL.concat("users/name/").concat(API_USERNAME));

        if (responseO.getKey() == 404) {
            JSONObject habraBot = new JSONObject();
            habraBot.put("userName", API_USERNAME);
            habraBot.put("newPassword", API_PASSWORD);
            habraBot.put("firstName", API_FIRSTNAME);
            habraBot.put("enabled", true);
            responseO = HttpJsonReaderWriter.writeJsonObjectToUrl(habraBot, API_URL.concat("users"));
        }

        String userId = responseO.getValue().getString("id");
        System.out.println("userId: " + userId);

//        for (;;) {

            Pair <Integer, JSONArray> responseA = HttpJsonReaderWriter.readJsonArrayFromUrl(API_URL.concat("messages/user/").concat(userId), API_USERNAME, API_PASSWORD);
            for (Object inMessage : responseA.getValue()) {

                JSONObject outMessage = new JSONObject();
                outMessage.put("author", userId);
                outMessage.put("room", inMessage);
                outMessage.put("text", habrArticle());
                responseO = HttpJsonReaderWriter.writeJsonObjectToUrl(outMessage, API_URL.concat("users"), API_USERNAME, API_PASSWORD);

            }


//        }

        System.out.println(responseO.getValue());
    }

    private static String habrArticle() {

        return "https://habr.com/post/472686/";
    }

}
