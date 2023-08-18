package ru.barrier.services;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import net.bytebuddy.asm.Advice;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.payments.Invoice;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.barrier.configs.BotConfig;
import ru.barrier.models.User;
import ru.barrier.models.UserBarrier;
import ru.barrier.repository.UserBarrierRepository;
import ru.barrier.repository.UserRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Log4j
public class TelegramBotImpl extends TelegramLongPollingBot implements TelegramBot {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserBarrierRepository userBarrierRepository;
    @Autowired
    private AddData addData;
    @Autowired
    private DataBaseService dataBaseService;
    final BotConfig botConfig;
    SendMessage sendMessage = new SendMessage();
    private Integer money = 0;

    public TelegramBotImpl(BotConfig config) {
        this.botConfig = config;

        // Меню
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Войти в бот"));
        listOfCommands.add(new BotCommand("/open", "Открыть шлагбаум"));
        listOfCommands.add(new BotCommand("/pay", "Оплатить парковку"));
        listOfCommands.add(new BotCommand("/balance", "Баланс вашей парковки"));
        listOfCommands.add(new BotCommand("/help", "Инструкция"));
        listOfCommands.add(new BotCommand("/agreement", "Соглашение"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    public String getBotUsername() {
        return botConfig.getBotName();
    }

    public String getBotToken() {
        return botConfig.getToken();
    }

    private void executeDocument(SendDocument sendDocument) {
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void executeMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void executePhoto(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    ArrayList<Integer> countTimingArrayList = new ArrayList<>();

    @Transactional
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageTest = update.getMessage().getText().toString();

            String rentEmoji = EmojiParser.parseToUnicode("📌");
            String openBarrierEmoji = EmojiParser.parseToUnicode("🚘");
            String myRentsEmoji = EmojiParser.parseToUnicode("🛍");
            String extendRentEmoji = EmojiParser.parseToUnicode("🕑");

            long chatID = update.getMessage().getChatId();
            switch (messageTest) {
                case "/start":
                    startMessage(chatID, update.getMessage().getChat().getFirstName());
                    break;
                case "Открыть шлагбаум":
                    sendMessage(chatID, "ОТКРЫВАЮ");
//                    collOnBarrier("https://zvonok.com/manager/cabapi_external/api/v1/phones/call/?",
//                            "1598159358",
//                            "9153700127",
//                            "bbc1cbcde48564215c0b78b649081cac");
//                    executeDocument(document(chatID,
//                            "http://test.school89.net/wp-content/uploads/2023/07/public_contract_foras.pdf",
//                            ""));
//                    dataBaseService.getUserById(chatID);
                    break;
                case "/open":
                    openMessage(chatID);
                    break;
                case "/agreement":
                    executeDocument(document(chatID,
                            "http://test.school89.net/wp-content/uploads/2023/07/public_contract_foras.pdf",
                            ""));
                    break;
                default:
                    System.out.println(messageTest.toString());

                    if (messageTest.equals(rentEmoji + " Арендовать место")) {
                        User user = userRepository.getUserById(chatID);
                        System.out.println(user);
                        if (user != null) {
                            if (user.getUserBarrier() != null && user.getUserBarrier().getDateTimeNextPayment() != null) {
//                            sendMessageTiming(chatID);
                                sendMessage(chatID, "У Вас имеется действующая аренда. Вы можете продлить аренду.");
                            } else sendMessageTiming(chatID);
                        } else sendMessage(chatID, "Вас нет в базе");
                    } else if (messageTest.equals(openBarrierEmoji + " ОТКРЫТЬ ШЛАГБАУМ")) {
                        LocalDateTime localDateTime = userRepository.getDateNextPayment(chatID);
                        System.out.println("............." + localDateTime);
                        if (localDateTime != null) {
                            System.out.println("Тест   " + userRepository.getDateNextPayment(chatID));
                            Duration duration = compareTime(LocalDateTime.now(), localDateTime);
                            if (duration.toDays() >= 0 && duration.toHours() % 24 >= 0 && duration.toMinutes() >= 0) {
                                System.out.println("Проезжайте");
//                                collOnBarrier("https://zvonok.com/manager/cabapi_external/api/v1/phones/call/?",
//                                        "1598159358",
//                                        "9153700127",
//                                        "bbc1cbcde48564215c0b78b649081cac");

                                if (userRepository.getUserById(chatID).getUserBarrier().getStoppedBy() == 0) {
                                    addData.stoppedByT(userRepository.getUserById(chatID), 1);
                                    sendMessage(chatID, "ЗАЕЗЖАЙТЕ!");
                                } else {
                                    addData.stoppedByT(userRepository.getUserById(chatID), 0);
                                    sendMessage(chatID, "ВЫЕЗЖАЙТЕ!");
                                }

                            } else {
                                sendMessage(chatID, "Оплатите парковку");
                            }
                            System.out.printf(
                                    "%dд %dч %dмин%n",
                                    duration.toDays(),
                                    duration.toHours() % 24,
                                    duration.toMinutes() % 60
                            );
                        } else {
                            sendMessage(chatID, "Оплатите парковку");
                        }
                    } else if (messageTest.equals(myRentsEmoji + " Мои аренды")) {

//                        User user = dataBaseService.getUserBarrierById(chatID).getUserBarrier().getUser();
                        User user = userRepository.getUserById(chatID);
                        if (user != null && user.getUserBarrier() != null && user.getUserBarrier().getDateTimeNextPayment() != null) {
                            LocalDateTime localDateTime = user.getUserBarrier().getDateTimeNextPayment();
                            Duration duration = compareTime(LocalDateTime.now(), localDateTime);
                            sendMessage(chatID,
                                    "Ваше парковочное место №" +
                                    Integer.toString(user.getUserBarrier().getParkingPlace()) + "\n" +
                                    "Дата окончания аренды через: " + duration.toDays() + "дн, " +
                                    duration.toHours() % 24 + " час, " +
                                    duration.toMinutes() % 60 + "мин.");
//                            LocalDateTime localDateTime = user.getUserBarrier().getDateTimeNextPayment();
                            System.out.println(user.getUserBarrier().getDateTimeNextPayment().getDayOfMonth());
                        } else sendMessage(chatID, "Оплатите парковку");
                    } else if (messageTest.equals(extendRentEmoji + " Продлить аренду")) {


                        OkHttpClient client = new OkHttpClient().newBuilder()
                                .build();
                        MediaType mediaType = MediaType.parse("application/json");
                        RequestBody body = RequestBody.create(mediaType,
                                "{\n        \"amount\": " +
                                "{\n          \"value\": \"1.00\",\n          \"currency\": \"RUB\"\n        }," +
                                "\n      \n        \"confirmation\": {\n          \"type\": \"redirect\"," +
                                "\n          \"return_url\": \"https://www.example.com/return_url\"\n        }," +
                                "\n        \"description\": \"Заказ №1\"\n      }");
                        Request request = new Request.Builder()
                                .url("https://api.yookassa.ru/v3/payments")
                                .method("POST", body)
                                .addHeader("Idempotence-Key", "11111")
                                .addHeader("Content-Type", "application/json")
                                .addHeader("Authorization", "Basic OTg0NzMzOmxpdmVfdG43anc5ZWtvZnhQWVM5VUpwV3JyNkNJTTEyaGlHWElMUnJVdzJQdnd4OA==")
                                .build();

                        try {
                            Response response = client.newCall(request).execute();
                            String result = response.body().string();
                            System.out.println(result);
                            JSONObject jsonObject = new JSONObject(result);
                            Iterator<String> keys = jsonObject.keys();
                            JSONObject valueN = new JSONObject();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                Object value = jsonObject.get(key);
                                System.out.println("Ключ: " + key + ", Значение: " + value);
                                if (key.equals("confirmation")) {
                                    valueN = (JSONObject) value;
                                }
                            }

                            String valueNew = valueN.toString();
                            System.out.println(valueNew);
                            JSONObject jsonObjectNew = new JSONObject(valueNew);
                            Iterator<String> keysNew = jsonObjectNew.keys();
                            while (keysNew.hasNext()) {
                                String keyNew = keysNew.next();
                                Object valueNewn = jsonObjectNew.get(keyNew);
                                System.out.println("Ключ!: " + keyNew + ", Значение:! " + valueNewn);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }


                        User user = userRepository.getUserById(chatID);
                        if (user != null && user.getUserBarrier() != null && user.getUserBarrier().getDateTimeNextPayment() != null) {
                            sendMessageTimingForRenting(chatID);


                            sendMessage(chatID, "!");
                        } else sendMessage(chatID, "?");
                    } else sendMessage(chatID, "Оплатите парковку");

            }

        }
//        byte countTiming = 0;
        if (update.hasCallbackQuery()) {
            boolean choicePlace = false;

            Long chatId = update.getCallbackQuery().getFrom().getId();

            System.out.println("!!!!!!!!!!   " + update.hasCallbackQuery());
            String getData = update.getCallbackQuery().getData().toString();
            System.out.println("?????????????   " + getData);
            System.out.println(getData.substring(0, 5));

            log.debug(update.getCallbackQuery().getFrom().getUserName());
            log.debug(update.getCallbackQuery().getFrom().getFirstName());
            log.debug(chatId);

            Integer countTiming = 0;
            Integer countTimingRenting = 0;

            LocalDateTime localDateTime = userRepository.getDateNextPayment(chatId);
            log.debug(localDateTime + "    null");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!1" + dataBaseService.getUserBarrierById(chatId) + "  " + chatId + " " + userRepository.getDateNextPayment(chatId));

            if (update.getCallbackQuery().getData().toString().equals("Accept")) {

                if (dataBaseService.getChatIdUserById(chatId) == null) {
                    registerUser(update.getCallbackQuery().getFrom().getId());
                }

                // добавить основное меню
//                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("");
//                System.out.println(dataBaseService.getUserBarrierById(chatId)+"  "+);
//                if (dataBaseService.getUserBarrierById(chatId) != null &&
//                    dataBaseService.getUserBarrierById(chatId).getUserBarrier().getDateTimeNextPayment() != null) {
                MenuBot menuBot = new MenuBot();
                menuBot.baseMenu(sendMessage);
//                    sendMessage(chatId, "MENU");
//                } else {
//                    MenuBot menuBot = new MenuBot();
//                    menuBot.openBarrier(sendMessage);
//                }
                sendMessage(chatId, "Теперь Вы можете оплатить услугу");
                sendMessageTiming(chatId);
            }

            if (update.getCallbackQuery().getData().toString().equals("oneDay")) {
                sendMessage(chatId, "Один день");
                countTiming = 1;
                countTimingArrayList.add(countTiming);
                money = 300;

            }
            if (update.getCallbackQuery().getData().toString().equals("sevenDay")) {
                sendMessage(chatId, "Семь дней");
                countTiming = 7;
                countTimingArrayList.add(countTiming);
                money = 2000;
            }
            if (update.getCallbackQuery().getData().toString().equals("tenDay")) {
                sendMessage(chatId, "10 дней");
                countTiming = 10;
                countTimingArrayList.add(countTiming);
                money = 2500;
            }
            if (update.getCallbackQuery().getData().toString().equals("fifteenDay")) {
                sendMessage(chatId, "15 дней");
                countTiming = 15;
                countTimingArrayList.add(countTiming);
                money = 3500;
            }
            if (update.getCallbackQuery().getData().toString().equals("oneMonth")) {
                sendMessage(chatId, "1 месяц");
                countTiming = 30;
                countTimingArrayList.add(countTiming);
                money = 6000;
            }


            if (update.getCallbackQuery().getData().toString().equals("oneDayRenting")) {
                sendMessage(chatId, "Один день");
                countTimingRenting = 1;
                money = 300;

            }
            if (update.getCallbackQuery().getData().toString().equals("sevenDayRenting")) {
                sendMessage(chatId, "Семь дней");
                countTimingRenting = 7;
                money = 2000;
            }
            if (update.getCallbackQuery().getData().toString().equals("tenDayRenting")) {
                sendMessage(chatId, "10 дней");
                countTimingRenting = 10;
                money = 2500;
            }
            if (update.getCallbackQuery().getData().toString().equals("fifteenDayRenting")) {
                sendMessage(chatId, "15 дней");
                countTimingRenting = 15;
                money = 3500;
            }
            if (update.getCallbackQuery().getData().toString().equals("oneMonthRenting")) {
                sendMessage(chatId, "1 месяц");
                countTimingRenting = 30;
                money = 6000;
            }
            User user = userRepository.getUserById(chatId);
            if (countTimingRenting != 0 && user != null && user.getUserBarrier() != null && user.getUserBarrier().getDateTimeNextPayment() != null) {
                payment(chatId, "Счёт",
                        "Оплатите за " + Integer.toString(countTimingRenting) + " дней стоянки",
                        "Выставлен счет на оплату",
                        "390540012:LIVE:37489",
                        "RUB",
                        Collections.singletonList(new LabeledPrice("label", 100 * 100)));

                LocalDateTime localDateTimeNew = user.getUserBarrier().getDateTimeNextPayment().plusDays(countTimingRenting);
                addData.timingRenting(user, localDateTimeNew);
                sendMessage(chatId, String.valueOf(localDateTimeNew) + "  " + String.valueOf(countTimingRenting));
//                Collections.singletonList(new LabeledPrice("label", money * 100)));
            }


            if (countTiming != 0) {
                log.debug("countTiming = " + countTiming);
                sendLocalPhoto(String.valueOf(chatId));
                addData.newUserTest(); //--------------------------------------------- тестовые данные ------------------
                log.debug("Тестовые данные загружены");
                List<User> users = userRepository.findAll().stream().toList();
                choicePlace = true;
                System.out.println("countTiming = " + countTiming + "    choosePlace = " + choicePlace);
            }

            System.out.println(money + " money");

            System.out.println("choicePlace:   " + choicePlace);
            if (choicePlace == true) {
                List<UserBarrier> listBusyPlace = userBarrierRepository.findAll().stream().toList();
                ArrayList<Integer> arrayListBusyPlace = new ArrayList<>();
                for (int i = 0; i < listBusyPlace.size(); i++) {
                    arrayListBusyPlace.add(listBusyPlace.get(i).getParkingPlace());
                }
                System.out.println(arrayListBusyPlace);
                Collections.sort(arrayListBusyPlace);
                System.out.println(arrayListBusyPlace + " : " + arrayListBusyPlace.size());

                Integer differenceValue = 0;
                ArrayList<Integer> arrayListFreePlace = new ArrayList<>();

                if (arrayListBusyPlace.size() == 0) {
                    for (int i = 0; i < 27; i++) {
                        arrayListFreePlace.add(i + 1);
                    }
                }
                //заполнение до первого элемента
                if (arrayListBusyPlace.size() != 0)
                    if (arrayListBusyPlace.get(0) != 1) {
                        for (int i = 1; i < arrayListBusyPlace.get(0); i++) {
                            arrayListFreePlace.add(i);
                        }
                    }
                // заполнение в промежутке
                for (int i = 0; i < arrayListBusyPlace.size() - 1; i++) {
                    differenceValue = arrayListBusyPlace.get(i + 1) - arrayListBusyPlace.get(i);
                    if (differenceValue != 1) {
                        Integer start = arrayListBusyPlace.get(i) + 1;
                        Integer finish = arrayListBusyPlace.get(i + 1);
                        for (int j = start; j < finish; j++) {
                            arrayListFreePlace.add(j);
                        }
                    }
                }
                // заполенение после последнего
                if (arrayListBusyPlace.size() != 0)
                    if (arrayListBusyPlace.get(arrayListBusyPlace.size() - 1) != 27) {
                        for (int i = arrayListBusyPlace.get(arrayListBusyPlace.size() - 1); i < 27; i++) {
                            arrayListFreePlace.add(i + 1);
                        }
                    }
                System.out.println(arrayListFreePlace + " : " + arrayListFreePlace.size());
                sendMessageChoiceFreePlace(chatId, arrayListFreePlace);

            }

            if (getData.substring(0, 5).equals("place")) {
                Integer place = Integer.parseInt(getData.substring(5));
                if (userRepository.getChatIdUserById(chatId) != null) {
                    addData.newUserBarrier(chatId, place);
                    sendMessage(chatId, "Вы выбрали место - " + EmojiParser.parseToUnicode("🚘") + "    " + getData.substring(5));
                    sendMessage(chatId, "Оплатите счет в размере " + money + " руб.");

//-----------------------get ------------------------------------------------------------------------------------------

                    System.out.println(countTimingArrayList + "----------------------------------------------------------");
                    payment(chatId, "Счёт",
                            "Оплатите за " + Integer.toString(countTimingArrayList.get(countTimingArrayList.size() - 1)) + " дней стоянки",
                            "Выставлен счет на оплату",
                            "1832575495:TEST:845cc76ac2a56fa83b2b0b34a811f9fa26743a05eacf0919000bd6f2ffed934d",
                            "RUB",
                            Collections.singletonList(new LabeledPrice("label", 100 * 100)));
//                            Collections.singletonList(new LabeledPrice("label", money * 100)));

                    addData.newPayment(chatId, place, countTimingArrayList.get(countTimingArrayList.size() - 1));
                    log.debug("Оплата прошла");
//                    dataBaseService.getUserBarrierById(chatId);
//                    userRepository.getUserBarrierById(chatId);

//----------------------- другое меню ------------------------------------------------------------------------------------
//                    sendMessage.setChatId(String.valueOf(chatId));
//                    sendMessage.setText("");
////                    sendMessage(chatId, "cccccc");
//                    MenuBot menuBot = new MenuBot();
//                    menuBot.openBarrier(sendMessage); //отправить вместе с сообщением меню
                } else {
                    sendMessage(chatId, "Скорее всего вы не подписали соглашение. Нажмите /start");

                }

            }


        }
    }

    @Override
    public void startMessage(long chatID, String name) {
        String answer = "Здравствуйте " + name + ".\n" + "Добро пожаловать на нашу парковку. " +
                        "Перед началом аренды, пожалуйста, ознакомьтесь с нашей офертой и " +
                        "согласитесь с ней для продолжения.";
        SendDocument sendDocument = document(chatID, "http://test.school89.net/wp-content/uploads/2023/07/public_contract_foras.pdf", answer);

        //Добавить меню с сообщением
        MenuBot menuBot = new MenuBot();
        menuBot.doingAcceptContractMenu(sendDocument); //отправить вместе с сообщением меню

        executeDocument(sendDocument);
        log.debug(answer + ", " + chatID);
    }

    @Override
    public void openMessage(long chatID) {
        String answer = "Открываю";
        sendMessage(chatID, answer);
        log.debug(answer + "  sendMessage    " + sendMessage);
    }

    @Override
    public void sendMessage(long chatID, String textToSend) {
        sendMessage.setChatId(String.valueOf(chatID));
        sendMessage.setText(textToSend);

        executeMessage(sendMessage);
    }

    @Override
    public void registerUser(Long chatId) {
        addData.registerUser(chatId);
    }

    @Override
    public void sendMessageTiming(Long chatId) {
        String text = "1 день - 300 руб.\n" +
                      "7 дней - 2000\n" +
                      "10 дней -2500\n" +
                      "15 дней - 3500\n" +
                      "1 месяц- 6000";
        MenuBot menuBot = new MenuBot();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        menuBot.timing(sendMessage); //отправить вместе с сообщением меню

        executeMessage(sendMessage);
    }

    public void sendMessageTimingForRenting(Long chatId) {
        String text = "1 день - 300 руб.\n" +
                      "7 дней - 2000\n" +
                      "10 дней -2500\n" +
                      "15 дней - 3500\n" +
                      "1 месяц- 6000";
        MenuBot menuBot = new MenuBot();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        menuBot.timingForRenting(sendMessage); //отправить вместе с сообщением меню

        executeMessage(sendMessage);
    }

    @Override
    public void sendLocalPhoto(String chatId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile("http://test.school89.net/wp-content/uploads/2023/08/scheme_one_foras.jpg"));
//        sendPhoto.setCaption("Выберите пожалуйста место");

        executePhoto(sendPhoto);
        log.debug(chatId);
    }

    @Override
    public void sendMessageChoiceFreePlace(Long chatId, ArrayList arrayListFreePlace) {
        String text = "Выбирай";
        MenuBot menuBot = new MenuBot();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        menuBot.choiceFreePlace(sendMessage, arrayListFreePlace); //отправить вместе с сообщением меню
        System.out.println(sendMessage);

        executeMessage(sendMessage);
    }

    @Override
    public boolean collOnBarrier(String urlCollCenter, String campaign_id, String phone, String public_key) {
        URL url = null;
        try {
            url = new URL(urlCollCenter +
                          "campaign_id=" +
                          campaign_id +
                          "&phone=%2B7" +
                          phone +
                          "&public_key=" +
                          public_key);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        URLConnection connection = null;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean payment(Long chatId, String title, String description, String payload, String providerToken,
                           String Currency, List<LabeledPrice> prices) {
        SendInvoice sendInvoice = new SendInvoice();
        sendInvoice.setChatId(chatId);
        sendInvoice.setTitle(title);
        sendInvoice.setDescription(description);
        sendInvoice.setPayload(payload);
        sendInvoice.setProviderToken(providerToken);
        sendInvoice.setCurrency(Currency);
        sendInvoice.setPrices(prices);
//        sendInvoice.setSt

        try {
            execute(sendInvoice);
        } catch (TelegramApiException e) {
            sendMessage(chatId, "Счёт не выставлен. Попробуйте еще.");
            throw new RuntimeException(e);
        }
        System.out.println(sendInvoice);

        return true;
    }

    @Override
    public SendDocument document(Long chatId, String url, String captionText) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(new InputFile("http://test.school89.net/wp-content/uploads/2023/07/public_contract_foras.pdf"));
        sendDocument.setCaption(captionText);
        return sendDocument;
    }

    @Override
    public Duration compareTime(LocalDateTime nowTime, LocalDateTime startTime) {
        Duration duration = Duration.between(nowTime, startTime);


//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//        String ttt = "2018-05-11 00:46";
//        LocalDateTime start = LocalDateTime.parse(ttt, formatter);
//        LocalDateTime end = LocalDateTime.parse("2016-05-10 12:26", formatter);
//
////                            LocalDateTime start = LocalDateTime.now();
////                            LocalDateTime end = LocalDateTime.parse("2016-05-10 12:26", formatter);
//
//        Duration duration = Duration.between(start, end);
////
//        System.out.printf(
//                "%dд %dч %dмин%n",
//                duration.toDays(),
//                duration.toHours() % 24,
//                duration.toMinutes() % 60
//        );ё
        return duration;
    }

    public User myRents(Long chatId) {
        return dataBaseService.getUserBarrierById(chatId);
    }
}
