package io.github.tavodin.techstock_manager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.config.security.filters.JwtAuthenticationFilter;
import io.github.tavodin.techstock_manager.dto.SupplierDTO;
import io.github.tavodin.techstock_manager.dto.SupplierRequestDTO;
import io.github.tavodin.techstock_manager.exceptions.AlreadyExistsException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.services.SupplierService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SupplierController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupplierService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final String PATH = "/suppliers";
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 2L;
    private static final String NOT_FOUND_MSG = "Supplier not found";
    private static final String EXIST_DOCUMENT_MSG = "A record with the same document value already exists";
    private static final String EXIST_EMAIL_MSG = "Email already registered";

    private SupplierDTO dto;
    private SupplierRequestDTO request;

    @BeforeEach
    void setUp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 07, 20, 14, 02);
        LocalDateTime updatedAt = createdAt.plusHours(1L);

        dto = new SupplierDTO(
                VALID_ID,
                "TechParts",
                "00000000000191",
                "techparts@gmail.com",
                "6137644711",
                createdAt,
                updatedAt
        );

        request = new SupplierRequestDTO(
                dto.getName(),
                dto.getDocument(),
                dto.getEmail(),
                dto.getPhone()
        );
    }

    @Test
    void shouldReturnSupplierDTOAndOkWhenFindingWithValidId() throws Exception {
        when(service.findById(VALID_ID)).thenReturn(dto);

        mockMvc.perform(get(PATH + "/{id}", VALID_ID))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.document").value(dto.getDocument()))
                .andExpect(jsonPath("$.email").value(dto.getEmail()))
                .andExpect(jsonPath("$.phone").value(dto.getPhone()));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenFindingWithInvalidId() throws Exception {
        when(service.findById(INVALID_ID)).thenThrow(new ResourceNotFoundException(NOT_FOUND_MSG));

        mockMvc.perform(get(PATH + "/{id}", INVALID_ID))
                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value(PATH + "/" + INVALID_ID))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MSG))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnSupplierPageAndOkWhenFindAll() throws Exception {
        List<SupplierDTO> suppliers = List.of(dto);
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(1, 0, 1);
        PagedModel<SupplierDTO> pagedModel = PagedModel.of(suppliers, metadata);

        when(service.findAll(any(Pageable.class))).thenReturn(pagedModel);

        mockMvc.perform(get(PATH)
                .param("page", "0")
                .param("size", "10"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._embedded.suppliers[0]").exists())
                .andExpect(jsonPath("$._embedded.suppliers[0].name").value(dto.getName()))
                .andExpect(jsonPath("$._embedded.suppliers[0].document").value(dto.getDocument()))
                .andExpect(jsonPath("$._embedded.suppliers[0].email").value(dto.getEmail()))
                .andExpect(jsonPath("$._embedded.suppliers[0].phone").value(dto.getPhone()));
    }

    @Test
    void shouldReturnSupplierDTOAndCreateWhenSavingWithValidData() throws Exception {
        when(service.save(any(SupplierRequestDTO.class))).thenReturn(dto);

        mockMvc.perform(post(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.document").value(dto.getDocument()))
                .andExpect(jsonPath("$.email").value(dto.getEmail()))
                .andExpect(jsonPath("$.phone").value(dto.getPhone()))

                .andExpect(header().string("Location",
                        containsString("http://localhost" + PATH + "/" + dto.getId())));
    }

    @Test
    void shouldReturnCustomErrorAndConflictWhenSavingWithExistDocument() throws Exception {
        when(service.save(any(SupplierRequestDTO.class)))
                .thenThrow(new AlreadyExistsException(EXIST_DOCUMENT_MSG));

        mockMvc.perform(post(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isConflict())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(EXIST_DOCUMENT_MSG))
                .andExpect(jsonPath("$.path").value(PATH));
    }

    @Test
    void shouldReturnCustomErrorAndConflictWhenSavingWithExistEmail() throws Exception {
        when(service.save(any(SupplierRequestDTO.class)))
                .thenThrow(new AlreadyExistsException(EXIST_EMAIL_MSG));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isConflict())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(EXIST_EMAIL_MSG))
                .andExpect(jsonPath("$.path").value(PATH));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullName() throws Exception {
        request.setName(null);
        mockMvc.perform(post(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Name is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithTooLongName() throws Exception {
        request.setName("e".repeat(201));
        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("The name must contain a maximum of 200 characters")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullDocument() throws Exception {
        request.setDocument(null);
        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("document")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Document is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithTooLongDocument() throws Exception {
        request.setDocument("e".repeat(15));
        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("document")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("The document must contain a maximum of 14 characters")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullEmail() throws Exception {
        request.setEmail(null);
        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Email is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithTooLongEmail() throws Exception {
        request.setEmail("e".repeat(201));
        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("The email must contain a maximum of 200 characters")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithInvalidEmail() throws Exception {
        request.setEmail("e");
        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Invalid Email")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullPhone() throws Exception {
        request.setPhone(null);
        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("phone")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Phone is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithTooLongPhone() throws Exception {
        request.setPhone("e".repeat(15));
        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("phone")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("The phone must contain a maximum of 14 characters")));
    }

    @Test
    void shouldReturnSupplierDTOAndOkWhenUpdatingWithValidData() throws Exception {
        when(service.update(eq(VALID_ID), any(SupplierRequestDTO.class))).thenReturn(dto);

        mockMvc.perform(put(PATH + "/{id}", VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.document").value(dto.getDocument()))
                .andExpect(jsonPath("$.email").value(dto.getEmail()))
                .andExpect(jsonPath("$.phone").value(dto.getPhone()));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenUpdatingWithInvalidId() throws Exception {
        when(service.update(eq(INVALID_ID), any(SupplierRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(NOT_FOUND_MSG));

        mockMvc.perform(put(PATH + "/{id}", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MSG))
                .andExpect(jsonPath("$.path").value(PATH + "/" + INVALID_ID));
    }

    @Test
    void shouldReturnCustomErrorAndConflictWhenUpdatingWithExistDocument() throws Exception {
        when(service.update(eq(INVALID_ID), any(SupplierRequestDTO.class)))
                .thenThrow(new AlreadyExistsException(EXIST_DOCUMENT_MSG));

        mockMvc.perform(put(PATH + "/{id}", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isConflict())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(EXIST_DOCUMENT_MSG))
                .andExpect(jsonPath("$.path").value(PATH + "/" + INVALID_ID));
    }

    @Test
    void shouldReturnCustomErrorAndConflictWhenUpdatingWithExistEmail() throws Exception {
        when(service.update(eq(INVALID_ID), any(SupplierRequestDTO.class)))
                .thenThrow(new AlreadyExistsException(EXIST_EMAIL_MSG));

        mockMvc.perform(put(PATH + "/{id}", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isConflict())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(EXIST_EMAIL_MSG))
                .andExpect(jsonPath("$.path").value(PATH + "/" + INVALID_ID));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithNullName() throws Exception {
        request.setName(null);
        mockMvc.perform(put(PATH + "/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH + "/" + VALID_ID))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Name is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithTooLongName() throws Exception {
        request.setName("e".repeat(201));
        mockMvc.perform(put(PATH + "/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH + "/" + VALID_ID))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("The name must contain a maximum of 200 characters")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithNullDocument() throws Exception {
        request.setDocument(null);
        mockMvc.perform(put(PATH + "/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH + "/" + VALID_ID))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("document")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Document is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithTooLongDocument() throws Exception {
        request.setDocument("e".repeat(15));
        mockMvc.perform(put(PATH + "/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH + "/" + VALID_ID))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("document")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("The document must contain a maximum of 14 characters")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithNullEmail() throws Exception {
        request.setEmail(null);
        mockMvc.perform(put(PATH + "/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH + "/" + VALID_ID))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Email is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithTooLongEmail() throws Exception {
        request.setEmail("e".repeat(201));
        mockMvc.perform(put(PATH + "/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH + "/" + VALID_ID))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("The email must contain a maximum of 200 characters")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithInvalidEmail() throws Exception {
        request.setEmail("e");
        mockMvc.perform(put(PATH + "/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH + "/" + VALID_ID))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Invalid Email")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithNullPhone() throws Exception {
        request.setPhone(null);
        mockMvc.perform(put(PATH + "/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH + "/" + VALID_ID))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("phone")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Phone is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithTooLongPhone() throws Exception {
        request.setPhone("e".repeat(15));
        mockMvc.perform(put(PATH + "/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Entity validation error"))
                .andExpect(jsonPath("$.path").value(PATH + "/" + VALID_ID))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("phone")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("The phone must contain a maximum of 14 characters")));
    }

    @Test
    void shouldDoNothingAndReturnNoContentWhenDeletingWithValidId() throws Exception {
        doNothing().when(service).delete(VALID_ID);

        mockMvc.perform(delete(PATH + "/{id}", VALID_ID))

                .andExpect(status().isNoContent());

        verify(service).delete(VALID_ID);
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenDeletingWithInvalidId() throws Exception {
        doThrow(new ResourceNotFoundException(NOT_FOUND_MSG)).when(service).delete(INVALID_ID);

        mockMvc.perform(delete(PATH + "/{id}", INVALID_ID))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MSG))
                .andExpect(jsonPath("$.path").value(PATH + "/" + INVALID_ID));
    }
}
