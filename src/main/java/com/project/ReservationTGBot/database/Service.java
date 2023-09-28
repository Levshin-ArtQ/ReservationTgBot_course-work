package com.project.ReservationTGBot.database;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "services") //привязываемся к существующей таблице с готовыми колонками
public class Service {
    String name;
    @Id
    String code;
    int cost;

    String parentType;

    boolean hasChildType;

    String emoji;

    public Service(String code) {
        this.code = code;
    }

    public boolean hasParentType() {
        return !parentType.equals("0");
    }

    public Service() {
    }
}
