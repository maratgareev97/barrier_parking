package ru.barrier.services;

import com.vdurmont.emoji.EmojiParser;
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
//        Thread current = Thread.currentThread();
        System.out.println(dataTimeNextPayment + "----------------------------------------------------- " + newOrAdd);

        Response informationAboutPayment = null;
        String informationAboutPaymentInString = "";
        String status;
        int flag = 0;
        while (i < 50) {
            i++;
            System.out.println(i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            informationAboutPayment = informationAboutPayment(idPayment);
            try {
                informationAboutPaymentInString = informationAboutPayment.body().string();
                System.out.println(informationAboutPaymentInString);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            status = parserJson(informationAboutPaymentInString, "status");
            System.out.println(status);
            String paymentConfirmationString = "";
            if (status.equals("waiting_for_capture")) {
                Response paymentConfirmation = paymentConfirmation(idPayment, idempotenceKey, money);
                try {
                    paymentConfirmationString = paymentConfirmation.body().string();
                    status = parserJson(paymentConfirmationString, "status");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (status.equals("succeeded")) {
                    System.out.println("succeeded");
                    flag = 1;

//                    System.out.println("База данных");
//                    addData();
                    break;
//                    endPayment = true;
                } else sendMessage(chatId, "Платеж не прошел");
                break;
            }
        }
        if (flag == 1) {
//            current.interrupt();
//            addData.newPayment(chatId, parkingPlace, amountOfDays);
            if (newOrAdd.equals("new")) {
                System.out.println("11111111111111111111111111111new111111111111111111111111111111111new");
                addData();
            } else {
                System.out.println("ADDDDDDDDDDDDDDD");
                addDataRenting();
                System.out.println("DDDDDDADDDDDDDDDDDDDD");

            }
            sendMessage(chatId, "Оплачено");
        }
        if (flag == 0) {
            String attention = EmojiParser.parseToUnicode("⚠");
            sendMessage(chatId, String.valueOf(attention) + " Счет на оплату отменен! " + String.valueOf(attention));
//            System.out.println("Усе");
        }
    }

    //    public Boolean getBoolean(){
//        return endPayment;
//    }

    public void addDataRenting() {
        System.out.println("addDataRenting   " + dataTimeNextPayment);
        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            String query = "UPDATE user_barrier SET data_time_next_payment=? WHERE chat_id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(dataTimeNextPayment));
//            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
//            preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusDays(amountOfDays)));
            preparedStatement.setLong(2, chatId);
            preparedStatement.executeUpdate();

            query = "UPDATE payment SET data_time_payment = ? WHERE chat_id=?";
            preparedStatement = connection.prepareStatement(query);
//            preparedStatement.setInt(1, amountOfDays);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
//            preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusDays(amountOfDays)));
            preparedStatement.setLong(2, chatId);
            preparedStatement.executeUpdate();

            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


//        addData.newPayment(chatId, parkingPlace, amountOfDays);
        System.out.println("GOOD!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    public void addData() {
        System.out.println(chatId + " " + parkingPlace + " " + amountOfDays);
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
//            preparedStatement.setInt(1, amountOfDays);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
//            preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusDays(amountOfDays)));
            preparedStatement.setLong(2, chatId);
            preparedStatement.executeUpdate();

            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


//        addData.newPayment(chatId, parkingPlace, amountOfDays);
        System.out.println("GOOD!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    public Response creatingPayment(Integer money) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        System.out.println("idempotenceKey " + idempotenceKey);

        RequestBody body = RequestBody.create(mediaType,
                "{\n        \"amount\": " +
                "{\n          \"value\": \"" + money + "\",\n          \"currency\": \"RUB\"\n        }," +
                "\n      \n        \"confirmation\": {\n          \"type\": \"redirect\"," +
                "\n          \"return_url\": \"https://www.example.com/return_url\"\n        }," +
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
            System.out.println(response);
        } catch (IOException e) {
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
            throw new RuntimeException(e);
        }
        return response;
    }
}
