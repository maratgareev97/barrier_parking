package ru.barrier.services;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;

import java.util.ArrayList;
import java.util.List;

public interface TelegramBot {
    public void startMessage(long chatID, String name);
    public void openMessage(long chatID);
    public void sendMessage(long chatID, String textToSend);
    public void registerUser(Long chatId);
    public void sendMessageTiming(Long chatId);
    public void sendLocalPhoto(String chatId);
    public void sendMessageChoiceFreePlace(Long chatId, ArrayList arrayListFreePlace);
    public boolean collOnBarrier(String urlCollCenter, String campaign_id, String phone, String public_key);

    public boolean payment(Long chatId, String title, String description, String payload, String providerToken,
                           String Currency, List<LabeledPrice> prices);
}
