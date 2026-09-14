package io.github.tavodin.techstock_manager.dto.category;

import io.github.tavodin.techstock_manager.entities.Category;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CategoryLoadDTO {
    private Long id;
    private String name;
    private Set<SpecificationCategoryLoadDTO> specifications = new HashSet<>();

    public CategoryLoadDTO() {
    }

    public CategoryLoadDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();

        if (!category.getSpecifications().isEmpty()) {
            this.specifications = category.getSpecifications().stream()
                    .map(SpecificationCategoryLoadDTO::new)
                    .collect(Collectors.toSet());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SpecificationCategoryLoadDTO> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Set<SpecificationCategoryLoadDTO> specifications) {
        this.specifications = specifications;
    }
}
