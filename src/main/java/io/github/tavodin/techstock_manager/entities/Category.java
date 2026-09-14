package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Category extends BaseEntity {

    @Column(length = 100, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "categories")
    private Set<Product> products = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "category_specification",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "specification_id"))
    private Set<Specification> specifications = new HashSet<>();

    public Category() {
    }

    public Category(Long id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }

    public Set<Specification> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Set<Specification> specifications) {
        this.specifications = specifications;
    }
}
