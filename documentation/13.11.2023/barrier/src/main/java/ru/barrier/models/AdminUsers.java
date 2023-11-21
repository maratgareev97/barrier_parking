package ru.barrier.models;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "admin_users")
public class AdminUsers {
    @Id
    @Column(name = "chat_id", nullable = false)
    private long chatId;
}
