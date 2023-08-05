package ru.barrier.services;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramBot {
    public void startMessage(long chatID, String name);
    public void registerUser(Long chatId);

//    public void timing();
}
