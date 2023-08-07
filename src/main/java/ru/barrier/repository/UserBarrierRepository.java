package ru.barrier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.barrier.models.UserBarrier;

public interface UserBarrierRepository extends JpaRepository<UserBarrier, Long> {
//    @Query("select user_barrier (chat_id) values (1)" )
//    void getById();
}
