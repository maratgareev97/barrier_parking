package ru.barrier.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @Column(name = "chat_id", nullable = false)
    private long chatId;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_barrier", nullable = false)
//    private UserBarrier userBarrier;


    @Transient
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "chatId")
    private Set<Payment> payments;

//    @Override
//    public String toString() {
//        return "User{" +
//               "chatId=" + chatId +
//               ", userBarrier=" + userBarrier +
//               ", payments=" + payments +
//               '}';
//    }
}
