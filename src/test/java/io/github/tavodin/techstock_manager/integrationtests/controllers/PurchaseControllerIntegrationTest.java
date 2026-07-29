package io.github.tavodin.techstock_manager.integrationtests.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.builder.ProductBuilder;
import io.github.tavodin.techstock_manager.configurations.AbstractIntegrationTest;
import io.github.tavodin.techstock_manager.dto.PurchaseDTO;
import io.github.tavodin.techstock_manager.dto.PurchaseItemRequestDTO;
import io.github.tavodin.techstock_manager.dto.PurchaseRequestDTO;
import io.github.tavodin.techstock_manager.dto.error.CustomError;
import io.github.tavodin.techstock_manager.dto.error.FieldError;
import io.github.tavodin.techstock_manager.dto.error.ValidationError;
import io.github.tavodin.techstock_manager.entities.*;
import io.github.tavodin.techstock_manager.enums.MovementType;
import io.github.tavodin.techstock_manager.enums.PurchaseStatus;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PurchaseControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private static final String PATH = "/purchases";
    private static final String CONTENT_TYPE = "application/json";
    private static RequestSpecification specification;
    private static final Long INVALID_ID = Long.MAX_VALUE;
    private static final String PURCHASE_NOT_FOUND_MSG = "Purchase not found";
    private static final String SUPPLIER_NOT_FOUND_MSG = "Supplier not found";
    private static final String PRODUCT_NOT_FOUND_MSG = "Product not found";
    private static final String VALIDATION_ERROR = "Entity validation error";
    private PurchaseRequestDTO request;
    private String token;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        token = AuthTestUtil.getToken(port);

        specification = new RequestSpecBuilder()
                .setBasePath(PATH)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        purchaseItemRepository.deleteAll();
        purchaseRepository.deleteAll();
        supplierRepository.deleteAll();
        stockMovementRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();
    }

    @Test
    void shouldCreatePurchaseWhenSavingWithValidData() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);
        Product savedProduct = createProduct();

        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                savedProduct.getId(),
                3,
                BigDecimal.valueOf(254.99)
        );

        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                savedSupplier.getId(),
                List.of(item)
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString();

        PurchaseDTO body = objectMapper.readValue(response, PurchaseDTO.class);

        assertNotNull(body.getId());
        assertEquals(PurchaseStatus.OPEN, body.getStatus());
        assertNotNull(body.getPurchaseDate());

        BigDecimal total = item.unitCost().multiply(BigDecimal.valueOf(item.quantity()));
        assertTrue(body.getTotalAmount().compareTo(total) == 0);
    }

    @Test
    void shouldNotSavePurchaseWhenSavingWithInvalidSupplierId() {
        Product savedProduct = createProduct();

        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                savedProduct.getId(),
                3,
                BigDecimal.valueOf(254.99)
        );

        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                INVALID_ID,
                List.of(item)
        );

        CustomError response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(response.getTimestamp());
        assertEquals(SUPPLIER_NOT_FOUND_MSG, response.getMessage());
        assertEquals(404, response.getStatus());
        assertEquals(PATH, response.getPath());
    }

    @Test
    void shouldNotSavePurchaseWhenSavingWithInvalidProductId() {
        Supplier savedSupplier = createSupplier(null);

        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                INVALID_ID,
                3,
                BigDecimal.valueOf(254.99)
        );

        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                savedSupplier.getId(),
                List.of(item)
        );

        CustomError response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(response.getTimestamp());
        assertEquals(PRODUCT_NOT_FOUND_MSG, response.getMessage());
        assertEquals(404, response.getStatus());
        assertEquals(PATH, response.getPath());
    }

    @Test
    void shouldNotSavePurchaseWhenSavingWithNullPurchaseDate() throws JsonProcessingException {

        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                1L,
                3,
                BigDecimal.valueOf(254.99)
        );

        request = new PurchaseRequestDTO(
                null,
                1L,
                List.of(item)
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError errors = objectMapper.readValue(response, ValidationError.class);
        Map<String, String> error = errors.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals(VALIDATION_ERROR, errors.getMessage());
        assertEquals(400, errors.getStatus());
        assertNotNull(errors.getTimestamp());
        assertEquals(PATH, errors.getPath());

        assertEquals("Purchase Date is required", error.get("purchaseDate"));
    }

    @Test
    void shouldNotSavePurchaseWhenSavingWithNullSupplierId() throws JsonProcessingException {

        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                1L,
                3,
                BigDecimal.valueOf(254.99)
        );

        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                null,
                List.of(item)
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError errors = objectMapper.readValue(response, ValidationError.class);
        Map<String, String> error = errors.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals(VALIDATION_ERROR, errors.getMessage());
        assertEquals(400, errors.getStatus());
        assertNotNull(errors.getTimestamp());
        assertEquals(PATH, errors.getPath());

        assertEquals("Supplier is required", error.get("supplierId"));
    }

    @Test
    void shouldNotSavePurchaseWhenSavingWithNullItems() throws JsonProcessingException {
        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                1L,
                null
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError errors = objectMapper.readValue(response, ValidationError.class);
        Map<String, String> error = errors.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals(VALIDATION_ERROR, errors.getMessage());
        assertEquals(400, errors.getStatus());
        assertNotNull(errors.getTimestamp());
        assertEquals(PATH, errors.getPath());

        assertEquals("Purchase Item is required", error.get("items"));
    }

    @Test
    void shouldNotSavePurchaseWhenSavingWithEmptyItems() throws JsonProcessingException {
        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                1L,
                List.of()
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError errors = objectMapper.readValue(response, ValidationError.class);
        Map<String, String> error = errors.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals(VALIDATION_ERROR, errors.getMessage());
        assertEquals(400, errors.getStatus());
        assertNotNull(errors.getTimestamp());
        assertEquals(PATH, errors.getPath());

        assertEquals("Purchase Item is required", error.get("items"));
    }

    @Test
    void shouldNotSavePurchaseWhenSavingWithNullProductId() throws JsonProcessingException {
        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                null,
                3,
                BigDecimal.valueOf(254.99)
        );

        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                1L,
                List.of(item)
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError errors = objectMapper.readValue(response, ValidationError.class);
        Map<String, String> error = errors.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals(VALIDATION_ERROR, errors.getMessage());
        assertEquals(400, errors.getStatus());
        assertNotNull(errors.getTimestamp());
        assertEquals(PATH, errors.getPath());

        assertEquals("Product is required", error.get("items[0].productId"));
    }

    @Test
    void shouldNotSavePurchaseWhenSavingWithNullQuantity() throws JsonProcessingException {
        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                1L,
                null,
                BigDecimal.valueOf(254.99)
        );

        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                1L,
                List.of(item)
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError errors = objectMapper.readValue(response, ValidationError.class);
        Map<String, String> error = errors.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals(VALIDATION_ERROR, errors.getMessage());
        assertEquals(400, errors.getStatus());
        assertNotNull(errors.getTimestamp());
        assertEquals(PATH, errors.getPath());

        assertEquals("Quantity is required", error.get("items[0].quantity"));
    }

    @Test
    void shouldNotSavePurchaseWhenSavingWithZeroQuantity() throws JsonProcessingException {
        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                1L,
                0,
                BigDecimal.valueOf(254.99)
        );

        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                1L,
                List.of(item)
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError errors = objectMapper.readValue(response, ValidationError.class);
        Map<String, String> error = errors.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals(VALIDATION_ERROR, errors.getMessage());
        assertEquals(400, errors.getStatus());
        assertNotNull(errors.getTimestamp());
        assertEquals(PATH, errors.getPath());

        assertEquals("Quantity must be positive", error.get("items[0].quantity"));
    }

    @Test
    void shouldNotSavePurchaseWhenSavingWithNegativeQuantity() throws JsonProcessingException {
        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                1L,
                0,
                BigDecimal.valueOf(254.99)
        );

        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                1L,
                List.of(item)
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError errors = objectMapper.readValue(response, ValidationError.class);
        Map<String, String> error = errors.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals(VALIDATION_ERROR, errors.getMessage());
        assertEquals(400, errors.getStatus());
        assertNotNull(errors.getTimestamp());
        assertEquals(PATH, errors.getPath());

        assertEquals("Quantity must be positive", error.get("items[0].quantity"));
    }

    @Test
    void shouldNotSavePurchaseWhenSavingWithNullUnitCost() throws JsonProcessingException {
        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                1L,
                1,
                null
        );

        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                1L,
                List.of(item)
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError errors = objectMapper.readValue(response, ValidationError.class);
        Map<String, String> error = errors.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals(VALIDATION_ERROR, errors.getMessage());
        assertEquals(400, errors.getStatus());
        assertNotNull(errors.getTimestamp());
        assertEquals(PATH, errors.getPath());

        assertEquals("Unit Cost is required", error.get("items[0].unitCost"));
    }

    @Test
    void shouldNotSavePurchaseWhenSavingWithNegativeUnitCost() throws JsonProcessingException {
        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                1L,
                1,
                BigDecimal.valueOf(-1.0)
        );

        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                1L,
                List.of(item)
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError errors = objectMapper.readValue(response, ValidationError.class);
        Map<String, String> error = errors.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals(VALIDATION_ERROR, errors.getMessage());
        assertEquals(400, errors.getStatus());
        assertNotNull(errors.getTimestamp());
        assertEquals(PATH, errors.getPath());

        assertEquals("Unit Cost must be unit cost", error.get("items[0].unitCost"));
    }

    @Test
    void shouldNotSavePurchaseWhenSavingWithZeroUnitCost() throws JsonProcessingException {
        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                1L,
                1,
                BigDecimal.ZERO
        );

        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                1L,
                List.of(item)
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError errors = objectMapper.readValue(response, ValidationError.class);
        Map<String, String> error = errors.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals(VALIDATION_ERROR, errors.getMessage());
        assertEquals(400, errors.getStatus());
        assertNotNull(errors.getTimestamp());
        assertEquals(PATH, errors.getPath());

        assertEquals("Unit Cost must be unit cost", error.get("items[0].unitCost"));
    }

    @Test
    void shouldChangePurchaseStatusToCompletedWhenCompletingWithValidData() throws JsonProcessingException {
        PurchaseDTO purchase = createPurchase();

        var response = given()
                .spec(specification)
                .pathParam("id", purchase.getId())
                .patch("/{id}/completed")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        PurchaseDTO body = objectMapper.readValue(response, PurchaseDTO.class);

        assertEquals(purchase.getId(), body.getId());
        assertEquals(PurchaseStatus.COMPLETED, body.getStatus());
        assertEquals(purchase.getPurchaseDate(), body.getPurchaseDate());
        assertTrue(body.getTotalAmount().compareTo(purchase.getTotalAmount()) == 0);
    }

    @Test
    void shouldChangeProductQuantityAndCostPriceToCompletedWhenCompletingWithValidData() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);
        Product savedProduct = createProduct();

        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                savedProduct.getId(),
                3,
                BigDecimal.valueOf(254.99)
        );

        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                savedSupplier.getId(),
                List.of(item)
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString();

        PurchaseDTO body = objectMapper.readValue(response, PurchaseDTO.class);

        given()
                .spec(specification)
                .pathParam("id", body.getId())
                .patch("/{id}/completed")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        Product product = productRepository.findById(savedProduct.getId()).get();

        assertTrue(product.getCostPrice().compareTo(savedProduct.getCostPrice()) > 0);
        assertTrue(product.getQuantityInStock() != savedProduct.getQuantityInStock());
    }

    @Test
    void shouldCreateStockMovementWhenCompletingWithValidData() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);
        Product savedProduct = createProduct();

        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                savedProduct.getId(),
                3,
                BigDecimal.valueOf(254.99)
        );

        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                savedSupplier.getId(),
                List.of(item)
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString();

        PurchaseDTO body = objectMapper.readValue(response, PurchaseDTO.class);

        given()
                .spec(specification)
                .pathParam("id", body.getId())
                .patch("/{id}/completed")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        List<StockMovement> stocks = stockMovementRepository.findAll();

        StockMovement movement = stocks.getFirst();

        assertNotNull(movement.getMovementDate());
        assertEquals(MovementType.PURCHASE, movement.getType());
        assertEquals(item.quantity(), movement.getQuantity());
        assertEquals("Purchase product", movement.getReason());
    }

    @Test
    void shouldNotChangeStatusWhenCompletingWithInvalidId() {
        CustomError error = given()
                .spec(specification)
                .pathParam("id", INVALID_ID)
                .patch("/{id}/completed")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals(PURCHASE_NOT_FOUND_MSG, error.getMessage());
        assertEquals(PATH + "/" + INVALID_ID + "/completed", error.getPath());
    }

    @Test
    void shouldNotChangeStatusWhenCompletingWithInvalidPurchaseStatus() throws JsonProcessingException {
        PurchaseDTO purchase = createPurchase();

        given()
                .spec(specification)
                .pathParam("id", purchase.getId())
                .patch("/{id}/canceled")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        CustomError error = given()
                .spec(specification)
                .pathParam("id", purchase.getId())
                .patch("/{id}/completed")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals("The purchase status must be 'OPEN'", error.getMessage());
        assertEquals(PATH + "/" + purchase.getId() + "/completed", error.getPath());
    }

    @Test
    void shouldChangePurchaseStatusToCanceledWhenCancelingWithValidData() throws JsonProcessingException {
        PurchaseDTO purchase = createPurchase();

        var response = given()
                .spec(specification)
                .pathParam("id", purchase.getId())
                .patch("/{id}/canceled")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        PurchaseDTO body = objectMapper.readValue(response, PurchaseDTO.class);

        assertNotNull(body.getPurchaseDate());
        assertEquals(PurchaseStatus.CANCELED, body.getStatus());
        assertTrue(body.getTotalAmount().compareTo(purchase.getTotalAmount()) == 0);
    }

    @Test
    void shouldChangePurchaseStatusToCanceledWhenCancelingWithInvalidPurchaseId() {
        CustomError error = given()
                .spec(specification)
                .pathParam("id", INVALID_ID)
                .patch("/{id}/canceled")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);


        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals("Purchase not found", error.getMessage());
        assertEquals(PATH + "/" + INVALID_ID + "/canceled", error.getPath());
    }

    @Test
    void shouldNotChangeStatusWhenCancelingWithInvalidPurchaseStatus() throws JsonProcessingException {
        PurchaseDTO purchase = createPurchase();

        given()
                .spec(specification)
                .pathParam("id", purchase.getId())
                .patch("/{id}/completed")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        CustomError error = given()
                .spec(specification)
                .pathParam("id", purchase.getId())
                .patch("/{id}/canceled")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals("The purchase status must be 'OPEN'", error.getMessage());
        assertEquals(PATH + "/" + purchase.getId() + "/canceled", error.getPath());
    }

    private PurchaseDTO createPurchase() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);
        Product savedProduct = createProduct();

        PurchaseItemRequestDTO item = new PurchaseItemRequestDTO(
                savedProduct.getId(),
                3,
                BigDecimal.valueOf(254.99)
        );

        request = new PurchaseRequestDTO(
                LocalDate.of(2026, 07, 28),
                savedSupplier.getId(),
                List.of(item)
        );

        var response = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString();

        return objectMapper.readValue(response, PurchaseDTO.class);
    }

    private Product createProduct() {
        Brand brand = brandRepository.save(new Brand("DELL", null));
        Category category = categoryRepository.save(
                new Category(null, "Monitor", null, null));

        Product product = ProductBuilder
                .builder()
                .withQuantityInStock(0)
                .withCostPrice(BigDecimal.ZERO)
                .withId(null)
                .build();

        product.setBrand(brand);
        product.setCategories(Set.of(category));

        return productRepository.saveAndFlush(product);
    }

    private Supplier createSupplier(Supplier supplier) {
        if (supplier == null) {
            supplier = new Supplier(
                    "TechParts",
                    "00000000000191",
                    "techparts@gmail.com",
                    "6137644711",
                    true
            );
        }
        return supplierRepository.save(supplier);
    }
}
