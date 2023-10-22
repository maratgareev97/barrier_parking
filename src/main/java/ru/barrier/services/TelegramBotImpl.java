package ru.barrier.services;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import okhttp3.*;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.barrier.configs.BotConfig;
import ru.barrier.models.User;
import ru.barrier.models.UserBarrier;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@Log4j
public class TelegramBotImpl extends TelegramLongPollingBot implements TelegramBot {
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
            log.error("no SendDocument: " + new RuntimeException(e));
            throw new RuntimeException(e);
        }
    }

    private Properties getWorkProperties() throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream("src/main/resources/work.properties"));
        return props;
    }

    @Override
    public void executeMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("no SendMessage: " + new RuntimeException(e));
            throw new RuntimeException(e);
        }
    }

    private void executePhoto(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("no SendPhoto: " + new RuntimeException(e));
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
            User user = dataBaseService.getUserById(chatID);
            switch (messageTest) {
                case "/start":
//                    User user = dataBaseService.getUserById(chatID);
                    if (user != null) {
                        if (user.getUserBarrier() != null && user.getUserBarrier().getDateTimeNextPayment() != null) {
                            sendMessage(chatID, "У Вас имеется действующая аренда. Вы можете продлить аренду.");
                            log.debug(chatID + "  У Вас имеется действующая аренда. Вы можете продлить аренду.");
                        }
                    } else
                        startMessage(chatID, update.getMessage().getChat().getFirstName());
                    break;
                case "1":
                    if (dataBaseService.getAdminUsersByChatId(chatID) != null) {
                        addData.cashPayment(1);
                        log.debug(chatID + " Можно оплатить");
                        sendMessage(chatID, "Можно оплатить");
                    }
                    break;
                case "0":
                    if (dataBaseService.getAdminUsersByChatId(chatID) != null) {
                        addData.cashPayment(0);
                        log.debug(chatID + " Оплата запрещена");
                        sendMessage(chatID, "Оплата запрещена");
                    }
                    break;
                case "/admin list all place":
//                    sendMessage(chatID, dataBaseService.getAllUsers().stream().toList().toString());
                    List<UserBarrier> userBarrierList = dataBaseService.getAllUsersBarrier().stream().toList();
                    for (UserBarrier i : userBarrierList) {
                        sendMessage(chatID, "ID: " + i.getChatId() + "\n" +
                                            "Имя: " + i.getName() + "\n" +
                                            "дни: " + i.getAmountOfDays() + "\n" +
                                            "от: " + i.getDateTimeLastPayment() + "\n" +
                                            "до: " + i.getDateTimeNextPayment() + "\n" +
                                            "место: " + i.getParkingPlace() + "\n" +
                                            "на территории или нет: " + i.getStoppedBy());
                    }
                    break;
                case "/agreement":
                    executeDocument(document(chatID,
                            "http://test.school89.net/wp-content/uploads/2023/07/public_contract_foras.pdf",
                            ""));
                    break;
                default:
                    if (messageTest.equals(rentEmoji + " Арендовать место")) {
//                        User user = dataBaseService.getUserById(chatID);
                        if (user != null) {
                            if (user.getUserBarrier() != null && user.getUserBarrier().getDateTimeNextPayment() != null) {
                                sendMessage(chatID, "У Вас имеется действующая аренда. Вы можете продлить аренду.");
                                log.debug(chatID + "  У Вас имеется действующая аренда. Вы можете продлить аренду.");
                            } else sendMessageTiming(chatID);
                        } else {
                            sendMessage(chatID, "Вас нет в базе");
                            sendMessage(chatID, "Нажмите /start");
                            log.debug(chatID + "   Вас нет в базе");
                        }
                    } else if (messageTest.equals(openBarrierEmoji + " ОТКРЫТЬ ШЛАГБАУМ")) {
                        if (user == null) {
                            sendMessage(chatID, "Оплатите парковку");
                            log.debug(chatID + "  Оплатите парковку");
                        } else {
                            LocalDateTime localDateTime = dataBaseService.getDateNextPayment(chatID);
//                            Integer stoppedBy = dataBaseService.getUserById(chatID).getUserBarrier().getStoppedBy();
                            if (localDateTime != null) {
                                Duration duration = compareTime(LocalDateTime.now(), localDateTime);
                                if (duration.toDays() >= 0 && duration.toHours() % 24 >= 0 && duration.toMinutes() >= 0) {
                                    String numberPhoneBarrier = "";
                                    try {
                                        numberPhoneBarrier = getWorkProperties().getProperty("numberPhoneBarrier");
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    try {
                                        collOnBarrier("https://zvonok.com/manager/cabapi_external/api/v1/phones/call/?",
                                                "215654108",
                                                numberPhoneBarrier,
                                                "bbc1cbcde48564215c0b78b649081cac");
                                    } catch (Exception e) {
                                        log.error("Звонок не прошел");
                                    }
                                    ;

                                    if (dataBaseService.getUserById(chatID).getUserBarrier().getStoppedBy() == 0) {
                                        addData.stoppedByT(dataBaseService.getUserById(chatID), 1);
                                        sendMessage(chatID, "ЗАЕЗЖАЙТЕ!\nчерез несколько секунд откроется шлагбаум");
                                        log.debug(chatID + "    ЗАЕЗЖАЙТЕ!\nчерез несколько секунд откроется шлагбаум");
                                    } else {
                                        addData.stoppedByT(dataBaseService.getUserById(chatID), 0);
                                        sendMessage(chatID, "ВЫЕЗЖАЙТЕ!\nчерез несколько секунд откроется шлагбаум");
                                        log.debug(chatID + "    ЗАЕЗЖАЙТЕ!\nчерез несколько секунд откроется шлагбаум");
                                    }

                                } else {
                                    sendMessage(chatID, "Оплатите парковку");
                                    log.debug(chatID + "  Оплатите парковку");
                                }
                            } else {
                                sendMessage(chatID, "Оплатите парковку");
                                log.debug(chatID + "  Оплатите парковку");
                            }
                        }
                    } else if (messageTest.equals(myRentsEmoji + " Мои аренды")) {

//                        User user = dataBaseService.getUserBarrierById(chatID).getUserBarrier().getUser();
//                        User user = dataBaseService.getUserById(chatID);
                        if (user != null && user.getUserBarrier() != null && user.getUserBarrier().getDateTimeNextPayment() != null) {
                            LocalDateTime localDateTime = user.getUserBarrier().getDateTimeNextPayment();
                            Duration duration = compareTime(LocalDateTime.now(), localDateTime);
                            sendMessage(chatID,
                                    "Ваше парковочное место №" +
                                    Integer.toString(user.getUserBarrier().getParkingPlace()) + "\n" +
                                    "Дата окончания аренды через: " + duration.toDays() + "дн, " +
                                    duration.toHours() % 24 + " час, " +
                                    duration.toMinutes() % 60 + "мин.");
                            log.debug(chatID +
                                      " Ваше парковочное место №" +
                                      Integer.toString(user.getUserBarrier().getParkingPlace()) + "\n" +
                                      "Дата окончания аренды через: " + duration.toDays() + "дн, " +
                                      duration.toHours() % 24 + " час, " +
                                      duration.toMinutes() % 60 + "мин.");
                        } else {
                            sendMessage(chatID, "Оплатите парковку");
                            log.debug(chatID + "  Оплатите парковку");
                        }
                    } else if (messageTest.equals(extendRentEmoji + " Продлить аренду")) {


//                        ------------------------------------------------------------------------------------------------------------------------
//                        baseMethodPayment(chatID, 1);


//----------------------------------------

//                        User user = dataBaseService.getUserById(chatID);
                        if (user != null && user.getUserBarrier() != null && user.getUserBarrier().getDateTimeNextPayment() != null) {
                            sendMessageTimingForRenting(chatID);
                        } else {
                            sendMessage(chatID, "Оплатите парковку");
                            log.debug(chatID + "  Оплатите парковку");
                        }
                    } else if (messageTest.startsWith("add admin user:")) {
                        addData.addAdminUsers(Long.valueOf(messageTest.substring(16)));

                        sendMessage(chatID, "Вы добавили " + messageTest.substring(16));
                    } else if (messageTest.equals("delete all admin users")) {
                        dataBaseService.truncateTableAdminUsers();
                        sendMessage(chatID, "Все админы удалены");
                    } else if (messageTest.startsWith("delete id:") && dataBaseService.getAdminUsersByChatId(chatID) != null) {
                        try {
                            dataBaseService.deleteUserBarrierById(Long.valueOf(messageTest.substring(11)));
                            sendMessage(chatID, "Арендартор c ID: " + messageTest.substring(11) + " удален");
                        } catch (Exception e) {
                            sendMessage(chatID, "Нет такого ID");
                        }
                    } else {
                        sendMessage(chatID, "Оплатите парковку");
                        log.debug(chatID + "  Оплатите парковку");
                    }

            }

        }
//        byte countTiming = 0;
        if (update.hasCallbackQuery()) {
            boolean choicePlace = false;

            Long chatId = update.getCallbackQuery().getFrom().getId();

            String getData = update.getCallbackQuery().getData().toString();

            Integer countTiming = 0;
            Integer countTimingRenting = 0;

            LocalDateTime localDateTime = dataBaseService.getDateNextPayment(chatId);

            if (update.getCallbackQuery().getData().toString().equals("Accept")) {

                if (dataBaseService.getChatIdUserById(chatId) == null) {
                    registerUser(update.getCallbackQuery().getFrom().getId());
                }

                // добавить основное меню
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("");

                MenuBot menuBot = new MenuBot();
                menuBot.baseMenu(sendMessage);
//                    sendMessage(chatId, "MENU");
//                } else {
//                    MenuBot menuBot = new MenuBot();
//                    menuBot.openBarrier(sendMessage);
//                }
                sendMessage(chatId, "Теперь Вы можете оплатить услугу");
                log.debug(chatId + "  Теперь Вы можете оплатить услугу");
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
                money = 5000;
            }


            if (update.getCallbackQuery().getData().toString().equals("oneDayRenting")) {
                sendMessage(chatId, "Один день");
                countTimingRenting = 1;
                countTimingArrayList.add(countTimingRenting);
                money = 300;

            }
            if (update.getCallbackQuery().getData().toString().equals("sevenDayRenting")) {
                sendMessage(chatId, "Семь дней");
                countTimingRenting = 7;
                countTimingArrayList.add(countTimingRenting);
                money = 2000;
            }
            if (update.getCallbackQuery().getData().toString().equals("tenDayRenting")) {
                sendMessage(chatId, "10 дней");
                countTimingRenting = 10;
                countTimingArrayList.add(countTimingRenting);
                money = 2500;
            }
            if (update.getCallbackQuery().getData().toString().equals("fifteenDayRenting")) {
                sendMessage(chatId, "15 дней");
                countTimingRenting = 15;
                countTimingArrayList.add(countTimingRenting);
                money = 3500;
            }
            if (update.getCallbackQuery().getData().toString().equals("oneMonthRenting")) {
                sendMessage(chatId, "1 месяц");
                countTimingRenting = 30;
                countTimingArrayList.add(countTimingRenting);
                money = 5000;
            }

            if (update.getCallbackQuery().getData().toString().equals("cashPayment")) {
                if (dataBaseService.getCashPayment(1) != null && dataBaseService.getCashPayment(1) == 1) {
                    User user = dataBaseService.getUserById(chatId);
                    LocalDateTime dataTimeLastPayment = user.getUserBarrier().getDateTimeLastPayment();
                    LocalDateTime dataTimeNextPayment = user.getUserBarrier().getDateTimeNextPayment();
                    if (dataTimeLastPayment != null) {
                        dataTimeNextPayment = dataTimeNextPayment.plusDays(countTimingArrayList.get(countTimingArrayList.size() - 1));
                    } else {
                        dataTimeLastPayment = LocalDateTime.now();
                        dataTimeNextPayment = LocalDateTime.now().plusDays(countTimingArrayList.get(countTimingArrayList.size() - 1));
                    }
                    addData.newUserBarrier(chatId,
                            user.getUserBarrier().getName(),
                            user.getUserBarrier().getParkingPlace(),
                            countTimingArrayList.get(countTimingArrayList.size() - 1),
                            dataTimeLastPayment,
                            dataTimeNextPayment);

                    addData.cashPayment(0);
                    log.debug(chatId + " Оплачено наличными");
                    sendMessage(chatId, "Оплачено наличными");
                } else sendMessage(chatId, "Обратитесь к администратору");
            }

            User user = dataBaseService.getUserById(chatId);
            if (countTimingRenting != 0 && user != null && user.getUserBarrier() != null && user.getUserBarrier().getDateTimeNextPayment() != null) {

                LocalDateTime localDateTimeNew = user.getUserBarrier().getDateTimeNextPayment().plusDays(countTimingRenting);
                baseMethodPayment(chatId, null, countTimingRenting, money, localDateTimeNew, "add");
            }


            if (countTiming != 0) {
                sendLocalPhoto(String.valueOf(chatId));
//                addData.newUserTest(); //--------------------------------------------- тестовые данные ------------------
                List<User> users = dataBaseService.getAllUsers();
                choicePlace = true;
            }

            if (choicePlace == true) {
                List<UserBarrier> listBusyPlace = dataBaseService.getAllUsersBarrier();
                ArrayList<Integer> arrayListBusyPlace = new ArrayList<>();
                for (int i = 0; i < listBusyPlace.size(); i++) {
                    arrayListBusyPlace.add(listBusyPlace.get(i).getParkingPlace());
                }
                Collections.sort(arrayListBusyPlace);

                Integer differenceValue = 0;
                ArrayList<Integer> arrayListFreePlace = new ArrayList<>();

                if (arrayListBusyPlace.size() == 0) {
                    for (int i = 0; i < 80; i++) {
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
                // заполнение после последнего
                if (arrayListBusyPlace.size() != 0)
                    if (arrayListBusyPlace.get(arrayListBusyPlace.size() - 1) != 80) {
                        for (int i = arrayListBusyPlace.get(arrayListBusyPlace.size() - 1); i < 80; i++) {
                            arrayListFreePlace.add(i + 1);
                        }
                    }
                sendMessageChoiceFreePlace(chatId, arrayListFreePlace);

            }

            if (getData.substring(0, 5).equals("place")) {
                Integer place = Integer.parseInt(getData.substring(5));
                if (dataBaseService.getChatIdUserById(chatId) != null) {
                    addData.newUserBarrier(chatId, String.valueOf(update.getCallbackQuery().getFrom().getFirstName()), place, 0, null, null);
                    sendMessage(chatId, "Вы выбрали место - " + EmojiParser.parseToUnicode("🚘") + "    " + getData.substring(5));
                    log.debug(chatId + "  Вы выбрали место - " + EmojiParser.parseToUnicode("🚘") + "    " + getData.substring(5));
                    sendMessage(chatId, "Оплатите счет в размере " + money + " руб.");
                    log.debug(chatId + "  Оплатите счет в размере " + money + " руб.");

//-----------------------get ------------------------------------------------------------------------------------------
                    baseMethodPayment(chatId, place, countTimingArrayList.get(countTimingArrayList.size() - 1), money, null, "new");

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
                      "1 месяц- 5000";
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
                      "1 месяц- 5000";
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
        sendPhoto.setPhoto(new InputFile("http://test.school89.net/wp-content/uploads/2023/09/public_contract_foras.jpg"));

        executePhoto(sendPhoto);
    }

    @Override
    public void sendMessageChoiceFreePlace(Long chatId, ArrayList arrayListFreePlace) {
        String text = "Выберите парковочное место:";
        MenuBot menuBot = new MenuBot();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        menuBot.choiceFreePlace(sendMessage, arrayListFreePlace); //отправить вместе с сообщением меню

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
            log.error("no creat url for coll: " + new RuntimeException(e));
            throw new RuntimeException(e);
        }
        URLConnection connection = null;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            log.error("no creat connection coll: " + new RuntimeException(e));
            throw new RuntimeException(e);
        }

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                log.debug("collOnBarrier " + line);
            }
        } catch (IOException e) {
            log.error("no colling: " + new RuntimeException(e));
            throw new RuntimeException(e);
        }
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
        return duration;
    }

    @Override
    public void baseMethodPayment(Long chatId, Integer parkingPlace, Integer amountOfDays, Integer money,
                                  LocalDateTime dataTimeNextPayment, String newOrAdd) {
        String urlServer = "";
        try {
            urlServer = getWorkProperties().getProperty("confirmation_url");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Payment payment = new Payment();
        Response response = payment.creatingPayment(money);
        String result = null;
        try {
            result = response.body().string();
        } catch (IOException e) {
            log.error("не получен ответ при формировании счета: " + new RuntimeException(e));
            throw new RuntimeException(e);
        }
        String confirmation = payment.parserJson(result, "confirmation");

        String idPayment = payment.parserJson(result, "id");
        addData.newPayment(chatId, idPayment);

        SendMessage sendMessage1 = new SendMessage();
        sendMessage1.setChatId(chatId);

        String confirmation_url = "";
        try {
            confirmation_url = payment.parserJson(confirmation, "confirmation_url").substring(8);
//            https://yoomoney.ru/checkout/payments/v2/contract?orderId=2c804aee-000f-5000-8000-137e76042135
            confirmation_url = urlServer + idPayment + "/" + chatId;
            sendMessage1.setText("Теперь вы можете оплатить счёт:");
            sendMessage1.setParseMode("HTML");
            MenuBot menuBot = new MenuBot();
            menuBot.link(sendMessage1, confirmation_url, "Оплатить", "О");
            executeMessage(sendMessage1);
            log.debug(sendMessage1);
            //           Запрос на существование счета
            if (newOrAdd.equals("new")) {
                ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(4);
                scheduledExecutorService.schedule(new Payment(chatId, parkingPlace, amountOfDays, idPayment, money, "new"), 1, TimeUnit.SECONDS);
                scheduledExecutorService.shutdown();
            }
            if (newOrAdd.equals("add")) {
                ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(4);
                scheduledExecutorService.schedule(new Payment(chatId, amountOfDays, idPayment, money, dataTimeNextPayment, "add"), 1, TimeUnit.SECONDS);
                scheduledExecutorService.shutdown();
            }

        } catch (Exception e) {
            log.error("Счет устарел. Создайте пожалуйста новый");
            sendMessage(chatId, "Счет устарел. Создайте пожалуйста новый");
        }

    }
}
