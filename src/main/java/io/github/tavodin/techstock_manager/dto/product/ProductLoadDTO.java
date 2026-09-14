package io.github.tavodin.techstock_manager.dto.product;

import io.github.tavodin.techstock_manager.entities.Product;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductLoadDTO {

    private Long id;
    private String name;
    private BigDecimal salePrice;
    private String description;
    private String sku;
    private Integer minimumStock;
    private BrandLoadDTO brand;
    private Set<CategoryLoadDTO> categories = new HashSet<>();
    private Set<SpecificationProductLoadDTO> specifications = new HashSet<>();

    public ProductLoadDTO() {
    }

    public ProductLoadDTO(Product entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.salePrice = entity.getSalePrice();
        this.description = entity.getDescription();
        this.sku = entity.getSku();
        this.minimumStock = entity.getMinimumStock();

        if(entity.getBrand() != null) {
            this.brand = new BrandLoadDTO(entity.getBrand());
        }

        this.categories = entity.getCategories().stream()
                .map(CategoryLoadDTO::new)
                .collect(Collectors.toSet());

        this.specifications = entity.getSpecifications().stream()
                .map(SpecificationProductLoadDTO::new)
                .collect(Collectors.toSet());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public String getDescription() {
        return description;
    }

    public String getSku() {
        return sku;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public BrandLoadDTO getBrand() {
        return brand;
    }

    public Set<CategoryLoadDTO> getCategories() {
        return categories;
    }

    public Set<SpecificationProductLoadDTO> getSpecifications() {
        return specifications;
    }
}
