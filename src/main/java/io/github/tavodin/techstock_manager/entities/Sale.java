package io.github.tavodin.techstock_manager.entities;

import io.github.tavodin.techstock_manager.enums.SaleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Sale extends AuditableEntity {

    @Column(name = "saleDate", nullable = false)
    private LocalDateTime saleDate;

    @Column(nullable = false)
    private SaleStatus status;

    @Column(name = "payment_method", length = 45, nullable = false)
    private String paymentMethod;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "sale")
    private Set<SaleItem> saleItems = new HashSet<>();

    public Sale() {
    }

    public Sale(LocalDateTime saleDate, SaleStatus status, String paymentMethod, BigDecimal totalAmount, Set<SaleItem> saleItems) {
        this.saleDate = saleDate;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.saleItems = saleItems;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Set<SaleItem> getSaleItems() {
        return saleItems;
    }

    public void setSaleItems(Set<SaleItem> saleItems) {
        this.saleItems = saleItems;
    }
}
