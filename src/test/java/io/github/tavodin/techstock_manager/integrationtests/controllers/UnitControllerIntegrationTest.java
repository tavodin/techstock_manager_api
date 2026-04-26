package io.github.tavodin.techstock_manager.integrationtests.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.dto.UnitDTO;
import io.github.tavodin.techstock_manager.dto.UnitRequestDTO;
import io.github.tavodin.techstock_manager.dto.error.CustomError;
import io.github.tavodin.techstock_manager.dto.error.FieldError;
import io.github.tavodin.techstock_manager.dto.error.ValidationError;
import io.github.tavodin.techstock_manager.integrationtests.testcontainers.AbstractIntegrationTest;
import io.github.tavodin.techstock_manager.integrationtests.utils.AuthTestUtil;
import io.github.tavodin.techstock_manager.repositories.UnitRepository;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

class UnitControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private static RequestSpecification specification;
    private static UnitRequestDTO request;
    private static Long invalidId;

    @Autowired
    private UnitRepository repository;

    @LocalServerPort
    private int port;

    @BeforeAll
    static void beforeAll() {
        request = new UnitRequestDTO("Gigahertz", "GHz");
        invalidId = Long.MAX_VALUE;
    }

    @BeforeEach
    void setup() {
        RestAssured.port = port;

        String token = AuthTestUtil.getToken(port);

        specification = new RequestSpecBuilder()
                .setBasePath("/units")
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        repository.deleteAll();
    }

    @Test
    void shouldReturnUnitWhenIdExist() throws JsonProcessingException {
        UnitDTO savedUnit = createUnit(request);

        var result = given().spec(specification)
                .pathParam("id", savedUnit.getId())
                .when()
                .get("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        UnitDTO findUnit = objectMapper.readValue(result, UnitDTO.class);

        assertTrue(findUnit.getId() > 0);
        assertEquals(savedUnit.getName(), findUnit.getName());
        assertEquals(savedUnit.getSymbol(), findUnit.getSymbol());
        assertNotNull(findUnit.getCreatedAt());
        assertNotNull(findUnit.getUpdatedAt());
    }

    @Test
    void shouldReturnNotFoundAndErrorObjectWhenIdDoesNotExistInFindById() {
        var error = given().spec(specification)
                .pathParam("id", invalidId)
                .when()
                .get("{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(error.getStatus(), 404);
        assertEquals(error.getMessage(), "Unit not found!");
        assertEquals(error.getPath(), "/units/" + invalidId);
    }

    @Test
    void shouldReturnUnitsWhenFindAll() throws JsonProcessingException {
        createUnit(request);

        given().spec(specification)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("_embedded.units[0].id", notNullValue())
                .body("_embedded.units[0].symbol", equalTo("GHz"))
                .body("_embedded.units[0].createdAt", notNullValue())
                .body("_embedded.units[0].updatedAt", notNullValue());
    }

    @Test
    void shouldReturnPagedUnitsWhenFindAll() throws JsonProcessingException {
        createUnit(request);
        createUnit(new UnitRequestDTO("Centímetro", "cm"));

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
                .body("_embedded.units.name", hasItems("Centímetro", "Gigahertz"))
                .body("_links.self.href",
                        containsString(String.format("/units?page=%s&size=%s&sort=%s", number, size, order)))
                .body("page.size", equalTo(5))
                .body("page.totalElements", equalTo(2))
                .body("page.totalPages", equalTo(1))
                .body("page.number", equalTo(0));
    }

    @Test
    void shouldSaveUnitAndReturnUnitObjectWhenSaveUnit() throws JsonProcessingException {
        UnitDTO savedUnit = createUnit(request);

        assertTrue(savedUnit.getId() > 0);
        assertEquals(request.name(), savedUnit.getName());
        assertEquals(request.symbol(), savedUnit.getSymbol());
        assertNotNull(savedUnit.getCreatedAt());
        assertNotNull(savedUnit.getUpdatedAt());
    }

    @Test
    void shouldReturnValidationErrorWhenDataIsInvalidInSave() throws JsonProcessingException {
        UnitRequestDTO invalidRequest = new UnitRequestDTO("e".repeat(46), null);

        var content = given().spec(specification)
                .body(invalidRequest)
                .contentType("application/json")
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
        assertEquals("/units", error.getPath());
        assertEquals("Entity validation error", error.getMessage());

        assertEquals("Unit Symbol is required!", errors.get("symbol"));
        assertEquals("The unit name must contain 45 characters.", errors.get("name"));
    }

    @Test
    void shouldSaveUnitWhenNameIsNull() throws JsonProcessingException {
        UnitRequestDTO requestDTO = new UnitRequestDTO(null, "Hz");

        var content = given().spec(specification)
                .contentType("application/json")
                .body(requestDTO)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString();

        UnitDTO savedUnit = objectMapper.readValue(content, UnitDTO.class);

        assertNull(savedUnit.getName());
        assertEquals("Hz", savedUnit.getSymbol());
    }

    @Test
    void shouldNotSaveUnitWhenNameIsTooLong() {
        UnitRequestDTO invalidRequest = new UnitRequestDTO("e".repeat(46), "Hz");

        given().spec(specification)
                .contentType("application/json")
                .body(invalidRequest)
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    void shouldNotSaveUnitWhenSymbolIsTooLong() {
        UnitRequestDTO invalidRequest = new UnitRequestDTO("Hertz", "e".repeat(11));

        given().spec(specification)
                .contentType("application/json")
                .body(invalidRequest)
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    void shouldNotSaveUnitWhenSymbolIsNull() {
        UnitRequestDTO invalidRequest = new UnitRequestDTO("Hertz", null);

        given().spec(specification)
                .contentType("application/json")
                .body(invalidRequest)
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    void shouldUpdateUnitAndReturnUnitObjectWhenUpdateUnit() throws JsonProcessingException {
        UnitDTO savedUnit = createUnit(request);

        UnitRequestDTO updateRequest = new UnitRequestDTO("Gramas", "g");

        var content = given().spec(specification)
                .pathParam("id", savedUnit.getId())
                .contentType("application/json")
                .body(updateRequest)
                .when()
                .put("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        UnitDTO updatedUnit = objectMapper.readValue(content, UnitDTO.class);

        assertTrue(updatedUnit.getId() > 0);
        assertEquals(updateRequest.name(), updatedUnit.getName());
        assertEquals(updateRequest.symbol(), updatedUnit.getSymbol());
        assertNotNull(updatedUnit.getCreatedAt());
        assertNotNull(updatedUnit.getUpdatedAt());
    }

    @Test
    void shouldReturnNotFoundAndObjectErrorWhenIdDoesNotExistInUpdate() {
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
        assertEquals(error.getStatus(), 404);
        assertEquals(error.getMessage(), "Unit not found!");
        assertEquals(error.getPath(), "/units/" + invalidId);
    }

    @Test
    void shouldReturnValidationErrorWhenDataIsInvalidInUpdate() throws JsonProcessingException {
        UnitDTO savedUnit = createUnit(request);
        UnitRequestDTO invalidRequest = new UnitRequestDTO("e".repeat(46), null);

        var content = given().spec(specification)
                .pathParam("id", savedUnit.getId())
                .contentType("application/json")
                .body(invalidRequest)
                .put("{id}")
                .then()
                .statusCode(400)
                .extract()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);
        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals("Entity validation error", error.getMessage());
        assertEquals("/units/" + savedUnit.getId(), error.getPath());

        assertEquals("Unit Symbol is required!", errors.get("symbol"));
        assertEquals("The unit name must contain 45 characters.", errors.get("name"));
    }

    @Test
    void shouldUpdateUnitWhenNameIsNull() throws JsonProcessingException {
        UnitDTO savedUnit = createUnit(request);
        UnitRequestDTO updateRequest = new UnitRequestDTO(null, "Hertz");

        var content = given().spec(specification)
                .pathParam("id", savedUnit.getId())
                .contentType("application/json")
                .body(updateRequest)
                .put("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        UnitDTO updatedUnit = objectMapper.readValue(content, UnitDTO.class);

        assertEquals(savedUnit.getId(), updatedUnit.getId());
        assertNull(updatedUnit.getName());
        assertEquals(updatedUnit.getSymbol(), updatedUnit.getSymbol());
        assertTrue(updatedUnit.getUpdatedAt().isAfter(savedUnit.getUpdatedAt()));
    }

    @Test
    void shouldNotUpdateUnitWhenNameIsTooLong() throws JsonProcessingException {
        UnitDTO savedUnit = createUnit(request);
        UnitRequestDTO invalidRequest = new UnitRequestDTO("e".repeat(46), "Hertz");

        given().spec(specification)
                .pathParam("id", savedUnit.getId())
                .contentType("application/json")
                .body(invalidRequest)
                .put("{id}")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldNotUpdateUnitWhenSymbolIsNull() throws JsonProcessingException {
        UnitDTO savedUnit = createUnit(request);
        UnitRequestDTO invalidRequest = new UnitRequestDTO("Hertz", null);

        given().spec(specification)
                .pathParam("id", savedUnit.getId())
                .contentType("application/json")
                .body(invalidRequest)
                .put("{id}")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldNotUpdateUnitWhenSymbolIsTooLong() throws JsonProcessingException {
        UnitDTO savedUnit = createUnit(request);
        UnitRequestDTO invalidRequest = new UnitRequestDTO("Hertz", "e".repeat(11));

        given().spec(specification)
                .pathParam("id", savedUnit.getId())
                .contentType("application/json")
                .body(invalidRequest)
                .put("{id}")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldDeleteUnitAndReturnNoContentWhenIdIsValid() throws JsonProcessingException {
        UnitDTO savedUnit = createUnit(request);

        given().spec(specification)
                .pathParam("id", savedUnit.getId())
                .delete("{id}")
                .then()
                .statusCode(204);

        given().spec(specification)
                .pathParam("id", savedUnit.getId())
                .get("{id}")
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
        assertEquals(error.getStatus(), 404);
        assertEquals(error.getMessage(), "Unit not found!");
        assertEquals(error.getPath(), "/units/" + invalidId);
    }

    @Test
    void shouldNotDeleteUnitWhenUnitIsInUse() {
        fail();
    }

    private UnitDTO createUnit(UnitRequestDTO request) throws JsonProcessingException {
        var content = given().spec(specification)
                .contentType("application/json")
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString();

        return objectMapper.readValue(content, UnitDTO.class);
    }
}
