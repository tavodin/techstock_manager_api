package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Unit extends BaseEntity {

    @Column(length = 45)
    private String name;

    @Column(length = 10, nullable = false)
    private String symbol;

}
