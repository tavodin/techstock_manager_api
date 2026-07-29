package io.github.tavodin.techstock_manager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseRequestDTO {

    @NotNull(message = "Purchase Date is required")
    private LocalDate purchaseDate;

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotEmpty(message = "Purchase Item is required")
    @Valid
    private List<PurchaseItemRequestDTO> items = new ArrayList<>();

    public PurchaseRequestDTO() {
    }

    public PurchaseRequestDTO(LocalDate purchaseDate, Long supplierId, List<PurchaseItemRequestDTO> items) {
        this.purchaseDate = purchaseDate;
        this.supplierId = supplierId;
        this.items = items;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public List<PurchaseItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItemRequestDTO> items) {
        this.items = items;
    }
}
