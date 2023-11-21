package ru.barrier.models;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "all_payments")
public class AllPayments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "chat_id", nullable = false)
    private long chatId;

    @Column(name = "data_time_payment")
    private LocalDateTime dateTimePayment;

    @Column(name = "id_payment")
    private String idPayment;
}
