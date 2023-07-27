package ru.barrier.services;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.barrier.configs.BotConfig;
import ru.barrier.models.User;
import ru.barrier.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@Log4j
public class TelegramBotImpl extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;
    final BotConfig config;

    public TelegramBotImpl(BotConfig config) {
        this.config = config;
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
        return config.getBotName();
    }

    public String getBotToken() {
        return config.getToken();
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageTest = update.getMessage().getText().toString();
            long chatID = update.getMessage().getChatId();
            switch (messageTest) {
                case "/start":
                    startMessage(chatID, update.getMessage().getChat().getFirstName());
                    registerCar(update.getMessage());
                    break;
                case "/open":
                    openMessage(chatID);
                    break;
                default:
                    sendMessage(chatID, "Не поддерживается");
            }
        }
    }

    private void startMessage(long chatID, String name) {
        String answer = "Привет " + name + ". Рад тебя видеть";
        sendMessage(chatID, answer);
        log.debug(answer + ", " + chatID);
    }

    private void openMessage(long chatID) {
        String answer = "Открываю";
        sendMessage(chatID, answer);
        log.debug(answer);
    }

    private void sendMessage(long chatID, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatID));
        sendMessage.setText(textToSend);

        try {
            execute(sendMessage);
            log.debug(textToSend);
        } catch (TelegramApiException e) {
            System.out.println("Не ушло");
        }
    }

    public void registerCar(Message message) {
        System.out.println(userRepository.findById(message.getChatId()).toString());
//        if (userRepository.findById(message.getChatId())).y
        var chat=message.getChat();
        var chatID = message.getChatId();
        User user = new User();
        user.setChatID(chatID);
        user.setNumberCar("123");
        user.setUse(true);
        userRepository.save(user);
    }

}
