package io.github.tavodin.techstock_manager.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "category_specification")
public class CategorySpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean required;

    @Column(nullable = false)
    private Integer displayOrder;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "specification_id")
    private Specification specification;

    public CategorySpecification() {
    }

    public CategorySpecification(Long id, Boolean required, Integer displayOrder, Category category, Specification specification) {
        this.id = id;
        this.required = required;
        this.displayOrder = displayOrder;
        this.category = category;
        this.specification = specification;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Specification getSpecification() {
        return specification;
    }

    public void setSpecification(Specification specification) {
        this.specification = specification;
    }
}
