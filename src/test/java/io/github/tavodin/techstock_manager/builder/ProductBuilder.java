package io.github.tavodin.techstock_manager.builder;

import io.github.tavodin.techstock_manager.entities.Product;

import java.math.BigDecimal;

public class ProductBuilder {

    private Long id = 1L;
    private String name = "Monitor DELL";
    private BigDecimal costPrice = BigDecimal.valueOf(1000.0);
    private BigDecimal salePrice = BigDecimal.valueOf(1300.00);
    private String description = "Monitor DELL 24 polegadas para escritório";
    private String sku = "MON-001";
    private Integer quantityInStock = 10;
    private Integer minimumStock = 5;
    private Boolean active = true;

    public static ProductBuilder builder() {
        return new ProductBuilder();
    }

    public ProductBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ProductBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProductBuilder withCostPrice(Double costPrice) {
        this.costPrice = BigDecimal.valueOf(costPrice);
        return this;
    }

    public ProductBuilder withSalePrice(Double salePrice) {
        this.salePrice = BigDecimal.valueOf(salePrice);
        return this;
    }

    public ProductBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ProductBuilder withSku(String sku) {
        this.sku = sku;
        return this;
    }

    public ProductBuilder withQuantityInStock(Integer qtdInStock) {
        quantityInStock = qtdInStock;
        return this;
    }

    public ProductBuilder withMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
        return this;
    }

    public ProductBuilder withActive(Boolean active) {
        this.active = active;
        return this;
    }

    public Product build() {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setCostPrice(costPrice);
        product.setSalePrice(salePrice);
        product.setSku(sku);
        product.setDescription(description);
        product.setQuantityInStock(quantityInStock);
        product.setMinimumStock(minimumStock);
        product.setActive(active);
        return product;
    }
}
