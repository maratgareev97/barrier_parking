package ru.barrier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.barrier.models.User;

import java.time.LocalDateTime;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Transactional
    @Query("SELECT  u.chatId from User u where u.chatId=:chatId")
    Long getChatIdUserById(@Param("chatId") Long chatId);     //-----------------------

    @Transactional
    @Query("SELECT  u from User u where u.chatId=:chatId")
    User getUserById(@Param("chatId") Long chatId);

    @Transactional
    @Query("""
            select u 
            from User u 
            right join UserBarrier ub 
            on u.userBarrier.chatId = ub.chatId 
            where u.chatId = :chatId
                        """)
    User getUserBarrierById(@Param("chatId") Long chatId);     //-----------------

    @Transactional
    @Query("""
            select u.userBarrier.dateTimeNextPayment 
            from User u 
            right join UserBarrier ub 
            on u.userBarrier.chatId = ub.chatId 
            where u.chatId = :chatId
                        """)
    LocalDateTime getDateNextPayment(@Param("chatId") Long chatId);

}
