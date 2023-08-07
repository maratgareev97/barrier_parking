package ru.barrier.services;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
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
import ru.barrier.repository.UserBarrierRepository;
import ru.barrier.repository.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Log4j
public class TelegramBotImpl extends TelegramLongPollingBot implements TelegramBot {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserBarrierRepository userBarrierRepository;

    @Autowired
    private AddDataTest addDataTest;

    final BotConfig botConfig;

    SendMessage sendMessage = new SendMessage();
    MenuBot menuBot = new MenuBot();


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
                    break;
                case "/open":
                    openMessage(chatID);
                    break;
                default:
                    if (messageTest.equals(rentEmoji + " Арендовать место")) {
                        timing(chatID);
                    } else {
                        sendMessage(chatID, "Не поддерживается");
                    }
            }

        }
        if (update.hasCallbackQuery()) {
            boolean choosePlace = false;

            Long chatId = update.getCallbackQuery().getFrom().getId();

            System.out.println("!!!!!!!!!!   " + update.hasCallbackQuery());
            System.out.println("?????????????   " + update.getCallbackQuery().getData().toString());
            log.debug(update.getCallbackQuery().getFrom().getUserName());
            log.debug(update.getCallbackQuery().getFrom().getFirstName());
            log.debug(chatId);

            byte countTiming = 0;

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
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("");
                sendMessage(chatId, "Один день");
                countTiming = 1;
            }
            if (update.getCallbackQuery().getData().toString().equals("sevenDay")) {
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("");
                sendMessage(chatId, "Семь дней");
                countTiming = 7;
            }
            if (update.getCallbackQuery().getData().toString().equals("tenDay")) {
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("");
                sendMessage(chatId, "10 дней");
                countTiming = 10;
            }
            if (update.getCallbackQuery().getData().toString().equals("fifteenDay")) {
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("");
                sendMessage(chatId, "15 дней");
                countTiming = 15;
            }
            if (update.getCallbackQuery().getData().toString().equals("oneMonth")) {
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("");
                sendMessage(chatId, "1 месяц");
                countTiming = 30;
            }
            if (countTiming != 0) {
                log.debug("countTiming = " + countTiming);
                sendLocalPhoto(String.valueOf(chatId));
                addDataTest.newUser();
                log.debug("Тестовые данные загружены");
                List<User> users = userRepository.findAll().stream().toList();
//                for (int i = 0; i < users.size(); i++) {
//                    System.out.println(users.get(i).getChatId());
//                }
                choosePlace = true;
                System.out.println("countTiming = " + countTiming + "    choosePlace = " + choosePlace);
            }
            if (choosePlace == true) {
                List<UserBarrier> listBasyPlace = userBarrierRepository.findAll().stream().toList();

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

        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        log.debug(answer + ", " + chatID);
    }

    private void openMessage(long chatID) {
        String answer = "Открываю";
        sendMessage(chatID, answer);
        log.debug(answer + "  sendMessage    " + sendMessage);
    }

    private void sendMessage(long chatID, String textToSend) {
        sendMessage.setChatId(String.valueOf(chatID));
        sendMessage.setText(textToSend);

        try {
            execute(sendMessage);
            log.debug(textToSend);
        } catch (TelegramApiException e) {
            System.out.println("Не ушло");
        }
    }

    @Override
    public void registerUser(Long chatId) {
        User user = new User();
        user.setChatId(chatId);
        userRepository.save(user);
    }

    public void timing(Long chatId) {
        String text = "1 день - 300 руб.\n" +
                      "7 дней - 2000\n" +
                      "10 дней -2500\n" +
                      "15 дней - 3500\n" +
                      "1 месяц- 6000";
        MenuBot menuBot = new MenuBot();
        SendMessage sendMessage1 = new SendMessage();
        sendMessage1.setChatId(chatId);
        sendMessage1.setText(text);
        menuBot.timing(sendMessage1); //отправить вместе с сообщением меню
//        sendMessage(chatId, text);
        try {
            execute(sendMessage1);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendLocalPhoto(String chatId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile("http://test.school89.net/wp-content/uploads/2023/08/scheme_one_foras.jpg"));
        sendPhoto.setCaption("Выберите пожалуйста место");
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        log.debug(chatId);
    }


}
