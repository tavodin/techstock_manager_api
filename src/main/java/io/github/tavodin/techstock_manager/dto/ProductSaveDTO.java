package io.github.tavodin.techstock_manager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ProductSaveDTO {

    @NotBlank(message = "Name is required")
    @Length(min = 2, max = 200, message = "Name must contain between {min} and {max} characters")
    private String name;

    @NotNull(message = "Sale Price is required")
    @Positive(message = "Sale Price cannot be equal to zero or negative")
    private BigDecimal salePrice;

    private String description;

    @NotBlank(message = "SKU is required")
    @Length(min = 5, max = 30, message = "SKU must contain between {min} and {max} characters")
    private String sku;

    @NotNull(message = "Minimum Stock is required")
    @Positive(message = "Minimum Stock cannot be equal to zero or negative")
    private Integer minimumStock;

    @NotNull(message = "Brand ID is required")
    private Long brandId;

    @NotEmpty(message = "Category ID is required")
    private Set<Long> categoryIds;

    @Valid
    @NotEmpty(message = "Specifications are required")
    private List<ProductSpecificationSaveDTO> specifications;

    public ProductSaveDTO() {
    }

    public ProductSaveDTO(String name, BigDecimal salePrice, String description, String sku, Integer minimumStock, Long brandId, Set<Long> categoriesId, List<ProductSpecificationSaveDTO> specifications) {
        this.name = name;
        this.salePrice = salePrice;
        this.description = description;
        this.sku = sku;
        this.minimumStock = minimumStock;
        this.brandId = brandId;
        this.categoryIds = categoriesId;
        this.specifications = specifications;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Set<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(Set<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<ProductSpecificationSaveDTO> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(List<ProductSpecificationSaveDTO> specifications) {
        this.specifications = specifications;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProductSaveDTO that = (ProductSaveDTO) o;
        return Objects.equals(name, that.name) && Objects.equals(salePrice, that.salePrice) && Objects.equals(description, that.description) && Objects.equals(sku, that.sku) && Objects.equals(minimumStock, that.minimumStock) && Objects.equals(brandId, that.brandId) && Objects.equals(categoryIds, that.categoryIds) && Objects.equals(specifications, that.specifications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, salePrice, description, sku, minimumStock, brandId, categoryIds, specifications);
    }
}

