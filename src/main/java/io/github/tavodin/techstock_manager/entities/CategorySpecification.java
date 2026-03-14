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
}
