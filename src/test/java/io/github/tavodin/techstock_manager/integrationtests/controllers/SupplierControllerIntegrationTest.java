package io.github.tavodin.techstock_manager.integrationtests.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.configurations.AbstractIntegrationTest;
import io.github.tavodin.techstock_manager.dto.SupplierDTO;
import io.github.tavodin.techstock_manager.dto.SupplierRequestDTO;
import io.github.tavodin.techstock_manager.dto.error.CustomError;
import io.github.tavodin.techstock_manager.dto.error.FieldError;
import io.github.tavodin.techstock_manager.dto.error.ValidationError;
import io.github.tavodin.techstock_manager.entities.Supplier;
import io.github.tavodin.techstock_manager.integrationtests.utils.AuthTestUtil;
import io.github.tavodin.techstock_manager.repositories.SupplierRepository;
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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SupplierControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SupplierRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private static final String PATH = "/suppliers";
    private static final String CONTENT_TYPE = "application/json";
    private static RequestSpecification specification;
    private static final Long INVALID_ID = Long.MAX_VALUE;
    private static final String NOT_FOUND_MSG = "Supplier not found";
    private static final String VALIDATION_ERROR_MSG = "Entity validation error";
    private static final String EXIST_DOCUMENT_MSG = "A record with the same document value already exists";
    private static final String EXIST_EMAIL_MSG = "Email already registered";
    private String token;

    private SupplierRequestDTO request;

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

        request = new SupplierRequestDTO(
                "ScanSource",
                "62173620000180",
                "scansource@gmail.com",
                "8537360756"
                );

        repository.deleteAll();
    }

    @Test
    void shouldFindSupplierWhenFindingWithValidId() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);

        var response = given()
                .spec(specification)
                .pathParam("id", savedSupplier.getId())
                .get("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        SupplierDTO findSupplier = objectMapper.readValue(response, SupplierDTO.class);

        assertTrue(findSupplier.getId() > 0);
        assertEquals(savedSupplier.getName(), findSupplier.getName());
        assertEquals(savedSupplier.getDocument(), findSupplier.getDocument());
        assertEquals(savedSupplier.getEmail(), findSupplier.getEmail());
        assertEquals(savedSupplier.getPhone(), findSupplier.getPhone());
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenFindingWithInvalidId() {
        CustomError error = given()
                .spec(specification)
                .pathParam("id", INVALID_ID)
                .get("/{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(NOT_FOUND_MSG, error.getMessage());
        assertEquals(404, error.getStatus());
        assertEquals(PATH + "/" + INVALID_ID, error.getPath());
    }

    @Test
    void shouldReturnSuppliersWhenFindAll() {
        Supplier savedSupplier = createSupplier(null);

        given()
                .spec(specification)
                .get()
                .then()
                .statusCode(200)
                .body("_embedded.suppliers[0].id", notNullValue())
                .body("_embedded.suppliers[0].name", equalTo(savedSupplier.getName()))
                .body("_embedded.suppliers[0].document", equalTo(savedSupplier.getDocument()))
                .body("_embedded.suppliers[0].email", equalTo(savedSupplier.getEmail()))
                .body("_embedded.suppliers[0].phone", equalTo(savedSupplier.getPhone()))
                .body("_embedded.suppliers[0].createdAt", notNullValue())
                .body("_embedded.suppliers[0].updatedAt", notNullValue());

    }

    @Test
    void shouldReturnPageWhenFindAll() {
        Supplier savedSupplier1 = createSupplier(null);
        Supplier savedSupplier2 = createSupplier(new Supplier(
                "Distribuidora A",
                "00000000000192",
                "distroa@gmail.com",
                "9887644587",
                true
        ));

        int number = 0;
        int size = 5;
        String order = "name,asc";

        given()
                .spec(specification)
                .queryParam("page", number)
                .queryParam("size", size)
                .queryParam("sort", order)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("_embedded.suppliers.name",
                        hasItems(savedSupplier1.getName(), savedSupplier2.getName()))
                .body("page.size", equalTo(size))
                .body("page.totalElements", equalTo(2))
                .body("page.totalPages", equalTo(1))
                .body("page.number", equalTo(number));
    }

    @Test
    void shouldSaveSupplierWhenSavingWithValidData() throws JsonProcessingException {
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

        SupplierDTO savedSupplier = objectMapper.readValue(response, SupplierDTO.class);

        assertTrue(savedSupplier.getId() > 0);
        assertEquals(request.getName(), savedSupplier.getName());
        assertEquals(request.getDocument(), savedSupplier.getDocument());
        assertEquals(request.getEmail(), savedSupplier.getEmail());
        assertEquals(request.getPhone(), savedSupplier.getPhone());
    }

    @Test
    void shouldNotSaveWhenSavingWithExistDocument() {
        Supplier savedSupplier = createSupplier(null);
        request.setDocument(savedSupplier.getDocument());

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
        assertEquals(EXIST_DOCUMENT_MSG, error.getMessage());
        assertEquals(409, error.getStatus());
        assertEquals(PATH, error.getPath());
    }

    @Test
    void shouldNotSaveWhenSavingWithExistEmail() {
        Supplier savedSupplier = createSupplier(null);
        request.setEmail(savedSupplier.getEmail());

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
        assertEquals(EXIST_EMAIL_MSG, error.getMessage());
        assertEquals(409, error.getStatus());
        assertEquals(PATH, error.getPath());
    }

    @Test
    void shouldNotSaveWhenSavingWithNullName() throws JsonProcessingException {
        request.setName(null);
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

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(PATH, error.getPath());
        assertEquals(VALIDATION_ERROR_MSG, error.getMessage());

        assertEquals("Name is required", errors.get("name"));
    }

    @Test
    void shouldNotSaveWhenSavingWithTooLongName() throws JsonProcessingException {
        request.setName("e".repeat(201));
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

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("The name must contain a maximum of 200 characters", errors.get("name"));
    }

    @Test
    void shouldNotSaveWhenSavingWithNullDocument() throws JsonProcessingException {
        request.setDocument(null);
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

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("Document is required", errors.get("document"));
    }

    @Test
    void shouldNotSaveWhenSavingWithTooLongDocument() throws JsonProcessingException {
        request.setDocument("e".repeat(15));
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

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("The document must contain a maximum of 14 characters", errors.get("document"));
    }

    @Test
    void shouldNotSaveWhenSavingWithNullEmail() throws JsonProcessingException {
        request.setEmail(null);
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

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("Email is required", errors.get("email"));
    }

    @Test
    void shouldNotSaveWhenSavingWithInvalidEmail() throws JsonProcessingException {
        request.setEmail("e");
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

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("Invalid Email", errors.get("email"));
    }

    @Test
    void shouldNotSaveWhenSavingWithTooLongEmail() throws JsonProcessingException {
        request.setEmail("e".repeat(201));
        given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(request)
                .post()
                .then()
                .statusCode(400)
                .body("errors.message", hasItem("The email must contain a maximum of 200 characters"));
    }

    @Test
    void shouldNotSaveWhenSavingWithNullPhone() throws JsonProcessingException {
        request.setPhone(null);
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

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("Phone is required", errors.get("phone"));
    }

    @Test
    void shouldNotSaveWhenSavingWithTooLongPhone() throws JsonProcessingException {
        request.setPhone("e".repeat(15));
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

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("The phone must contain a maximum of 14 characters", errors.get("phone"));
    }

    @Test
    void shouldUpdateWhenUpdatingWithValidData() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);

        var response = given()
                .spec(specification)
                .pathParam("id", savedSupplier.getId())
                .contentType(CONTENT_TYPE)
                .body(request)
                .put("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        SupplierDTO updatedSupplier = objectMapper.readValue(response, SupplierDTO.class);

        assertTrue(updatedSupplier.getId() > 0);
        assertEquals(request.getName(), updatedSupplier.getName());
        assertEquals(request.getDocument(), updatedSupplier.getDocument());
        assertEquals(request.getEmail(), updatedSupplier.getEmail());
        assertEquals(request.getPhone(), updatedSupplier.getPhone());
        assertNotNull(updatedSupplier.getUpdatedAt());
        assertNotNull(updatedSupplier.getCreatedAt());
    }

    @Test
    void shouldNotUpdateWhenUpdatingWithExistDocument() {
        Supplier savedSupplier1 = createSupplier(null);
        Supplier savedSupplier2 = createSupplier(new Supplier(
                "Distribuidora A",
                "00000000000192",
                "distroa@gmail.com",
                "9887644587",
                true
        ));

        request.setDocument(savedSupplier2.getDocument());

        CustomError error = given()
                .spec(specification)
                .pathParam("id", savedSupplier1.getId())
                .contentType(CONTENT_TYPE)
                .body(request)
                .put("/{id}")
                .then()
                .statusCode(409)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(409, error.getStatus());
        assertEquals(PATH + "/" + savedSupplier1.getId(), error.getPath());
        assertEquals(EXIST_DOCUMENT_MSG, error.getMessage());
    }

    @Test
    void shouldNotUpdateWhenUpdatingWithExistEmail() {
        Supplier savedSupplier1 = createSupplier(null);
        Supplier savedSupplier2 = createSupplier(new Supplier(
                "Distribuidora A",
                "00000000000192",
                "distroa@gmail.com",
                "9887644587",
                true
        ));

        request.setEmail(savedSupplier2.getEmail());

        CustomError error = given()
                .spec(specification)
                .pathParam("id", savedSupplier1.getId())
                .contentType(CONTENT_TYPE)
                .body(request)
                .put("/{id}")
                .then()
                .statusCode(409)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(409, error.getStatus());
        assertEquals(PATH + "/" + savedSupplier1.getId(), error.getPath());
        assertEquals(EXIST_EMAIL_MSG, error.getMessage());
    }

    @Test
    void shouldNotUpdateWhenUpdatingWithInvalidId() {
        CustomError error = given()
                .spec(specification)
                .pathParam("id", INVALID_ID)
                .contentType(CONTENT_TYPE)
                .body(request)
                .put("/{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals(PATH + "/" + INVALID_ID, error.getPath());
        assertEquals(NOT_FOUND_MSG, error.getMessage());
    }

    @Test
    void shouldNotUpdateWhenUpdatingWithNullName() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);
        request.setName(null);

        var response = given()
                .spec(specification)
                .pathParam("id", savedSupplier.getId())
                .contentType(CONTENT_TYPE)
                .body(request)
                .put("/{id}")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(PATH + "/" + savedSupplier.getId(), error.getPath());
        assertEquals(VALIDATION_ERROR_MSG, error.getMessage());

        assertEquals("Name is required", errors.get("name"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingWithTooLongName() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);
        request.setName("e".repeat(201));

        var response = given()
                .spec(specification)
                .pathParam("id", savedSupplier.getId())
                .contentType(CONTENT_TYPE)
                .body(request)
                .put("/{id}")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("The name must contain a maximum of 200 characters", errors.get("name"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingWithNullDocument() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);
        request.setDocument(null);

        var response = given()
                .spec(specification)
                .pathParam("id", savedSupplier.getId())
                .contentType(CONTENT_TYPE)
                .body(request)
                .put("/{id}")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("Document is required", errors.get("document"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingWithTooLongDocument() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);
        request.setDocument("e".repeat(15));

        var response = given()
                .spec(specification)
                .pathParam("id", savedSupplier.getId())
                .contentType(CONTENT_TYPE)
                .body(request)
                .put("/{id}")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("The document must contain a maximum of 14 characters", errors.get("document"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingWithNullEmail() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);
        request.setEmail(null);

        var response = given()
                .spec(specification)
                .pathParam("id", savedSupplier.getId())
                .contentType(CONTENT_TYPE)
                .body(request)
                .put("/{id}")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("Email is required", errors.get("email"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingWithInvalidEmail() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);
        request.setEmail("e");

        var response = given()
                .spec(specification)
                .pathParam("id", savedSupplier.getId())
                .contentType(CONTENT_TYPE)
                .body(request)
                .put("/{id}")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("Invalid Email", errors.get("email"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingWithTooLongEmail() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);
        request.setEmail("e".repeat(201));

        given()
                .spec(specification)
                .pathParam("id", savedSupplier.getId())
                .contentType(CONTENT_TYPE)
                .body(request)
                .put("/{id}")
                .then()
                .statusCode(400)
                .body("errors.message", hasItem("The email must contain a maximum of 200 characters"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingWithNullPhone() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);
        request.setPhone(null);

        var response = given()
                .spec(specification)
                .pathParam("id", savedSupplier.getId())
                .contentType(CONTENT_TYPE)
                .body(request)
                .put("/{id}")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("Phone is required", errors.get("phone"));
    }

    @Test
    void shouldNotUpdateWhenUpdatingWithTooLongPhone() throws JsonProcessingException {
        Supplier savedSupplier = createSupplier(null);
        request.setPhone("e".repeat(15));

        var response = given()
                .spec(specification)
                .pathParam("id", savedSupplier.getId())
                .contentType(CONTENT_TYPE)
                .body(request)
                .put("/{id}")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(response, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertEquals("The phone must contain a maximum of 14 characters", errors.get("phone"));
    }

    @Test
    void shouldSoftDeleteWhenDeletingWithValidId() {
        Supplier savedSupplier = createSupplier(null);

        given()
                .spec(specification)
                .pathParam("id", savedSupplier.getId())
                .delete("/{id}")
                .then()
                .statusCode(204);

        Optional<Supplier> findSupplier = repository.findById(savedSupplier.getId());

        assertEquals(findSupplier.get().getActive(), false);
    }

    @Test
    void shouldNotDeleteWhenDeletingWithInvalidId() {
        CustomError error = given()
                .spec(specification)
                .pathParam("id", INVALID_ID)
                .delete("/{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals(PATH + "/" + INVALID_ID, error.getPath());
        assertEquals(NOT_FOUND_MSG, error.getMessage());
    }

    private Supplier createSupplier(Supplier supplier) {
        if(supplier == null) {
            supplier = new Supplier(
                    "TechParts",
                    "00000000000191",
                    "techparts@gmail.com",
                    "6137644711",
                    true
            );
            LocalDateTime createdAt = LocalDateTime.of(2026, 07, 20, 14, 02);

            supplier.setCreatedAt(createdAt);
        }
        return repository.save(supplier);
    }
}
