package ru.barrier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.barrier.models.AllPayments;
import ru.barrier.models.Payment;

public interface AllPaymentsRepository extends JpaRepository<AllPayments, Long> {
}
