package ru.barrier.models;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "cash_payment")
public class CashPayment {
    @Id
    private Integer id;

    @Column(name="cash_allowed")
    private Integer cashAllowed;
}
