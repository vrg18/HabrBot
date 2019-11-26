package edu.vrg18.habr_bot;

import edu.vrg18.habr_bot.entity.Familiarize;
import edu.vrg18.habr_bot.entity.Message;
import edu.vrg18.habr_bot.entity.User;
import edu.vrg18.habr_bot.service.HttpReaderWriterService;
import javafx.util.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    private static Logger LOGGER;

    static {
        try(FileInputStream ins = new FileInputStream("./src/main/resources/logging.properties")) {
            LogManager.getLogManager().readConfiguration(ins);
            LOGGER = Logger.getLogger(Main.class.getName());
        } catch (Exception ignore){
            ignore.printStackTrace();
        }
    }

    //    private static final String API_URL = "http://159.69.208.196:8080/rest/";  // Адрес API
    private static final String API_URL = "http://localhost:8080/rest/";  // Адрес API
    private static final String API_USERNAME = "habrabot";
    private static final String API_PASSWORD = "hik191101";
    private static final String API_FIRSTNAME = "HabrBot";
    private static final int MAX_NUMBER_HABR_ARTICLE = 477000;

    public static void main(String[] args) throws IOException, InterruptedException {

        Pair<Integer, Object> responseObject;
        long startTime = System.currentTimeMillis();
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        do {

            responseObject = HttpReaderWriterService.readObjectFromUrl(
                    User.class,
                    API_URL.concat("users/name/").concat(API_USERNAME));

            if (responseObject.getKey() == 404) {
                User habraBot = new User(null, API_USERNAME, API_PASSWORD, API_FIRSTNAME, null, true);
                responseObject =
                        HttpReaderWriterService.writeObjectToUrl("POST", habraBot, API_URL.concat("users"));
            }

            Thread.sleep(10000);    // 10 сек

        } while (responseObject.getKey() != 200);

        User habraBot = (User) responseObject.getValue();
        LOGGER.info("User: " + habraBot.getFirstName());

        String uptime = null;
        int responseObjectKey;

        for (; ; ) {

            Pair<Integer, List<?>> responseList = HttpReaderWriterService.readListFromUrl(
                            Message.class,
                            API_URL.concat("messages/user/").concat(habraBot.getId().toString()),
                            API_USERNAME,
                            API_PASSWORD);

            if (responseList.getKey() == 200) {

                for (Object inObject : responseList.getValue()) {

                    Message inMessage = (Message) inObject;

                    if (inMessage.getAuthor().getId().equals(habraBot.getId())) continue;

                    LOGGER.info("Получен запрос от " + inMessage.getAuthor().getFirstName() + ": " + inMessage.getText());
                    String textOutMessage = habrArticle();
                    LOGGER.info("Статья \"" + textOutMessage + "\"" + " отправляется... ");

                    Message outMessage = new Message(null, habraBot, inMessage.getRoom(), textOutMessage);
                    responseObject = HttpReaderWriterService.writeObjectToUrl(
                            "POST", outMessage, API_URL.concat("messages"), API_USERNAME, API_PASSWORD);

                    if (responseObject.getKey() == 200) {

                        outMessage = (Message) responseObject.getValue();
                        LOGGER.info("успешно!");

                        Familiarize markInMessageAsRead = new Familiarize(inMessage, habraBot);
                        HttpReaderWriterService.writeObjectToUrl("POST", markInMessageAsRead,
                                API_URL.concat("messages/familiarized"), API_USERNAME, API_PASSWORD);

                        Familiarize markOutMessageAsRead = new Familiarize(outMessage, habraBot);
                        HttpReaderWriterService.writeObjectToUrl("POST", markOutMessageAsRead,
                                API_URL.concat("messages/familiarized"), API_USERNAME, API_PASSWORD);

                    } else if (responseObject.getKey() == 422) {
                        LOGGER.info("хм-м-м... это уже было.");

                    } else {
                        LOGGER.info("упс-с-с... почему-то не получилось, ошибка " +
                                responseObject.getKey().toString() + ".");
                    }
                }

                uptime = format.format(new Date(System.currentTimeMillis() - startTime));
                habraBot.setLastName(" (up " + uptime + ")");
                responseObject = HttpReaderWriterService.writeObjectToUrl
                        ("PUT", habraBot, API_URL.concat("users"), API_USERNAME, API_PASSWORD);
                if (responseObject.getKey() == 200) {
                    habraBot = (User) responseObject.getValue();
                }

                responseObjectKey = responseObject.getKey();

            } else {

                uptime = format.format(new Date(System.currentTimeMillis() - startTime));
                responseObjectKey = 0;
            }

            if (responseList.getKey() != 200 || responseObjectKey != 200) {
                LOGGER.warning("uptime: " + uptime + ", errorGetListMessage: " +
                        responseList.getKey() + ", errorLastResponse: " + responseObjectKey + ")");
            }

            Thread.sleep(10000);    // 10 сек
        }
    }

    private static String habrArticle() throws IOException, InterruptedException {

        String habrUrl;
        do {
            Thread.sleep(1000);    // 10 сек
            habrUrl = "https://habr.com/post/" + (int) (Math.random() * (MAX_NUMBER_HABR_ARTICLE) + 1) + "/";
            LOGGER.info("Проверяем \"" + habrUrl + "\"" + " на существование... ");
        } while (!HttpReaderWriterService.checkGetRequestFor200(habrUrl));
        LOGGER.info("существует!");
        return habrUrl;
    }
}
