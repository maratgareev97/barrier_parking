package ru.barrier.services;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import okhttp3.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.*;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Iterator;

@Component
@Log4j
public class Payment implements Runnable {

    private final String url = "jdbc:postgresql://localhost:5432/barrier_db";
    private final String user = "postgres";
    private final String password = "GOGUDAserver123!";
    private Integer i = 0;
    private String idPayment;
    private Long chatId;
    private Integer money;
    private Integer parkingPlace;
    private Integer amountOfDays;
    private LocalDateTime dataTimeNextPayment;
    private String newOrAdd;
    private String idempotenceKey = "Bill_" + RandomStringUtils.randomNumeric(20);

    private int finishTimeWorkPayment = 300;

    public Payment() {
    }

    public Payment(Long chatId, Integer amountOfDays, String idPayment, Integer money, LocalDateTime dataTimeNextPayment, String newOrAdd) {
        this.chatId = chatId;
        this.amountOfDays = amountOfDays;
        this.idPayment = idPayment;
        this.money = money;
        this.dataTimeNextPayment = dataTimeNextPayment;
        this.newOrAdd = newOrAdd;
    }

    public Payment(Long chatId, Integer parkingPlace, Integer amountOfDays, String idPayment, Integer money, String newOrAdd) {
        this.chatId = chatId;
        this.parkingPlace = parkingPlace;
        this.amountOfDays = amountOfDays;
        this.idPayment = idPayment;
        this.money = money;
        this.newOrAdd = newOrAdd;
    }

    @Override
    public void run() {
        Response informationAboutPayment = null;
        String informationAboutPaymentInString = "";
        String status = null;
        int flag = 0;
        while (i < finishTimeWorkPayment) {
            i++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            informationAboutPayment = informationAboutPayment(idPayment);
            try {
                informationAboutPaymentInString = informationAboutPayment.body().string();
            } catch (IOException e) {
                log.error("informationAboutPayment  " + new RuntimeException(e));
                throw new RuntimeException(e);
            }

            status = parserJson(informationAboutPaymentInString, "status");
            String paymentConfirmationString = "";
            if (status.equals("waiting_for_capture")) {
                Response paymentConfirmation = paymentConfirmation(idPayment, idempotenceKey, money);
                try {
                    paymentConfirmationString = paymentConfirmation.body().string();
                    status = parserJson(paymentConfirmationString, "status");
                } catch (IOException e) {
                    log.error("paymentConfirmation  " + new RuntimeException(e));
                    throw new RuntimeException(e);
                }
                if (status.equals("succeeded")) {
                    flag = 1;
                    break;
                } else {
                    sendMessage(chatId, "Платеж не прошел");
                    log.debug(chatId + " Платеж не прошел");
                }
                break;
            }
        }
        if (flag == 1) {
            if (newOrAdd.equals("new")) {
                addData();
                addDataAllPayments();
            } else {
                addDataRenting();
                addDataAllPayments();
            }

            sendMessage(chatId, "Оплачено");
            log.debug(chatId + " Оплачено");
        }
        if (flag == 0 && newOrAdd.equals("new") && getDataTimeNextPayment() == null) {
            String attention = EmojiParser.parseToUnicode("⚠");
            String noUrl = EmojiParser.parseToUnicode("📵");
            sendMessage(chatId, String.valueOf(attention) + " Счет на оплату отменен! " + String.valueOf(attention));
            sendMessage(chatId, String.valueOf(noUrl) + " Ссылка более не действительна! " + String.valueOf(noUrl));
            deleteUserForNotPayment();
            log.debug(chatId + "   " + String.valueOf(attention) + " Счет на оплату отменен! " + String.valueOf(attention));
        }
    }

    public String getDataTimeNextPayment() {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            String query = "SELECT * FROM user_barrier WHERE chat_id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, chatId);
            ResultSet result1 = preparedStatement.executeQuery();
            String result = null;
            while (result1.next()) {
                result = result1.getString("data_time_next_payment");
            }
            return result;

        } catch (Exception e) {
            log.error("Не удален");
        }
        return null;
    }

    public void deleteUserForNotPayment() {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            String query = "DELETE FROM public.user_barrier WHERE chat_id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, chatId);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            log.error("Не удален");
        }
    }

    public void addDataAllPayments() {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            String query = "INSERT INTO public.all_payments (chat_id, data_time_payment, id_payment) VALUES(?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, chatId);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setString(3, idPayment);
            preparedStatement.executeUpdate();

            connection.close();
        } catch (SQLException e) {
            log.error("DriverManager.getConnection  " + new RuntimeException(e));
            throw new RuntimeException(e);
        }
    }

    public void addDataRenting() {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            String query = "UPDATE user_barrier SET data_time_next_payment=? WHERE chat_id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(dataTimeNextPayment));
            preparedStatement.setLong(2, chatId);
            preparedStatement.executeUpdate();

            query = "UPDATE payment SET data_time_payment = ? WHERE chat_id=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setLong(2, chatId);
            preparedStatement.executeUpdate();


            connection.close();
        } catch (SQLException e) {
            log.error("DriverManager.getConnection addDataRenting   " + new RuntimeException(e));
            throw new RuntimeException(e);
        }
    }

    public void addData() {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            String query = "UPDATE user_barrier SET amount_of_days = ?, " +
                           "data_time_last_payment = ?, data_time_next_payment=? WHERE chat_id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, amountOfDays);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusDays(amountOfDays)));
            preparedStatement.setLong(4, chatId);
            preparedStatement.executeUpdate();

            query = "UPDATE payment SET data_time_payment = ? WHERE chat_id=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setLong(2, chatId);
            preparedStatement.executeUpdate();

            connection.close();
        } catch (SQLException e) {
            log.error("DriverManager.getConnection addData   " + new RuntimeException(e));
            throw new RuntimeException(e);
        }
    }

    public Response creatingPayment(Integer money) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");

        RequestBody body = RequestBody.create(mediaType,
                "{\n        \"amount\": " +
                "{\n          \"value\": \"" + money + "\",\n          \"currency\": \"RUB\"\n        }," +
                "\n      \n        \"confirmation\": {\n          \"type\": \"redirect\"," +
                "\n          \"return_url\": \"https://t.me/open_barrier_bot\"\n        }," +
                "\n        \"description\": \"Заказ №" + idempotenceKey + "\"\n      }");
        Request request = new Request.Builder()
                .url("https://api.yookassa.ru/v3/payments")
                .method("POST", body)
                .addHeader("Idempotence-Key", idempotenceKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Basic OTg0NzMzOmxpdmVfdG43anc5ZWtvZnhQWVM5VUpwV3JyNkNJTTEyaGlHWElMUnJVdzJQdnd4OA==")
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            log.error("creatingPayment   " + new RuntimeException(e));
            throw new RuntimeException(e);
        }
        return response;
    }

    public String parserJson(String response, String keyValue) {
        JSONObject jsonObject = new JSONObject(response);
        Iterator<String> keys = jsonObject.keys();
        JSONObject valueN = new JSONObject();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);
            if (key.equals(keyValue)) {
                try {
                    valueN = (JSONObject) value;
                } catch (Exception e) {
//                    log.error("JSONObject " + String.valueOf(value));
                    return String.valueOf(value);
                }
            }
        }
        String result = valueN.toString();
        return result;
    }

    public Response informationAboutPayment(String idPayment) {
        OkHttpClient client = new OkHttpClient();

        String credential = Credentials.basic("984733", "live_tn7jw9ekofxPYS9UJpWrr6CIM12hiGXILRrUw2Pvwx8");

        Request request = new Request.Builder()
                .url("https://api.yookassa.ru/v3/payments/" + idPayment)
                .header("Authorization", credential)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            log.error("informationAboutPayment" + new RuntimeException(e));
            throw new RuntimeException(e);
        }
        return response;
    }

    public void sendMessage(Long chatId, String testMessage) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "{\"chat_id\": \"" + chatId + "\", \"text\": \"" + testMessage + "\", \"disable_notification\": true}");
        Request request = new Request.Builder()
                .url("https://api.telegram.org/bot6500675392:AAF7zPbeR49zw_1a2vLBFaRTPhqaHTU0bfM/sendMessage")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            log.error("sendMessage Payment");
            throw new RuntimeException(e);
        }
    }


    public Response paymentConfirmation(String idPayment, String idempotenceKey, Integer money) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n        \"amount\": {\n          \"value\": \"" + money + "\",\n          \"currency\": \"RUB\"\n        }\n      }");
        Request request = new Request.Builder()
                .url("https://api.yookassa.ru/v3/payments/" + idPayment + "/capture")
                .method("POST", body)
                .addHeader("Idempotence-Key", idempotenceKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Basic OTg0NzMzOmxpdmVfdG43anc5ZWtvZnhQWVM5VUpwV3JyNkNJTTEyaGlHWElMUnJVdzJQdnd4OA==")
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            log.error("client.newCall(request)");
            throw new RuntimeException(e);
        }
        return response;
    }
}
