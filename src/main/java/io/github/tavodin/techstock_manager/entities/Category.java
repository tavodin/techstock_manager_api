package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Category extends BaseEntity {

    @Column(length = 100, nullable = false)
    private String name;

}
