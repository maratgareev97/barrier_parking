package ru.barrier.models;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "user_barrier")
public class UserBarrier {
    @Id
    @Column(name = "chat_id", nullable = false)
    private long chatId;

    @Column(name = "data_last_payment")
    private LocalDate dataLastPayment;

    @Column(name = "use", nullable = false)
    private boolean use;

    @Column
    private String test;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user", nullable = false)
//    private User user;
}
