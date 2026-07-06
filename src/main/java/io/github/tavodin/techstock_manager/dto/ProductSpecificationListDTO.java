package io.github.tavodin.techstock_manager.dto;

public class ProductSpecificationListDTO {

    private Long id;
    private Long specificationId;
    private Long productId;
    private String specificationName;
    private String valueString;
    private Double valueNumber;
    private Boolean valueBoolean;
    private String unitSymbol;

    public ProductSpecificationListDTO() {
    }

    public ProductSpecificationListDTO(Long id, Long specificationId, Long productId, String specificationName, String valueString, Double valueNumber, Boolean valueBoolean, String unitSymbol) {
        this.id = id;
        this.specificationId = specificationId;
        this.productId = productId;
        this.specificationName = specificationName;
        this.valueString = valueString;
        this.valueNumber = valueNumber;
        this.valueBoolean = valueBoolean;
        this.unitSymbol = unitSymbol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSpecificationId() {
        return specificationId;
    }

    public void setSpecificationId(Long specificationId) {
        this.specificationId = specificationId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getSpecificationName() {
        return specificationName;
    }

    public void setSpecificationName(String specificationName) {
        this.specificationName = specificationName;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public Double getValueNumber() {
        return valueNumber;
    }

    public void setValueNumber(Double valueNumber) {
        this.valueNumber = valueNumber;
    }

    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public void setValueBoolean(Boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
    }

    public String getUnitSymbol() {
        return unitSymbol;
    }

    public void setUnitSymbol(String unitSymbol) {
        this.unitSymbol = unitSymbol;
    }
}
