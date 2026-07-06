package io.github.tavodin.techstock_manager.dto;

import io.github.tavodin.techstock_manager.enums.MovementType;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Relation(itemRelation = "stockMovement", collectionRelation = "stockMovements")
public class StockMovementDTO extends RepresentationModel<ProductDTO> {

    private Long id;
    private MovementType type;
    private Integer quantity;
    private LocalDateTime movementDate;
    private String reason;
    private String product;
    private String createdBy;
    private String updateBy;

    public StockMovementDTO() {
    }

    public StockMovementDTO(Long id, MovementType movementType, Integer quantity, LocalDateTime movementDate, String reason, String product, String createdBy, String updateBy) {
        this.id = id;
        this.type = movementType;
        this.quantity = quantity;
        this.movementDate = movementDate;
        this.reason = reason;
        this.product = product;
        this.createdBy = createdBy;
        this.updateBy = updateBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDateTime movementDate) {
        this.movementDate = movementDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }
}
