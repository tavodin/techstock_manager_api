package io.github.tavodin.techstock_manager.integrationtests.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.builder.ProductBuilder;
import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.configurations.AbstractIntegrationTest;
import io.github.tavodin.techstock_manager.dto.*;
import io.github.tavodin.techstock_manager.dto.error.CustomError;
import io.github.tavodin.techstock_manager.dto.error.FieldError;
import io.github.tavodin.techstock_manager.dto.error.ValidationError;
import io.github.tavodin.techstock_manager.entities.*;
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
import java.util.Optional;
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
    private UnitRepository unitRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private static RequestSpecification specification;
    private String token;

    private static final String PATH = "/products";
    private static final String CONTENT_TYPE = "application/json";

    private Long validId = 1L;
    private Long invalidId = Long.MAX_VALUE;
    private String productNotFoundMsg = "Product not found";
    private String validationErrorMsg = "Entity validation error";
    private String brandNotFoundMsg = "Brand not found";
    private String categoryNotFoundMsg = "One or more categories were not found";
    private String specificationNotFoundMsg = "One or more specifications were not found";
    private String skuExistsMsg = "SKU already exists";
    private String missingSpecMsg = "Missing required specifications";

    private ProductSaveDTO request;
    private ProductUpdateDTO updateDTO;
    private Category category;
    private Brand brand;
    private Specification specificationEntity;
    private Unit unit;

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
        brand.setName("DELL");

        request = new ProductSaveDTO();
        request.setName("Monitor DELL");
        request.setSalePrice(BigDecimal.valueOf(1999.99));
        request.setSku("MON-001");
        request.setDescription(null);
        request.setMinimumStock(5);

        updateDTO = new ProductUpdateDTO(
                "Teclado Logitech sem fio",
                BigDecimal.valueOf(399.99),
                "Teclado sem fio para escritório",
                "TEC-001",
                3,
                validId,
                Set.of(validId)
        );

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

        unit = new Unit("Hertz", "Hz");

        productSpecificationRepository.deleteAll();
        productRepository.deleteAll();
        specificationRepository.deleteAll();
        unitRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();
    }

    @Test
    void shouldFindProductWhenFindingWithValidId() throws JsonProcessingException {
        Brand savedBrand = saveBrand(brand);

        Product product = new Product(
                request.getName(),
                BigDecimal.ZERO,
                request.getSalePrice(),
                request.getDescription(),
                request.getSku(),
                0,
                request.getMinimumStock(),
                true,
                savedBrand,
                null,
                null
        );

        product = productRepository.save(product);

        var content = given()
                .spec(specification)
                .pathParam("id", product.getId())
                .get("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        ProductDTO responseBody = objectMapper.readValue(content, ProductDTO.class);

        assertEquals(product.getId(), responseBody.getId());
        assertEquals(request.getName(), responseBody.getName());
        assertTrue(responseBody.getCostPrice().compareTo(BigDecimal.ZERO) == 0);
        assertEquals(request.getSalePrice(), responseBody.getSalePrice());
        assertEquals(request.getDescription(), responseBody.getDescription());
        assertEquals(request.getSku(), responseBody.getSku());
        assertEquals(Integer.valueOf(0), responseBody.getQuantityInStock());
        assertEquals(request.getMinimumStock(), responseBody.getMinimumStock());
        assertEquals(true, responseBody.getActive());
    }

    @Test
    void shouldNotFoundWhenFindingWithInvalidId() {
        CustomError error = given()
                .spec(specification)
                .pathParam("id", invalidId)
                .get("{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals("Product not found", error.getMessage());
        assertEquals(PATH + "/" + invalidId, error.getPath());
    }

    @Test
    void shouldSaveProductWhenSavingWithValidData() throws JsonProcessingException {
        Brand savedBrand = saveBrand(brand);
        Category savedCategory = saveCategory(category);
        Specification savedSpecification = saveSpecification(specificationEntity);

        CategorySpecification categorySpecification = new CategorySpecification(
                null, true, savedCategory, savedSpecification
        );

        saveCategorySpecification(categorySpecification);

        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setBrandId(savedBrand.getId());

        request.setSpecifications(List.of(
                new ProductSpecificationSaveDTO(
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
    void shouldSaveWhenSavingWithValidSpecifications() throws JsonProcessingException {
        Brand savedBrand = saveBrand(brand);
        Category savedCategory = saveCategory(category);
        Map<String, Specification> specifications = createSpecifications(savedCategory);

        request.setBrandId(savedBrand.getId());
        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setSpecifications(List.of(
                        new ProductSpecificationSaveDTO(
                                specifications.get("resolution").getId(),
                                "1920x1080",
                                null,
                                null
                        ),
                        new ProductSpecificationSaveDTO(
                                specifications.get("refresh").getId(),
                                null,
                                60.0,
                                null
                        )
                )
        );

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .asString();

        ProductDTO response = objectMapper.readValue(content, ProductDTO.class);

        assertNotNull(response.getId());
        assertEquals(request.getName(), response.getName());
    }

    @Test
    void shouldNotSaveWhenSavingProductWithMissingRequiredSpecification() {
        Brand savedBrand = saveBrand(brand);
        Category savedCategory = saveCategory(category);
        Map<String, Specification> specifications = createSpecifications(savedCategory);

        request.setBrandId(savedBrand.getId());
        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setSpecifications(List.of(
                        new ProductSpecificationSaveDTO(
                                specifications.get("refresh").getId(),
                                null,
                                60.0,
                                null
                        )
                )
        );

        CustomError error = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals("Missing required specifications", error.getMessage());
        assertEquals(400, error.getStatus());
        assertEquals(PATH, error.getPath());
    }

    @Test
    void shouldSaveProductWhenSavingWithNullDescription() throws JsonProcessingException {
        Brand savedBrand = saveBrand(brand);
        Category savedCategory = saveCategory(category);
        Specification savedSpecification = saveSpecification(specificationEntity);

        CategorySpecification categorySpecification = new CategorySpecification(
                null, true, savedCategory, savedSpecification
        );

        saveCategorySpecification(categorySpecification);

        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setBrandId(savedBrand.getId());
        request.setSpecifications(List.of(
                new ProductSpecificationSaveDTO(
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
        Category savedCategory = saveCategory(category);
        Specification savedSpecification = saveSpecification(specificationEntity);

        CategorySpecification categorySpecification = new CategorySpecification(
                null, true, savedCategory, savedSpecification
        );

        saveCategorySpecification(categorySpecification);

        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setBrandId(invalidId);
        request.setSpecifications(List.of(
                new ProductSpecificationSaveDTO(
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
        Brand savedBrand = saveBrand(brand);
        Specification savedSpecification = saveSpecification(specificationEntity);

        request.setCategoryIds(Set.of(invalidId));
        request.setBrandId(savedBrand.getId());
        request.setSpecifications(List.of(
                new ProductSpecificationSaveDTO(
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
        Brand savedBrand = saveBrand(brand);
        Category savedCategory = saveCategory(category);
        Specification savedSpecification = saveSpecification(specificationEntity);

        CategorySpecification categorySpecification = new CategorySpecification(
                null, true, savedCategory, savedSpecification
        );

        saveCategorySpecification(categorySpecification);

        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setBrandId(savedBrand.getId());

        request.setSpecifications(List.of(
                new ProductSpecificationSaveDTO(
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
        Brand savedBrand = saveBrand(brand);
        Category savedCategory = saveCategory(category);

        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setBrandId(savedBrand.getId());
        request.setSpecifications(List.of(
                new ProductSpecificationSaveDTO(
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
        Brand savedBrand = saveBrand(brand);
        Category savedCategory = saveCategory(category);

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

        Specification savedSpecification = saveSpecification(specificationEntity);
        Specification newSavedSpecification = saveSpecification(newSpecification);

        CategorySpecification categorySpecification = new CategorySpecification(
                null, false, savedCategory, savedSpecification
        );

        CategorySpecification newCategorySpecification = new CategorySpecification(
                null, true, savedCategory, newSavedSpecification
        );

        saveCategorySpecification(categorySpecification);
        saveCategorySpecification(newCategorySpecification);

        request.setCategoryIds(Set.of(savedCategory.getId()));
        request.setBrandId(savedBrand.getId());

        request.setSpecifications(List.of(
                new ProductSpecificationSaveDTO(
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

    @Test
    void shouldUpdateWhenUpdatingWithValidData() throws JsonProcessingException {
        Brand savedBrand = saveBrand(brand);
        Category savedCategory = saveCategory(category);

        Product product = ProductBuilder.builder().withId(null).withCostPrice(BigDecimal.ZERO).build();
        product.setBrand(savedBrand);
        product.setCategories(Set.of(savedCategory));

        product = productRepository.save(product);

        Brand updateBrand = saveBrand(new Brand(null, "Logitech", null, null));
        Category updateCategory = saveCategory(new Category(null, "Teclado", null, null));

        updateDTO.setBrandId(updateBrand.getId());
        updateDTO.setCategoryIds(Set.of(updateCategory.getId()));

        var content = given()
                .spec(specification)
                .pathParam("id", product.getId())
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        ProductDTO responseBody = objectMapper.readValue(content, ProductDTO.class);

        assertEquals(product.getId(), responseBody.getId());
        assertEquals(updateDTO.getName(), responseBody.getName());
        assertTrue(responseBody.getCostPrice().compareTo(BigDecimal.ZERO) == 0);
        assertEquals(updateDTO.getDescription(), responseBody.getDescription());
        assertEquals(updateDTO.getMinimumStock(), responseBody.getMinimumStock());
        assertEquals(updateDTO.getSalePrice(), responseBody.getSalePrice());
    }

    @Test
    void shouldUpdateWhenUpdatingProductWithNullDescription() throws JsonProcessingException {
        Brand savedBrand = saveBrand(brand);
        Category savedCategory = saveCategory(category);

        Product product = ProductBuilder.builder()
                .withId(null)
                .withDescription(null)
                .build();

        product.setBrand(savedBrand);
        product.setCategories(Set.of(savedCategory));

        product = productRepository.save(product);

        Brand updateBrand = saveBrand(new Brand(null, "Logitech", null, null));
        Category updateCategory = saveCategory(new Category(null, "Teclado", null, null));

        updateDTO.setDescription(null);
        updateDTO.setBrandId(updateBrand.getId());
        updateDTO.setCategoryIds(Set.of(updateCategory.getId()));

        var content = given()
                .spec(specification)
                .pathParam("id", product.getId())
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        ProductDTO body = objectMapper.readValue(content, ProductDTO.class);

        assertNull(body.getDescription());
    }

    @Test
    void shouldNotUpdateWhenUpdatingWithInvalidProductId() {
        CustomError error = given()
                .spec(specification)
                .pathParam("id", invalidId)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(productNotFoundMsg, error.getMessage());
    }

    @Test
    void shouldNotUpdateWhenUpdatingWithExitSku() {
        Brand savedBrand = saveBrand(brand);
        Category savedCategory = saveCategory(category);

        Product product = ProductBuilder.builder().withId(null).build();
        product.setBrand(savedBrand);
        product.setCategories(Set.of(savedCategory));

        product = productRepository.save(product);

        Brand newBrand = saveBrand(new Brand(null, "Logitech", null, null));
        Category newCategory = saveCategory(new Category(null, "Teclado", null, null));

        Product newProduct = ProductBuilder.builder().withId(null).withSku("TEC-001").build();
        newProduct.setBrand(newBrand);
        newProduct.setCategories(Set.of(newCategory));

        newProduct = productRepository.save(newProduct);

        ProductUpdateDTO newUpdateDTO = new ProductUpdateDTO(
                "Teclado Logitech sem fio",
                BigDecimal.valueOf(399.99),
                "Teclado sem fio para escritório",
                newProduct.getSku(),
                3,
                savedBrand.getId(),
                Set.of(savedCategory.getId())
        );

        CustomError error = given()
                .spec(specification)
                .pathParam("id", product.getId())
                .contentType(CONTENT_TYPE)
                .body(newUpdateDTO)
                .put("{id}")
                .then()
                .statusCode(409)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(409, error.getStatus());
        assertEquals(PATH + "/" + product.getId(), error.getPath());
        assertEquals(skuExistsMsg, error.getMessage());
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithInvalidBrandId() {
        Brand savedBrand = saveBrand(brand);
        Category savedCategory = saveCategory(category);

        Product product = ProductBuilder.builder().withId(null).build();
        product.setBrand(savedBrand);
        product.setCategories(Set.of(savedCategory));

        product = productRepository.save(product);

        updateDTO.setBrandId(invalidId);
        updateDTO.setCategoryIds(Set.of(category.getId()));

        CustomError error = given()
                .pathParam("id", product.getId())
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(brandNotFoundMsg, error.getMessage());
        assertEquals(404, error.getStatus());
        assertEquals(PATH + "/" + product.getId(), error.getPath());
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithInvalidCategoryIds() {
        Brand savedBrand = saveBrand(brand);
        Category savedCategory = saveCategory(category);

        Product product = ProductBuilder.builder().withId(null).build();
        product.setBrand(savedBrand);
        product.setCategories(Set.of(savedCategory));

        product = productRepository.save(product);

        updateDTO.setBrandId(savedBrand.getId());
        updateDTO.setCategoryIds(Set.of(invalidId));

        CustomError error = given()
                .pathParam("id", product.getId())
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(categoryNotFoundMsg, error.getMessage());
        assertEquals(404, error.getStatus());
        assertEquals(PATH + "/" + product.getId(), error.getPath());
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithNullName() throws JsonProcessingException {
        updateDTO.setName(null);
        var content = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
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
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Name is required", errors.get("name"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithTooShortName() throws JsonProcessingException {
        updateDTO.setName("e");
        var content = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
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
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Name must contain between 2 and 200 characters", errors.get("name"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithTooLongName() throws JsonProcessingException {
        updateDTO.setName("e".repeat(201));
        var content = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
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
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Name must contain between 2 and 200 characters", errors.get("name"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithNullSalePrice() throws JsonProcessingException {
        updateDTO.setSalePrice(null);
        var content = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
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
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Sale Price is required", errors.get("salePrice"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithZeroCostPrice() throws JsonProcessingException {
        updateDTO.setSalePrice(BigDecimal.ZERO);
        var content = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
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
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Sale Price cannot be equal to zero or negative", errors.get("salePrice"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithNegativeCostPrice() throws JsonProcessingException {
        updateDTO.setSalePrice(BigDecimal.valueOf(-10.1));
        var content = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
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
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Sale Price cannot be equal to zero or negative", errors.get("salePrice"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithNullSku() throws JsonProcessingException {
        updateDTO.setSku(null);
        var content = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
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
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("SKU is required", errors.get("sku"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithTooShortSku() throws JsonProcessingException {
        updateDTO.setSku("e");
        var content = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
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
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("SKU must contain between 5 and 30 characters", errors.get("sku"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithTooLongSku() throws JsonProcessingException {
        updateDTO.setSku("e".repeat(31));
        var content = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
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
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("SKU must contain between 5 and 30 characters", errors.get("sku"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithNullMinimumStock() throws JsonProcessingException {
        updateDTO.setMinimumStock(null);
        var content = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
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
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Minimum Stock is required", errors.get("minimumStock"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithZeroMinimumStock() throws JsonProcessingException {
        updateDTO.setMinimumStock(0);
        var content = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
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
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Minimum Stock cannot be equal to zero or negative", errors.get("minimumStock"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithNegativeMinimumStock() throws JsonProcessingException {
        updateDTO.setMinimumStock(-1);
        var content = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
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
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Minimum Stock cannot be equal to zero or negative", errors.get("minimumStock"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithNullBrandId() throws JsonProcessingException {
        updateDTO.setBrandId(null);
        var content = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
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
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Brand ID is required", errors.get("brandId"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingProductWithNullCategoryIds() throws JsonProcessingException {
        updateDTO.setCategoryIds(null);
        var content = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(updateDTO)
                .put("{id}")
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
        assertEquals(PATH + "/" + invalidId, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Category ID is required", errors.get("categoryIds"));
    }

    @Test
    void shouldDeleteProductWhenDeletingWithValidId() throws JsonProcessingException {
        Brand savedBrand = saveBrand(brand);
        Category savedCategory = saveCategory(category);

        Product product = ProductBuilder
                .builder()
                .withId(null)
                .build();

        product.setBrand(savedBrand);
        product.setCategories(Set.of(savedCategory));

        product = productRepository.save(product);

        given()
                .pathParam("id", product.getId())
                .spec(specification)
                .delete("{id}")
                .then()
                .statusCode(204);

        var content = given()
                .pathParam("id", product.getId())
                .spec(specification)
                .get("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        ProductDTO response = objectMapper.readValue(content, ProductDTO.class);

        assertEquals(false, response.getActive());
    }

    @Test
    void shouldNotDeleteWhenDeletingProductWithInvalidId() {
        CustomError error = given()
                .pathParam("id", invalidId)
                .spec(specification)
                .delete("{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals("Product not found", error.getMessage());
        assertEquals(PATH + "/" + invalidId, error.getPath());
    }

    @Test
    void shouldFindAllSpecificationWhenFindAllWithValidProductId() throws JsonProcessingException {
        Product savedProduct = createProduct();
        Optional<Category> findCategory = savedProduct.getCategories().stream().findFirst();
        Map<String, Specification> specifications = createSpecifications(findCategory.get());

        Specification resolution = specifications.get("resolution");
        ProductSpecification prodSpecResolution = new ProductSpecification();
        prodSpecResolution.setValueString("1920x1080");
        prodSpecResolution.setProduct(savedProduct);
        prodSpecResolution.setSpecification(resolution);

        Specification refresh = specifications.get("refresh");
        ProductSpecification prodSpecRefresh = new ProductSpecification();
        prodSpecRefresh.setValueNumber(60.0);
        prodSpecRefresh.setProduct(savedProduct);
        prodSpecRefresh.setSpecification(refresh);

        prodSpecResolution = productSpecificationRepository.save(prodSpecResolution);
        prodSpecRefresh = productSpecificationRepository.save(prodSpecRefresh);

        savedProduct.setSpecifications(Set.of(prodSpecResolution, prodSpecRefresh));

        savedProduct = productRepository.save(savedProduct);

        var content = given()
                .spec(specification)
                .pathParam("id", savedProduct.getId())
                .get("/{id}/specifications")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        List<ProductSpecificationListDTO> response = objectMapper.readValue(content,
                new TypeReference<List<ProductSpecificationListDTO>>() {}
        );

        ProductSpecificationListDTO responseResolution = response.get(0);
        ProductSpecificationListDTO responseRefresh = response.get(1);

        assertEquals(prodSpecResolution.getId() , responseResolution.getId());
        assertEquals(savedProduct.getId(), responseResolution.getProductId());
        assertEquals(resolution.getId(), responseResolution.getSpecificationId());
        assertEquals(prodSpecResolution.getValueString(), responseResolution.getValueString());
        assertNull(responseResolution.getValueNumber());
        assertNull(responseResolution.getValueBoolean());
        assertEquals(resolution.getName(), responseResolution.getSpecificationName());
        assertNull(responseResolution.getUnitSymbol());

        assertEquals(prodSpecRefresh.getId() , responseRefresh.getId());
        assertEquals(savedProduct.getId(), responseRefresh.getProductId());
        assertEquals(refresh.getId(), responseRefresh.getSpecificationId());
        assertEquals(prodSpecRefresh.getValueNumber(), responseRefresh.getValueNumber());
        assertNull(responseRefresh.getValueString());
        assertNull(responseRefresh.getValueBoolean());
        assertEquals(refresh.getName(), responseRefresh.getSpecificationName());
        assertEquals(refresh.getUnit().getSymbol(), responseRefresh.getUnitSymbol());
    }

    @Test
    void shouldNotGetSpecificationWhenFindAllSpecificationWithInvalidProductId() {
        CustomError error = given()
                .spec(specification)
                .pathParam("id", invalidId)
                .get("/{id}/specifications")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals("Product not found", error.getMessage());
        assertEquals(PATH + "/" + invalidId + "/specifications", error.getPath());
    }

    @Test
    void shouldSaveSpecificationWhenSavingWithValidData() throws JsonProcessingException {
        Product savedProduct = createProduct();
        Specification savedSpecification = saveSpecification(specificationEntity);

        ProductSpecificationSaveDTO saveRequest = new ProductSpecificationSaveDTO(
                savedSpecification.getId(), "1920x1080", null, null
        );

        var content = given()
                .spec(specification)
                .pathParam("id", savedProduct.getId())
                .contentType(CONTENT_TYPE)
                .body(saveRequest)
                .post("/{id}/specifications")
                .then()
                .statusCode(201)
                .extract()
                .asString();

        var specifications = given()
                .spec(specification)
                .pathParam("id", savedProduct.getId())
                .get("/{id}/specifications")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        ProductSpecificationDTO response = objectMapper.readValue(content, ProductSpecificationDTO.class);
        List<ProductSpecificationListDTO> specificationsResponse = objectMapper.readValue(specifications,
                new TypeReference<List<ProductSpecificationListDTO>>() {}
        );

        ProductSpecificationListDTO responseResolution = specificationsResponse.get(0);

        assertEquals(responseResolution.getId(), response.getId());
        assertEquals(saveRequest.valueString(), response.getValueString());
        assertNull(response.getValueBoolean());
        assertNull(response.getValueNumber());

        assertEquals(savedProduct.getId(), responseResolution.getProductId());
        assertEquals(savedSpecification.getId(), responseResolution.getSpecificationId());
        assertEquals(savedSpecification.getName(), responseResolution.getSpecificationName());
        assertEquals(saveRequest.valueString(), responseResolution.getValueString());
        assertNull(responseResolution.getValueBoolean());
        assertNull(responseResolution.getValueNumber());
        assertNull(responseResolution.getUnitSymbol());
    }

    @Test
    void shouldNotSaveWhenSavingSpecificationWithInvalidProductId() {
        ProductSpecificationSaveDTO saveRequest = new ProductSpecificationSaveDTO(
                invalidId, null, null, null
        );

        CustomError error = given()
                .spec(specification)
                .pathParam("id", invalidId)
                .contentType(CONTENT_TYPE)
                .body(saveRequest)
                .post("/{id}/specifications")
                .then()
                .statusCode(404)
                .extract()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals("Product not found", error.getMessage());
        assertEquals(PATH + "/" + invalidId + "/specifications", error.getPath());
    }

    @Test
    void shouldNotSaveWhenSavingSpecificationWithNullSpecificationId() throws JsonProcessingException {
        ProductSpecificationSaveDTO saveRequest = new ProductSpecificationSaveDTO(
                null, "1920x1080", null, null
        );

        var content = given()
                .spec(specification)
                .pathParam("id", invalidId)
                .contentType(CONTENT_TYPE)
                .body(saveRequest)
                .post("/{id}/specifications")
                .then()
                .statusCode(400)
                .extract()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(PATH + "/" + invalidId + "/specifications", error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Specification ID is required", errors.get("specificationId"));
    }

    @Test
    void shouldNotSaveWhenSavingSpecificationWithTooLongValueString() throws JsonProcessingException {
        ProductSpecificationSaveDTO saveRequest = new ProductSpecificationSaveDTO(
                null, "e".repeat(46), null, null
        );

        var content = given()
                .spec(specification)
                .pathParam("id", invalidId)
                .contentType(CONTENT_TYPE)
                .body(saveRequest)
                .post("/{id}/specifications")
                .then()
                .statusCode(400)
                .extract()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(PATH + "/" + invalidId + "/specifications", error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Value must contain a maximum of 45 characters", errors.get("valueString"));
    }

    private Product createProduct() {
        Brand savedBrand = saveBrand(brand); // DELL
        Category savedCategory = saveCategory(category); // MONITOR
        Product product = ProductBuilder
                .builder()
                .withId(null)
                .build();

        product.setBrand(savedBrand);
        product.setCategories(Set.of(savedCategory));

        return productRepository.save(product);
    }

    @Test
    void shouldNotSaveSpecificationWhenSavingWithNullValueNumber() {
        Product savedProduct = createProduct();
        specificationEntity.setDataType(SpecificationType.NUMBER);
        Specification savedSpecification = saveSpecification(specificationEntity);

        ProductSpecificationSaveDTO updateRequest = new ProductSpecificationSaveDTO(
                savedSpecification.getId(), null, null, null
        );

        CustomError error = given()
                .spec(specification)
                .pathParam("id", savedProduct.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .post("/{id}/specifications")
                .then()
                .statusCode(400)
                .extract()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals("Value Number cannot be null", error.getMessage());
        assertEquals(PATH + "/" + savedProduct.getId() + "/specifications", error.getPath());
    }

    @Test
    void shouldNotSaveSpecificationWhenSavingWithNullValueString() {
        Product savedProduct = createProduct();
        specificationEntity.setDataType(SpecificationType.STRING);
        Specification savedSpecification = saveSpecification(specificationEntity);

        ProductSpecificationSaveDTO updateRequest = new ProductSpecificationSaveDTO(
                savedSpecification.getId(), null, null, null
        );

        CustomError error = given()
                .spec(specification)
                .pathParam("id", savedProduct.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .post("/{id}/specifications")
                .then()
                .statusCode(400)
                .extract()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals("Value String cannot be null", error.getMessage());
        assertEquals(PATH + "/" + savedProduct.getId() + "/specifications", error.getPath());
    }

    @Test
    void shouldNotSaveSpecificationWhenSavingWithNullValueBoolean() {
        Product savedProduct = createProduct();
        specificationEntity.setDataType(SpecificationType.BOOLEAN);
        Specification savedSpecification = saveSpecification(specificationEntity);

        ProductSpecificationSaveDTO updateRequest = new ProductSpecificationSaveDTO(
                savedSpecification.getId(), null, null, null
        );

        CustomError error = given()
                .spec(specification)
                .pathParam("id", savedProduct.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .post("/{id}/specifications")
                .then()
                .statusCode(400)
                .extract()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals("Value Boolean cannot be null", error.getMessage());
        assertEquals(PATH + "/" + savedProduct.getId() + "/specifications", error.getPath());
    }

    @Test
    void shouldUpdateSpecificationWhenUpdatingWithValidData() throws JsonProcessingException {
        Product savedProduct = createProduct();
        Optional<Category> findCategory = savedProduct.getCategories().stream().findFirst();
        Map<String, Specification> specifications = createSpecifications(findCategory.get());

        Specification resolution = specifications.get("resolution");
        ProductSpecification prodSpecResolution = new ProductSpecification();
        prodSpecResolution.setValueString("1920x1080");
        prodSpecResolution.setProduct(savedProduct);
        prodSpecResolution.setSpecification(resolution);

        Specification refresh = specifications.get("refresh");
        ProductSpecification prodSpecRefresh = new ProductSpecification();
        prodSpecRefresh.setValueNumber(60.0);
        prodSpecRefresh.setProduct(savedProduct);
        prodSpecRefresh.setSpecification(refresh);

        prodSpecResolution = productSpecificationRepository.save(prodSpecResolution);
        prodSpecRefresh = productSpecificationRepository.save(prodSpecRefresh);

        savedProduct.setSpecifications(Set.of(prodSpecResolution, prodSpecRefresh));

        savedProduct = productRepository.save(savedProduct);

        ProductSpecificationUpdateDTO updateRequest = new ProductSpecificationUpdateDTO(
                "2560x1440", null, null
        );

        var content = given()
                .spec(specification)
                .pathParam("prodId", savedProduct.getId())
                .pathParam("specId", resolution.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("/{prodId}/specifications/{specId}")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        ProductSpecificationDTO response = objectMapper.readValue(content, ProductSpecificationDTO.class);

        assertNotNull(response.getId());
        assertEquals(updateRequest.valueString(), response.getValueString());
        assertNull(response.getValueNumber());
        assertNull(response.getValueBoolean());
    }

    @Test
    void shouldNotUpdateSpecificationWhenProductAndSpecificationDoNotMatch() {
        Product savedProduct = createProduct();
        ProductSpecificationUpdateDTO updateRequest = new ProductSpecificationUpdateDTO(
                "2560x1440", null, null
        );

       CustomError error = given()
                .spec(specification)
                .pathParam("prodId", savedProduct.getId())
                .pathParam("specId", invalidId)
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("/{prodId}/specifications/{specId}")
                .then()
                .statusCode(404)
                .extract()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals("Product Specification not found", error.getMessage());
        assertEquals(PATH + "/" + savedProduct.getId() + "/specifications/" + invalidId,
                error.getPath());
    }

    @Test
    void shouldNotUpdateSpecificationWhenUpdatingWithNullValueNumber() {
        Product savedProduct = createProduct();
        Optional<Category> findCategory = savedProduct.getCategories().stream().findFirst();
        Map<String, Specification> specifications = createSpecifications(findCategory.get());

        Specification refresh = specifications.get("refresh");
        ProductSpecification prodSpecRefresh = new ProductSpecification();
        prodSpecRefresh.setValueNumber(60.0);
        prodSpecRefresh.setProduct(savedProduct);
        prodSpecRefresh.setSpecification(refresh);

        prodSpecRefresh = productSpecificationRepository.save(prodSpecRefresh);

        savedProduct.setSpecifications(Set.of(prodSpecRefresh));

        savedProduct = productRepository.save(savedProduct);

        ProductSpecificationUpdateDTO updateRequest = new ProductSpecificationUpdateDTO(
                null, null, null
        );

        CustomError error = given()
                .spec(specification)
                .pathParam("prodId", savedProduct.getId())
                .pathParam("specId", refresh.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("/{prodId}/specifications/{specId}")
                .then()
                .statusCode(400)
                .extract()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals("Value Number cannot be null", error.getMessage());
        assertEquals(PATH + "/" + savedProduct.getId() + "/specifications/" + refresh.getId(),
                error.getPath());
    }

    @Test
    void shouldNotUpdateSpecificationWhenUpdatingWithNullValueString() {
        Product savedProduct = createProduct();
        Optional<Category> findCategory = savedProduct.getCategories().stream().findFirst();
        Map<String, Specification> specifications = createSpecifications(findCategory.get());

        Specification resolution = specifications.get("resolution");
        ProductSpecification prodSpecResolution = new ProductSpecification();
        prodSpecResolution.setValueString("1920x1080");
        prodSpecResolution.setProduct(savedProduct);
        prodSpecResolution.setSpecification(resolution);

        prodSpecResolution = productSpecificationRepository.save(prodSpecResolution);

        savedProduct.setSpecifications(Set.of(prodSpecResolution));

        savedProduct = productRepository.save(savedProduct);

        ProductSpecificationUpdateDTO updateRequest = new ProductSpecificationUpdateDTO(
                null, null, null
        );

        CustomError error = given()
                .spec(specification)
                .pathParam("prodId", savedProduct.getId())
                .pathParam("specId", resolution.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("/{prodId}/specifications/{specId}")
                .then()
                .statusCode(400)
                .extract()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals("Value String cannot be null", error.getMessage());
        assertEquals(PATH + "/" + savedProduct.getId() + "/specifications/" + resolution.getId(),
                error.getPath());
    }

    @Test
    void shouldNotUpdateSpecificationWhenUpdatingWithNullValueBoolean() {
        Product savedProduct = createProduct();

        Specification savedSpec = saveSpecification(SpecificationBuilder
                .builder()
                .withId(null)
                .withName("Gamer")
                .withSpecificationType(SpecificationType.BOOLEAN)
                .withUnit(null)
                .build());

        ProductSpecification prodSpecResolution = new ProductSpecification();
        prodSpecResolution.setValueBoolean(false);
        prodSpecResolution.setProduct(savedProduct);
        prodSpecResolution.setSpecification(savedSpec);

        prodSpecResolution = productSpecificationRepository.save(prodSpecResolution);

        savedProduct.setSpecifications(Set.of(prodSpecResolution));

        savedProduct = productRepository.save(savedProduct);

        ProductSpecificationUpdateDTO updateRequest = new ProductSpecificationUpdateDTO(
                null, null, null
        );

        CustomError error = given()
                .spec(specification)
                .pathParam("prodId", savedProduct.getId())
                .pathParam("specId", savedSpec.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("/{prodId}/specifications/{specId}")
                .then()
                .statusCode(400)
                .extract()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals("Value Boolean cannot be null", error.getMessage());
        assertEquals(PATH + "/" + savedProduct.getId() + "/specifications/" + savedSpec.getId(),
                error.getPath());
    }

    @Test
    void shouldNotUpdateSpecificationWhenUpdatingWithTooLongValueString() throws JsonProcessingException {
        Product savedProduct = createProduct();
        Specification savedSpec = saveSpecification(specificationEntity);

        ProductSpecification prodSpecResolution = new ProductSpecification();
        prodSpecResolution.setValueString("1920x1080");
        prodSpecResolution.setProduct(savedProduct);
        prodSpecResolution.setSpecification(savedSpec);

        prodSpecResolution = productSpecificationRepository.save(prodSpecResolution);

        savedProduct.setSpecifications(Set.of(prodSpecResolution));

        savedProduct = productRepository.save(savedProduct);

        ProductSpecificationUpdateDTO updateRequest = new ProductSpecificationUpdateDTO(
                "e".repeat(46), null, null
        );

        var content = given()
                .spec(specification)
                .pathParam("prodId", savedProduct.getId())
                .pathParam("specId", savedSpec.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("/{prodId}/specifications/{specId}")
                .then()
                .statusCode(400)
                .extract()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(PATH + "/" + savedProduct.getId() + "/specifications/" + savedSpec.getId(),
                error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Value must contain a maximum of 45 characters", errors.get("valueString"));
    }

    @Test
    void shouldDeleteSpecificationWhenDeletingWithValidId() {
        Product savedProduct = createProduct();
        Optional<Category> findCategory = savedProduct.getCategories().stream().findFirst();
        Map<String, Specification> specifications = createSpecifications(findCategory.get());

        Specification refresh = specifications.get("refresh");
        ProductSpecification prodSpecRefresh = new ProductSpecification();
        prodSpecRefresh.setValueNumber(60.0);
        prodSpecRefresh.setProduct(savedProduct);
        prodSpecRefresh.setSpecification(refresh);

        prodSpecRefresh = productSpecificationRepository.save(prodSpecRefresh);

        savedProduct.setSpecifications(Set.of(prodSpecRefresh));

        savedProduct = productRepository.save(savedProduct);

        given()
                .spec(specification)
                .pathParam("prodId", savedProduct.getId())
                .pathParam("specId", refresh.getId())
                .delete("/{prodId}/specifications/{specId}")
                .then()
                .statusCode(204);
    }

    @Test
    void shouldNotDeleteSpecificationWhenDeletingWithInvalidProductId() {
        Product savedProduct = createProduct();
        Optional<Category> findCategory = savedProduct.getCategories().stream().findFirst();
        Map<String, Specification> specifications = createSpecifications(findCategory.get());

        Specification refresh = specifications.get("refresh");
        ProductSpecification prodSpecRefresh = new ProductSpecification();
        prodSpecRefresh.setValueNumber(60.0);
        prodSpecRefresh.setProduct(savedProduct);
        prodSpecRefresh.setSpecification(refresh);

        prodSpecRefresh = productSpecificationRepository.save(prodSpecRefresh);

        savedProduct.setSpecifications(Set.of(prodSpecRefresh));

        productRepository.save(savedProduct);

        CustomError error = given()
                .spec(specification)
                .pathParam("prodId", invalidId)
                .pathParam("specId", refresh.getId())
                .delete("/{prodId}/specifications/{specId}")
                .then()
                .statusCode(404)
                .extract()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals(productNotFoundMsg, error.getMessage());
        assertEquals(PATH + "/" + invalidId + "/specifications/" + refresh.getId(),
                error.getPath());
    }

    @Test
    void shouldNotDeleteSpecificationWhenProductAndSpecificationDoesNotMatch() {
        Product savedProduct = createProduct();
        Optional<Category> findCategory = savedProduct.getCategories().stream().findFirst();
        Map<String, Specification> specifications = createSpecifications(findCategory.get());

        Specification refresh = specifications.get("refresh");
        ProductSpecification prodSpecRefresh = new ProductSpecification();
        prodSpecRefresh.setValueNumber(60.0);
        prodSpecRefresh.setProduct(savedProduct);
        prodSpecRefresh.setSpecification(refresh);

        prodSpecRefresh = productSpecificationRepository.save(prodSpecRefresh);

        savedProduct.setSpecifications(Set.of(prodSpecRefresh));

        productRepository.save(savedProduct);

        CustomError error = given()
                .spec(specification)
                .pathParam("prodId", savedProduct.getId())
                .pathParam("specId", invalidId)
                .delete("/{prodId}/specifications/{specId}")
                .then()
                .statusCode(404)
                .extract()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals("Product Specification not found", error.getMessage());
        assertEquals(PATH + "/" + savedProduct.getId() + "/specifications/" + invalidId,
                error.getPath());
    }

    @Test
    void shouldNotDeleteSpecificationWhenDeletingRequiredSpecification() {
        Product savedProduct = createProduct();
        Optional<Category> findCategory = savedProduct.getCategories().stream().findFirst();
        Map<String, Specification> specifications = createSpecifications(findCategory.get());

        Specification resolution = specifications.get("resolution");
        ProductSpecification prodSpecResolution = new ProductSpecification();
        prodSpecResolution.setValueString("1920x1080");
        prodSpecResolution.setProduct(savedProduct);
        prodSpecResolution.setSpecification(resolution);

        prodSpecResolution = productSpecificationRepository.save(prodSpecResolution);

        savedProduct.setSpecifications(Set.of(prodSpecResolution));

        savedProduct = productRepository.save(savedProduct);

        CustomError error = given()
                .spec(specification)
                .pathParam("prodId", savedProduct.getId())
                .pathParam("specId", resolution.getId())
                .delete("/{prodId}/specifications/{specId}")
                .then()
                .statusCode(400)
                .extract()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals("The specification is required and cannot be excluded", error.getMessage());
        assertEquals(PATH + "/" + savedProduct.getId() + "/specifications/" + resolution.getId(),
                error.getPath());
    }

    private Map<String, Specification> createSpecifications(Category category) {
        Unit savedUnit = saveUnit(unit); // HERTZ
        Specification resolutionSpec = saveSpecification(specificationEntity); // RESOLUÇÃO
        Specification refreshRateSpec = saveSpecification(SpecificationBuilder // FREQUÊNCIA
                .builder()
                .withId(null)
                .withName("Frequência")
                .withUnit(savedUnit)
                .withUpdatedAt(null)
                .withCreatedAt(null)
                .build());

        CategorySpecification catSpecResolution =
                new CategorySpecification(null, true, category, resolutionSpec);

        CategorySpecification catSpecRefresh =
                new CategorySpecification(null, false, category, refreshRateSpec);

        categorySpecificationRepository.save(catSpecResolution);
        categorySpecificationRepository.save(catSpecRefresh);

        return Map.of(
                "resolution", resolutionSpec,
                "refresh", refreshRateSpec
        );
    }

    private Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    private Specification saveSpecification(Specification specification) {
        return specificationRepository.save(specification);
    }

    private Brand saveBrand(Brand brand) {
        return brandRepository.save(brand);
    }

    private CategorySpecification saveCategorySpecification(CategorySpecification categorySpecification) {
        return categorySpecificationRepository.save(categorySpecification);
    }

    private Unit saveUnit(Unit unit) {
        return unitRepository.save(unit);
    }
}
