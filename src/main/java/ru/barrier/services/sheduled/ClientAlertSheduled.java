package ru.barrier.services.sheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.barrier.repository.UserBarrierRepository;
import ru.barrier.services.TelegramBot;

import java.time.LocalDateTime;
import java.util.ArrayList;


@Component
@EnableScheduling
public class ClientAlertSheduled {

    private final UserBarrierRepository userBarrierRepository;

    private final TelegramBot telegramBot;

    public ClientAlertSheduled(UserBarrierRepository userBarrierRepository, TelegramBot telegramBot) {
        this.userBarrierRepository = userBarrierRepository;
        this.telegramBot = telegramBot;
    }

    @Scheduled(fixedRate = 10000)
    public void alertClientInOneHour() {
        ArrayList<Long> arrayList = (ArrayList<Long>) userBarrierRepository.getUserBarrierDataTime(LocalDateTime.now().plusHours(1));
        for (Long i : arrayList) {
            telegramBot.sendMessage(i, "Время стоянки заканчивается.");
        }
        System.out.println(arrayList);
        System.out.println("sheduled");
    }

}
