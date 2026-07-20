package io.github.tavodin.techstock_manager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupplierRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "The name must contain a maximum of {max} characters")
    private String name;

    @NotBlank(message = "Document is required")
    @Size(max = 14, message = "The document must contain a maximum of {max} characters")
    private String document;

    @Email(message = "Invalid Email")
    @Size(max = 200, message = "The email must contain a maximum of {max} characters")
    private String email;

    @NotBlank(message = "Phone is required")
    @Size(max = 14, message = "The phone must contain a maximum of {max} characters")
    private String phone;

    public SupplierRequestDTO() {
    }

    public SupplierRequestDTO(String name, String document, String email, String phone) {
        this.name = name;
        this.document = document;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
