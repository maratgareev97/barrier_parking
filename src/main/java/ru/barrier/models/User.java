package ru.barrier.models;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    //    @Transient
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_barrier")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserBarrier userBarrier;

//    @Override
//    public String toString() {
//        return "User{" +
//               "chatId=" + chatId +
//               ", userBarrier=" + userBarrier +
//               '}';
//    }
}
