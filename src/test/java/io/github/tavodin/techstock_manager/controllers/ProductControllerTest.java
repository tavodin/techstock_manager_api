package io.github.tavodin.techstock_manager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tavodin.techstock_manager.config.security.filters.JwtAuthenticationFilter;
import io.github.tavodin.techstock_manager.dto.*;
import io.github.tavodin.techstock_manager.exceptions.AlreadyExistsException;
import io.github.tavodin.techstock_manager.exceptions.BusinessException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.services.ProductService;
import io.github.tavodin.techstock_manager.services.ProductSpecificationService;
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
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
    private ProductSpecificationService prodSpecService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private final String PATH = "/products";
    private String productNotFoundMsg = "Product not found";
    private String brandNotFoundMsg = "Brand not found";
    private String categoryNotFoundMsg = "One or more categories were not found";
    private String specificationNotFoundMsg = "One or more specifications were not found";
    private String prodSpecNotFoundMsg = "Product Specification not found";
    private String validationMsg = "Entity validation error";
    private String skuExistsMsg = "SKU already exists";
    private String nullValueStringMsg = "Value String cannot be null";
    private String nullValueNumberMsg = "Value Number cannot be null";
    private String nullValueBooleanMsg = "Value Boolean cannot be null";

    private Long validId = 1L;
    private Long invalidId = 2L;

    private ProductDTO dto;
    private ProductSaveDTO request;
    private ProductUpdateDTO updateDTO;
    private ProductSpecificationListDTO listSpecDTO;
    private ProductSpecificationSaveDTO saveSpecRequest;
    private ProductSpecificationUpdateDTO updateSpecRequest;
    private ProductSpecificationDTO specDTO;

    @BeforeEach
    void setUp() {
        request = new ProductSaveDTO(
                "Monitor DELL",
                BigDecimal.valueOf(1999.99),
                "Monitor DELL 24 polegadas ideal para escritório",
                "MON-001",
                5,
                validId,
                Set.of(validId),
                List.of(new ProductSpecificationSaveDTO(validId, "1920x1080", null, null))
        );

        updateDTO = new ProductUpdateDTO();
        updateDTO.setName("Teclado DELL sem fio");
        updateDTO.setSalePrice(BigDecimal.valueOf(300.0));
        updateDTO.setDescription("Teclado para escritório");
        updateDTO.setMinimumStock(2);
        updateDTO.setSku("TEC-001");
        updateDTO.setBrandId(validId);
        updateDTO.setCategoryIds(Set.of(validId));

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

        listSpecDTO = new ProductSpecificationListDTO(
                validId,
                validId,
                validId,
                "Frequência",
                null,
                60.0,
                null,
                "Hz"
        );

        saveSpecRequest = new ProductSpecificationSaveDTO(
                validId,
                "1920x1080",
                null,
                null
        );

        updateSpecRequest = new ProductSpecificationUpdateDTO(
                "2560x1440",
                null,
                null
        );

        specDTO = new ProductSpecificationDTO(
                validId,
                "1920x1080",
                null,
                null
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
    void shouldSaveProductAndReturnCreatedWhenSavingWithNullDescription() throws Exception {
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
        when(service.save(any(ProductSaveDTO.class)))
                .thenThrow(new ResourceNotFoundException(brandNotFoundMsg));

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
        when(service.save(any(ProductSaveDTO.class)))
                .thenThrow(new ResourceNotFoundException(categoryNotFoundMsg));

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
        when(service.save(any(ProductSaveDTO.class)))
                .thenThrow(new AlreadyExistsException(skuExistsMsg));

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
        when(service.save(any(ProductSaveDTO.class)))
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
    void shouldReturnCustomErrorAndBadRequestWhenSavingWithMissingSpecification() throws Exception {
        when(service.save(any(ProductSaveDTO.class)))
                .thenThrow(new BusinessException("Missing required specifications"));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Missing required specifications"))
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
                new ProductSpecificationSaveDTO(null, null ,null, null)));
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
                new ProductSpecificationSaveDTO(validId, "e".repeat(46) ,null, null)));
        when(service.save(request)).thenReturn(dto);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("specifications[0].valueString")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Value must contain a maximum of 45 characters")));
    }

    @Test
    void shouldReturnProductDTOAndOkWhenUpdatingProductWithValidData() throws Exception {
        dto.setName(updateDTO.getName());
        dto.setSalePrice(updateDTO.getSalePrice());
        dto.setDescription(updateDTO.getDescription());
        dto.setSku(updateDTO.getSku());
        dto.setMinimumStock(updateDTO.getMinimumStock());

        when(service.update(validId, updateDTO)).thenReturn(dto);

        mockMvc.perform(put(PATH + "/{id}", validId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validId))
                .andExpect(jsonPath("$.name").value(updateDTO.getName()))
                .andExpect(jsonPath("$.costPrice").value(dto.getCostPrice()))
                .andExpect(jsonPath("$.salePrice").value(updateDTO.getSalePrice()))
                .andExpect(jsonPath("$.description").value(updateDTO.getDescription()))
                .andExpect(jsonPath("$.sku").value(updateDTO.getSku()))
                .andExpect(jsonPath("$.quantityInStock").value(dto.getQuantityInStock()))
                .andExpect(jsonPath("$.minimumStock").value(updateDTO.getMinimumStock()))
                .andExpect(jsonPath("$.active").value(dto.getActive()));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenUpdatingWithInvalidProductId() throws Exception {
        when(service.update(invalidId, updateDTO)).thenThrow(new ResourceNotFoundException(productNotFoundMsg));

        mockMvc.perform(put(PATH + "/{id}", invalidId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(productNotFoundMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + invalidId));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenUpdatingWithInvalidBrandId() throws Exception {
        when(service.update(validId, updateDTO)).thenThrow(new ResourceNotFoundException(brandNotFoundMsg));

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(brandNotFoundMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + validId));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenUpdatingWithInvalidCategoryIds() throws Exception {
        when(service.update(validId, updateDTO)).thenThrow(new ResourceNotFoundException(categoryNotFoundMsg));

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(categoryNotFoundMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + validId));
    }

    @Test
    void shouldReturnCustomErrorAndConflictWhenUpdatingWithExistSku() throws Exception {
        when(service.update(validId, updateDTO)).thenThrow(new AlreadyExistsException(skuExistsMsg));

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isConflict())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(skuExistsMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + validId));
    }

    @Test
    void shouldUpdateProductAndReturnOkWhenUpdatingWithNullDescription() throws Exception {
        updateDTO.setDescription(null);
        when(service.update(validId, updateDTO)).thenReturn(dto);

        mockMvc.perform(put(PATH + "/{id}", validId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnValidationAndBadRequestWhenUpdatingWithNullName() throws Exception {
        updateDTO.setName(null);

        mockMvc.perform(put(PATH + "/{id}", validId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(validationMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + validId))

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Name is required")));
    }

    @Test
    void shouldReturnValidationAndBadRequestWhenUpdatingWithTooShortName() throws Exception {
        updateDTO.setName("e");

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Name must contain between 2 and 200 characters")));
    }

    @Test
    void shouldReturnValidationAndBadRequestWhenUpdatingWithTooLongName() throws Exception {
        request.setName("e".repeat(201));

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Name must contain between 2 and 200 characters")));
    }

    @Test
    void shouldReturnValidationAndBadRequestWhenUpdatingWithNullSalePrice() throws Exception {
        updateDTO.setSalePrice(null);

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("salePrice")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Sale Price is required")));
    }

    @Test
    void shouldReturnValidationAndBadRequestWhenUpdatingWithNegativeSalePrice() throws Exception {
        updateDTO.setSalePrice(BigDecimal.valueOf(-2.0));

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("salePrice")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Sale Price cannot be equal to zero or negative")));
    }

    @Test
    void shouldReturnValidationAndBadRequestWhenUpdatingWithZeroSalePrice() throws Exception {
        updateDTO.setSalePrice(BigDecimal.ZERO);

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("salePrice")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Sale Price cannot be equal to zero or negative")));
    }

    @Test
    void shouldReturnValidationAndBadRequestWhenUpdatingWithNullSku() throws Exception {
        updateDTO.setSku(null);

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("sku")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("SKU is required")));
    }

    @Test
    void shouldReturnValidationAndBadRequestWhenUpdatingWithTooShortSku() throws Exception {
        updateDTO.setSku("e");

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("sku")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("SKU must contain between 5 and 30 characters")));
    }

    @Test
    void shouldReturnValidationAndBadRequestWhenUpdatingWithTooLongSku() throws Exception {
        updateDTO.setSku("e".repeat(31));

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("sku")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("SKU must contain between 5 and 30 characters")));
    }

    @Test
    void shouldReturnValidationAndBadRequestWhenUpdatingWithNullMinimumStock() throws Exception {
        updateDTO.setMinimumStock(null);

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("minimumStock")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Minimum Stock is required")));
    }

    @Test
    void shouldReturnValidationAndBadRequestWhenUpdatingWithNegativeMinimumStock() throws Exception {
        updateDTO.setMinimumStock(-1);

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("minimumStock")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Minimum Stock cannot be equal to zero or negative")));
    }

    @Test
    void shouldReturnValidationAndBadRequestWhenUpdatingWithZeroMinimumStock() throws Exception {
        updateDTO.setMinimumStock(0);

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("minimumStock")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Minimum Stock cannot be equal to zero or negative")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingWithNullBrandId() throws Exception {
        updateDTO.setBrandId(null);

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("brandId")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Brand ID is required")));
    }

    @Test
    void shouldReturnValidationAndBadRequestWhenUpdatingWithNullCategoryIds() throws Exception {
        updateDTO.setCategoryIds(null);

        mockMvc.perform(put(PATH + "/{id}", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("categoryIds")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Category ID is required")));
    }


    @Test
    void shouldSoftDeleteAndReturnNoContentProductWhenDeletingWithValidId() throws Exception {
        doNothing().when(service).delete(validId);

        mockMvc.perform(delete(PATH + "/{id}", validId))

                .andExpect(status().isNoContent());

        verify(service).delete(validId);
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenDeletingWithInvalidProductId() throws Exception {
        doThrow(new ResourceNotFoundException(productNotFoundMsg)).when(service).delete(invalidId);

        mockMvc.perform(delete(PATH + "/{id}", invalidId))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(productNotFoundMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + invalidId));
    }

    @Test
    void shouldReturnProductSpecificationListAndOkWhenFindingAllSpecificationsByProductId() throws Exception{
        when(prodSpecService.findAll(validId)).thenReturn(List.of(listSpecDTO));

        mockMvc.perform(get(PATH + "/{id}" + "/specifications", validId))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.[0].id").value(listSpecDTO.getId()))
                .andExpect(jsonPath("$.[0].specificationId").value(listSpecDTO.getSpecificationId()))
                .andExpect(jsonPath("$.[0].productId").value(listSpecDTO.getProductId()))
                .andExpect(jsonPath("$.[0].specificationName").value(listSpecDTO.getSpecificationName()))
                .andExpect(jsonPath("$.[0].valueString").value(listSpecDTO.getValueString()))
                .andExpect(jsonPath("$.[0].valueNumber").value(listSpecDTO.getValueNumber()))
                .andExpect(jsonPath("$.[0].valueBoolean").value(listSpecDTO.getValueBoolean()))
                .andExpect(jsonPath("$.[0].unitSymbol").value(listSpecDTO.getUnitSymbol()));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenFindingAllSpecificationsByProductIdWithInvalidId() throws Exception {
        when(prodSpecService.findAll(invalidId)).thenThrow(new ResourceNotFoundException(productNotFoundMsg));

        mockMvc.perform(get(PATH + "/{id}" + "/specifications", invalidId))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(productNotFoundMsg))
                .andExpect(jsonPath("$.path").value(PATH + "/" + invalidId + "/specifications"));
    }

    @Test
    void shouldReturnProductDTOAndCreatedWhenSavingSpecificationWithValidData() throws Exception {
        when(prodSpecService.save(validId, saveSpecRequest)).thenReturn(specDTO);

        mockMvc.perform(post(PATH + "/{id}" + "/specifications", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveSpecRequest)))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.id").value(specDTO.getId()))
                .andExpect(jsonPath("$.valueNumber").doesNotExist())
                .andExpect(jsonPath("$.valueString").value(specDTO.getValueString()))
                .andExpect(jsonPath("$.valueBoolean").doesNotExist())

                .andExpect(header().string("Location",
                    containsString("http://localhost" + PATH+"/"+dto.getId()+"/specifications")));
    }

    @Test
    void shouldReturnCustomErrorAndConflictWhenSavingSpecificationWithExistSpecification() throws Exception {
        String message = "The product cannot have the same specification";
        when(prodSpecService.save(anyLong(), any(ProductSpecificationSaveDTO.class)))
                .thenThrow(new AlreadyExistsException(message));

        mockMvc.perform(post(PATH + "/{id}" + "/specifications", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveSpecRequest)))

                .andExpect(status().isConflict())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.path")
                        .value(PATH + "/" + invalidId + "/specifications"));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenSavingSpecificationWithInvalidProductId() throws Exception {
        when(prodSpecService.save(anyLong(), any(ProductSpecificationSaveDTO.class)))
                .thenThrow(new ResourceNotFoundException(productNotFoundMsg));

        mockMvc.perform(post(PATH + "/{id}" + "/specifications", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveSpecRequest)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(productNotFoundMsg))
                .andExpect(jsonPath("$.path")
                        .value(PATH + "/" + invalidId + "/specifications"));
    }

    @Test
    void shouldReturnCustomErrorAndBadRequestWhenSavingSpecificationWithNullValueString() throws Exception {
        when(prodSpecService.save(anyLong(), any(ProductSpecificationSaveDTO.class)))
                .thenThrow(new BusinessException(nullValueStringMsg));

        mockMvc.perform(post(PATH + "/{id}" + "/specifications", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveSpecRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(nullValueStringMsg))
                .andExpect(jsonPath("$.path")
                        .value(PATH + "/" + validId + "/specifications"));
    }

    @Test
    void shouldReturnCustomErrorAndBadRequestWhenSavingSpecificationWithNullValueNumber() throws Exception {
        when(prodSpecService.save(anyLong(), any(ProductSpecificationSaveDTO.class)))
                .thenThrow(new BusinessException(nullValueNumberMsg));

        mockMvc.perform(post(PATH + "/{id}" + "/specifications", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveSpecRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(nullValueNumberMsg))
                .andExpect(jsonPath("$.path")
                        .value(PATH + "/" + validId + "/specifications"));
    }

    @Test
    void shouldReturnCustomErrorAndBadRequestWhenSavingSpecificationWithNullValueBoolean() throws Exception {
        when(prodSpecService.save(anyLong(), any(ProductSpecificationSaveDTO.class)))
                .thenThrow(new BusinessException(nullValueBooleanMsg));

        mockMvc.perform(post(PATH + "/{id}" + "/specifications", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveSpecRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(nullValueBooleanMsg))
                .andExpect(jsonPath("$.path")
                        .value(PATH + "/" + validId + "/specifications"));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingSpecificationWithNullSpecificationId() throws Exception {
        ProductSpecificationSaveDTO invalidRequest = new ProductSpecificationSaveDTO(
          null, null, null, null
        );

        mockMvc.perform(post(PATH + "/{id}" + "/specifications", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("specificationId")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Specification ID is required")));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenSavingSpecificationWithTooLongValueString() throws Exception {
        ProductSpecificationSaveDTO invalidRequest = new ProductSpecificationSaveDTO(
                validId, "e".repeat(46), null, null
        );

        mockMvc.perform(post(PATH + "/{id}" + "/specifications", validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("valueString")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Value must contain a maximum of 45 characters")));
    }

    @Test
    void shouldReturnProductDTOAndOkWhenUpdatingSpecificationWithValidData() throws Exception {
        specDTO.setValueString(updateSpecRequest.valueString());
        when(prodSpecService.update(anyLong(), anyLong(), any(ProductSpecificationUpdateDTO.class)))
                .thenReturn(specDTO);

        mockMvc.perform(put(PATH + "/{prodId}/specifications/{specId}", validId, validId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateSpecRequest)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(specDTO.getId()))
                .andExpect(jsonPath("$.valueString").value(updateSpecRequest.valueString()))
                .andExpect(jsonPath("$.valueNumber").value(updateSpecRequest.valueNumber()))
                .andExpect(jsonPath("$.valueBoolean").value(updateSpecRequest.valueBoolean()));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenUpdatingSpecificationWithInvalidProductOrSpecificationId() throws Exception {
        String path = PATH + "/" + invalidId + "/specifications/" + invalidId;
        when(prodSpecService.update(eq(invalidId), eq(invalidId), any(ProductSpecificationUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException(prodSpecNotFoundMsg));

        mockMvc.perform(put(PATH + "/{prodId}/specifications/{specId}", invalidId, invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSpecRequest)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(prodSpecNotFoundMsg))
                .andExpect(jsonPath("$.path").value(path));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenUpdatingSpecificationWithInvalidSpecificationId() throws Exception {
        String path = PATH + "/" + validId + "/specifications/" + invalidId;
        when(prodSpecService.update(eq(validId), eq(invalidId), any(ProductSpecificationUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException(specificationNotFoundMsg));

        mockMvc.perform(put(PATH + "/{prodId}/specifications/{specId}", validId, invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSpecRequest)))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(specificationNotFoundMsg))
                .andExpect(jsonPath("$.path").value(path));
    }

    @Test
    void shouldReturnCustomErrorAndBadRequestWhenUpdatingSpecificationWithNullValueString() throws Exception {
        String path = PATH + "/" + validId + "/specifications/" + validId;
        when(prodSpecService.update(eq(validId), eq(validId), any(ProductSpecificationUpdateDTO.class)))
                .thenThrow(new BusinessException(nullValueStringMsg));

        mockMvc.perform(put(PATH + "/{prodId}/specifications/{specId}", validId, validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSpecRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(nullValueStringMsg))
                .andExpect(jsonPath("$.path").value(path));
    }

    @Test
    void shouldReturnCustomErrorAndBadRequestWhenUpdatingSpecificationWithNullValueNumber() throws Exception {
        String path = PATH + "/" + validId + "/specifications/" + validId;
        when(prodSpecService.update(eq(validId), eq(validId), any(ProductSpecificationUpdateDTO.class)))
                .thenThrow(new BusinessException(nullValueNumberMsg));

        mockMvc.perform(put(PATH + "/{prodId}/specifications/{specId}", validId, validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSpecRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(nullValueNumberMsg))
                .andExpect(jsonPath("$.path").value(path));
    }

    @Test
    void shouldReturnCustomErrorAndBadRequestWhenUpdatingSpecificationWithNullValueBoolean() throws Exception {
        String path = PATH + "/" + validId + "/specifications/" + validId;
        when(prodSpecService.update(eq(validId), eq(validId), any(ProductSpecificationUpdateDTO.class)))
                .thenThrow(new BusinessException(nullValueBooleanMsg));

        mockMvc.perform(put(PATH + "/{prodId}/specifications/{specId}", validId, validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSpecRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(nullValueBooleanMsg))
                .andExpect(jsonPath("$.path").value(path));
    }

    @Test
    void shouldReturnValidationErrorAndBadRequestWhenUpdatingSpecificationWithTooLongValueString() throws Exception {
        ProductSpecificationUpdateDTO invalidRequest = new ProductSpecificationUpdateDTO(
                "e".repeat(46), null, null
        );

        mockMvc.perform(put(PATH + "/{prodId}/specifications/{specId}", validId, validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.errors[*].field").value(hasItem("valueString")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Value must contain a maximum of 45 characters")));
    }

    @Test
    void shouldReturnNoContentWhenDeletingSpecificationWithValidId() throws Exception {
        doNothing().when(prodSpecService).delete(validId, validId);

        mockMvc.perform(delete(PATH + "/{prodId}/specifications/{specId}", validId, validId))

                .andExpect(status().isNoContent());

        verify(prodSpecService).delete(anyLong(), anyLong());
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenDeletingSpecificationWithInvalidProductId() throws Exception {
        String path = PATH + "/" + invalidId + "/specifications/" + validId;

        doThrow(new ResourceNotFoundException(productNotFoundMsg))
                .when(prodSpecService).delete(invalidId, validId);

        mockMvc.perform(delete(PATH + "/{prodId}/specifications/{specId}", invalidId, validId))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(productNotFoundMsg))
                .andExpect(jsonPath("$.path").value(path));
    }

    @Test
    void shouldReturnCustomErrorAndBadRequestWhenDeletingSpecificationWithRequiredSpecification() throws Exception {
        String msg = "The specification is required and cannot be excluded";
        String path = PATH + "/" + validId + "/specifications/" + invalidId;

        doThrow(new BusinessException(msg))
                .when(prodSpecService).delete(validId, invalidId);

        mockMvc.perform(delete(PATH + "/{prodId}/specifications/{specId}", validId, invalidId))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(msg))
                .andExpect(jsonPath("$.path").value(path));
    }

    @Test
    void shouldReturnCustomErrorAndNotFoundWhenDeletingSpecificationWithInvalidProductOrSpecificationId() throws Exception {
        String path = PATH + "/" + invalidId + "/specifications/" + invalidId;

        doThrow(new ResourceNotFoundException(prodSpecNotFoundMsg))
                .when(prodSpecService).delete(invalidId, invalidId);

        mockMvc.perform(delete(PATH + "/{prodId}/specifications/{specId}", invalidId, invalidId))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(prodSpecNotFoundMsg))
                .andExpect(jsonPath("$.path").value(path));
    }
}
