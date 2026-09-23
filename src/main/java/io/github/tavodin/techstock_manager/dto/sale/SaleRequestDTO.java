package io.github.tavodin.techstock_manager.dto.sale;

import io.github.tavodin.techstock_manager.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

public class SaleRequestDTO {

    @NotNull(message = "Payment Method is required")
    private PaymentMethod paymentMethod;

    @NotEmpty(message = "Sale must contain at least one item")
    @Valid
    private Set<SaleItemRequestDTO> items = new HashSet<>();

    public SaleRequestDTO() {
    }

    public SaleRequestDTO(PaymentMethod paymentMethod, Set<SaleItemRequestDTO> items) {
        this.paymentMethod = paymentMethod;
        this.items = items;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Set<SaleItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(Set<SaleItemRequestDTO> items) {
        this.items = items;
    }
}
