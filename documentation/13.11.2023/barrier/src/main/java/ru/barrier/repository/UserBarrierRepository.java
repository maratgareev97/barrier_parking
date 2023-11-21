package ru.barrier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.barrier.models.UserBarrier;

import java.time.LocalDateTime;
import java.util.List;

public interface UserBarrierRepository extends JpaRepository<UserBarrier, Long> {
    //    @Query("select user_barrier (chat_id) values (1)" )
//    void getById();
    @Query("SELECT u.chatId from User u where u.chatId=:chatId")
    UserBarrier getUserById(@Param("chatId") Long chatId);

    @Query("select ub.chatId from UserBarrier ub where ub.dateTimeNextPayment<=:dateTimeNextPayment")
    List<Long> getUserBarrierDataTime(@Param("dateTimeNextPayment") LocalDateTime dateTimeNextPayment);

    @Query("select ub.chatId from UserBarrier ub where ub.dateTimeNextPayment<:dateTimeNow and ub.stoppedBy=0")
    List<Long> getUserByEndTime(@Param("dateTimeNow") LocalDateTime dateTimeNow);
}


