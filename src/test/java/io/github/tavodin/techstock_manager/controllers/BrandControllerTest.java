package io.github.tavodin.techstock_manager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.config.security.filters.JwtAuthenticationFilter;
import io.github.tavodin.techstock_manager.dto.BrandDTO;
import io.github.tavodin.techstock_manager.dto.BrandRequestDTO;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.services.BrandService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BrandController.class)
@AutoConfigureMockMvc(addFilters = false)
class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BrandService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private final String PATH = "/brands";
    private String notFoundMsg = "Brand not found";
    private String validationMsg = "Entity validation error";
    private String entityInUseMsg = "Brand is in use and cannot be deleted";
    private Long validId = 1L;
    private Long invalidId = 2L;

    private BrandDTO dto;
    private BrandRequestDTO request;

    @BeforeEach
    void setUp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 05, 25, 17, 30);
        LocalDateTime updatedAt = createdAt.plusHours(1);

        dto = new BrandDTO(validId, "DELL");
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);

        request = new BrandRequestDTO(dto.getName());
    }

    @Test
    void shouldReturnBrandDTOAndOkWhenFindingWithValidId() throws Exception {
        when(service.findById(validId)).thenReturn(dto);

        mockMvc.perform(get(PATH + "/{id}", validId))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(validId))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenFindingWithInvalidId() throws Exception {
        when(service.findById(invalidId)).thenThrow(new ResourceNotFoundException(notFoundMsg));

        mockMvc.perform(get(PATH + "/{id}", invalidId))
                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value(PATH + "/" + invalidId))
                .andExpect(jsonPath("$.message").value(notFoundMsg))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnBrandPageAndOkWhenFindAll() throws Exception {
        List<BrandDTO> brands = List.of(dto);
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(1, 0, 1);
        PagedModel<BrandDTO> pagedModel = PagedModel.of(brands, metadata);

        when(service.findAll(any(Pageable.class))).thenReturn(pagedModel);

        mockMvc.perform(get(PATH)
                        .param("page", "0")
                        .param("size", "10"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._embedded.brands[0].id")
                        .value(dto.getId()))
                .andExpect(jsonPath("$._embedded.brands[0].name")
                        .value(dto.getName()))
                .andExpect(jsonPath("$._embedded.brands[0].createdAt").exists())
                .andExpect(jsonPath("$._embedded.brands[0].updatedAt").exists());
    }

    @Test
    void shouldReturnBrandDTOAndCreatedWhenSavingWithValidData() throws Exception {
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.id").value(validId))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())

                .andExpect(header().string("Location",
                        containsString("http://localhost" + PATH + "/" + dto.getId())));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingNullName() throws Exception {
        BrandRequestDTO invalidRequest = new BrandRequestDTO(null);

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
                        .value(hasItem("Name is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithLongName() throws Exception {
        BrandRequestDTO invalidRequest = new BrandRequestDTO("e".repeat(101));

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
                        .value(hasItem("Name must contain between 2 and 100 characters")));
    }

    @Test
    void shouldReturnBrandDTOAndOkWhenUpdatingWithValidId() throws Exception {
        when(service.update(validId, request)).thenReturn(dto);

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(validId))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenUpdatingWithInvalidId() throws Exception {
        when(service.update(eq(invalidId), any(BrandRequestDTO.class)))
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
        BrandRequestDTO invalidRequest = new BrandRequestDTO(null);

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
                        .value(hasItem("Name is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithLongName() throws Exception {
        BrandRequestDTO invalidRequest = new BrandRequestDTO("e".repeat(101));

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
                        .value(hasItem("Name must contain between 2 and 100 characters")));
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
        doThrow(new EntityInUseException(entityInUseMsg))
                .when(service).delete(invalidId);

        mockMvc.perform(delete(PATH + "/{id}", invalidId))

                .andExpect(status().isConflict())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(entityInUseMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + invalidId));
    }
}