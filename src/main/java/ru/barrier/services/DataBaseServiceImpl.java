package ru.barrier.services;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.barrier.models.User;
import ru.barrier.repository.UserRepository;

import java.time.LocalDateTime;

@Component
@Log4j
public class DataBaseServiceImpl implements DataBaseService {

    @Autowired
    UserRepository userRepository;

    @Override
    public User getUserBarrierById(Long chatId) {
        return userRepository.getUserBarrierById(chatId);
    }

    @Override
    public Long getChatIdUserById(Long chatId) {
        return userRepository.getChatIdUserById(chatId);
    }

    @Override
    public LocalDateTime getDateNextPayment(Long chatId) {
        return userRepository.getDateNextPayment(chatId);
    }


}