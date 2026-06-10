package io.github.tavodin.techstock_manager.dto;

import io.github.tavodin.techstock_manager.entities.Product;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

@Relation(itemRelation = "product", collectionRelation = "products")
public class ProductDTO extends RepresentationModel<ProductDTO> {

    private Long id;
    private String name;
    private BigDecimal costPrice;
    private BigDecimal salePrice;
    private String description;
    private String sku;
    private Integer quantityInStock;
    private Integer minimumStock;
    private Boolean active;

    public ProductDTO() {
    }

    public ProductDTO(Long id, String name, BigDecimal costPrice, BigDecimal salePrice, String description, String sku, Integer quantityInStock, Integer minimumStock, Boolean active) {
        this.id = id;
        this.name = name;
        this.costPrice = costPrice;
        this.salePrice = salePrice;
        this.description = description;
        this.sku = sku;
        this.quantityInStock = quantityInStock;
        this.minimumStock = minimumStock;
        this.active = active;
    }

    public ProductDTO(Product entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.costPrice = entity.getCostPrice();
        this.salePrice = entity.getSalePrice();
        this.description = entity.getDescription();
        this.sku = entity.getSku();
        this.quantityInStock = entity.getQuantityInStock();
        this.minimumStock = entity.getMinimumStock();
        this.active = entity.getActive();
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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
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
}
