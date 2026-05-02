package io.github.tavodin.techstock_manager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.config.security.filters.JwtAuthenticationFilter;
import io.github.tavodin.techstock_manager.dto.SpecificationDTO;
import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.services.SpecificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
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

    private String path;
    private String notFoundMsg;
    private Long validId;
    private Long invalidId;

    @BeforeEach
    void setUp() {
        specification = SpecificationBuilder.builder().build();
        specificationDTO = new SpecificationDTO(specification);

        path = "/specifications";
        validId = 1L;
        invalidId = 2L;
    }

    @Test
    void shouldReturnSpecificationDTOAndOkWhenIdExist() throws Exception {
        when(service.findById(validId)).thenReturn(specificationDTO);

        mockMvc.perform(get(path + "/{id}", validId))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(validId))
                .andExpect(jsonPath("$.name").value(specificationDTO.getName()))
                .andExpect(jsonPath("$.dataType").value(specificationDTO.getDataType()))
                .andExpect(jsonPath("$.filterable").value(specification.getFilterable()));

        verify(service).findById(validId);
    }

    @Test
    void shouldReturnErrorObjectAndNotFoundWhenIdDoesNotExist() throws Exception {
        when(service.findById(invalidId)).thenThrow(new ResourceNotFoundException(notFoundMsg));

        mockMvc.perform(get(path + "/{id}", invalidId))

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(notFoundMsg))
                .andExpect(jsonPath("$.path").value(path + "/" + invalidId));
    }
}
