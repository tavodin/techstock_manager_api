package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Supplier extends AuditableEntity {

    @Column(length = 200, nullable = false)
    private String name;

    @Column(length = 14, nullable = false, unique = true)
    private String document;

    @Column(length = 200, unique = true)
    private String email;

    @Column(length = 20, nullable = false)
    private String phone;

}
