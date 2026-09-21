package io.github.tavodin.techstock_manager.dto;

import java.math.BigDecimal;

public class PurchaseItemDTO {

    private String productName;
    private Integer quantity;
    private BigDecimal unitCost;
    private BigDecimal subTotal;

    public PurchaseItemDTO() {
    }

    public PurchaseItemDTO(String productName, Integer quantity, BigDecimal unitCost, BigDecimal subTotal) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.subTotal = subTotal;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }
}
