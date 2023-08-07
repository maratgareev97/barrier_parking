package ru.barrier.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

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

    @Column(name = "stopped_by")
    private boolean stoppedBy;

    @Column(name = "parking_place")
    private int parkingPlace;


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL,mappedBy = "userBarrier")
    private User user;

    @Override
    public String toString() {
        return "UserBarrier{" +
               "chatId=" + chatId +
               ", dataLastPayment=" + dataLastPayment +
               ", use=" + stoppedBy +
               ", test='" + parkingPlace + '\'' +
               ", user=" + user +
               '}';
    }
}
