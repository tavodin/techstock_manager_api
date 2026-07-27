package io.github.tavodin.techstock_manager.dto;

import io.github.tavodin.techstock_manager.enums.PurchaseStatus;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDate;

@Relation(itemRelation = "purchase", collectionRelation = "purchases")
public class PurchaseDTO extends RepresentationModel<PurchaseDTO> {

    private Long id;
    private LocalDate purchaseDate;
    private PurchaseStatus status;
    private BigDecimal totalAmount;

    public PurchaseDTO() {
    }

    public PurchaseDTO(Long id, LocalDate purchaseDate, PurchaseStatus status, BigDecimal totalAmount) {
        this.id = id;
        this.purchaseDate = purchaseDate;
        this.status = status;
        this.totalAmount = totalAmount;
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
}
