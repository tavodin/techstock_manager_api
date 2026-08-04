package io.github.tavodin.techstock_manager.integrationtests.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.builder.ProductBuilder;
import io.github.tavodin.techstock_manager.configurations.AbstractIntegrationTest;
import io.github.tavodin.techstock_manager.dto.PurchaseRequestDTO;
import io.github.tavodin.techstock_manager.dto.SaleDTO;
import io.github.tavodin.techstock_manager.dto.SaleItemRequestDTO;
import io.github.tavodin.techstock_manager.dto.SaleRequestDTO;
import io.github.tavodin.techstock_manager.dto.error.CustomError;
import io.github.tavodin.techstock_manager.dto.error.FieldError;
import io.github.tavodin.techstock_manager.dto.error.ValidationError;
import io.github.tavodin.techstock_manager.entities.*;
import io.github.tavodin.techstock_manager.enums.MovementType;
import io.github.tavodin.techstock_manager.enums.PurchaseStatus;
import io.github.tavodin.techstock_manager.enums.SaleStatus;
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
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SaleControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private static final String PATH = "/sales";
    private static final String CONTENT_TYPE = "application/json";
    private static RequestSpecification specification;
    private static final Long INVALID_ID = Long.MAX_VALUE;
    private static final String SALE_NOT_FOUND_MSG = "Sale not found";
    private static final String PRODUCTS_NOT_FOUND_MSG = "One or more products were not found";
    private static final String EXCEEDING_STOCK_MSG = "Insufficient stock quantity";
    private static final String VALIDATION_ERROR = "Entity validation error";
    private Integer quantity = 2;
    private String paymentMethod = "Débito";

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

        stockMovementRepository.deleteAll();
        saleItemRepository.deleteAll();
        saleRepository.deleteAll();
        productRepository.deleteAll();
        brandRepository.deleteAll();
    }

    @Test
    void shouldFindSaleWhenFindingWithValidId() throws JsonProcessingException {
        SaleDTO savedSale = createSale(null);

        var response = given()
                .spec(specification)
                .pathParam("id", savedSale.getId())
                .get("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        SaleDTO body = objectMapper.readValue(response, SaleDTO.class);

        assertNotNull(body.getId());
        assertNotNull(body.getSaleDate());
        assertEquals(SaleStatus.COMPLETED, body.getStatus());
        assertTrue(body.getTotalAmount().compareTo(BigDecimal.valueOf(1200.50)) == 0);
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenFindingWithInvalidId() {
        CustomError response = given()
                .spec(specification)
                .pathParam("id", INVALID_ID)
                .get("/{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(response.getTimestamp());
        assertEquals(404, response.getStatus());
        assertEquals(SALE_NOT_FOUND_MSG, response.getMessage());
        assertEquals(PATH + "/" + INVALID_ID, response.getPath());
    }

    @Test
    void shouldReturnPurchasesWhenFindAll() throws JsonProcessingException {
        SaleDTO savedSale = createSale(null);

        given()
                .spec(specification)
                .get()
                .then()
                .statusCode(200)
                .body("_embedded.sales[0].id", notNullValue())
                .body("_embedded.sales[0].status", equalTo(savedSale.getStatus().toString()))
                .body("_embedded.sales[0].paymentMethod", equalTo(savedSale.getPaymentMethod()))
                .body("_embedded.sales[0].totalAmount", equalTo(1200.50F));
    }

    @Test
    void shouldReturnPurchasePagedWhenFindAll() throws JsonProcessingException {
        createSale(null);

        int number = 0;
        int size = 5;

        given().spec(specification)
                .queryParam("page", number)
                .queryParam("size", size)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("_links.self.href",
                        containsString(String.format("%s?page=%s&size=%s", PATH, number, size)))
                .body("page.size", equalTo(5))
                .body("page.totalElements", equalTo(1))
                .body("page.totalPages", equalTo(1))
                .body("page.number", equalTo(0));
    }

    @Test
    void shouldCreateSaleAndReturnCreatedWhenSavingWithValidData() throws JsonProcessingException {
        Product savedProduct = createProduct(null);
        SaleItemRequestDTO item = new SaleItemRequestDTO(quantity, savedProduct.getId());

        SaleRequestDTO request = new SaleRequestDTO(
                paymentMethod,
                Set.of(item)
        );

        BigDecimal totalAmount = savedProduct.getSalePrice().multiply(BigDecimal.valueOf(quantity));

        SaleDTO savedSale = createSale(request);

        assertNotNull(savedSale.getId());
        assertNotNull(savedSale.getSaleDate());
        assertEquals(SaleStatus.COMPLETED, savedSale.getStatus());
        assertTrue(savedSale.getTotalAmount().compareTo(totalAmount) == 0);
    }

    @Test
    void shouldCreateSaleItemWhenSavingWithValidData() throws JsonProcessingException {
        Product savedProduct = createProduct(null);
        SaleItemRequestDTO itemRequest = new SaleItemRequestDTO(quantity, savedProduct.getId());

        SaleRequestDTO request = new SaleRequestDTO(
                paymentMethod,
                Set.of(itemRequest)
        );

        BigDecimal subTotal = savedProduct.getSalePrice()
                .multiply(BigDecimal.valueOf(itemRequest.quantity()));

        SaleDTO savedSale = createSale(request);

        List<SaleItem> items = saleItemRepository.findAll();
        SaleItem item = items.getFirst();

        assertEquals(savedSale.getId(), item.getSale().getId());
        assertEquals(savedProduct, item.getProduct());
        assertEquals(savedProduct.getSalePrice(), item.getUnitPrice());
        assertTrue(item.getSubtotal().compareTo(subTotal) == 0);
    }

    @Test
    void shouldCreateStockMovementWhenSavingWithValidData() throws JsonProcessingException {
        createSale(null);

        List<StockMovement> movements = stockMovementRepository.findAll();
        StockMovement movement = movements.getFirst();

        assertNotNull(movement.getMovementDate());
        assertEquals(MovementType.SALE, movement.getType());
        assertEquals("Sale Product", movement.getReason());
        assertEquals(quantity, movement.getQuantity());
        assertNotNull(movement.getProduct());
    }

    @Test
    void shouldReduceProductQuantityWhenSavingWithValidData() throws JsonProcessingException {
        Product savedProduct = createProduct(null);
        SaleItemRequestDTO item = new SaleItemRequestDTO(quantity, savedProduct.getId());

        Integer quantity = savedProduct.getQuantityInStock() - item.quantity();

        SaleRequestDTO request = new SaleRequestDTO(
                paymentMethod,
                Set.of(item)
        );

        createSale(request);

        Product findProduct = productRepository.findById(savedProduct.getId()).get();

        assertEquals(quantity, findProduct.getQuantityInStock());
    }

    @Test
    void shouldNotSaveWhenSavingWithInvalidProductId() throws JsonProcessingException {
        Product savedProduct = createProduct(null);
        SaleItemRequestDTO firstItem = new SaleItemRequestDTO(quantity, savedProduct.getId());
        SaleItemRequestDTO secondItem = new SaleItemRequestDTO(quantity, INVALID_ID);

        SaleRequestDTO request = new SaleRequestDTO(
                paymentMethod,
                Set.of(firstItem, secondItem)
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
        assertEquals(PRODUCTS_NOT_FOUND_MSG, error.getMessage());
        assertEquals(PATH, error.getPath());
    }

    @Test
    void shouldNotSaveWhenQuantityExceedingStockCapacity() throws JsonProcessingException {
        Product product = ProductBuilder.builder()
                .withId(null)
                .withQuantityInStock(3)
                .build();

        Product savedProduct = createProduct(product);
        SaleItemRequestDTO firstItem = new SaleItemRequestDTO(quantity + 10, savedProduct.getId());

        SaleRequestDTO request = new SaleRequestDTO(
                paymentMethod,
                Set.of(firstItem)
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
        assertEquals(EXCEEDING_STOCK_MSG, error.getMessage());
        assertEquals(PATH, error.getPath());
    }

    @Test
    void shouldNotSaveWhenSavingWithNullPaymentMethod() throws JsonProcessingException {
        SaleItemRequestDTO item = new SaleItemRequestDTO(quantity, INVALID_ID);

        SaleRequestDTO request = new SaleRequestDTO(
                null,
                Set.of(item)
        );

        var response = given()
                .spec(specification)
                .body(request)
                .contentType(CONTENT_TYPE)
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

        assertEquals("Payment Method is required", error.get("paymentMethod"));
    }

    @Test
    void shouldNotSaveWhenSavingWithTooLongPaymentMethod() throws JsonProcessingException {
        SaleItemRequestDTO item = new SaleItemRequestDTO(quantity, INVALID_ID);

        SaleRequestDTO request = new SaleRequestDTO(
                "e".repeat(46),
                Set.of(item)
        );

        var response = given()
                .spec(specification)
                .body(request)
                .contentType(CONTENT_TYPE)
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

        assertEquals("The name must contain a maximum of 45 characters", error.get("paymentMethod"));
    }

    @Test
    void shouldNotSaveWhenSavingWithEmptySaleItems() throws JsonProcessingException {
        SaleRequestDTO request = new SaleRequestDTO(
                paymentMethod,
                Set.of()
        );

        var response = given()
                .spec(specification)
                .body(request)
                .contentType(CONTENT_TYPE)
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

        assertEquals("Sale must contain at least one item", error.get("items"));
    }

    @Test
    void shouldNotSaveWhenSavingWithNullItemQuantity() throws JsonProcessingException {
        SaleItemRequestDTO item = new SaleItemRequestDTO(null, INVALID_ID);

        SaleRequestDTO request = new SaleRequestDTO(
                paymentMethod,
                Set.of(item)
        );

        var response = given()
                .spec(specification)
                .body(request)
                .contentType(CONTENT_TYPE)
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

        assertEquals("Quantity is required", error.get("items[].quantity"));
    }

    @Test
    void shouldNotSaveWhenSavingWithNegativeItemQuantity() throws JsonProcessingException {
        SaleItemRequestDTO item = new SaleItemRequestDTO(-1, INVALID_ID);

        SaleRequestDTO request = new SaleRequestDTO(
                paymentMethod,
                Set.of(item)
        );

        var response = given()
                .spec(specification)
                .body(request)
                .contentType(CONTENT_TYPE)
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

        assertEquals("Quantity must be positive", error.get("items[].quantity"));
    }

    @Test
    void shouldNotSaveWhenSavingWithZeroItemQuantity() throws JsonProcessingException {
        SaleItemRequestDTO item = new SaleItemRequestDTO(0, INVALID_ID);

        SaleRequestDTO request = new SaleRequestDTO(
                paymentMethod,
                Set.of(item)
        );

        var response = given()
                .spec(specification)
                .body(request)
                .contentType(CONTENT_TYPE)
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

        assertEquals("Quantity must be positive", error.get("items[].quantity"));
    }

    @Test
    void shouldNotSaveWhenSavingWithNullProductId() throws JsonProcessingException {
        SaleItemRequestDTO item = new SaleItemRequestDTO(1, null);

        SaleRequestDTO request = new SaleRequestDTO(
                paymentMethod,
                Set.of(item)
        );

        var response = given()
                .spec(specification)
                .body(request)
                .contentType(CONTENT_TYPE)
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

        assertEquals("Product is required", error.get("items[].productId"));
    }

    @Test
    void shouldChangeStatusToCanceledWhenCancelingWithValidId() throws JsonProcessingException {
        SaleDTO sale = createSale(null);

        var response = given()
                .spec(specification)
                .pathParam("id", sale.getId())
                .patch("/{id}/canceled")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        SaleDTO body = objectMapper.readValue(response, SaleDTO.class);

        assertEquals(SaleStatus.CANCELED, body.getStatus());
    }

    @Test
    void shouldIncreaseTheQuantityProductWhenCancelingWithValidId() throws JsonProcessingException {
        Product product = ProductBuilder
                .builder()
                .withId(null)
                .withQuantityInStock(5)
                .build();

        Product savedProduct = createProduct(product);
        SaleItemRequestDTO item = new SaleItemRequestDTO(quantity, savedProduct.getId());

        SaleRequestDTO request = new SaleRequestDTO(
                paymentMethod,
                Set.of(item)
        );

        SaleDTO sale = createSale(request);

        given()
                .spec(specification)
                .pathParam("id", sale.getId())
                .patch("/{id}/canceled")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        Product findProduct = productRepository.findById(savedProduct.getId()).get();

        assertEquals(savedProduct.getQuantityInStock(), findProduct.getQuantityInStock());
    }

    @Test
    void shouldCreateStockMovementWhenCancelingWithValidId() throws JsonProcessingException {
        SaleDTO sale = createSale(null);

        given()
                .spec(specification)
                .pathParam("id", sale.getId())
                .patch("/{id}/canceled")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        List<StockMovement> movements = stockMovementRepository.findAll();
        StockMovement movement = movements.get(1);

        assertNotNull(movement.getMovementDate());
        assertEquals(MovementType.SALE_RETURN, movement.getType());
        assertEquals("Product return", movement.getReason());
        assertEquals(quantity, movement.getQuantity());
        assertNotNull(movement.getProduct());
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenCancelingWithInvalidId() {
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
        assertEquals(SALE_NOT_FOUND_MSG, error.getMessage());
        assertEquals(PATH + "/" + INVALID_ID + "/canceled", error.getPath());
    }

    @Test
    void shouldReturnCustomErrorAndBadRequestWhenCancelingWithCanceledStatus() throws JsonProcessingException {
        SaleDTO sale = createSale(null);

        var response = given()
                .spec(specification)
                .pathParam("id", sale.getId())
                .patch("/{id}/canceled")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        SaleDTO canceledSale = objectMapper.readValue(response, SaleDTO.class);

        CustomError error = given()
                .spec(specification)
                .pathParam("id", canceledSale.getId())
                .patch("/{id}/canceled")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals("Sale status must be 'COMPLETED'", error.getMessage());
        assertEquals(PATH + "/" + canceledSale.getId() + "/canceled", error.getPath());
    }

    private SaleDTO createSale(SaleRequestDTO request) throws JsonProcessingException {
        if(request == null) {
            Product savedProduct = createProduct(null);
            SaleItemRequestDTO item = new SaleItemRequestDTO(quantity, savedProduct.getId());

            request = new SaleRequestDTO(
                   paymentMethod,
                    Set.of(item)
            );
        }

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

        return objectMapper.readValue(response, SaleDTO.class);
    }

    private Product createProduct(Product product) {
        if(product == null) {
            Brand brand = new Brand();
            brand.setName("Logitech");
            brand = brandRepository.save(brand);

            product = ProductBuilder.builder()
                    .withId(null)
                    .withSalePrice(BigDecimal.valueOf(600.25))
                    .build();

            product.setBrand(brand);
        }

        if(product.getBrand() == null) {
            Brand brand = new Brand();
            brand.setName("Logitech");

            brand = brandRepository.save(brand);
            product.setBrand(brand);
        }

        return productRepository.save(product);
    }
}
