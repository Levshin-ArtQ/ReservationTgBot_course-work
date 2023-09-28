package com.project.ReservationTGBot.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
// import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.util.Set;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.OnDeleteAction;


@Data
@Entity(name = "tg_data") //привязываемся к существующей таблице с готовыми колонками
public class User {

    @Id
    @Column(name = "user_id")
    private long id; //BigInt
    private String name; //Text
    private int msg_numb; //Integer
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    private Set<Reservation> reservationSet; //
}
