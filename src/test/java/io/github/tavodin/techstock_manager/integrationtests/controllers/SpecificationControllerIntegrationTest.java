package io.github.tavodin.techstock_manager.integrationtests.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.configurations.AbstractIntegrationTest;
import io.github.tavodin.techstock_manager.dto.SpecificationDTO;
import io.github.tavodin.techstock_manager.dto.SpecificationRequestDTO;
import io.github.tavodin.techstock_manager.dto.error.CustomError;
import io.github.tavodin.techstock_manager.dto.error.FieldError;
import io.github.tavodin.techstock_manager.dto.error.ValidationError;
import io.github.tavodin.techstock_manager.entities.Unit;
import io.github.tavodin.techstock_manager.enums.SpecificationType;
import io.github.tavodin.techstock_manager.integrationtests.utils.AuthTestUtil;
import io.github.tavodin.techstock_manager.repositories.SpecificationRepository;
import io.github.tavodin.techstock_manager.repositories.UnitRepository;
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
class SpecificationControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpecificationRepository repository;

    @Autowired
    private UnitRepository unitRepository;

    @LocalServerPort
    private int port;

    private static RequestSpecification specification;
    private Long invalidId = Long.MAX_VALUE;
    private Long unitId;
    private String path = "/specifications";
    private String notFoundMsg = "Specification not found!";
    private String unitNotFoundMsg = "Unit not found!";
    private String validationErrorMsg = "Entity validation error";
    private SpecificationRequestDTO request = new SpecificationRequestDTO(
            "RAM", SpecificationType.NUMBER, true, 1L);
    private String token;

    @BeforeEach
    void setup() {
        RestAssured.port = port;

        token = AuthTestUtil.getToken(port);

        specification = new RequestSpecBuilder()
                .setBasePath("/specifications")
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        repository.deleteAll();
        unitRepository.deleteAll();
    }

    @Test
    void shouldFindSpecificationWhenIdExist() throws JsonProcessingException {
        SpecificationDTO savedSpecification = createSpecification(request);

        var result = given().spec(specification)
                .pathParam("id", savedSpecification.getId())
                .when()
                .get("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        SpecificationDTO findSpecification = objectMapper.readValue(result, SpecificationDTO.class);

        assertTrue(findSpecification.getId() > 0);
        assertEquals(savedSpecification.getName(), findSpecification.getName());
        assertEquals(savedSpecification.getDataType(), findSpecification.getDataType());
        assertEquals(savedSpecification.getFilterable(), findSpecification.getFilterable());
        assertEquals(savedSpecification.getUnitSymbol(), findSpecification.getUnitSymbol());
        assertNotNull(findSpecification.getCreatedAt());
        assertNotNull(findSpecification.getUpdatedAt());
    }

    @Test
    void shouldReturnNotFoundAndCustomErrorObjectWhenFindingWithInvalidId() {
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
        assertEquals(404, error.getStatus());
        assertEquals(error.getMessage(), notFoundMsg);
        assertEquals(error.getPath(), path + "/" + invalidId);
    }

    @Test
    void shouldReturnUnitsWhenFindAll() throws JsonProcessingException {
        createSpecification(request);

        given().spec(specification)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("_embedded.specifications[0].id", notNullValue())
                .body("_embedded.specifications[0].name", equalTo("RAM"))
                .body("_embedded.specifications[0].dataType", equalTo(request.dataType().name()))
                .body("_embedded.specifications[0].createdAt", notNullValue())
                .body("_embedded.specifications[0].updatedAt", notNullValue());
    }

    @Test
    void shouldReturnPageSpecificationsWhenFindAll() throws JsonProcessingException {
        createSpecification(request);
        createSpecification(
                new SpecificationRequestDTO("VRAM", SpecificationType.NUMBER, true, null));

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
                .body("_embedded.specifications.name", hasItems("RAM", "VRAM"))
                .body("_links.self.href",
                        containsString(String.format("%s?page=%s&size=%s&sort=%s", path, number, size, order)))
                .body("page.size", equalTo(5))
                .body("page.totalElements", equalTo(2))
                .body("page.totalPages", equalTo(1))
                .body("page.number", equalTo(0));
    }

    @Test
    void shouldSaveSpecificationAndReturnUnitObjectWhenSaving() throws JsonProcessingException {
        SpecificationDTO savedUnit = createSpecification(request);

        assertTrue(savedUnit.getId() > 0);
        assertEquals(request.name(), savedUnit.getName());
        assertEquals(request.dataType(), savedUnit.getDataType());
        assertEquals(request.filterable(), savedUnit.getFilterable());
        assertEquals("GB", savedUnit.getUnitSymbol());
        assertNotNull(savedUnit.getCreatedAt());
        assertNotNull(savedUnit.getUpdatedAt());
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullName() throws JsonProcessingException {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                null, request.dataType(), request.filterable(), request.unitId());

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
        assertEquals(path, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Name is required!", errors.get("name"));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithLongName() throws JsonProcessingException {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                "e".repeat(101), request.dataType(), request.filterable(), request.unitId());

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
        assertEquals(path, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("The name must contain a maximum of 100 characters.", errors.get("name"));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullDataType() throws JsonProcessingException {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                request.name(), null, request.filterable(), request.unitId());

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
        assertEquals(path, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Data Type is required!", errors.get("dataType"));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithInvalidDataType() throws JsonProcessingException {
        String invalidRequest = """
                {
                    "name": "Test",
                    "dataType": "INVALID",
                    "filterable": true,
                    "unitId": 1
                }
                """;

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
        assertEquals(path, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Invalid value. Allowed values: STRING, NUMBER, BOOLEAN", errors.get("dataType"));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullFilterable() throws JsonProcessingException {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                request.name(), request.dataType(), null, request.unitId());

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
        assertEquals(path, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Filterable is required!", errors.get("filterable"));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullUnitId() throws JsonProcessingException {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                request.name(), request.dataType(), request.filterable(), null);

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
        assertEquals(path, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Unit ID is required!", errors.get("unitId"));
    }

    @Test
    void shouldReturnValidationErrorAndNotFoundWhenSavingWithInvalidUnitId() {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                request.name(), request.dataType(), request.filterable(), Long.MAX_VALUE);

        var error = given().spec(specification)
                .body(invalidRequest)
                .contentType("application/json")
                .post()
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals(path, error.getPath());
        assertEquals(unitNotFoundMsg, error.getMessage());
    }

    @Test
    void shouldUpdateSpecificationWhenUpdatingWithValidData() throws JsonProcessingException {
        SpecificationDTO savedSpecification = createSpecification(request);
        SpecificationRequestDTO updateRequest = new SpecificationRequestDTO(
                "VRAM", SpecificationType.NUMBER, true, unitId);

        var content = given().spec(specification)
                .pathParam("id", savedSpecification.getId())
                .contentType("application/json")
                .body(updateRequest)
                .when()
                .put("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        SpecificationDTO updatedUnit = objectMapper.readValue(content, SpecificationDTO.class);

        assertTrue(updatedUnit.getId() > 0);
        assertEquals(updateRequest.name(), updatedUnit.getName());
        assertEquals(updateRequest.dataType(), updatedUnit.getDataType());
        assertEquals(updateRequest.filterable(), updatedUnit.getFilterable());
        assertEquals(updatedUnit.getUnitSymbol(), updatedUnit.getUnitSymbol());
        assertNotNull(updatedUnit.getCreatedAt());
        assertNotNull(updatedUnit.getUpdatedAt());
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenUpdatingWithInvalidId() throws JsonProcessingException {
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
        assertEquals(error.getPath(), path + "/" + invalidId);
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithNullName() throws JsonProcessingException {
        SpecificationDTO savedSpecification = createSpecification(request);
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                null, SpecificationType.NUMBER, true, unitId);

        var content = given().spec(specification)
                .pathParam("id", savedSpecification.getId())
                .contentType("application/json")
                .body(invalidRequest)
                .when()
                .put("{id}")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        String pathError = path + "/" + savedSpecification.getId();

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(pathError, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Name is required!", errors.get("name"));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithLongName() throws JsonProcessingException {
        SpecificationDTO savedSpecification = createSpecification(request);
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                "e".repeat(101), SpecificationType.NUMBER, true, unitId);

        var content = given().spec(specification)
                .pathParam("id", savedSpecification.getId())
                .contentType("application/json")
                .body(invalidRequest)
                .when()
                .put("{id}")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        String pathError = path + "/" + savedSpecification.getId();

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(pathError, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("The name must contain a maximum of 100 characters.", errors.get("name"));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithNullDataType() throws JsonProcessingException {
        SpecificationDTO savedSpecification = createSpecification(request);
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                "VRAM", null, true, unitId);

        var content = given().spec(specification)
                .pathParam("id", savedSpecification.getId())
                .contentType("application/json")
                .body(invalidRequest)
                .when()
                .put("{id}")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        String pathError = path + "/" + savedSpecification.getId();

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(pathError, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Data Type is required!", errors.get("dataType"));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithInvalidDataType() throws JsonProcessingException {
        String invalidRequest = """
                {
                    "name": "Test",
                    "dataType": "INVALID",
                    "filterable": true,
                    "unitId": 1
                }
                """;

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
        assertEquals(path, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Invalid value. Allowed values: STRING, NUMBER, BOOLEAN", errors.get("dataType"));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithNullFilterable() throws JsonProcessingException {
        SpecificationDTO savedSpecification = createSpecification(request);
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                "VRAM", SpecificationType.NUMBER, null, unitId);

        var content = given().spec(specification)
                .pathParam("id", savedSpecification.getId())
                .contentType("application/json")
                .body(invalidRequest)
                .when()
                .put("{id}")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        String pathError = path + "/" + savedSpecification.getId();

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(pathError, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Filterable is required!", errors.get("filterable"));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithNullUnitId() throws JsonProcessingException {
        SpecificationDTO savedSpecification = createSpecification(request);
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                "VRAM", SpecificationType.NUMBER, true, null);

        var content = given().spec(specification)
                .pathParam("id", savedSpecification.getId())
                .contentType("application/json")
                .body(invalidRequest)
                .when()
                .put("{id}")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asString();

        ValidationError error = objectMapper.readValue(content, ValidationError.class);

        Map<String, String> errors = error.getErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getMessage));

        String pathError = path + "/" + savedSpecification.getId();

        assertNotNull(error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals(pathError, error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Unit ID is required!", errors.get("unitId"));
    }

    @Test
    void shouldReturnValidationErrorAndNotFoundWhenUpdatingWithInvalidUnitId() throws JsonProcessingException {
        SpecificationDTO savedSpecification = createSpecification(request);
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                "VRAM", SpecificationType.NUMBER, true, Long.MAX_VALUE);

        var error = given().spec(specification)
                .pathParam("id", savedSpecification.getId())
                .contentType("application/json")
                .body(invalidRequest)
                .when()
                .put("{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        String pathError = path + "/" + savedSpecification.getId();

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals(pathError, error.getPath());
        assertEquals(unitNotFoundMsg, error.getMessage());
    }

    @Test
    void shouldDeleteWhenDeletingWithValidId() throws JsonProcessingException {
        SpecificationDTO savedSpecification = createSpecification(request);

        given()
                .spec(specification)
                .pathParam("id", savedSpecification.getId())
                .delete("/{id}")
                .then()
                .statusCode(204);

        given()
                .spec(specification)
                .pathParam("id", savedSpecification.getId())
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
        assertEquals(path + "/" + invalidId, error.getPath());
    }

    @Test
    void shouldNotDeleteUnitWhenUnitIsInUse() {
        fail();
    }

    private SpecificationDTO createSpecification(SpecificationRequestDTO request) throws JsonProcessingException {
        Unit unit = unitRepository.save(new Unit("Gigabyte", "GB"));
        unitId = unit.getId();
        request = new SpecificationRequestDTO(request.name(), request.dataType(), request.filterable(), unit.getId());

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

        return objectMapper.readValue(content, SpecificationDTO.class);
    }

}
