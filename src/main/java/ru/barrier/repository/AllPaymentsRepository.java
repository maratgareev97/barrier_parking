package ru.barrier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.barrier.models.AllPayments;
import ru.barrier.models.Payment;

public interface AllPaymentsRepository extends JpaRepository<AllPayments, Long> {
    @Query("""
            delete FROM AllPayments a WHERE a.id=:id
            """)
    void delete(@Param("id") Long id);
}
