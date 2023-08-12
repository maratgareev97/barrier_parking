package ru.barrier.services;

import org.springframework.stereotype.Component;
import ru.barrier.models.User;

@Component
public interface DataBaseService {
    public User getUserBarrierById(Long chatId);
}
