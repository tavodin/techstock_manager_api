package io.github.tavodin.techstock_manager.integrationtests.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.configurations.AbstractIntegrationTest;
import io.github.tavodin.techstock_manager.dto.CategoryDTO;
import io.github.tavodin.techstock_manager.dto.CategoryRequestDTO;
import io.github.tavodin.techstock_manager.dto.error.CustomError;
import io.github.tavodin.techstock_manager.dto.error.FieldError;
import io.github.tavodin.techstock_manager.dto.error.ValidationError;
import io.github.tavodin.techstock_manager.entities.Category;
import io.github.tavodin.techstock_manager.entities.CategorySpecification;
import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.integrationtests.utils.AuthTestUtil;
import io.github.tavodin.techstock_manager.repositories.CategoryRepository;
import io.github.tavodin.techstock_manager.repositories.CategorySpecificationRepository;
import io.github.tavodin.techstock_manager.repositories.SpecificationRepository;
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
class CategoryControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository repository;

    @Autowired
    private SpecificationRepository specificationRepository;

    @Autowired
    private CategorySpecificationRepository catSpecRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private static final String PATH = "/categories";
    private static final String CONTENT_TYPE = "application/json";
    private static RequestSpecification specification;
    private Long invalidId = Long.MAX_VALUE;
    private Long unitId;
    private String notFoundMsg = "Category not found!";
    private String validationErrorMsg = "Entity validation error";
    private CategoryRequestDTO request = new CategoryRequestDTO("Monitor");
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
        catSpecRepository.deleteAll();
        specificationRepository.deleteAll();
    }

    @Test
    void shouldFindCategoryWhenFindingWithValidId() throws JsonProcessingException {
        CategoryDTO savedCategory = createCategory(request);

        var response = given().spec(specification)
                .pathParam("id", savedCategory.getId())
                .get("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        CategoryDTO findCategory = objectMapper.readValue(response, CategoryDTO.class);

        assertTrue(findCategory.getId() > 0);
        assertEquals(savedCategory.getName(), findCategory.getName());
        assertNotNull(findCategory.getCreatedAt());
        assertNotNull(findCategory.getUpdatedAt());
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
    void shouldReturnCategoriesWhenFindAll() throws JsonProcessingException {
        createCategory(request);

        given()
                .spec(specification)
                .get()
                .then()
                .statusCode(200)
                .body("_embedded.categories[0].id", notNullValue())
                .body("_embedded.categories[0].name", equalTo("Monitor"))
                .body("_embedded.categories[0].createdAt", notNullValue())
                .body("_embedded.categories[0].updatedAt", notNullValue());
    }

    @Test
    void shouldReturnPageCategoriesWhenFindAll() throws JsonProcessingException {
        createCategory(request);
        createCategory(
                new CategoryRequestDTO("Mouse"));

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
                .body("_embedded.categories.name", hasItems("Monitor", "Mouse"))
                .body("_links.self.href",
                        containsString(String.format("%s?page=%s&size=%s&sort=%s", PATH, number, size, order)))
                .body("page.size", equalTo(5))
                .body("page.totalElements", equalTo(2))
                .body("page.totalPages", equalTo(1))
                .body("page.number", equalTo(0));
    }

    @Test
    void shouldReturnSpecificationsListWhenFindingSpecificationsWithValidCategoryId() throws JsonProcessingException {
        Specification spec = SpecificationBuilder.builder().withId(null).withUnit(null).build();

        Category category = new Category();
        category.setName("Monitor");

        category = repository.save(category);
        spec = specificationRepository.save(spec);

        CategorySpecification specCategory = new CategorySpecification();
        specCategory.setCategory(category);
        specCategory.setSpecification(spec);
        specCategory.setRequired(true);

        specCategory = catSpecRepository.save(specCategory);

        given().spec(specification)
                .pathParam("id", specCategory.getId())
                .get("/{id}/specifications")
                .then()
                .statusCode(200)
                .body("[0].categorySpecificationId", notNullValue())
                .body("[0].specificationName", equalTo(spec.getName()))
                .body("[0].isRequired", equalTo(specCategory.getRequired()));
    }

    @Test
    void shouldReturnCustomErrorDTOAndNotFoundWhenFindingSpecificationWithInvalidCategoryId() {
        CustomError error = given().spec(specification)
                .pathParam("id", invalidId)
                .get("/{id}/specifications")
                .then()
                .statusCode(404)
                .extract()
                .as(CustomError.class);

        assertEquals(notFoundMsg, error.getMessage());
        assertEquals(404, error.getStatus());
        assertEquals(PATH + "/" + invalidId + "/specifications", error.getPath());
        assertNotNull(error.getTimestamp());
    }

    @Test
    void shouldSaveCategoryWhenSavingWithValidData() throws JsonProcessingException {
        CategoryDTO savedCategory = createCategory(request);

        assertTrue(savedCategory.getId() > 0);
        assertEquals(request.name(), savedCategory.getName());
        assertNotNull(savedCategory.getCreatedAt());
        assertNotNull(savedCategory.getUpdatedAt());
    }

    @Test
    void shouldNotSaveCategoryWhenSavingWithNullName() throws JsonProcessingException {
        CategoryRequestDTO invalidRequest = new CategoryRequestDTO(null);

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

        assertEquals("Name is required!", errors.get("name"));
    }

    @Test
    void shouldNotSaveCategoryWhenSavingWithLongName() throws JsonProcessingException {
        CategoryRequestDTO invalidRequest = new CategoryRequestDTO("e".repeat(101));

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

        assertEquals("The name must contain a maximum of 100 characters.", errors.get("name"));
    }

    @Test
    void shouldUpdateCategoryWhenUpdatingWithValidData() throws JsonProcessingException {
        CategoryDTO savedCategory = createCategory(request);
        CategoryRequestDTO updateRequest = new CategoryRequestDTO("Webcam");

        var response = given()
                .spec(specification)
                .pathParam("id", savedCategory.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        CategoryDTO updatedCategory = objectMapper.readValue(response, CategoryDTO.class);

        assertEquals(savedCategory.getId(), updatedCategory.getId());
        assertEquals(updateRequest.name(), updatedCategory.getName());
        assertNotNull(updatedCategory.getCreatedAt());
        assertNotNull(updatedCategory.getUpdatedAt());
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
        assertEquals(error.getPath(), PATH + "/" + invalidId);
    }

    @Test
    void shouldNotUpdateCategoryWhenUpdatingWithNullName() throws JsonProcessingException {
        CategoryDTO savedCategory = createCategory(request);
        CategoryRequestDTO invalidRequest = new CategoryRequestDTO(null);

        var content = given()
                .spec(specification)
                .pathParam("id", savedCategory.getId())
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
        assertEquals(PATH + "/" + savedCategory.getId(), error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("Name is required!", errors.get("name"));
    }

    @Test
    void shouldNotUpdateCategoryWhenUpdatingLongName() throws JsonProcessingException {
        CategoryDTO savedCategory = createCategory(request);
        CategoryRequestDTO invalidRequest = new CategoryRequestDTO("e".repeat(101));

        var content = given()
                .spec(specification)
                .pathParam("id", savedCategory.getId())
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
        assertEquals(PATH + "/" + savedCategory.getId(), error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals("The name must contain a maximum of 100 characters.", errors.get("name"));
    }

    @Test
    void shouldDeleteWhenDeletingWithValidId() throws JsonProcessingException {
        CategoryDTO savedCategory = createCategory(request);

        given()
                .spec(specification)
                .pathParam("id", savedCategory.getId())
                .delete("/{id}")
                .then()
                .statusCode(204);

        given()
                .spec(specification)
                .pathParam("id", savedCategory.getId())
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
    void shouldNotDeleteUnitWhenUnitIsInUse() {
        fail();
    }

    private CategoryDTO createCategory(CategoryRequestDTO request) throws JsonProcessingException {
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

        return objectMapper.readValue(content, CategoryDTO.class);
    }
}
