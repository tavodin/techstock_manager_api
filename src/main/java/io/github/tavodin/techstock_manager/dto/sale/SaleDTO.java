package io.github.tavodin.techstock_manager.dto.sale;

import io.github.tavodin.techstock_manager.enums.PaymentMethod;
import io.github.tavodin.techstock_manager.enums.SaleStatus;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Relation(itemRelation = "sale", collectionRelation = "sales")
public class SaleDTO extends RepresentationModel<SaleDTO> {

    private Long id;
    private LocalDateTime saleDate;
    private SaleStatus status;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;

    public SaleDTO() {
    }

    public SaleDTO(Long id, LocalDateTime saleDate, SaleStatus status, PaymentMethod paymentMethod, BigDecimal totalAmount) {
        this.id = id;
        this.saleDate = saleDate;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
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
}
