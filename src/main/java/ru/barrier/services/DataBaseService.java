package ru.barrier.services;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import ru.barrier.models.User;

import java.time.LocalDateTime;

@Component
public interface DataBaseService {
    public User getUserBarrierById(Long chatId);

    Long getChatIdUserById(@Param("chatId") Long chatId);

    LocalDateTime getDateNextPayment(@Param("chatId") Long chatId);
}
