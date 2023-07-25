package ru.barrier.services;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.barrier.configs.BotConfig;

@Component
@Log4j
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
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
                    break;
                default:
                    sendMessage(chatID, "Не поддерживается");
            }
        }
    }

    private void startMessage(long chatID, String name) {
        String answer = "Привет " + name + ". Рад тебя видеть";
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
}
