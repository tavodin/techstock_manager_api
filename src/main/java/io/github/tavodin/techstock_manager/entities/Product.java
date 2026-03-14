package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Product extends AuditableEntity {

    @Column(length = 200, nullable = false)
    private String name;

    @Column(name = "cost_price", nullable = false)
    private BigDecimal costPrice;

    @Column(name = "sale_price", nullable = false)
    private BigDecimal salePrice;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(length = 10, nullable = false)
    private String varchar;

    @Column(name = "quantity_in_stock", nullable = false)
    private Integer quantityInStock;

    @Column(name = "minimum_stock", nullable = false)
    private Integer minimumStock;

    @Column(nullable = false)
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToMany
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<ProductSpecification> specifications = new HashSet<>();

    public Product() {
    }

    public Product(String name, BigDecimal costPrice, BigDecimal salePrice, String description, String varchar, Integer quantityInStock, Integer minimumStock, Boolean active, Brand brand, Set<Category> categories, Set<ProductSpecification> specifications) {
        this.name = name;
        this.costPrice = costPrice;
        this.salePrice = salePrice;
        this.description = description;
        this.varchar = varchar;
        this.quantityInStock = quantityInStock;
        this.minimumStock = minimumStock;
        this.active = active;
        this.brand = brand;
        this.categories = categories;
        this.specifications = specifications;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVarchar() {
        return varchar;
    }

    public void setVarchar(String varchar) {
        this.varchar = varchar;
    }

    public Integer getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(Integer quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public Set<ProductSpecification> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Set<ProductSpecification> specifications) {
        this.specifications = specifications;
    }
}
