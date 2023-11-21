package ru.barrier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.barrier.models.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
