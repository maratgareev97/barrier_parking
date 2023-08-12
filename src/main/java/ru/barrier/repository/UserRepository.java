package ru.barrier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.barrier.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT  u.chatId from User u where u.chatId=:chatId")
    Long getUserById(@Param("chatId") Long chatId);

    @Query("""
            select u 
            from User u 
            right join UserBarrier ub 
            on u.userBarrier.chatId = ub.chatId 
            where u.chatId = :chatId
                        """)
    User getUserBarrierById(@Param("chatId") Long chatId);

}
