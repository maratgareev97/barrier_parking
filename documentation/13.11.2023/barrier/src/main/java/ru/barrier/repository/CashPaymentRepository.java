package ru.barrier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.barrier.models.AllPayments;
import ru.barrier.models.CashPayment;

public interface CashPaymentRepository extends JpaRepository<CashPayment, Long> {

    @Query("SELECT  cp.cashAllowed from CashPayment cp where cp.id=:id")
    Long getCashPaymentBy(@Param("id") Integer id);
}
