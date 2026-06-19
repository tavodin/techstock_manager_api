package io.github.tavodin.techstock_manager.integrationtests.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.configurations.AbstractIntegrationTest;
import io.github.tavodin.techstock_manager.dto.BrandDTO;
import io.github.tavodin.techstock_manager.dto.BrandRequestDTO;
import io.github.tavodin.techstock_manager.dto.error.CustomError;
import io.github.tavodin.techstock_manager.dto.error.FieldError;
import io.github.tavodin.techstock_manager.dto.error.ValidationError;
import io.github.tavodin.techstock_manager.integrationtests.utils.AuthTestUtil;
import io.github.tavodin.techstock_manager.repositories.BrandRepository;
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

import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class BrandControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BrandRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private static final String PATH = "/brands";
    private static final String CONTENT_TYPE = "application/json";
    private static RequestSpecification specification;
    private Long invalidId = Long.MAX_VALUE;
    private String notFoundMsg = "Brand not found";
    private String validationErrorMsg = "Entity validation error";
    private BrandRequestDTO request = new BrandRequestDTO("DELL");
    private String token;

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

        repository.deleteAll();
    }

    @Test
    void shouldFindBrandWhenFindingWithValidId() throws JsonProcessingException {
        BrandDTO savedBrand = createBrand(request);

        var response = given().spec(specification)
                .pathParam("id", savedBrand.getId())
                .get("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        BrandDTO findBrand = objectMapper.readValue(response, BrandDTO.class);

        assertTrue(findBrand.getId() > 0);
        assertEquals(savedBrand.getName(), findBrand.getName());
        assertNotNull(findBrand.getCreatedAt());
        assertNotNull(findBrand.getUpdatedAt());
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenFindingWithInvalidId() {
        CustomError error = given().spec(specification)
                .pathParam("id", invalidId)
                .get("/{id}")
                .then()
                .extract()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals(notFoundMsg, error.getMessage());
        assertEquals(PATH + "/" + invalidId, error.getPath());
    }

    @Test
    void shouldReturnBrandsWhenFindAll() throws JsonProcessingException {
        createBrand(request);

        given()
                .spec(specification)
                .get()
                .then()
                .statusCode(200)
                .body("_embedded.brands[0].id", notNullValue())
                .body("_embedded.brands[0].name", equalTo("DELL"))
                .body("_embedded.brands[0].createdAt", notNullValue())
                .body("_embedded.brands[0].updatedAt", notNullValue());
    }

    @Test
    void shouldReturnPageBrandsWhenFindAll() throws JsonProcessingException {
        createBrand(request);
        createBrand(
                new BrandRequestDTO("HP"));

        int number = 0;
        int size = 5;
        String order = "name,asc";

        given().spec(specification)
                .queryParam("page", number)
                .queryParam("size", size)
                .queryParam("sort", order)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("_embedded.brands.name", hasItems("DELL", "HP"))
                .body("_links.self.href",
                        containsString(String.format("%s?page=%s&size=%s&sort=%s", PATH, number, size, order)))
                .body("page.size", equalTo(5))
                .body("page.totalElements", equalTo(2))
                .body("page.totalPages", equalTo(1))
                .body("page.number", equalTo(0));
    }

    @Test
    void shouldSaveBrandWhenSavingWithValidData() throws JsonProcessingException {
        BrandDTO savedBrand = createBrand(request);

        assertTrue(savedBrand.getId() > 0);
        assertEquals(request.name(), savedBrand.getName());
        assertNotNull(savedBrand.getCreatedAt());
        assertNotNull(savedBrand.getUpdatedAt());
    }

    @Test
    void shouldNotSaveBrandWhenSavingWithNullName() throws JsonProcessingException {
        BrandRequestDTO invalidRequest = new BrandRequestDTO(null);

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(invalidRequest)
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
    void shouldNotSaveBrandWhenSavingWithLongName() throws JsonProcessingException {
        BrandRequestDTO invalidRequest = new BrandRequestDTO("e".repeat(101));

        var content = given()
                .spec(specification)
                .contentType(CONTENT_TYPE)
                .body(invalidRequest)
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

        assertEquals("Name must contain between 2 and 100 characters", errors.get("name"));
    }

    @Test
    void shouldUpdateBrandWhenUpdatingWithValidData() throws JsonProcessingException {
        BrandDTO savedBrand = createBrand(request);
        BrandRequestDTO updateRequest = new BrandRequestDTO("Webcam");

        var response = given()
                .spec(specification)
                .pathParam("id", savedBrand.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        BrandDTO updatedBrandDTO = objectMapper.readValue(response, BrandDTO.class);

        assertEquals(savedBrand.getId(), updatedBrandDTO.getId());
        assertEquals(updateRequest.name(), updatedBrandDTO.getName());
        assertNotNull(updatedBrandDTO.getCreatedAt());
        assertNotNull(updatedBrandDTO.getUpdatedAt());
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenUpdatingWithInvalidId() {
        var error = given().spec(specification)
                .pathParam("id", invalidId)
                .contentType("application/json")
                .body(request)
                .when()
                .put("{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals(error.getMessage(), notFoundMsg);
        assertEquals(error.getPath(), PATH + "/" + invalidId);
    }

    @Test
    void shouldNotUpdateBrandWhenUpdatingWithNullName() throws JsonProcessingException {
        BrandDTO savedBrand = createBrand(request);
        BrandRequestDTO invalidRequest = new BrandRequestDTO(null);

        var content = given()
                .spec(specification)
                .pathParam("id", savedBrand.getId())
                .contentType(CONTENT_TYPE)
                .body(invalidRequest)
                .put("/{id}")
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
        assertEquals(PATH + "/" + savedBrand.getId(), error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Name is required", errors.get("name"));
    }

    @Test
    void shouldNotUpdateBrandWhenUpdatingLongName() throws JsonProcessingException {
        BrandDTO savedBrand = createBrand(request);
        BrandRequestDTO invalidRequest = new BrandRequestDTO("e".repeat(101));

        var content = given()
                .spec(specification)
                .pathParam("id", savedBrand.getId())
                .contentType(CONTENT_TYPE)
                .body(invalidRequest)
                .put("/{id}")
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
        assertEquals(PATH + "/" + savedBrand.getId(), error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Name must contain between 2 and 100 characters", errors.get("name"));
    }

    @Test
    void shouldDeleteWhenDeletingWithValidId() throws JsonProcessingException {
        BrandDTO savedBrand = createBrand(request);

        given()
                .spec(specification)
                .pathParam("id", savedBrand.getId())
                .delete("/{id}")
                .then()
                .statusCode(204);

        given()
                .spec(specification)
                .pathParam("id", savedBrand.getId())
                .delete("/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturnNotFoundWhenIdDoesNotExistInDelete() {
        var error = given().spec(specification)
                .pathParam("id", invalidId)
                .when()
                .delete("{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals(notFoundMsg, error.getMessage());
        assertEquals(PATH + "/" + invalidId, error.getPath());
    }

    @Test
    void shouldNotDeleteBrandWhenBrandIsInUse() {
        fail();
    }

    private BrandDTO createBrand(BrandRequestDTO request) throws JsonProcessingException {
        var content = given()
                .spec(specification)
                .contentType("application/json")
                .body(request)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString();

        return objectMapper.readValue(content, BrandDTO.class);
    }
}
