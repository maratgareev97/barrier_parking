package ru.barrier.services;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import com.vdurmont.emoji.EmojiParser;

import java.util.ArrayList;
import java.util.List;

public class MenuBot {
    // Для обычных кнопок
//    private ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
//
//    private List<KeyboardRow> keyboardRows = new ArrayList<>();
//    private KeyboardRow row = new KeyboardRow();
    // Для кнопок под сообщением
    private InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    private List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
    List<InlineKeyboardButton> rowInLine = new ArrayList<>();

    public SendMessage baseMenu(SendMessage sendMessage) {

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        replyKeyboardMarkup.setResizeKeyboard(Boolean.TRUE);

        String rentEmoji = EmojiParser.parseToUnicode("📌");
        row.add(rentEmoji + " Арендовать место");
        keyboardRows.add(row);

        row = new KeyboardRow();
        String contractEmoji = EmojiParser.parseToUnicode("🤝");
        row.add(contractEmoji + " Соглашение");
        String myRentsEmoji = EmojiParser.parseToUnicode("🛍");
        row.add(myRentsEmoji + " Мои аренды");
        String extendRentEmoji = EmojiParser.parseToUnicode("🕑");
        row.add(extendRentEmoji + " Продлить аренду");

        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }

    public SendDocument doingAcceptContractMenu(SendDocument sendMessage) {
        var yesButton = new InlineKeyboardButton();
        String emoji = EmojiParser.parseToUnicode("✅");
        yesButton.setText(emoji + "Принять соглашение");

        yesButton.setCallbackData("Accept");
        rowInLine.add(yesButton);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }

    public SendMessage timing(SendMessage sendMessage) {
        var oneDayButton = new InlineKeyboardButton();
        oneDayButton.setText("1 день");
        oneDayButton.setCallbackData("oneDay");

        var sevenDayButton = new InlineKeyboardButton();
        sevenDayButton.setText("7 дней");
        sevenDayButton.setCallbackData("sevenDay");

        rowInLine.add(oneDayButton);
        rowInLine.add(sevenDayButton);

        rowsInLine.add(rowInLine);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }


}
