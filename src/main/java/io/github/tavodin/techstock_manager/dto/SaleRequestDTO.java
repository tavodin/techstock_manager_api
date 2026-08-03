package io.github.tavodin.techstock_manager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

public class SaleRequestDTO {

    @NotBlank(message = "Payment Method is required")
    @Size(max = 45, message = "The name must contain a maximum of {max} characters")
    private String paymentMethod;

    @NotEmpty(message = "Sale must contain at least one item")
    @Valid
    private Set<SaleItemRequestDTO> items = new HashSet<>();

    public SaleRequestDTO() {
    }

    public SaleRequestDTO(String paymentMethod, Set<SaleItemRequestDTO> items) {
        this.paymentMethod = paymentMethod;
        this.items = items;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Set<SaleItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(Set<SaleItemRequestDTO> items) {
        this.items = items;
    }
}
