package ru.barrier.services;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import ru.barrier.models.AdminUsers;
import ru.barrier.models.User;
import ru.barrier.models.UserBarrier;

import java.time.LocalDateTime;
import java.util.List;

@Component
public interface DataBaseService {
    public User getUserBarrierById(Long chatId);

    Long getChatIdUserById(@Param("chatId") Long chatId);

    User getUserById(@Param("chatId") Long chatId);

    LocalDateTime getDateNextPayment(@Param("chatId") Long chatId);

    List<User> getAllUsers();

    List<UserBarrier> getAllUsersBarrier();

    Long getAdminUsersByChatId(Long chatId);

    Long getCashPayment(Integer chatId);

    void truncateTableAdminUsers();

    void deleteUserBarrierById(Long chatId);

}
