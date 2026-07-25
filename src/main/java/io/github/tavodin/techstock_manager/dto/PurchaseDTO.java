package io.github.tavodin.techstock_manager.dto;

import io.github.tavodin.techstock_manager.enums.PurchaseStatus;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Relation(itemRelation = "purchase", collectionRelation = "purchases")
public class PurchaseDTO extends RepresentationModel<PurchaseDTO> {

    private Long id;
    private LocalDate purchaseData;
    private PurchaseStatus status;
    private BigDecimal totalAmount;

    public PurchaseDTO() {
    }

    public PurchaseDTO(Long id, LocalDate purchaseData, PurchaseStatus status, BigDecimal totalAmount) {
        this.id = id;
        this.purchaseData = purchaseData;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getPurchaseData() {
        return purchaseData;
    }

    public void setPurchaseData(LocalDate purchaseData) {
        this.purchaseData = purchaseData;
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
}
