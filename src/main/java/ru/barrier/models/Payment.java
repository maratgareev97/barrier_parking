package ru.barrier.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "chat_id", nullable = false)
    private long chatId;
    @Column(name = "data_payment")
    private LocalDate dataPayment;

    //    @ManyToOne(fetch=FetchType.LAZY)
//    @JoinColumn(name = "chatId")
//    private User user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "users_chat_id", nullable = false)
    private User user;
}
