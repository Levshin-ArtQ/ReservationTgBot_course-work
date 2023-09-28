package notifier.NotificationManager.database;

//package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static notifier.NotificationManager.Utils.reformatDateString;
import static notifier.NotificationManager.Utils.reformatDateTimeString;


import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.CascadeType;


@Data
@Entity(name = "reservation_data")
public class Reservation implements Comparable<Reservation> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "reservation_id")
    Integer ID;

//    @JsonProperty
    Long chatID;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    // @OnDelete(action = OnDeleteAction.CASCADE)
    User user;

    @Column(name = "user_id")
    Long userId;
//    @JsonProperty
    String serviceType;
    String serviceCode;
//    @JsonProperty
    Integer descriptionMessageID;
//    @JsonProperty
    @Column(columnDefinition = "TIMESTAMP")
    LocalDateTime dateTime;

//    @JsonProperty
    @Column(columnDefinition = "DATE")
    LocalDate date;

    @Column(columnDefinition = "TIMESTAMP")
    LocalDateTime creationDateTime;


//    @JsonProperty
    boolean accepted;


//    @JsonProperty
    boolean rejected;

//    @JsonProperty
    boolean denied;
//    @JsonProperty
    Integer cost;

//    @JsonProperty
//    boolean[] notified = new boolean[3];

    boolean notified1;
    boolean notified2;
    boolean notified3;

    public Reservation(){}


    public Reservation(Long chatID, String serviceType, Integer cost, String serviceCode) {
        this.chatID = chatID;
        this.serviceType = serviceType;
        this.cost = cost;
        this.denied = false;
        this.accepted = false;
        this.rejected = false;
        this.serviceCode = serviceCode;
        this.notified1 = false;
        this.notified2 = false;
        this.notified3 = false;

//        Arrays.fill(this.notified, false);
    }

    public Reservation(Integer ID, Long chatID, String serviceType, Integer descriptionMessageID, LocalDateTime date) {
        this.ID = ID;
        this.chatID = chatID;
        this.serviceType = serviceType;
        this.descriptionMessageID = descriptionMessageID;
        this.dateTime = date;
        accepted = false;
        cost = -1;
    }

    @Override
    public String toString() {
        StringBuilder message = new StringBuilder();

        if (this.accepted)
            message.append("Запись принята мастером\n");
        else if (this.rejected)
            message.append("Запись отклонена мастером, он не сможет вас принять\n");
        else if (this.denied) {
            message.append("Вы уже отменили эту запись\n");
        }
        else message.append("Запись ещё не просмотрена мастером\n");


        if (dateTime != null)
            message.append("Запись назначенная на: \n").append(reformatDateTimeString(dateTime)).append("\n");
        else if (date != null) {
            message.append("Запись назначенная на: \n").append(reformatDateString(date)).append("\n");
        }

        if (serviceType != null)
            message.append("тип услуги: ").append(serviceType).append("\n");

        if (cost != null)
            message.append("Стоимость услуги: ").append(cost).append("р\n");

        return message.toString();
    }

    public Integer getID() {
        return ID;
    }

    public void setDescriptionMessageID(Integer descriptionMessageID) {
        this.descriptionMessageID = descriptionMessageID;
    }

    public Integer getDescriptionMessageID() {
        if (descriptionMessageID == null)
            return -1;
        return descriptionMessageID;
    }

    public String getServiceType() {
        return serviceType;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setAccepted(boolean status) {
        this.accepted = status;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean isRejected() {
        return rejected;
    }
    public boolean isDenied() {
        return denied;
    }

    public void setRejected() {
        this.rejected = true;
    }
    public void setDenied() {
        this.denied = true;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }


    public void setCost(Integer cost) {
        this.cost = cost;
    }

    @Override
    public int compareTo(Reservation reservation) {
        if (getDateTime() == null || reservation.getDateTime() == null)
            return 0;
        return getDateTime().compareTo(reservation.getDateTime());
    }
}



