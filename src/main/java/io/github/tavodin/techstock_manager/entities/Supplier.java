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

    public Supplier() {
    }

    public Supplier(String name, String document, String email, String phone) {
        this.name = name;
        this.document = document;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
