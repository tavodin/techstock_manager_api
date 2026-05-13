package io.github.tavodin.techstock_manager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.config.security.filters.JwtAuthenticationFilter;
import io.github.tavodin.techstock_manager.dto.SpecificationDTO;
import io.github.tavodin.techstock_manager.dto.SpecificationRequestDTO;
import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.services.SpecificationService;
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

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpecificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SpecificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpecificationService service;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    private Specification specification;
    private SpecificationDTO specificationDTO;
    private SpecificationRequestDTO request;

    private String path;
    private String notFoundMsg;
    private String unitNotFoundMsg;
    private String validationMsg;
    private Long validId;
    private Long invalidId;

    @BeforeEach
    void setUp() {
        specification = SpecificationBuilder.builder().build();
        specificationDTO = new SpecificationDTO(specification);

        request = new SpecificationRequestDTO(
                specificationDTO.getName(),
                specificationDTO.getDataType(),
                specificationDTO.getFilterable(),
                1L
        );

        path = "/specifications";
        notFoundMsg = "Specification not found!";
        unitNotFoundMsg = "Unit not found!";
        validationMsg = "Entity validation error";
        validId = 1L;
        invalidId = 2L;
    }

    @Test
    void shouldReturnSpecificationDTOAndOkWhenFindingWithExistId() throws Exception {
        when(service.findById(validId)).thenReturn(specificationDTO);

        mockMvc.perform(get(path + "/{id}", validId))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(validId))
                .andExpect(jsonPath("$.name").value(specificationDTO.getName()))
                .andExpect(jsonPath("$.dataType").value(specificationDTO.getDataType().name()))
                .andExpect(jsonPath("$.filterable").value(specification.getFilterable()));

        verify(service).findById(validId);
    }

    @Test
    void shouldReturnCustomErrorObjectAndNotFoundWhenFindingWithInvalidId() throws Exception {
        when(service.findById(invalidId)).thenThrow(new ResourceNotFoundException(notFoundMsg));

        mockMvc.perform(get(path + "/{id}", invalidId))

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(notFoundMsg))
                .andExpect(jsonPath("$.path").value(path + "/" + invalidId));
    }

    @Test
    void shouldReturnSpecificationPageAndOkWhenFindAll() throws Exception {
        List<SpecificationDTO> specifications = List.of(specificationDTO);

        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(1, 0, 1);
        PagedModel<SpecificationDTO> pagedModel = PagedModel.of(specifications, metadata);

        when(service.findAll(any(Pageable.class))).thenReturn(pagedModel);

        mockMvc.perform(get(path)
                .param("page", "0")
                .param("size", "10"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._embedded.specifications[0].id")
                        .value(specificationDTO.getId()))
                .andExpect(jsonPath("$._embedded.specifications[0].name")
                        .value(specificationDTO.getName()))
                .andExpect(jsonPath("$._embedded.specifications[0].dataType")
                        .value(specificationDTO.getDataType().name()))
                .andExpect(jsonPath("$._embedded.specifications[0].filterable")
                        .value(specificationDTO.getFilterable()))
                .andExpect(jsonPath("$._embedded.specifications[0].createdAt").exists())
                .andExpect(jsonPath("$._embedded.specifications[0].updatedAt").exists());
    }

    @Test
    void shouldReturnSpecificationDTOAndCreatedWhenSavingWithValidData() throws Exception {
        when(service.save(any(SpecificationRequestDTO.class))).thenReturn(specificationDTO);

        mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.id").value(specificationDTO.getId()))
                .andExpect(jsonPath("$.name").value(specificationDTO.getName()))
                .andExpect(jsonPath("$.dataType").value(specificationDTO.getDataType().name()))
                .andExpect(jsonPath("$.filterable").value(specificationDTO.getFilterable()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())

                .andExpect(header().string("Location",
                        containsString("http://localhost" + path + "/" + specificationDTO.getId())));
    }

    @Test
    void shouldReturnCustomErrorObjectAndBadRequestWhenSavingWithNullName() throws Exception {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                null, specification.getDataType(), specification.getFilterable(), 1L);

        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(path))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Name is required!")));
    }

    @Test
    void shouldReturnCustomErrorObjectAndBadRequestWhenSavingWithLongName() throws Exception {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                "e".repeat(101), specification.getDataType(), specification.getFilterable(), 1L);

        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(path))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("The name must contain a maximum of 100 characters.")));
    }

    @Test
    void shouldReturnCustomErrorObjectAndBadRequestWhenSavingWithInvalidDataType() throws Exception {
        String invalidRequest = """
                {
                    "name": "Test",
                    "dataType": "INVALID",
                    "filterable": true,
                    "unitId": 1
                }
                """;

        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(path))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("dataType")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Invalid value. Allowed values: STRING, NUMBER, BOOLEAN")));
    }

    @Test
    void shouldReturnCustomErrorObjectAndBadRequestWhenSavingWithNullDataType() throws Exception {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                specification.getName(), null, specification.getFilterable(), 1L);

        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(path))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("dataType")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Data Type is required!")));
    }

    @Test
    void shouldReturnCustomErrorObjectAndBadRequestWhenSavingWithNullFilterable() throws Exception {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                specification.getName(), specification.getDataType(), null, 1L);

        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(path))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("filterable")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Filterable is required!")));
    }

    @Test
    void shouldReturnCustomErrorObjectAndNotFoundWhenSavingWithInvalidUnitId() throws Exception {
        when(service.save(any(SpecificationRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(unitNotFoundMsg));

        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(unitNotFoundMsg))
                .andExpect(jsonPath("$.path").value(path));
    }

    @Test
    void shouldReturnCustomErrorObjectAndBadRequestWhenSavingWithNullUnitId() throws Exception {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                specification.getName(), specification.getDataType(), specification.getFilterable(), null);

        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(path))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("unitId")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Unit ID is required!")));
    }

    @Test
    void shouldReturnSpecificationDTOAndOkWhenUpdate() throws Exception {
        when(service.update(validId, request)).thenReturn(specificationDTO);

        mockMvc.perform(put(path + "/{id}", validId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(validId))
                .andExpect(jsonPath("$.name").value(specificationDTO.getName()))
                .andExpect(jsonPath("$.dataType").value(specificationDTO.getDataType().name()))
                .andExpect(jsonPath("$.filterable").value(specificationDTO.getFilterable()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturnCustomErrorObjectAndNotFoundWhenIdDoesNotExistInUpdate() throws Exception {
        when(service.update(eq(invalidId), any(SpecificationRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(notFoundMsg));

        mockMvc.perform(put(path + "/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(notFoundMsg))
                .andExpect(jsonPath("$.path").value(path + "/" + invalidId));
    }

    @Test
    void shouldReturnCustomErrorObjectAndBadRequestWhenUpdatingWithNullName() throws Exception {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                null, specification.getDataType(), specification.getFilterable(), 1L);

        mockMvc.perform(put(path + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(path + "/" + validId))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Name is required!")));
    }

    @Test
    void shouldReturnCustomErrorObjectAndBadRequestWhenUpdatingWithLongName() throws Exception {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                "e".repeat(101), specification.getDataType(), specification.getFilterable(), 1L);

        mockMvc.perform(put(path + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(path + "/" + validId))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("The name must contain a maximum of 100 characters.")));
    }

    @Test
    void shouldReturnCustomErrorObjectAndBadRequestWhenUpdatingWithInvalidDataType() throws Exception {
        String invalidRequest = """
                {
                    "name": "Test",
                    "dataType": "INVALID",
                    "filterable": true,
                    "unitId": 1
                }
                """;

        mockMvc.perform(put(path + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(path + "/" + validId))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("dataType")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Invalid value. Allowed values: STRING, NUMBER, BOOLEAN")));
    }

    @Test
    void shouldReturnCustomErrorObjectAndBadRequestWhenUpdatingWithNullDataType() throws Exception {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                specification.getName(), null, specification.getFilterable(), 1L);

        mockMvc.perform(put(path + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(path + "/" + validId))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("dataType")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Data Type is required!")));
    }

    @Test
    void shouldReturnCustomErrorObjectAndBadRequestWhenUpdatingWithNullFilterable() throws Exception {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                specification.getName(), specification.getDataType(), null, 1L);

        mockMvc.perform(put(path + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(path + "/" + validId))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("filterable")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Filterable is required!")));
    }

    @Test
    void shouldReturnCustomErrorObjectAndNotFoundWhenUpdatingWithInvalidUnitId() throws Exception {
        when(service.update(anyLong(), any(SpecificationRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(unitNotFoundMsg));

        mockMvc.perform(put(path + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(unitNotFoundMsg))
                .andExpect(jsonPath("$.path").value(path + "/" + validId));
    }

    @Test
    void shouldReturnCustomErrorObjectAndBadRequestWhenUpdatingWithNullUnitId() throws Exception {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                specification.getName(), specification.getDataType(), specification.getFilterable(), null);

        mockMvc.perform(put(path + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(path + "/" + validId))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("unitId")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Unit ID is required!")));
    }

    @Test
    void shouldReturnNoContentWhenDeletingWithValidId() throws Exception {
        doNothing().when(service).delete(validId);

        mockMvc.perform(delete(path + "/{id}", validId))
                .andExpect(status().isNoContent());

        verify(service).delete(validId);
    }

    @Test
    void shouldReturnCustomErrorObjectAndNotFoundWhenDeletingWithInvalidId() throws Exception {
        doThrow(new ResourceNotFoundException(notFoundMsg)).when(service).delete(invalidId);

        mockMvc.perform(delete(path + "/{id}", invalidId))
                .andExpect(status().isNotFound());

        verify(service).delete(invalidId);
    }

    @Test
    void shouldReturnCustomErrorObjectAndConflictWhenDeletingWithAssociatedEntity() throws Exception {
        doThrow(new EntityInUseException("Unit is in use and cannot be deleted"))
                .when(service).delete(anyLong());

        mockMvc.perform(delete(path + "/{id}", invalidId))
                .andExpect(status().isConflict());

        verify(service).delete(invalidId);
    }
}
