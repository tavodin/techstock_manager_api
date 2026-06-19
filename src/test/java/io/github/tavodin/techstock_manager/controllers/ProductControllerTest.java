package io.github.tavodin.techstock_manager.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.config.security.filters.JwtAuthenticationFilter;
import io.github.tavodin.techstock_manager.dto.ProductDTO;
import io.github.tavodin.techstock_manager.dto.ProductRequestDTO;
import io.github.tavodin.techstock_manager.dto.ProductSpecificationRequestDTO;
import io.github.tavodin.techstock_manager.exceptions.AlreadyExistsException;
import io.github.tavodin.techstock_manager.exceptions.BusinessException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private final String PATH = "/products";
    private String productNotFoundMsg = "Product not found";
    private String brandNotFoundMsg = "Brand not found";
    private String categoryNotFoundMsg = "One or more categories were not found";
    private String specificationNotFoundMsg = "One or more specifications were not found";
    private String validationMsg = "Entity validation error";
    private String entityInUseMsg = "Brand is in use and cannot be deleted";
    private String skuExistsMsg = "SKU already exists";

    private Long validId = 1L;
    private Long invalidId = 2L;

    private ProductDTO dto;
    private ProductRequestDTO request;

    @BeforeEach
    void setUp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 05, 25, 17, 30);
        LocalDateTime updatedAt = createdAt.plusHours(1);

        request = new ProductRequestDTO(
                "Monitor DELL",
                BigDecimal.valueOf(1999.99),
                "Monitor DELL 24 polegadas ideal para escritório",
                "MON-001",
                5,
                validId,
                Set.of(validId),
                List.of(new ProductSpecificationRequestDTO(validId, "1920x1080", null, null))
        );

        dto = new ProductDTO(
                validId,
                request.getName(),
                BigDecimal.ZERO,
                request.getSalePrice(),
                request.getDescription(),
                request.getSku(),
                0,
                request.getMinimumStock(),
                true
        );
    }

    @Test
    void shouldReturnProductDTOAndOkWhenFindingWithValidId() throws Exception {
        when(service.findById(validId)).thenReturn(dto);

        mockMvc.perform(get(PATH + "/{id}", validId))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(validId))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.costPrice").value(dto.getCostPrice()))
                .andExpect(jsonPath("$.salePrice").value(dto.getSalePrice()))
                .andExpect(jsonPath("$.sku").value(dto.getSku()))
                .andExpect(jsonPath("$.description").value(dto.getDescription()))
                .andExpect(jsonPath("$.minimumStock").value(dto.getMinimumStock()))
                .andExpect(jsonPath("$.quantityInStock").value(dto.getQuantityInStock()))
                .andExpect(jsonPath("$.active").value(dto.getActive()));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenFindingWithInvalidId() throws Exception {
        when(service.findById(invalidId)).thenThrow(new ResourceNotFoundException(productNotFoundMsg));

        mockMvc.perform(get(PATH + "/{id}", invalidId))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(productNotFoundMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + invalidId));
    }

    @Test
    void shouldReturnProductDTOAndCreatedWhenSavingWithValidData() throws Exception {
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(validId))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.costPrice").value(dto.getCostPrice()))
                .andExpect(jsonPath("$.salePrice").value(dto.getSalePrice()))
                .andExpect(jsonPath("$.description").value(dto.getDescription()))
                .andExpect(jsonPath("$.sku").value(dto.getSku()))
                .andExpect(jsonPath("$.quantityInStock").value(dto.getQuantityInStock()))
                .andExpect(jsonPath("$.minimumStock").value(dto.getMinimumStock()))
                .andExpect(jsonPath("$.active").value(dto.getActive()))

                .andExpect(header().string("Location",
                        containsString("http://localhost" + PATH + "/" + dto.getId())));
    }

    @Test
    void shouldReturnProductDTOAndCreatedWhenSavingWithNullDescription() throws Exception {
        request.setDescription(null);
        dto.setDescription(null);
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenSavingWithInvalidBrandId() throws Exception {
        when(service.save(any(ProductRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(specificationNotFoundMsg));

        mockMvc.perform(post(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(brandNotFoundMsg))
                .andExpect(jsonPath("$.path").value(PATH));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenSavingWithInvalidCategoryIds() throws Exception {
        when(service.save(any(ProductRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(specificationNotFoundMsg));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(categoryNotFoundMsg))
                .andExpect(jsonPath("$.path").value(PATH));
    }

    @Test
    void shouldReturnCustomErrorAndConflictWhenSavingWithExistsSku() throws Exception {
        when(service.save(any(ProductRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(specificationNotFoundMsg));

        mockMvc.perform(post(PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isConflict())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(skuExistsMsg))
                .andExpect(jsonPath("$.path").value(PATH));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenSavingWithInvalidSpecificationIds() throws Exception {
        when(service.save(any(ProductRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException(specificationNotFoundMsg));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(specificationNotFoundMsg))
                .andExpect(jsonPath("$.path").value(PATH));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullName() throws Exception {
        request.setName(null);
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

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
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithTooShotName() throws Exception {
        request.setName("e");
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Name must contain between 2 and 200 characters")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithTooLongName() throws Exception {
        request.setName("e".repeat(201));
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Name must contain between 2 and 200 characters")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullSalePrice() throws Exception {
        request.setSalePrice(null);
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("salePrice")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Sale Price is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNegativeSalePrice() throws Exception {
        request.setSalePrice(BigDecimal.valueOf(-1.0));
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("salePrice")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Sale Price cannot be equal to zero or negative")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithZeroSalePrice() throws Exception {
        request.setSalePrice(BigDecimal.ZERO);
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("salePrice")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Sale Price cannot be equal to zero or negative")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullSku() throws Exception {
        request.setSku(null);
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("sku")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("SKU is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithTooShortSku() throws Exception {
        request.setSku("e");
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("sku")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("SKU must contain between 5 and 30 characters")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithTooLongSku() throws Exception {
        request.setSku("e".repeat(31));
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("sku")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("SKU must contain between 5 and 30 characters")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullMinimumStock() throws Exception {
        request.setMinimumStock(null);
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("minimumStock")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Minimum Stock is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNegativeMinimumStock() throws Exception {
        request.setMinimumStock(-1);
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("minimumStock")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Minimum Stock cannot be equal to zero or negative")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithZeroMinimumStock() throws Exception {
        request.setMinimumStock(0);
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("minimumStock")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Minimum Stock cannot be equal to zero or negative")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullBrandId() throws Exception {
        request.setBrandId(null);
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("brandId")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Brand ID is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullCategoriesId() throws Exception {
        request.setCategoryIds(null);
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("categoryIds")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Category ID is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullSpecifications() throws Exception {
        request.setSpecifications(null);
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("specifications")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Specifications are required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithNullSpecificationId() throws Exception {
        request.setSpecifications(List.of(
                new ProductSpecificationRequestDTO(null, null ,null, null)));
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("specifications[0].specificationId")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Specification ID is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingWithTooLongValueString() throws Exception {
        request.setSpecifications(List.of(
                new ProductSpecificationRequestDTO(validId, "e".repeat(46) ,null, null)));
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("specifications[0].valueString")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Value must contain a maximum of 45 characters")));
    }
}
