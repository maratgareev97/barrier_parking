package ru.barrier.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "payment")
public class Payment {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private long id;

    @Id
    @Column(name = "chat_id", nullable = false)
    private long chatId;

    @Column(name = "data_time_payment")
    private LocalDateTime dateTimePayment;

    @Column(name = "id_payment")
    private String idPayment;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_barrier_id")
//    private UserBarrier userBarrier;

    //    @ManyToOne(fetch=FetchType.LAZY)
//    @JoinColumn(name = "chatId")
//    private User user;


//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "users_chat_id", nullable = false)
//    private UserBarrier userBarrier;
}
