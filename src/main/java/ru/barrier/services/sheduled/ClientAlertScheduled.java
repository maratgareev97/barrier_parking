package ru.barrier.services.sheduled;

import lombok.extern.log4j.Log4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.barrier.repository.AllPaymentsRepository;
import ru.barrier.repository.UserBarrierRepository;
import ru.barrier.repository.UserRepository;
import ru.barrier.services.TelegramBot;

import java.time.LocalDateTime;
import java.util.ArrayList;


@Component
@EnableScheduling
@Log4j
public class ClientAlertScheduled {

    private final UserBarrierRepository userBarrierRepository;
    private final UserRepository userRepository;

    private final TelegramBot telegramBot;
    private final AllPaymentsRepository allPaymentsRepository;

    public ClientAlertScheduled(UserBarrierRepository userBarrierRepository, UserRepository userRepository, TelegramBot telegramBot, AllPaymentsRepository allPaymentsRepository) {
        this.userBarrierRepository = userBarrierRepository;
        this.userRepository = userRepository;
        this.telegramBot = telegramBot;
        this.allPaymentsRepository = allPaymentsRepository;
    }

    @Scheduled(fixedRate = 900000)
    public void alertClientInOneHour() {
        try {
            ArrayList<Long> arrayList1 = (ArrayList<Long>) userBarrierRepository.getUserByEndTime(LocalDateTime.now());
            for (Long chatId : arrayList1) {
                userRepository.deleteById(chatId);
                telegramBot.sendMessage(chatId, "Время стоянки закончилось.\nБлагодарим Вас.\nДля возобновления использования наших услуг нажмите\n/start");
            }
        } catch (Exception e) {
            log.debug("Scheduled. При удалении пользователя из базы что-то пошло не так");
        }
    }

    @Scheduled(fixedRate = 3200000)
    public void alertClientInFifteenMinutes() {
        System.out.println("15");
        ArrayList<Long> arrayList = (ArrayList<Long>) userBarrierRepository.getUserBarrierDataTime(LocalDateTime.now().plusHours(1));
        for (Long chatId : arrayList) {
            try {
                telegramBot.sendMessage(chatId, "Время стоянки заканчивается.");
            } catch (Exception e) {
                log.debug("Scheduled. Нет такого контакта");
            }
        }
    }

}
