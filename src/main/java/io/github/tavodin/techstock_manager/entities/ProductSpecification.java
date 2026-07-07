package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(
        name = "product_specification",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "product_id",
                                "specification_id"
                        }
                )
        }
)
public class ProductSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "value_string",length = 45)
    private String valueString;

    @Column(name = "value_number")
    private Double valueNumber;

    @Column(name = "value_boolean")
    private Boolean valueBoolean;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specification_id")
    private Specification specification;

    public ProductSpecification() {
    }

    public ProductSpecification(Long id, String valueString, Double valueNumber, Boolean valueBoolean, Product product, Specification specification) {
        this.id = id;
        this.valueString = valueString;
        this.valueNumber = valueNumber;
        this.valueBoolean = valueBoolean;
        this.product = product;
        this.specification = specification;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProductSpecification that = (ProductSpecification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
