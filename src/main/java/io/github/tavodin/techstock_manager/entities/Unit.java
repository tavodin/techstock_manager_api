package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class Unit extends BaseEntity {

    @Column(length = 45)
    private String name;

    @Column(length = 10, nullable = false)
    private String symbol;

    public Unit() {
    }

    public Unit(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, String name, String symbol) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.symbol = symbol;
    }

    public Unit(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
