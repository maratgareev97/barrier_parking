package ru.barrier.models;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "user_barrier")
public class UserBarrier {
    @Id
    @Column(name = "chat_id", nullable = false)
    private long chatId;

    @Column(name="name")
    private String name;

    @Column(name = "stopped_by")
    private int stoppedBy;

    @Column(name = "parking_place")
    private int parkingPlace;

    @Column(name = "data_time_last_payment")
    private LocalDateTime dateTimeLastPayment;
    @Column(name = "data_time_next_payment")
    private LocalDateTime dateTimeNextPayment;

    @Column(name = "amount_of_days")
    private int amountOfDays;


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "userBarrier")
    @OnDelete(action = OnDeleteAction.CASCADE)
//    @OneToOne(fetch = FetchType.LAZY, mappedBy = "userBarrier")
    private User user;

//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
////    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinColumn(name = "user_chat_id")
//    private List<Payment> payments = new ArrayList<>();

    @Override
    public String toString() {
        return "UserBarrier{" +
               "chatId=" + chatId +
               ", dataLastPayment=" + dateTimeLastPayment +
               ", use=" + stoppedBy +
               ", test='" + parkingPlace + '\'' +
               ", user=" + user +
               '}';
    }
}
