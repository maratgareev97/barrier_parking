package ru.barrier.services;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.barrier.models.User;
import ru.barrier.models.UserBarrier;
import ru.barrier.repository.AdminUsersRepository;
import ru.barrier.repository.CashPaymentRepository;
import ru.barrier.repository.UserBarrierRepository;
import ru.barrier.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Log4j
public class DataBaseServiceImpl implements DataBaseService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserBarrierRepository userBarrierRepository;

    @Autowired
    AdminUsersRepository adminUsersRepository;

    @Autowired
    CashPaymentRepository cashPaymentRepository;

    @Override
    public User getUserBarrierById(Long chatId) {
        return userRepository.getUserBarrierById(chatId);
    }

    @Override
    public Long getChatIdUserById(Long chatId) {
        return userRepository.getChatIdUserById(chatId);
    }

    @Override
    public User getUserById(Long chatId) {
        return userRepository.getUserById(chatId);
    }

    @Override
    public LocalDateTime getDateNextPayment(Long chatId) {
        return userRepository.getDateNextPayment(chatId);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll().stream().toList();
    }

    @Override
    public List<UserBarrier> getAllUsersBarrier() {
        return userBarrierRepository.findAll().stream().toList();
    }

    @Override
    public Long getAdminUsersByChatId(Long chatId) {
        return adminUsersRepository.getAdminUsersByChatId(chatId);
    }

    @Override
    public Long getCashPayment(Integer chatId) {
        return cashPaymentRepository.getCashPaymentBy(chatId);
    }

    @Override
    public void truncateTableAdminUsers() {
        adminUsersRepository.truncateTableAdminUsers();
    }


   @Override
    public void deleteUserBarrierById(Long chatId) {
        userBarrierRepository.deleteById(chatId);
    }

}
