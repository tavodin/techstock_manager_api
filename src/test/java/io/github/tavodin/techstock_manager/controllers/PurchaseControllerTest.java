package io.github.tavodin.techstock_manager.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.config.security.filters.JwtAuthenticationFilter;
import io.github.tavodin.techstock_manager.dto.BrandDTO;
import io.github.tavodin.techstock_manager.dto.PurchaseDTO;
import io.github.tavodin.techstock_manager.dto.PurchaseItemRequestDTO;
import io.github.tavodin.techstock_manager.dto.PurchaseRequestDTO;
import io.github.tavodin.techstock_manager.enums.PurchaseStatus;
import io.github.tavodin.techstock_manager.exceptions.BusinessException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.services.PurchaseService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PurchaseService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final String PATH = "/purchases";
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 2L;
    private static final String PURCHASE_NOT_FOUND_MSG = "Purchase not found";
    private static final String SUPPLIER_NOT_FOUND_MSG = "Supplier not found";
    private static final String PRODUCT_NOT_FOUND_MSG = "Product not found";
    private static final String VALIDATION_ERROR = "Entity validation error";

    private PurchaseDTO dto;
    private PurchaseRequestDTO request;

    @BeforeEach
    void setUp() {
        dto = new PurchaseDTO(
                VALID_ID,
                LocalDate.of(2026, 07, 25),
                PurchaseStatus.OPEN,
                BigDecimal.valueOf(235.22)
        );

        request = new PurchaseRequestDTO();
        request.setPurchaseDate(dto.getPurchaseDate());
        request.setSupplierId(VALID_ID);
        request.setItems(List.of(new PurchaseItemRequestDTO(
                VALID_ID,
                2,
                BigDecimal.valueOf(799.99)
        )));
    }

    @Test
    void shouldReturnPurchaseDTOAndOkWhenFindingWithValidId() throws Exception {
        when(service.findById(VALID_ID)).thenReturn(dto);

        mockMvc.perform(get(PATH + "/{id}", VALID_ID))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.purchaseDate").exists())
                .andExpect(jsonPath("$.status").value(dto.getStatus().toString()))
                .andExpect(jsonPath("$.totalAmount").value(dto.getTotalAmount()));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenFindingWithInvalidId() throws Exception {
        when(service.findById(INVALID_ID)).thenThrow(new ResourceNotFoundException(PURCHASE_NOT_FOUND_MSG));

        mockMvc.perform(get(PATH + "/{id}", INVALID_ID))
                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value(PATH + "/" + INVALID_ID))
                .andExpect(jsonPath("$.message").value(PURCHASE_NOT_FOUND_MSG))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnPurchasePageAndOkWhenFindAll() throws Exception {
        List<PurchaseDTO> purchases = List.of(dto);
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(1, 0, 1);
        PagedModel<PurchaseDTO> pagedModel = PagedModel.of(purchases, metadata);

        when(service.findAll(any(Pageable.class))).thenReturn(pagedModel);

        mockMvc.perform(get(PATH)
                        .param("page", "0")
                        .param("size", "10"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._embedded.purchases[0].id")
                        .value(dto.getId()))
                .andExpect(jsonPath("$._embedded.purchases[0].purchaseDate")
                        .exists())
                .andExpect(jsonPath("$._embedded.purchases[0].status")
                        .value(dto.getStatus().toString()))
                .andExpect(jsonPath("$._embedded.purchases[0].totalAmount")
                        .value(dto.getTotalAmount()))
                .andExpect(jsonPath("$.page.size").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.number").value(0));
    }

    @Test
    void shouldSaveAndReturnPurchaseDTOAndCreatedWhenSavingWithValidData() throws Exception {
        when(service.save(any(PurchaseRequestDTO.class))).thenReturn(dto);

        mockMvc.perform(post(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.purchaseDate").exists())
                .andExpect(jsonPath("$.status").value(dto.getStatus().toString()))
                .andExpect(jsonPath("$.totalAmount").value(dto.getTotalAmount()))

                .andExpect(header().string("Location",
                        containsString("http://localhost" + PATH + "/" + VALID_ID)));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenSavingWithInvalidSupplierId() throws Exception {
        when(service.save(any(PurchaseRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(SUPPLIER_NOT_FOUND_MSG));

        mockMvc.perform(post(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value(PATH))
                .andExpect(jsonPath("$.message").value(SUPPLIER_NOT_FOUND_MSG))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenSavingWithInvalidProductId() throws Exception {
        when(service.save(any(PurchaseRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(PRODUCT_NOT_FOUND_MSG));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value(PATH))
                .andExpect(jsonPath("$.message").value(PRODUCT_NOT_FOUND_MSG))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullPurchaseDate() throws Exception {
        request.setPurchaseDate(null);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("purchaseDate")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Purchase Date is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullSupplierId() throws Exception {
        request.setSupplierId(null);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("supplierId")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Supplier is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithEmptyPurchaseItems() throws Exception {
        request.setItems(null);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Purchase Item is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullProductId() throws Exception {
        request.setItems(List.of(new PurchaseItemRequestDTO(
                null, 1, BigDecimal.valueOf(200.0)
        )));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[0].productId")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Product is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullQuantity() throws Exception {
        request.setItems(List.of(new PurchaseItemRequestDTO(
                VALID_ID, null, BigDecimal.valueOf(200.0)
        )));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[0].quantity")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Quantity is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNegativeQuantity() throws Exception {
        request.setItems(List.of(new PurchaseItemRequestDTO(
                VALID_ID, -10, BigDecimal.valueOf(200.0)
        )));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[0].quantity")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Quantity must be positive")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithZeroQuantity() throws Exception {
        request.setItems(List.of(new PurchaseItemRequestDTO(
                VALID_ID, 0, BigDecimal.valueOf(200.0)
        )));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[0].quantity")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Quantity must be positive")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullUnitCost() throws Exception {
        request.setItems(List.of(new PurchaseItemRequestDTO(
                VALID_ID, 1, null
        )));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[0].unitCost")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Unit Cost is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNegativeUnitCost() throws Exception {
        request.setItems(List.of(new PurchaseItemRequestDTO(
                VALID_ID, 1, BigDecimal.valueOf(-200.0)
        )));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[0].unitCost")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Unit Cost must be unit cost")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithZeroUnitCost() throws Exception {
        request.setItems(List.of(new PurchaseItemRequestDTO(
                VALID_ID, 1, BigDecimal.ZERO
        )));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[0].unitCost")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Unit Cost must be unit cost")));
    }

    @Test
    void shouldChangeStatusToCompletedAndReturnPurchaseDTOAndOkWhenCompletingWithValidData() throws Exception {
        dto.setStatus(PurchaseStatus.COMPLETED);
        when(service.completedPurchase(VALID_ID)).thenReturn(dto);

        mockMvc.perform(patch(PATH + "/{id}/" + "completed", VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.purchaseDate").exists())
                .andExpect(jsonPath("$.status").value(dto.getStatus().toString()))
                .andExpect(jsonPath("$.totalAmount").value(dto.getTotalAmount()));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenCompletingWithInvalidId() throws Exception {
        when(service.completedPurchase(INVALID_ID))
                .thenThrow(new ResourceNotFoundException(PURCHASE_NOT_FOUND_MSG));

        mockMvc.perform(patch(PATH + "/{id}/" + "completed", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(PURCHASE_NOT_FOUND_MSG))
                .andExpect(jsonPath("$.path")
                        .value(PATH + "/" + INVALID_ID + "/completed"));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenCompletingWithStatusOtherThanOpen() throws Exception {
        when(service.completedPurchase(VALID_ID))
                .thenThrow(new BusinessException("The purchase status must be 'OPEN'"));

        mockMvc.perform(patch(PATH + "/{id}/" + "completed", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("The purchase status must be 'OPEN'"))
                .andExpect(jsonPath("$.path")
                        .value(PATH + "/" + VALID_ID + "/completed"));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenCompletingWithNullPurchaseDate() throws Exception {
        request.setPurchaseDate(null);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("purchaseDate")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Purchase Date is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenCompletingWithNullSupplierId() throws Exception {
        request.setSupplierId(null);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("supplierId")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Supplier is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenCompletingWithEmptyItems() throws Exception {
        request.setItems(null);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Purchase Item is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenCompletingWithNullProductId() throws Exception {
        request.setItems(List.of(new PurchaseItemRequestDTO(
                null, 1, BigDecimal.valueOf(200.0)
        )));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[0].productId")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Product is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenCompletingWithNullQuantity() throws Exception {
        request.setItems(List.of(new PurchaseItemRequestDTO(
                VALID_ID, null, BigDecimal.valueOf(200.0)
        )));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[0].quantity")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Quantity is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenCompletingWithZeroQuantity() throws Exception {
        request.setItems(List.of(new PurchaseItemRequestDTO(
                VALID_ID, 0, BigDecimal.valueOf(200.0)
        )));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[0].quantity")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Quantity must be positive")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenCompletingWithNegativeQuantity() throws Exception {
        request.setItems(List.of(new PurchaseItemRequestDTO(
                VALID_ID, -1, BigDecimal.valueOf(200.0)
        )));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[0].quantity")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Quantity must be positive")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenCompletingWithNullUnitCost() throws Exception {
        request.setItems(List.of(new PurchaseItemRequestDTO(
                VALID_ID, 1, BigDecimal.valueOf(-200.0)
        )));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[0].unitCost")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Unit Cost must be unit cost")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenCompletingWithZeroUnitCost() throws Exception {
        request.setItems(List.of(new PurchaseItemRequestDTO(
                VALID_ID, 1, BigDecimal.ZERO
        )));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[0].unitCost")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Unit Cost must be unit cost")));
    }

    @Test
    void shouldChangeStatusToCanceledWhenCancelingWithValidId() throws Exception {
        dto.setStatus(PurchaseStatus.CANCELED);
        when(service.canceledPurchase(VALID_ID)).thenReturn(dto);

        mockMvc.perform(patch(PATH + "/{id}/" + "canceled", VALID_ID))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.status").value(dto.getStatus().toString()));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenCancelingWithInvalidId() throws Exception {
        when(service.canceledPurchase(INVALID_ID))
                .thenThrow(new ResourceNotFoundException(PURCHASE_NOT_FOUND_MSG));

        mockMvc.perform(patch(PATH + "/{id}/" + "canceled", INVALID_ID))
                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(PURCHASE_NOT_FOUND_MSG))
                .andExpect(jsonPath("$.path")
                        .value(PATH + "/" + INVALID_ID + "/canceled"));
    }

    @Test
    void shouldReturnCustomErrorAndBadRequestWhenCancelingWithStatusOtherThanOpen() throws Exception {
        when(service.canceledPurchase(VALID_ID))
                .thenThrow(new BusinessException("The purchase status must be 'OPEN'"));

        mockMvc.perform(patch(PATH + "/{id}/" + "canceled", VALID_ID))
                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("The purchase status must be 'OPEN'"))
                .andExpect(jsonPath("$.path")
                        .value(PATH + "/" + VALID_ID + "/canceled"));
    }
}
