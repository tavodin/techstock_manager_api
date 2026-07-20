package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "supplier")
    private List<Purchase> purchases = new ArrayList<>();

    public Supplier() {
    }

    public Supplier(String name, String document, String email, String phone, Boolean active) {
        this.name = name;
        this.document = document;
        this.email = email;
        this.phone = phone;
        this.active = active;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<Purchase> purchases) {
        this.purchases = purchases;
    }
}
