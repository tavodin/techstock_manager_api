package io.github.tavodin.techstock_manager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.config.security.filters.JwtAuthenticationFilter;
import io.github.tavodin.techstock_manager.dto.CategoryDTO;
import io.github.tavodin.techstock_manager.dto.CategoryRequestDTO;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationsListDTO;
import io.github.tavodin.techstock_manager.entities.Category;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.services.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    private Long validId = 1L;
    private Long invalidId = 2L;
    private static final String PATH = "/categories";
    private String notFoundMsg = "Category not found!";
    private String entityInUseMsg = "Category is in use and cannot be deleted";
    private String validationMsg = "Entity validation error";
    private LocalDateTime createdAt = LocalDateTime.of(2026, 3, 30, 10, 0);
    private LocalDateTime updatedAt = createdAt.plusHours(1L);

    private Category category = new Category();
    private CategoryDTO categoryDTO;
    private CategoryRequestDTO request;


    @BeforeEach
    void setUp() {
        category.setId(validId);
        category.setName("Monitor");
        category.setCreatedAt(createdAt);
        category.setUpdatedAt(updatedAt);

        categoryDTO = new CategoryDTO(category);
        request = new CategoryRequestDTO(category.getName());
    }

    @Test
    void shouldReturnCategoryDTOAndOkWhenFindingWithValidId() throws Exception {
        when(service.findById(validId)).thenReturn(categoryDTO);

        mockMvc.perform(get(PATH + "/{id}", validId))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(validId))
                .andExpect(jsonPath("$.name").value(categoryDTO.getName()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenFindingWithInvalidId() throws Exception {
        when(service.findById(invalidId)).thenThrow(new ResourceNotFoundException(notFoundMsg));

        mockMvc.perform(get(PATH + "/{id}", invalidId))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(notFoundMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + invalidId));
    }

    @Test
    void shouldReturnCategoryPageAndOkWhenFindAll() throws Exception {
        List<CategoryDTO> categories = List.of(categoryDTO);
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(1, 0, 1);
        PagedModel<CategoryDTO> pagedModel = PagedModel.of(categories, metadata);

        when(service.findAll(any(Pageable.class))).thenReturn(pagedModel);

        mockMvc.perform(get(PATH)
                        .param("page", "0")
                        .param("size", "10"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._embedded.categories[0].id")
                        .value(categoryDTO.getId()))
                .andExpect(jsonPath("$._embedded.categories[0].name")
                        .value(categoryDTO.getName()))
                .andExpect(jsonPath("$._embedded.categories[0].createdAt").exists())
                .andExpect(jsonPath("$._embedded.categories[0].updatedAt").exists());
    }

    @Test
    void shouldReturnSpecificationsWhenFindingSpecificationsByValidCategoryId() throws Exception {
        CategorySpecificationsListDTO dto = new CategorySpecificationsListDTO(1L, "Frequência", true);

        when(service.findAllSpecificationByCategoryId(validId)).thenReturn(List.of(dto));

        mockMvc.perform(get(PATH + "/{id}/specifications", validId))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.[0].id").value(dto.getCategorySpecificationId()))
                .andExpect(jsonPath("$.[0].name").value(dto.getSpecificationName()))
                .andExpect(jsonPath("$.[0].isRequired").value(dto.getIsRequired()));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenFindingSpecificationsWithInvalidCategoryId() throws Exception {
        when(service.findAllSpecificationByCategoryId(invalidId)).thenThrow(new ResourceNotFoundException(notFoundMsg));

        mockMvc.perform(get(PATH + "/{id}/specifications", invalidId))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(notFoundMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + invalidId + "/specifications"));
    }

    @Test
    void shouldReturnCategoryDTOAndCreatedWhenSavingWithValidData() throws Exception {
        when(service.save(request)).thenReturn(categoryDTO);

        mockMvc.perform(post(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.id").value(validId))
                .andExpect(jsonPath("$.name").value(categoryDTO.getName()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())

                .andExpect(header().string("Location",
                        containsString("http://localhost" + PATH + "/" + categoryDTO.getId())));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingNullName() throws Exception {
        CategoryRequestDTO invalidRequest = new CategoryRequestDTO(null);
        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Name is required!")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithLongName() throws Exception {
        CategoryRequestDTO invalidRequest = new CategoryRequestDTO("e".repeat(101));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("The name must contain a maximum of 100 characters.")));
    }

    @Test
    void shouldReturnCategoryDTOAndOkWhenUpdatingWithValidId() throws Exception {
        when(service.update(validId, request)).thenReturn(categoryDTO);

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(validId))
                .andExpect(jsonPath("$.name").value(categoryDTO.getName()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenUpdatingWithInvalidId() throws Exception {
        when(service.update(eq(invalidId), any(CategoryRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(notFoundMsg));

        mockMvc.perform(put(PATH + "/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(notFoundMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + invalidId));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithNullName() throws Exception {
        CategoryRequestDTO invalidRequest = new CategoryRequestDTO(null);
        mockMvc.perform(put(PATH + "/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + invalidId))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Name is required!")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithLongName() throws Exception {
        CategoryRequestDTO invalidRequest = new CategoryRequestDTO("e".repeat(101));

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + validId))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("The name must contain a maximum of 100 characters.")));
    }

    @Test
    void shouldDoNothingWhenDeletingWithValidId() throws Exception {
        doNothing().when(service).delete(validId);

        mockMvc.perform(delete(PATH + "/{id}", validId))

                .andExpect(status().isNoContent());

        verify(service).delete(validId);
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenDeletingWithInvalidId() throws Exception {
        doThrow(new ResourceNotFoundException(notFoundMsg)).when(service).delete(invalidId);

        mockMvc.perform(delete(PATH + "/{id}", invalidId))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(notFoundMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + invalidId));
    }

    @Test
    void shouldThrownEntityInUseAndConflictWhenDeletingEntityWithRelationships() throws Exception {
        doThrow(new EntityInUseException("Category is in use and cannot be deleted"))
                .when(service).delete(invalidId);

        mockMvc.perform(delete(PATH + "/{id}", invalidId))

                .andExpect(status().isConflict())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Category is in use and cannot be deleted"))
                .andExpect(jsonPath("$.path").value(PATH + "/" + invalidId));
    }
}
