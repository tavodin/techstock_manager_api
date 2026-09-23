package io.github.tavodin.techstock_manager.dto.sale;

import io.github.tavodin.techstock_manager.enums.PaymentMethod;
import io.github.tavodin.techstock_manager.enums.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SaleListDTO {

    private Long id;
    private LocalDateTime saleDate;
    private SaleStatus status;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private String createdBy;
    private String updatedBy;

    public SaleListDTO(Long id, LocalDateTime saleDate, SaleStatus status, PaymentMethod paymentMethod, BigDecimal totalAmount, String createdBy, String updatedBy) {
        this.id = id;
        this.saleDate = saleDate;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.createdBy = createdBy;

        if(updatedBy != null) {
            this.updatedBy = updatedBy;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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
