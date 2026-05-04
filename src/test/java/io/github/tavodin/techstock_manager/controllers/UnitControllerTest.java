package io.github.tavodin.techstock_manager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.config.security.filters.JwtAuthenticationFilter;
import io.github.tavodin.techstock_manager.dto.UnitDTO;
import io.github.tavodin.techstock_manager.dto.UnitRequestDTO;
import io.github.tavodin.techstock_manager.entities.Unit;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.services.UnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.hateoas.Link;
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

@WebMvcTest(UnitController.class)
@AutoConfigureMockMvc(addFilters = false)
class UnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UnitService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    Long existId;
    Long nonExistId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String notFoundMessage;

    Unit unit;
    UnitDTO unitDTO;
    UnitRequestDTO request;

    @BeforeEach
    void setUp() {
        existId = 1L;
        nonExistId = 10L;
        notFoundMessage = "Unit not found!";
        createdAt = LocalDateTime.of(2026, 3, 30, 10, 0);
        updatedAt = createdAt.plusHours(1L);

        unit = new Unit(
                existId,
                createdAt,
                updatedAt,
                "Hertz",
                "Hz");

        unitDTO = new UnitDTO(
                unit.getId(),
                unit.getName(),
                unit.getSymbol(),
                unit.getCreatedAt(),
                unit.getUpdatedAt());
        unitDTO.add(Link.of("http://localhost:units/" + unitDTO.getId()).withSelfRel());

        request = new UnitRequestDTO("Gigahertz", "GHz");
    }

    @Test
    void shouldReturnUnitDTOWhenIdExist() throws Exception {
        when(service.findById(existId)).thenReturn(unitDTO);

        mockMvc.perform(get("/units/{id}", existId)
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(existId))
                .andExpect(jsonPath("$.name").value("Hertz"))
                .andExpect(jsonPath("$.symbol").value("Hz"))
                .andExpect(jsonPath("$._links").exists());

        verify(service).findById(existId);
    }

    @Test
    void shouldReturnNotFoundWhenIdDoesNotExistInFindById() throws Exception {
        when(service.findById(nonExistId))
                .thenThrow(new ResourceNotFoundException(notFoundMessage));

        mockMvc.perform(get("/units/{id}", nonExistId))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnErrorObjectWhenIdDoesNotExist() throws Exception {
        when(service.findById(nonExistId)).thenThrow(new ResourceNotFoundException(notFoundMessage));

        mockMvc.perform(get("/units/{id}", nonExistId))

                .andExpect(jsonPath(".timestamp").exists())
                .andExpect(jsonPath(".status").value(404))
                .andExpect(jsonPath(".message").value(notFoundMessage))
                .andExpect(jsonPath(".path").value("/units/" + nonExistId));
    }

    @Test
    void shouldReturnPageUnits() throws Exception {
        List<UnitDTO> units = List.of(unitDTO);

        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(1, 0, 1);
        PagedModel<UnitDTO> pagedModel = PagedModel.of(units, metadata);

        when(service.findAll(any(Pageable.class))).thenReturn(pagedModel);

        mockMvc.perform(get("/units")
                .param("page", "0")
                .param("size", "10"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$.page").exists())

                .andExpect(jsonPath("$._embedded.units[0].id").value(unitDTO.getId()))
                .andExpect(jsonPath("$._embedded.units[0].name").value(unitDTO.getName()))
                .andExpect(jsonPath("$._embedded.units[0].symbol").value(unitDTO.getSymbol()))

                .andExpect(jsonPath("$._embedded.units[0].createdAt").exists())
                .andExpect(jsonPath("$._embedded.units[0].updatedAt").exists())
                .andReturn();
    }

    @Test
    void shouldCreateUnitAndReturnCreatedStatusWhenDataIsValid() throws Exception {
        when(service.save(request)).thenReturn(unitDTO);

        mockMvc.perform(post("/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.id").value(unitDTO.getId()))
                .andExpect(jsonPath("$.name").value(unitDTO.getName()))
                .andExpect(jsonPath("$.symbol").value(unitDTO.getSymbol()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())

                .andExpect(header().string("Location",
                        containsString("http://localhost/units/" + unitDTO.getId())))

                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void shouldReturnBadRequestWhenInvalidData() throws Exception {
        UnitRequestDTO invalidRequest = new UnitRequestDTO(null, null);

        mockMvc.perform(post("/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnValidationErrorWhenInvalidData() throws Exception {
        UnitRequestDTO invalidRequest = new UnitRequestDTO("a".repeat(46), null);
        mockMvc.perform(post("/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value("/units"))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("The unit name must contain 45 characters.")))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("symbol")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Unit Symbol is required!")));
    }

    @Test
    void shouldSaveUnitWhenNameIsNull() throws Exception {
        unitDTO.setName(null);
        when(service.save(any(UnitRequestDTO.class))).thenReturn(unitDTO);

        mockMvc.perform(post("/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnBadRequestWhenNameIsTooLongInSave() throws Exception {
        UnitRequestDTO invalidRequest = new UnitRequestDTO("a".repeat(46), "Hz");

        mockMvc.perform(post("/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSymbolIsNullInSave() throws Exception {
        UnitRequestDTO invalidRequest = new UnitRequestDTO("Hertz", null);

        mockMvc.perform(post("/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSymbolIsTooLongInSave() throws Exception {
        UnitRequestDTO invalidRequest = new UnitRequestDTO("Hertz", "12345678901");

        mockMvc.perform(post("/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateUnitUnitWhenDataIsValid() throws Exception{
        unitDTO.setName("Gigahertz");
        unitDTO.setSymbol("GHz");

        when(service.update(eq(existId), any(UnitRequestDTO.class))).thenReturn(unitDTO);

        mockMvc.perform(put("/units/{id}", existId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(unitDTO.getId()))
                .andExpect(jsonPath("$.name").value(unitDTO.getName()))
                .andExpect(jsonPath("$.symbol").value(unitDTO.getSymbol()))

                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())

                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void shouldReturnNotFoundWhenIdDoesNotExistInUpdate() throws Exception {
        when(service.update(eq(nonExistId), any(UnitRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(notFoundMessage));

        mockMvc.perform(put("/units/{id}", nonExistId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateUnitWhenNameIsNull() throws Exception {
        unitDTO.setName(null);
        unitDTO.setSymbol("GHz");

        when(service.update(eq(existId), any(UnitRequestDTO.class))).thenReturn(unitDTO);

        mockMvc.perform(put("/units/{id}", existId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestWhenNameIsTooLongInUpdate() throws Exception {
        UnitRequestDTO invalidRequest = new UnitRequestDTO("e".repeat(46), "GHz");

        mockMvc.perform(put("/units/{id}", existId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSymbolIsNullInUpdate() throws Exception {
        UnitRequestDTO invalidRequest = new UnitRequestDTO("Gigahertz", null);

        mockMvc.perform(put("/units/{id}", existId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSymbolIsTooLongInUpdate() throws Exception {
        UnitRequestDTO invalidRequest = new UnitRequestDTO("Gigahertz", "e".repeat(11));

        mockMvc.perform(put("/units/{id}", existId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteUnitWhenIdExist() throws Exception {
        mockMvc.perform(delete("/units/{id}", existId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).delete(existId);
    }

    @Test
    void shouldReturnNotFoundWhenIdDoesNotExistInDelete() throws Exception {
        doThrow(new ResourceNotFoundException(notFoundMessage))
                .when(service).delete(nonExistId);

        mockMvc.perform(delete("/units/{id}", nonExistId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenUnitIsInUse() throws Exception {
        doThrow(new EntityInUseException("Unit is in use and cannot be deleted"))
                .when(service).delete(3L);

        mockMvc.perform(delete("/units/{id}", 3L))
                .andExpect(status().isConflict());
    }
}
