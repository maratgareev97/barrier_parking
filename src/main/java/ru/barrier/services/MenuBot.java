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

    public SendMessage openBarrier(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup1 = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        replyKeyboardMarkup1.setResizeKeyboard(Boolean.TRUE);

        String rentEmoji = EmojiParser.parseToUnicode("📌");
        row.add(rentEmoji + " Арендовать место");
        keyboardRows.add(row);

        row = new KeyboardRow();
        String contractEmoji = EmojiParser.parseToUnicode("🤝");
        row.add(contractEmoji + " Соглашение");


        keyboardRows.add(row);

        replyKeyboardMarkup1.setKeyboard(keyboardRows);

        sendMessage.setReplyMarkup(replyKeyboardMarkup1);
        return sendMessage;
    }

    public SendMessage baseMenu(SendMessage sendMessage) {

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        replyKeyboardMarkup.setResizeKeyboard(Boolean.TRUE);

        String openBarrierEmoji = EmojiParser.parseToUnicode("🚘");
        row.add(openBarrierEmoji + " ОТКРЫТЬ ШЛАГБАУМ");
        keyboardRows.add(row);

        row = new KeyboardRow();
//        String contractEmoji = EmojiParser.parseToUnicode("🤝");
//        row.add(contractEmoji + " Соглашение");
        String rentEmoji = EmojiParser.parseToUnicode("📌");
        row.add(rentEmoji + " Арендовать место");
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
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

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
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineOneDay = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineSevenDay = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineTenDay = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineDayFifteenDay = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineOneMonth = new ArrayList<>();

        var oneDayButton = new InlineKeyboardButton();
        oneDayButton.setText("1 день");
        oneDayButton.setCallbackData("oneDay");

        var sevenDayButton = new InlineKeyboardButton();
        sevenDayButton.setText("7 дней");
        sevenDayButton.setCallbackData("sevenDay");

        var tenDayButton = new InlineKeyboardButton();
        tenDayButton.setText("10 дней");
        tenDayButton.setCallbackData("tenDay");

        var fifteenDayButton = new InlineKeyboardButton();
        fifteenDayButton.setText("15 дней");
        fifteenDayButton.setCallbackData("fifteenDay");

        var oneMonthButton = new InlineKeyboardButton();
        oneMonthButton.setText("1 месяц");
        oneMonthButton.setCallbackData("oneMonth");

        rowInLineOneDay.add(oneDayButton);
        rowsInLine.add(rowInLineOneDay);

        rowInLineSevenDay.add(sevenDayButton);
        rowsInLine.add(rowInLineSevenDay);

        rowInLineTenDay.add(tenDayButton);
        rowsInLine.add(rowInLineTenDay);

        rowInLineDayFifteenDay.add(fifteenDayButton);
        rowsInLine.add(rowInLineDayFifteenDay);

        rowInLineOneMonth.add(oneMonthButton);
        rowsInLine.add(rowInLineOneMonth);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }

    public SendMessage timingForRenting(SendMessage sendMessage) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineOneDay = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineSevenDay = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineTenDay = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineDayFifteenDay = new ArrayList<>();
        List<InlineKeyboardButton> rowInLineOneMonth = new ArrayList<>();

        var oneDayButton = new InlineKeyboardButton();
        oneDayButton.setText("1 день");
        oneDayButton.setCallbackData("oneDayRenting");

        var sevenDayButton = new InlineKeyboardButton();
        sevenDayButton.setText("7 дней");
        sevenDayButton.setCallbackData("sevenDayRenting");

        var tenDayButton = new InlineKeyboardButton();
        tenDayButton.setText("10 дней");
        tenDayButton.setCallbackData("tenDayRenting");

        var fifteenDayButton = new InlineKeyboardButton();
        fifteenDayButton.setText("15 дней");
        fifteenDayButton.setCallbackData("fifteenDayRenting");

        var oneMonthButton = new InlineKeyboardButton();
        oneMonthButton.setText("1 месяц");
        oneMonthButton.setCallbackData("oneMonthRenting");

        rowInLineOneDay.add(oneDayButton);
        rowsInLine.add(rowInLineOneDay);

        rowInLineSevenDay.add(sevenDayButton);
        rowsInLine.add(rowInLineSevenDay);

        rowInLineTenDay.add(tenDayButton);
        rowsInLine.add(rowInLineTenDay);

        rowInLineDayFifteenDay.add(fifteenDayButton);
        rowsInLine.add(rowInLineDayFifteenDay);

        rowInLineOneMonth.add(oneMonthButton);
        rowsInLine.add(rowInLineOneMonth);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }


    public SendMessage choiceFreePlace(SendMessage sendMessage, ArrayList arrayListFreePlace) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        var oneDayButton = new InlineKeyboardButton();

        Integer countPlace = 0;
        Integer buttonCountRow = arrayListFreePlace.size();
        for (int j = 0; j < buttonCountRow / 8 + 1; j++) {
            List<InlineKeyboardButton> rowInLineOneDay = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                if (countPlace < buttonCountRow) {
                    oneDayButton = new InlineKeyboardButton();
                    oneDayButton.setText(String.valueOf(arrayListFreePlace.get(countPlace)));
                    oneDayButton.setCallbackData("place" + String.valueOf(arrayListFreePlace.get(countPlace)));
                    rowInLineOneDay.add(oneDayButton);
                } else break;
                countPlace++;
            }
            rowsInLine.add(rowInLineOneDay);
        }

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }

    public SendMessage link(SendMessage sendMessage, String url, String textLink, String textOnButton) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        String emoji = EmojiParser.parseToUnicode("💳");
        yesButton.setText(emoji + textLink);
        yesButton.setCallbackData(textOnButton);
        yesButton.setUrl(url);
        rowInLine.add(yesButton);
        rowsInLine.add(rowInLine);
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

}
