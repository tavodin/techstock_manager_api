package io.github.tavodin.techstock_manager.dto;

import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.enums.SpecificationType;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Relation(itemRelation = "specification", collectionRelation = "specifications")
public class SpecificationDTO extends RepresentationModel<SpecificationDTO> {

    private Long id;
    private String name;
    private SpecificationType dataType;
    private Boolean filterable;
    private String unitSymbol;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SpecificationDTO() {
    }

    public SpecificationDTO(Long id, String name, SpecificationType dataType, Boolean filterable, String unitSymbol, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.dataType = dataType;
        this.filterable = filterable;
        this.unitSymbol = unitSymbol;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public SpecificationDTO(Specification entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.dataType = entity.getDataType();
        this.filterable = entity.getFilterable();

        if(entity.getUnit() != null) {
            this.unitSymbol = entity.getUnit().getSymbol();
        }

        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SpecificationType getDataType() {
        return dataType;
    }

    public void setDataType(SpecificationType dataType) {
        this.dataType = dataType;
    }

    public Boolean getFilterable() {
        return filterable;
    }

    public void setFilterable(Boolean filterable) {
        this.filterable = filterable;
    }

    public String getUnitSymbol() {
        return unitSymbol;
    }

    public void setUnitSymbol(String unit) {
        this.unitSymbol = unit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
