package ru.barrier.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @Column(name = "chat_id", nullable = false)
    private long chatID;
    @Column(name = "number_car", nullable = false)
    private String numberCar;
    @Column(name = "data_last_payment")
    private LocalDate dataLastPayment;
    @Column(name = "use", nullable = false)
    private boolean use;

}
