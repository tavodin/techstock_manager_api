package io.github.tavodin.techstock_manager.integrationtests.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.configurations.AbstractIntegrationTest;
import io.github.tavodin.techstock_manager.dto.ProductDTO;
import io.github.tavodin.techstock_manager.dto.ProductRequestDTO;
import io.github.tavodin.techstock_manager.dto.ProductSpecificationRequestDTO;
import io.github.tavodin.techstock_manager.dto.error.CustomError;
import io.github.tavodin.techstock_manager.dto.error.FieldError;
import io.github.tavodin.techstock_manager.dto.error.ValidationError;
import io.github.tavodin.techstock_manager.entities.Brand;
import io.github.tavodin.techstock_manager.entities.Category;
import io.github.tavodin.techstock_manager.entities.CategorySpecification;
import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.enums.SpecificationType;
import io.github.tavodin.techstock_manager.integrationtests.utils.AuthTestUtil;
import io.github.tavodin.techstock_manager.repositories.*;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ProductControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SpecificationRepository specificationRepository;

    @Autowired
    private CategorySpecificationRepository categorySpecificationRepository;

    @Autowired
    private ProductSpecificationRepository productSpecificationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private static RequestSpecification specification;
    private String token;

    private static final String PATH = "/products";
    private static final String CONTENT_TYPE = "application/json";

    private Long invalidId = Long.MAX_VALUE;
    private String productNotFoundMsg = "Product not found";
    private String validationErrorMsg = "Entity validation error";
    private String brandNotFoundMsg = "Brand not found";
    private String categoryNotFoundMsg = "One or more categories were not found";
    private String specificationNotFoundMsg = "One or more specifications were not found";
    private String skuExistsMsg = "SKU already exists";
    private String missingSpecMsg = "Missing required specifications";

    private ProductRequestDTO request;
    private Category category;
    private Brand brand;
    private Specification specificationEntity;

    @BeforeEach
    void setup() {
        RestAssured.port = port;

        token = AuthTestUtil.getToken(port);

        specification = new RequestSpecBuilder()
                .setBasePath(PATH)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        category = new Category();
        category.setName("Monitor");

        brand = new Brand();
        brand.setName("Monitor");

        request = new ProductRequestDTO();
        request.setName("Monitor DELL");
        request.setSalePrice(BigDecimal.valueOf(1999.99));
        request.setSku("MON-001");
        request.setDescription(null);
        request.setMinimumStock(5);

        specificationEntity = SpecificationBuilder
                .builder()
                .withId(null)
                .withName("Resolução")
                .withUnit(null)
                .withFilterable(true)
                .withSpecificationType(SpecificationType.STRING)
                .withCreatedAt(null)
                .withUpdatedAt(null)
                .build();

        productSpecificationRepository.deleteAll();
        productRepository.deleteAll();
        specificationRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();
    }

    @Test
    void shouldFindProductWhenFindingWithValidId() {
        Brand savedBrand = createBrand(brand);
        Category savedCategory = createCategory(category);
        Specification savedSpecification = createSpecification(specificationEntity);

        CategorySpecification categorySpecification = new CategorySpecification(
                null, true, savedCategory, savedSpecification
        );

        createCategorySpecification(categorySpecification);

        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setBrandId(savedBrand.getId());

        request.setSpecifications(List.of(
                new ProductSpecificationRequestDTO(
                        savedSpecification.getId(),
                        "1920x1080",
                        null,
                        null
                )));
    }

    @Test
    void shouldSaveProductWhenSavingWithValidData() throws JsonProcessingException {
        Brand savedBrand = createBrand(brand);
        Category savedCategory = createCategory(category);
        Specification savedSpecification = createSpecification(specificationEntity);

        CategorySpecification categorySpecification = new CategorySpecification(
                null, true, savedCategory, savedSpecification
        );

        createCategorySpecification(categorySpecification);

        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setBrandId(savedBrand.getId());

        request.setSpecifications(List.of(
                new ProductSpecificationRequestDTO(
                        savedSpecification.getId(),
                    "1920x1080",
                    null,
                    null
        )));

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString();

        ProductDTO responseBody = objectMapper.readValue(content, ProductDTO.class);

        assertNotNull(responseBody.getId());
        assertEquals(request.getName(), responseBody.getName());
        assertTrue(responseBody.getSalePrice().compareTo(request.getSalePrice()) == 0);
        assertTrue(responseBody.getCostPrice().compareTo(BigDecimal.ZERO) == 0);
        assertEquals(request.getSku(), responseBody.getSku());
        assertEquals(request.getDescription(), responseBody.getDescription());
        assertNotNull(responseBody.getQuantityInStock());
        assertEquals(request.getMinimumStock(), responseBody.getMinimumStock());
        assertEquals(true, responseBody.getActive());
    }

    @Test
    void shouldSaveProductWhenSavingWithNullDescription() throws JsonProcessingException {
        Brand savedBrand = createBrand(brand);
        Category savedCategory = createCategory(category);
        Specification savedSpecification = createSpecification(specificationEntity);

        CategorySpecification categorySpecification = new CategorySpecification(
                null, true, savedCategory, savedSpecification
        );

        createCategorySpecification(categorySpecification);

        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setBrandId(savedBrand.getId());
        request.setSpecifications(List.of(
                new ProductSpecificationRequestDTO(
                        savedSpecification.getId(),
                        "1920x1080",
                        null,
                        null
                ))
        );

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString();

        ProductDTO responseBody = objectMapper.readValue(content, ProductDTO.class);

        assertNull(responseBody.getDescription());
    }

    @Test
    void shouldNotSaveWhenBrandIdIsInvalid() {
        Category savedCategory = createCategory(category);
        Specification savedSpecification = createSpecification(specificationEntity);

        CategorySpecification categorySpecification = new CategorySpecification(
                null, true, savedCategory, savedSpecification
        );

        createCategorySpecification(categorySpecification);

        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setBrandId(invalidId);
        request.setSpecifications(List.of(
                new ProductSpecificationRequestDTO(
                        savedSpecification.getId(),
                        "1920x1080",
                        null,
                        null
                ))
        );

        CustomError error = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals(brandNotFoundMsg, error.getMessage());
        assertEquals(PATH, error.getPath());
    }

    @Test
    void shouldNotSaveWhenCategoryIdsIsInvalid() {
        Brand savedBrand = createBrand(brand);
        Specification savedSpecification = createSpecification(specificationEntity);

        request.setCategoryIds(Set.of(invalidId));
        request.setBrandId(savedBrand.getId());
        request.setSpecifications(List.of(
                new ProductSpecificationRequestDTO(
                        savedSpecification.getId(),
                        "1920x1080",
                        null,
                        null
                ))
        );

        CustomError error = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals(categoryNotFoundMsg, error.getMessage());
        assertEquals(PATH, error.getPath());
    }

    @Test
    void shouldNotSaveWhenSkuExists() {
        Brand savedBrand = createBrand(brand);
        Category savedCategory = createCategory(category);
        Specification savedSpecification = createSpecification(specificationEntity);

        CategorySpecification categorySpecification = new CategorySpecification(
                null, true, savedCategory, savedSpecification
        );

        createCategorySpecification(categorySpecification);

        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setBrandId(savedBrand.getId());

        request.setSpecifications(List.of(
                new ProductSpecificationRequestDTO(
                        savedSpecification.getId(),
                        "1920x1080",
                        null,
                        null
                )));

        given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString();

        CustomError error = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(409)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(409, error.getStatus());
        assertEquals(skuExistsMsg, error.getMessage());
        assertEquals(PATH, error.getPath());
    }

    @Test
    void shouldNotSaveWhenSpecificationIdsIsInvalid() {
        Brand savedBrand = createBrand(brand);
        Category savedCategory = createCategory(category);

        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setBrandId(savedBrand.getId());
        request.setSpecifications(List.of(
                new ProductSpecificationRequestDTO(
                        invalidId,
                        "1920x1080",
                        null,
                        null
                ))
        );

        CustomError error = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals(specificationNotFoundMsg, error.getMessage());
        assertEquals(PATH, error.getPath());
    }

    @Test
    void shouldNotSaveProductWhenSavingWithMissingRequiredSpecifications() {
        Brand savedBrand = createBrand(brand);
        Category savedCategory = createCategory(category);

        Specification newSpecification = SpecificationBuilder
                .builder()
                .withId(null)
                .withName("Frequência")
                .withUnit(null)
                .withFilterable(true)
                .withSpecificationType(SpecificationType.NUMBER)
                .withCreatedAt(null)
                .withUpdatedAt(null)
                .build();
        Specification savedSpecification = createSpecification(specificationEntity);
        Specification newSavedSpecification = createSpecification(newSpecification);

        CategorySpecification categorySpecification = new CategorySpecification(
                null, false, savedCategory, savedSpecification
        );

        CategorySpecification newCategorySpecification = new CategorySpecification(
                null, true, savedCategory, newSavedSpecification
        );

        createCategorySpecification(categorySpecification);
        createCategorySpecification(newCategorySpecification);

        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setBrandId(savedBrand.getId());

        request.setSpecifications(List.of(
                new ProductSpecificationRequestDTO(
                        savedSpecification.getId(),
                        "1920x1080",
                        null,
                        null
                ))
        );

        CustomError error = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(missingSpecMsg, error.getMessage());
        assertEquals(PATH, error.getPath());
    }

    @Test
    void shouldNotSaveWhenSavingWithNullName() throws JsonProcessingException {
        request.setName(null);

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(PATH, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Name is required", errors.get("name"));
    }

    @Test
    void shouldNotSaveWhenSavingWithTooLongName() throws JsonProcessingException {
        request.setName("e".repeat(201));

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("Name must contain between 2 and 200 characters", errors.get("name"));
    }

    @Test
    void shouldNotSaveWhenSavingWithTooShortName() throws JsonProcessingException {
        request.setName("e");

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("Name must contain between 2 and 200 characters", errors.get("name"));
    }

    @Test
    void shouldNotSaveWhenSavingWithNullSalePrice() throws JsonProcessingException {
        request.setSalePrice(null);

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("Sale Price is required", errors.get("salePrice"));
    }

    @Test
    void shouldNotSaveWhenSavingWithNegativeSalePrice() throws JsonProcessingException {
        request.setSalePrice(BigDecimal.valueOf(-1));

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("Sale Price cannot be equal to zero or negative", errors.get("salePrice"));
    }

    @Test
    void shouldNotSaveWhenSavingWithZeroSalePrice() throws JsonProcessingException {
        request.setSalePrice(BigDecimal.ZERO);

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("Sale Price cannot be equal to zero or negative", errors.get("salePrice"));
    }

    @Test
    void shouldNotSaveWhenSavingWithNullSku() throws JsonProcessingException {
        request.setSku(null);

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(PATH, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("SKU is required", errors.get("sku"));
    }

    @Test
    void shouldNotSaveWhenSavingWithTooLongSku() throws JsonProcessingException {
        request.setSku("e".repeat(31));

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("SKU must contain between 5 and 30 characters", errors.get("sku"));
    }

    @Test
    void shouldNotSaveWhenSavingWithTooShortSku() throws JsonProcessingException {
        request.setSku("e");

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("SKU must contain between 5 and 30 characters", errors.get("sku"));
    }

    @Test
    void shouldNotSaveWhenSavingWithNullMinimumStock() throws JsonProcessingException {
        request.setMinimumStock(null);

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(PATH, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Minimum Stock is required", errors.get("minimumStock"));
    }

    @Test
    void shouldNotSaveWhenSavingWithNullBrandId() throws JsonProcessingException {
        request.setBrandId(null);

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(PATH, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Brand ID is required", errors.get("brandId"));
    }

    @Test
    void shouldNotSaveWhenSavingWithNullCategoryId() throws JsonProcessingException {
        request.setCategoryIds(null);

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(PATH, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Category ID is required", errors.get("categoryIds"));
    }

    @Test
    void shouldNotSaveWhenSavingWithNullSpecifications() throws JsonProcessingException {
        request.setSpecifications(null);

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(PATH, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Specifications are required", errors.get("specifications"));
    }

    private Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    private Specification createSpecification(Specification specification) {
        return specificationRepository.save(specification);
    }

    private Brand createBrand(Brand brand) {
        return brandRepository.save(brand);
    }

    private CategorySpecification createCategorySpecification(CategorySpecification categorySpecification) {
        return categorySpecificationRepository.save(categorySpecification);
    }
}
