package io.github.tavodin.techstock_manager.integrationtests.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.configurations.AbstractIntegrationTest;
import io.github.tavodin.techstock_manager.dto.CategoryDTO;
import io.github.tavodin.techstock_manager.dto.CategoryRequestDTO;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationDTO;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationRequestDTO;
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

        catSpecRepository.deleteAll();
        repository.deleteAll();
        specificationRepository.deleteAll();
    }

    @Test
    void shouldFindCategoryWhenFindingWithValidId() throws JsonProcessingException {
        CategoryDTO savedCategory = createCategoryDTO(request);

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
        createCategoryDTO(request);

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
        createCategoryDTO(request);
        createCategoryDTO(
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
        CategoryDTO savedCategory = createCategoryDTO(request);

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
        CategoryDTO savedCategory = createCategoryDTO(request);
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
    void shouldNotUpdateCategoryWhenUpdatingWithNullName() throws JsonProcessingException {
        CategoryDTO savedCategory = createCategoryDTO(request);
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
        CategoryDTO savedCategory = createCategoryDTO(request);
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
        CategoryDTO savedCategory = createCategoryDTO(request);

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
    void shouldNotDeleteCategoryWhenCategoryIsInUse() {
        String errorMsg = "Category is in use and cannot be deleted";
        Specification spec = createSpecification();
        Category category = createCategory();

        CategorySpecification specCategory = new CategorySpecification();
        specCategory.setCategory(category);
        specCategory.setSpecification(spec);
        specCategory.setRequired(true);

        catSpecRepository.save(specCategory);

        CustomError error = given().spec(specification)
                .pathParam("id", category.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(409)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(error.getTimestamp());
        assertEquals(409, error.getStatus());
        assertEquals(errorMsg, error.getMessage());
        assertEquals(PATH + "/" + category.getId(), error.getPath());
    }

    @Test
    void shouldReturnSpecificationsListWhenFindingSpecificationsWithValidCategoryId() {
        Specification spec = createSpecification();
        Category category = createCategory();

        CategorySpecification specCategory = new CategorySpecification();
        specCategory.setCategory(category);
        specCategory.setSpecification(spec);
        specCategory.setRequired(true);

        specCategory = catSpecRepository.save(specCategory);

        given().spec(specification)
                .pathParam("id", category.getId())
                .get("/{id}/specifications")
                .then()
                .statusCode(200)
                .body("[0].categorySpecificationId", notNullValue())
                .body("[0].specificationName", equalTo(spec.getName()))
                .body("[0].isRequired", equalTo(specCategory.getRequired()));
    }

    @Test
    void shouldSaveCategorySpecificationWhenSavingWithValidData() throws JsonProcessingException {
        Category category = createCategory();
        Specification spec = createSpecification();

        CategorySpecificationRequestDTO catSpecRequest =
                new CategorySpecificationRequestDTO(category.getId(), spec.getId(), true);

        var response = given().spec(specification)
                .contentType("application/json")
                .body(catSpecRequest)
                .post("/specifications")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString();

        CategorySpecificationDTO actual = objectMapper.readValue(response, CategorySpecificationDTO.class);

        assertNotNull(actual.getId());
        assertEquals(category.getId(), actual.getCategoryId());
        assertEquals(spec.getId(), actual.getSpecificationId());
        assertEquals(catSpecRequest.required(), actual.getRequired());
    }

    @Test
    void shouldNotSaveWhenSavingCategorySpecificationWithInvalidCategoryId() {
        Specification spec = createSpecification();

        CategorySpecificationRequestDTO catSpecRequest =
                new CategorySpecificationRequestDTO(invalidId, spec.getId(), true);

        CustomError actual = given().spec(specification)
                .contentType("application/json")
                .body(catSpecRequest)
                .post("/specifications")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(actual.getTimestamp());
        assertEquals(404, actual.getStatus());
        assertEquals(PATH + "/specifications", actual.getPath());
        assertEquals(notFoundMsg, actual.getMessage());
    }

    @Test
    void shouldNotSaveWhenSavingCategorySpecificationWithInvalidSpecificationId() {
        String errorMsg = "Specification not found!";
        Category category = createCategory();

        CategorySpecificationRequestDTO catSpecRequest =
                new CategorySpecificationRequestDTO(category.getId(), invalidId, true);

        CustomError actual = given().spec(specification)
                .contentType("application/json")
                .body(catSpecRequest)
                .post("/specifications")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(actual.getTimestamp());
        assertEquals(404, actual.getStatus());
        assertEquals(PATH + "/specifications", actual.getPath());
        assertEquals(errorMsg, actual.getMessage());
    }

    @Test
    void shouldNotSaveWhenSavingCategorySpecificationWithNullCategoryId() throws JsonProcessingException {
        String errorMsg = "Category ID is required!";

        CategorySpecificationRequestDTO catSpecRequest =
                new CategorySpecificationRequestDTO(null, 1L, true);

        var response = given().spec(specification)
                .contentType("application/json")
                .body(catSpecRequest)
                .post("/specifications")
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
        assertEquals(PATH + "/specifications", error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals(errorMsg, errors.get("categoryId"));
    }

    @Test
    void shouldNotSaveWhenSavingCategorySpecificationWithNullSpecificationId() throws JsonProcessingException {
        String errorMsg = "Specification ID is required!";

        CategorySpecificationRequestDTO catSpecRequest =
                new CategorySpecificationRequestDTO(1L, null, true);

        var response = given().spec(specification)
                .contentType("application/json")
                .body(catSpecRequest)
                .post("/specifications")
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
        assertEquals(PATH + "/specifications", error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals(errorMsg, errors.get("specificationId"));
    }

    @Test
    void shouldNotSaveWhenSavingCategorySpecificationWithNullRequiredId() throws JsonProcessingException {
        String errorMsg = "Required field is required!";

        CategorySpecificationRequestDTO catSpecRequest =
                new CategorySpecificationRequestDTO(1L, 1L, null);

        var response = given().spec(specification)
                .contentType("application/json")
                .body(catSpecRequest)
                .post("/specifications")
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
        assertEquals(PATH + "/specifications", error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals(errorMsg, errors.get("required"));
    }

    @Test
    void shouldUpdateWhenUpdatingCategorySpecificationWithValidData() throws JsonProcessingException {
        Category category = createCategory();
        Specification spec = createSpecification();

        CategorySpecification catSpec = new CategorySpecification();
        catSpec.setRequired(true);
        catSpec.setSpecification(spec);
        catSpec.setCategory(category);

        catSpec = catSpecRepository.save(catSpec);

        CategorySpecificationRequestDTO updateRequest =
                new CategorySpecificationRequestDTO(category.getId(), spec.getId(), false);

        var response = given()
                .spec(specification)
                .pathParam("id", catSpec.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("specifications/{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        CategorySpecificationDTO actual = objectMapper.readValue(response, CategorySpecificationDTO.class);

        assertNotNull(actual.getId());
        assertEquals(category.getId(), actual.getCategoryId());
        assertEquals(spec.getId(), actual.getSpecificationId());
        assertEquals(updateRequest.required(), actual.getRequired());
    }

    @Test
    void shouldNotUpdateWhenUpdatingCategorySpecificationWithInvalidCategorySpecificationId() {
        String errorMsg = "Category Specification not found!";
        CategorySpecificationRequestDTO updateRequest =
                new CategorySpecificationRequestDTO(1L, 1L, true);

        CustomError response = given()
                .spec(specification)
                .pathParam("id", invalidId)
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("specifications/{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(response.getTimestamp());
        assertEquals(404, response.getStatus());
        assertEquals(PATH + "/specifications/" + invalidId, response.getPath());
        assertEquals(errorMsg, response.getMessage());
    }

    @Test
    void shouldNotUpdateWhenUpdatingCategorySpecificationWithInvalidCategoryId() {
        Category category = createCategory();
        Specification spec = createSpecification();

        CategorySpecification catSpec = new CategorySpecification();
        catSpec.setRequired(true);
        catSpec.setSpecification(spec);
        catSpec.setCategory(category);

        catSpec = catSpecRepository.save(catSpec);

        CategorySpecificationRequestDTO updateRequest =
                new CategorySpecificationRequestDTO(invalidId, 1L, true);

        CustomError response = given()
                .spec(specification)
                .pathParam("id", catSpec.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("specifications/{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(response.getTimestamp());
        assertEquals(404, response.getStatus());
        assertEquals(PATH + "/specifications/" + catSpec.getId(), response.getPath());
        assertEquals(notFoundMsg, response.getMessage());
    }

    @Test
    void shouldUpdateWhenUpdatingCategorySpecificationWithInvalidSpecificationId() {
        String errorMsg = "Specification not found!";

        Category category = createCategory();
        Specification spec = createSpecification();

        CategorySpecification catSpec = new CategorySpecification();
        catSpec.setRequired(true);
        catSpec.setSpecification(spec);
        catSpec.setCategory(category);

        catSpec = catSpecRepository.save(catSpec);

        CategorySpecificationRequestDTO updateRequest =
                new CategorySpecificationRequestDTO(category.getId(), invalidId, true);

        CustomError response = given()
                .spec(specification)
                .pathParam("id", catSpec.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("specifications/{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(response.getTimestamp());
        assertEquals(404, response.getStatus());
        assertEquals(PATH + "/specifications/" + catSpec.getId(), response.getPath());
        assertEquals(errorMsg, response.getMessage());
    }

    @Test
    void shouldUpdateWhenUpdatingCategorySpecificationWithNullCategoryId() throws JsonProcessingException {
        String errorMsg = "Category ID is required!";

        Category category = createCategory();
        Specification spec = createSpecification();

        CategorySpecification catSpec = new CategorySpecification();
        catSpec.setRequired(true);
        catSpec.setSpecification(spec);
        catSpec.setCategory(category);

        catSpec = catSpecRepository.save(catSpec);

        CategorySpecificationRequestDTO updateRequest =
                new CategorySpecificationRequestDTO(null, spec.getId(), true);

        var response = given()
                .spec(specification)
                .pathParam("id", catSpec.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("specifications/{id}")
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
        assertEquals(PATH + "/specifications/" + catSpec.getId(), error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals(errorMsg, errors.get("categoryId"));
    }

    @Test
    void shouldUpdateWhenUpdatingCategorySpecificationWithNullSpecificationId() throws JsonProcessingException {
        String errorMsg = "Specification ID is required!";

        Category category = createCategory();
        Specification spec = createSpecification();

        CategorySpecification catSpec = new CategorySpecification();
        catSpec.setRequired(true);
        catSpec.setSpecification(spec);
        catSpec.setCategory(category);

        catSpec = catSpecRepository.save(catSpec);

        CategorySpecificationRequestDTO updateRequest =
                new CategorySpecificationRequestDTO(category.getId(), null, true);

        var response = given()
                .spec(specification)
                .pathParam("id", catSpec.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("specifications/{id}")
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
        assertEquals(PATH + "/specifications/" + catSpec.getId(), error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals(errorMsg, errors.get("specificationId"));
    }

    @Test
    void shouldUpdateWhenUpdatingCategorySpecificationWithNullRequiredField() throws JsonProcessingException {
        String errorMsg = "Required field is required!";

        Category category = createCategory();
        Specification spec = createSpecification();

        CategorySpecification catSpec = new CategorySpecification();
        catSpec.setRequired(true);
        catSpec.setSpecification(spec);
        catSpec.setCategory(category);

        catSpec = catSpecRepository.save(catSpec);

        CategorySpecificationRequestDTO updateRequest =
                new CategorySpecificationRequestDTO(category.getId(), spec.getId(), null);

        var response = given()
                .spec(specification)
                .pathParam("id", catSpec.getId())
                .contentType(CONTENT_TYPE)
                .body(updateRequest)
                .put("specifications/{id}")
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
        assertEquals(PATH + "/specifications/" + catSpec.getId(), error.getPath());
        assertEquals(validationErrorMsg, error.getMessage());

        assertEquals(errorMsg, errors.get("required"));
    }

    @Test
    void shouldDeleteCategorySpecificationWhenDeletingWithValidId() {
        Category category = createCategory();
        Specification spec = createSpecification();

        CategorySpecification catSpec = new CategorySpecification();
        catSpec.setRequired(true);
        catSpec.setSpecification(spec);
        catSpec.setCategory(category);

        catSpec = catSpecRepository.save(catSpec);

        given()
                .spec(specification)
                .pathParam("id", catSpec.getId())
                .delete("specifications/{id}")
                .then()
                .statusCode(204);
    }

    @Test
    void shouldNotDeleteCategorySpecificationWhenDeletingWithInvalidId() {
        String errorMsg = "Category Specification not found!";

        CustomError response = given()
                .spec(specification)
                .pathParam("id", invalidId)
                .delete("specifications/{id}")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(CustomError.class);

        assertNotNull(response.getTimestamp());
        assertEquals(errorMsg, response.getMessage());
        assertEquals(404, response.getStatus());
        assertEquals(PATH + "/specifications/" + invalidId, response.getPath());
    }

    private Category createCategory() {
        Category category = new Category();
        category.setName("Monitor");

        return repository.save(category);
    }

    private Specification createSpecification() {
        Specification spec = SpecificationBuilder.builder().withId(null).withUnit(null).build();
        return specificationRepository.save(spec);
    }

    private CategoryDTO createCategoryDTO(CategoryRequestDTO request) throws JsonProcessingException {
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
