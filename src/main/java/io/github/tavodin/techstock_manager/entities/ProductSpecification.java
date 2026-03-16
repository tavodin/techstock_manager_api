package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "product_specification")
public class ProductSpecification extends BaseEntity {

    @Column(name = "value_string",length = 45, nullable = false)
    private String valueString;

    @Column(name = "value_number", nullable = false)
    private Double valueNumber;

    @Column(name = "value_boolean", nullable = false)
    private Boolean valueBoolean;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "specification_id")
    private Specification specification;

    public ProductSpecification() {
    }

    public ProductSpecification(String valueString, Double valueNumber, Boolean valueBoolean, Product product, Specification specification) {
        this.valueString = valueString;
        this.valueNumber = valueNumber;
        this.valueBoolean = valueBoolean;
        this.product = product;
        this.specification = specification;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public Double getValueNumber() {
        return valueNumber;
    }

    public void setValueNumber(Double valueNumber) {
        this.valueNumber = valueNumber;
    }

    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public void setValueBoolean(Boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Specification getSpecification() {
        return specification;
    }

    public void setSpecification(Specification specification) {
        this.specification = specification;
    }
}
