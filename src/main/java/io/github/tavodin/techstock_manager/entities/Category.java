package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Category extends BaseEntity {

    @Column(length = 100, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "categories")
    private Set<Product> products = new HashSet<>();

    @OneToMany(mappedBy = "category")
    private Set<CategorySpecification> categorySpecifications = new HashSet<>();

    public Category() {
    }

    public Category(String name, Set<Product> products, Set<CategorySpecification> categorySpecifications) {
        this.name = name;
        this.products = products;
        this.categorySpecifications = categorySpecifications;
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

    public Set<CategorySpecification> getCategorySpecifications() {
        return categorySpecifications;
    }

    public void setCategorySpecifications(Set<CategorySpecification> categorySpecifications) {
        this.categorySpecifications = categorySpecifications;
    }
}
