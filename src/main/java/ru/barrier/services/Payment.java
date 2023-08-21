package ru.barrier.services;

import com.vdurmont.emoji.EmojiParser;
import okhttp3.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

public class Payment implements Runnable {

//    private static boolean endPayment;

    private Integer i = 0;
    private String idPayment;
    private Long chatId;
    private Integer money;
    private Integer parkingPlace;
    private Integer amountOfDays;

    private String idempotenceKey = "Bill_" + RandomStringUtils.randomNumeric(20);

    AddData addData = new AddData();

    public Payment() {
    }

    public Payment(Long chatId, Integer parkingPlace, Integer amountOfDays, String idPayment, Integer money) {
        this.chatId = chatId;
        this.parkingPlace = parkingPlace;
        this.amountOfDays = amountOfDays;
        this.idPayment = idPayment;
        this.money = money;

    }

    @Override
    public void run() {
        Response informationAboutPayment = null;
        String informationAboutPaymentInString;
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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            status = parserJson(informationAboutPaymentInString, "status");
//            System.out.println(status);
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
                    System.out.println(chatId + " " + parkingPlace + " " + amountOfDays);
                    addData.newPayment(chatId, parkingPlace, amountOfDays);

                    sendMessage(chatId, "Оплачено");
                    flag = 1;
//                    endPayment = true;
                } else sendMessage(chatId, "Платеж не прошел");
                break;
            }
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
