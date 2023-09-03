package ru.barrier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.barrier.models.AdminUsers;
import ru.barrier.models.AllPayments;

public interface AdminUsersRepository extends JpaRepository<AdminUsers, Long> {
    @Query("SELECT u.chatId from AdminUsers u where u.chatId=:chatId")
    Long getAdminUsersByChatId(@Param("chatId") Long chatId);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE admin_users", nativeQuery = true)
    void truncateTableAdminUsers();
}
