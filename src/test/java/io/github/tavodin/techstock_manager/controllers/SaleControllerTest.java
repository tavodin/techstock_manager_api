package io.github.tavodin.techstock_manager.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.config.security.filters.JwtAuthenticationFilter;
import io.github.tavodin.techstock_manager.dto.PurchaseDTO;
import io.github.tavodin.techstock_manager.dto.SaleDTO;
import io.github.tavodin.techstock_manager.dto.SaleItemRequestDTO;
import io.github.tavodin.techstock_manager.dto.SaleRequestDTO;
import io.github.tavodin.techstock_manager.entities.Sale;
import io.github.tavodin.techstock_manager.enums.SaleStatus;
import io.github.tavodin.techstock_manager.exceptions.BusinessException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.services.SaleService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SaleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SaleService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final String PATH = "/sales";
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 2L;
    private static final String SALE_NOT_FOUND_MSG = "Sale not found";
    private static final String PRODUCTS_NOT_FOUND_MSG = "One or more products were not found";
    private static final String EXCEEDING_STOCK_MSG = "Insufficient stock quantity";
    private static final String SALE_MSG = "Sale Product";
    private static final String CANCELED_MSG = "Product return";
    private static final String VALIDATION_ERROR = "Entity validation error";

    private SaleDTO saleDTO;
    private SaleRequestDTO request;

    @BeforeEach
    void setUp() {
        saleDTO = new SaleDTO();
        saleDTO.setId(VALID_ID);
        saleDTO.setSaleDate(LocalDateTime.of(2026, 8, 3, 13, 00));
        saleDTO.setStatus(SaleStatus.COMPLETED);
        saleDTO.setTotalAmount(BigDecimal.valueOf(250.5));
        saleDTO.setPaymentMethod("Débito");

        request = new SaleRequestDTO();
        request.setPaymentMethod("Débito");
        request.setItems(Set.of(new SaleItemRequestDTO(
                3,
                1L
        )));
    }

    @Test
    void shouldReturnSaleDTOAndOkWhenFindingWithValidId() throws Exception {
        when(service.findById(VALID_ID)).thenReturn(saleDTO);

        mockMvc.perform(get(PATH + "/{id}", VALID_ID))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.saleDate").exists())
                .andExpect(jsonPath("$.status").value(SaleStatus.COMPLETED.toString()))
                .andExpect(jsonPath("$.paymentMethod").value(saleDTO.getPaymentMethod()))
                .andExpect(jsonPath("$.totalAmount").value(saleDTO.getTotalAmount()));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenFindingWithInvalidId() throws Exception {
        when(service.findById(INVALID_ID)).thenThrow(new ResourceNotFoundException(SALE_NOT_FOUND_MSG));

        mockMvc.perform(get(PATH + "/{id}", INVALID_ID))
                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value(PATH + "/" + INVALID_ID))
                .andExpect(jsonPath("$.message").value(SALE_NOT_FOUND_MSG));
    }

    @Test
    void shouldReturnSalePageAndOkWhenFindAll() throws Exception {
        List<SaleDTO> sales = List.of(saleDTO);
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(1, 0, 1);
        PagedModel<SaleDTO> pagedModel = PagedModel.of(sales, metadata);

        when(service.findAll(any(Pageable.class))).thenReturn(pagedModel);

        mockMvc.perform(get(PATH)
                        .param("page", "0")
                        .param("size", "10"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._embedded.sales[0].id")
                        .value(saleDTO.getId()))
                .andExpect(jsonPath("$._embedded.sales[0].saleDate")
                        .exists())
                .andExpect(jsonPath("$._embedded.sales[0].status")
                        .value(saleDTO.getStatus().toString()))
                .andExpect(jsonPath("$._embedded.sales[0].paymentMethod")
                        .value(saleDTO.getPaymentMethod()))
                .andExpect(jsonPath("$._embedded.sales[0].totalAmount")
                        .value(saleDTO.getTotalAmount()))
                .andExpect(jsonPath("$.page.size").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.number").value(0));
    }

    @Test
    void shouldSaveAndReturnSaleDTOAndCreatedWhenSavingWithValidData() throws Exception {
        when(service.save(any(SaleRequestDTO.class))).thenReturn(saleDTO);

        mockMvc.perform(post(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.id").value(saleDTO.getId()))
                .andExpect(jsonPath("$.saleDate").exists())
                .andExpect(jsonPath("$.status").value(saleDTO.getStatus().toString()))
                .andExpect(jsonPath("$.paymentMethod").value(saleDTO.getPaymentMethod()))
                .andExpect(jsonPath("$.totalAmount").value(saleDTO.getTotalAmount()))

                .andExpect(header().string("Location",
                        containsString("http://localhost" + PATH + "/" + VALID_ID)));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenSavingWithInvalidProductId() throws Exception {
        when(service.save(any(SaleRequestDTO.class))).thenThrow(new ResourceNotFoundException(PRODUCTS_NOT_FOUND_MSG));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value(PATH))
                .andExpect(jsonPath("$.message").value(PRODUCTS_NOT_FOUND_MSG));
    }

    @Test
    void shouldReturnCustomErrorAndBadRequestWhenSavingWithQuantityExceedingStockCapacity() throws Exception {
        when(service.save(any(SaleRequestDTO.class))).thenThrow(new BusinessException(EXCEEDING_STOCK_MSG));

        mockMvc.perform(post(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value(PATH))
                .andExpect(jsonPath("$.message").value(EXCEEDING_STOCK_MSG));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullPaymentMethod() throws Exception {
        request.setPaymentMethod(null);

        mockMvc.perform(post(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("paymentMethod")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Payment Method is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithTooLongPaymentMethod() throws Exception {
        request.setPaymentMethod("e".repeat(46));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("paymentMethod")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("The name must contain a maximum of 45 characters")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithEmptySaleItem() throws Exception {
        request.setItems(Set.of());

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
                        .value(hasItem("Sale must contain at least one item")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullQuantityItem() throws Exception {
        request.setItems(Set.of(new SaleItemRequestDTO(null, 1L)));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[].quantity")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Quantity is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNegativeQuantityItem() throws Exception {
        request.setItems(Set.of(new SaleItemRequestDTO(-1, 1L)));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[].quantity")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Quantity must be positive")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullProductId() throws Exception {
        request.setItems(Set.of(new SaleItemRequestDTO(1, null)));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(VALIDATION_ERROR))
                .andExpect(jsonPath("$.path").value(PATH))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("items[].productId")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Product is required")));
    }

    @Test
    void shouldChangeSaleStatusToCanceledAndOkWhenCancelingWithValidId() throws Exception {
        saleDTO.setStatus(SaleStatus.CANCELED);
        when(service.canceled(VALID_ID)).thenReturn(saleDTO);

        mockMvc.perform(patch(PATH + "/" + VALID_ID))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.saleDate").exists())
                .andExpect(jsonPath("$.status").value(saleDTO.getStatus().toString()))
                .andExpect(jsonPath("$.paymentMethod").value(saleDTO.getPaymentMethod()))
                .andExpect(jsonPath("$.totalAmount").value(saleDTO.getTotalAmount()));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenCancelingWithInvalidId() throws Exception {
        when(service.canceled(INVALID_ID)).thenThrow(new ResourceNotFoundException(SALE_NOT_FOUND_MSG));

        mockMvc.perform(patch(PATH + "/" + INVALID_ID))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(SALE_NOT_FOUND_MSG))
                .andExpect(jsonPath("$.path").value(PATH + "/" + INVALID_ID));
    }

    @Test
    void shouldReturnCustomErrorAndBadRequestWhenCancelingWithCanceledStatus() throws Exception {
        when(service.canceled(VALID_ID)).thenThrow(new BusinessException("Sale status must be 'COMPLETED'"));

        mockMvc.perform(patch(PATH + "/" + VALID_ID))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Sale status must be 'COMPLETED'"))
                .andExpect(jsonPath("$.path").value(PATH + "/" + VALID_ID));
    }
}
