package ru.barrier.services;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.barrier.configs.BotConfig;
import ru.barrier.models.User;
import ru.barrier.models.UserBarrier;
import ru.barrier.repository.UserBarrierRepository;
import ru.barrier.repository.UserRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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

    final BotConfig botConfig;

    SendMessage sendMessage = new SendMessage();
    MenuBot menuBot = new MenuBot();

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

    void executeDocument(SendDocument sendDocument) {
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    void executeMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    void executePhoto(SendPhoto sendPhoto) {
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

            long chatID = update.getMessage().getChatId();
            switch (messageTest) {
                case "/start":
                    startMessage(chatID, update.getMessage().getChat().getFirstName());
                    break;
                case "Открыть шлагбаум":
                    sendMessage(chatID, "ОТКРЫВАЮ");
                    collOnBarrier("https://zvonok.com/manager/cabapi_external/api/v1/phones/call/?",
                            "1598159358",
                            "9153700127",
                            "bbc1cbcde48564215c0b78b649081cac");
                    break;
                case "/open":
                    openMessage(chatID);
                    break;
                default:
                    if (messageTest.equals(rentEmoji + " Арендовать место")) {
                        sendMessageTiming(chatID);
                    } else {
                        sendMessage(chatID, "Не поддерживается");
                    }
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

            if (update.getCallbackQuery().getData().toString().equals("Accept")) {

                registerUser(update.getCallbackQuery().getFrom().getId());

                // добавить основное меню
//                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("");
                menuBot.baseMenu(sendMessage); //отправить вместе с сообщением меню
                sendMessage(chatId, "Теперь Вы можете оплатить услугу");
            }

            if (update.getCallbackQuery().getData().toString().equals("oneDay")) {
//                sendMessage.setChatId(String.valueOf(chatId));
//                sendMessage.setText("");
                sendMessage(chatId, "Один день");
                countTiming = 1;
                countTimingArrayList.add(countTiming);
                money = 300;

            }
            if (update.getCallbackQuery().getData().toString().equals("sevenDay")) {
//                sendMessage.setChatId(String.valueOf(chatId));
//                sendMessage.setText("");
                sendMessage(chatId, "Семь дней");
                countTiming = 7;
                countTimingArrayList.add(countTiming);
                money = 2000;
            }
            if (update.getCallbackQuery().getData().toString().equals("tenDay")) {
//                sendMessage.setChatId(String.valueOf(chatId));
//                sendMessage.setText("");
                sendMessage(chatId, "10 дней");
                countTiming = 10;
                countTimingArrayList.add(countTiming);
                money = 2500;
            }
            if (update.getCallbackQuery().getData().toString().equals("fifteenDay")) {
//                sendMessage.setChatId(String.valueOf(chatId));
//                sendMessage.setText("");
                sendMessage(chatId, "15 дней");
                countTiming = 15;
                countTimingArrayList.add(countTiming);
                money = 3500;
            }
            if (update.getCallbackQuery().getData().toString().equals("oneMonth")) {
//                sendMessage.setChatId(String.valueOf(chatId));
//                sendMessage.setText("");
                sendMessage(chatId, "1 месяц");
                countTiming = 30;
                countTimingArrayList.add(countTiming);
                money = 6000;
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
                if (userRepository.getUserById(chatId) != null) {
                    addData.newUserBarrier(chatId, place);
                    sendMessage(chatId, "Вы выбрали место - " + EmojiParser.parseToUnicode("🚘") + "    " + getData.substring(5));
                    sendMessage(chatId, "Оплатите счет в размере " + money + " руб.");

//-----------------------get ------------------------------------------------------------------------------------------

                    System.out.println(countTimingArrayList + "----------------------------------------------------------");
                    payment(chatId, "Счёт",
                            "Оплатите за " + Integer.toString(countTimingArrayList.get(countTimingArrayList.size() - 1)) + " дней стоянки",
                            "Выставлен счет на оплату",
                            "381764678:TEST:62416",
                            "RUB",
                            Collections.singletonList(new LabeledPrice("label", money * 100)));

                    addData.newPayment(chatId, place, countTimingArrayList.get(countTimingArrayList.size() - 1));
                }

            }


        }
    }

    @Override
    public void startMessage(long chatID, String name) {
        String answer = "Здравствуйте " + name + ".\n" + "Добро пожаловать на нашу парковку. " +
                        "Перед началом аренды, пожалуйста, ознакомьтесь с нашей офертой и " +
                        "согласитесь с ней для продолжения.";
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatID);
        sendDocument.setDocument(new InputFile("http://test.school89.net/wp-content/uploads/2023/07/public_contract_foras.pdf"));
        sendDocument.setCaption(answer);

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
        User user = new User();
        user.setChatId(chatId);
        userRepository.save(user);
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

        try {
            execute(sendInvoice);
        } catch (TelegramApiException e) {
            sendMessage(chatId, "Счёт не выставлен. Попробуйте еще.");
            throw new RuntimeException(e);
        }
        System.out.println(sendInvoice);

        return true;
    }


}
