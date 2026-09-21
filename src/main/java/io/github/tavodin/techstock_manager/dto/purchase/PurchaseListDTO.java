package io.github.tavodin.techstock_manager.dto.purchase;

import io.github.tavodin.techstock_manager.enums.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PurchaseListDTO {
    private Long id;
    private LocalDate purchaseDate;
    private PurchaseStatus status;
    private BigDecimal totalAmount;
    private String supplier;
    private String createdBy;
    private String updatedBy;

    public PurchaseListDTO() {
    }

    public PurchaseListDTO(Long id, LocalDate purchaseDate, PurchaseStatus status, BigDecimal totalAmount, String supplier, String createdBy, String updatedBy) {
        this.id = id;
        this.purchaseDate = purchaseDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.supplier = supplier;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
