package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.ProductAssembler;
import io.github.tavodin.techstock_manager.builder.ProductBuilder;
import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.dto.product.*;
import io.github.tavodin.techstock_manager.entities.*;
import io.github.tavodin.techstock_manager.enums.SpecificationType;
import io.github.tavodin.techstock_manager.exceptions.AlreadyExistsException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SpecificationRepository specificationRepository;

    @Mock
    private ProductSpecificationRepository prodSpecRepository;

    @Mock
    private ProductAssembler assembler;

    @Mock
    private PagedResourcesAssembler<Product> pagedAssembler;

    @InjectMocks
    private ProductService service;

    private Long validId = 1L;
    private Long invalidId = 2L;
    private Set<Long> validCategoryIds = Set.of(validId);
    private List<Long> validSpecificationIds = List.of(validId);

    private String prodNotFoundMsg = "Product not found";
    private String brandNotFoundMsg = "Brand not found";
    private String categoryNotFoundMsg = "One or more categories were not found";
    private String specificationNotFoundMsg = "One or more specifications were not found";
    private String existsSkuMsg = "SKU already exists";

    private Product product;
    private Brand brand = new Brand();
    private Category category = new Category();
    private Specification specification;
    private ProductRequestDTO requestDTO = new ProductRequestDTO();
    private ProductDTO dto;
    private ProductSpecification productSpecification = new ProductSpecification();

    @BeforeEach
    void setUp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 06, 9, 16, 45);
        LocalDateTime updatedAt = createdAt.plusHours(1L);

        product = ProductBuilder.builder().build();
        product.setBrand(new Brand(validId + 1, brand.getName(), null, null));
        product.setCategories(Set.of(new Category(validId + 1, category.getName(), null, null)));

        brand.setId(validId);
        brand.setName("DELL");
        brand.setCreatedAt(createdAt);
        brand.setUpdatedAt(updatedAt);

        category.setId(validId);
        category.setName("Monitor");

        specification = SpecificationBuilder
                .builder()
                .withName("Resolução")
                .withSpecificationType(SpecificationType.STRING)
                .build();

        requestDTO.setName(product.getName());
        requestDTO.setSalePrice(product.getSalePrice());
        requestDTO.setDescription(product.getDescription());
        requestDTO.setMinimumStock(product.getMinimumStock());
        requestDTO.setActive(true);
        requestDTO.setSku(product.getSku());
        requestDTO.setBrandId(validId);
        requestDTO.setCategoryIds(validCategoryIds);
        requestDTO.setSpecifications(List.of(
                new ProductSpecificationSaveDTO(
                        validId,
                        "1920x1080",
                        null,
                        null
                )
        ));

        dto = new ProductDTO(product);

        productSpecification.setId(validId);
        productSpecification.setProduct(product);
        productSpecification.setSpecification(specification);
        productSpecification.setValueString("1920x1080");
    }

    @Test
    void shouldReturnProductDTOWhenFindingWithValidId() {
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(assembler.toModel(product)).thenReturn(dto);

        ProductDTO actual = service.findById(validId);

        assertNotNull(actual);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenFindingWithInvalidId() {
        when(productRepository.findById(invalidId)).thenThrow(new ResourceNotFoundException(prodNotFoundMsg));

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.findById(invalidId)
        );

        assertEquals(prodNotFoundMsg, actual.getMessage());

        verify(assembler, never()).toModel(any(Product.class));
    }

    @Test
    void shouldReturnProductLoadDTOWhenLoadProductByIdWithValidId() {
        when(productRepository.getProductToLoad(validId)).thenReturn(Optional.of(product));

        ProductLoadDTO result = service.loadProductById(validId);

        assertEquals(product.getId(), result.getId());
        assertEquals(product.getName(), result.getName());
        assertEquals(product.getMinimumStock(), result.getMinimumStock());
        assertEquals(product.getSku(), result.getSku());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenLoadProductByIdWithInvalidId() {
        when(productRepository.getProductToLoad(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException result = assertThrows(ResourceNotFoundException.class, () ->
                service.loadProductById(invalidId)
        );

        assertEquals(prodNotFoundMsg, result.getMessage());
    }

    @Test
    void shouldReturnProductDTOPagedModelWhenFindAll() {
        PagedModel<ProductDTO> pagedModel = mock(PagedModel.class);
        Pageable pageable = PageRequest.of(0, 3);
        Page<Product> page = new PageImpl<>(
                List.of(product),
                pageable,
                1
        );

        when(productRepository.getAll(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                pageable
        )).thenReturn(page);

        when(pagedAssembler.toModel(eq(page), any(ProductAssembler.class))).thenReturn(pagedModel);

        PagedModel<ProductDTO> result = service.findAll(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                pageable
        );

        assertNotNull(result);

        verify(productRepository).getAll(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                pageable
        );
    }

    @Test
    void shouldTrimNameWhenFindAllWithNameWithSpaces() {
        PagedModel<ProductDTO> pagedModel = mock(PagedModel.class);
        Pageable pageable = PageRequest.of(0, 3);
        Page<Product> page = new PageImpl<>(
                List.of(product),
                pageable,
                1
        );

        when(productRepository.getAll(
                product.getName(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                pageable
        )).thenReturn(page);

        when(pagedAssembler.toModel(eq(page), any(ProductAssembler.class))).thenReturn(pagedModel);

        PagedModel<ProductDTO> result = service.findAll(
                "  " + product.getName() + "  ",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                pageable
        );

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(productRepository).getAll(
                captor.capture(),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(pageable)
        );

        assertEquals(product.getName(), captor.getValue());
    }

    @Test
    void shouldReturnNullWhenFindAllWithBlankName() {
        PagedModel<ProductDTO> pagedModel = mock(PagedModel.class);
        Pageable pageable = PageRequest.of(0, 3);
        Page<Product> page = new PageImpl<>(
                List.of(product),
                pageable,
                1
        );

        when(productRepository.getAll(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                pageable
        )).thenReturn(page);

        when(pagedAssembler.toModel(eq(page), any(ProductAssembler.class))).thenReturn(pagedModel);

        PagedModel<ProductDTO> result = service.findAll(
                "  ",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                pageable
        );

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(productRepository).getAll(
                captor.capture(),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(pageable)
        );

        assertNull(captor.getValue());
    }

    @Test
    void shouldReturnProductAutocompleteDTOWhenGetAllAutocomplete() {
        ProductAutocompleteDTO autocompleteDTO = new ProductAutocompleteDTO(
                product.getId(),
                product.getName(),
                product.getSalePrice(),
                product.getQuantityInStock()
        );
        Pageable pageable = PageRequest.of(0, 5);

        when(productRepository.getAllAutocomplete(null, pageable)).thenReturn(List.of(autocompleteDTO));

        List<ProductAutocompleteDTO> result = service.getAllAutocomplete(null);

        assertNotNull(result);
        assertEquals(autocompleteDTO.id(), result.getFirst().id());
        assertEquals(autocompleteDTO.name(), result.getFirst().name());
        assertEquals(autocompleteDTO.salePrice(), result.getFirst().salePrice());
        assertEquals(autocompleteDTO.quantityInStock(), result.getFirst().quantityInStock());
    }

    @Test
    void shouldTrimNameWhenGetAllAutocompleteWithNameWithSpaces() {
        ProductAutocompleteDTO autocompleteDTO = new ProductAutocompleteDTO(
                product.getId(),
                product.getName(),
                product.getSalePrice(),
                product.getQuantityInStock()
        );
        Pageable pageable = PageRequest.of(0, 5);

        when(productRepository.getAllAutocomplete(product.getName(), pageable)).thenReturn(List.of(autocompleteDTO));

        service.getAllAutocomplete("  " + product.getName() + "  ");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(productRepository).getAllAutocomplete(captor.capture(), eq(pageable));

        assertEquals(product.getName(), captor.getValue());
    }

    @Test
    void shouldReturnNullWhenGetAllAutocompleteWithBlankName() {
        ProductAutocompleteDTO autocompleteDTO = new ProductAutocompleteDTO(
                product.getId(),
                product.getName(),
                product.getSalePrice(),
                product.getQuantityInStock()
        );
        Pageable pageable = PageRequest.of(0, 5);

        when(productRepository.getAllAutocomplete(null, pageable)).thenReturn(List.of(autocompleteDTO));

        service.getAllAutocomplete("  ");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(productRepository).getAllAutocomplete(captor.capture(), eq(pageable));

        assertNull(captor.getValue());
    }

    @Test
    void shouldCreateProductEntityWhenSavingWithValidId() {
        when(brandRepository.findById(validId)).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(validCategoryIds)).thenReturn(List.of(category));
        when(productRepository.existsBySku(requestDTO.getSku())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(specificationRepository.getSpecificationsByIds(validSpecificationIds)).thenReturn(List.of(specification));
        when(prodSpecRepository.saveAll(any(List.class))).thenReturn(List.of(productSpecification));
        when(assembler.toModel(any(Product.class))).thenReturn(dto);

        ProductDTO actual = service.save(requestDTO);

        assertNotNull(actual);

        ArgumentCaptor<Product> captor =
                ArgumentCaptor.forClass(Product.class);

        verify(productRepository).save(captor.capture());

        Product captured = captor.getValue();

        assertEquals(requestDTO.getName(), captured.getName());
        assertTrue(captured.getCostPrice().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(captured.getSalePrice().compareTo(requestDTO.getSalePrice()) == 0);
        assertEquals(requestDTO.getDescription(), captured.getDescription());
        assertEquals(requestDTO.getSku(), captured.getSku());
        assertTrue(captured.getQuantityInStock() == 0);
        assertEquals(requestDTO.getMinimumStock(), captured.getMinimumStock());
        assertEquals(true, captured.getActive());

        assertEquals(brand.getName(), captured.getBrand().getName());
        assertTrue(captured.getCategories().contains(category));
    }

    @Test
    void shouldCreateSpecificationWhenSavingWithValidData() {
        when(brandRepository.findById(validId)).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(validCategoryIds)).thenReturn(List.of(category));
        when(productRepository.existsBySku(requestDTO.getSku())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(specificationRepository.getSpecificationsByIds(validSpecificationIds)).thenReturn(List.of(specification));
        when(prodSpecRepository.saveAll(any(List.class))).thenReturn(List.of(productSpecification));
        when(assembler.toModel(any(Product.class))).thenReturn(dto);

        ProductDTO actual = service.save(requestDTO);

        assertNotNull(actual);

        ArgumentCaptor<List> captor =
                ArgumentCaptor.forClass(List.class);

        verify(prodSpecRepository).saveAll(captor.capture());

        List<ProductSpecification> captured = captor.getValue();

        assertTrue(captured.stream()
                .anyMatch(ps ->
                        ps.getSpecification().getName().equals(specification.getName())
                        && ps.getSpecification().getId().equals(specification.getId())
                )
        );

        assertTrue(captured.stream()
                .anyMatch(ps ->
                        ps.getProduct().getId().equals(product.getId())
                        && ps.getProduct().getName().equals(product.getName())
                )
        );

        assertTrue(captured.stream()
                .anyMatch(ps ->
                        ps.getValueString().equals("1920x1080")
                )
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSavingWithInvalidBrandId() {
        requestDTO.setBrandId(invalidId);
        when(brandRepository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
            service.save(requestDTO)
        );

        assertEquals(brandNotFoundMsg, actual.getMessage());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSavingWithInvalidCategoryId() {
        requestDTO.setCategoryIds(Set.of(invalidId));

        when(brandRepository.findById(validId)).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(Set.of(invalidId))).thenReturn(List.of());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.save(requestDTO)
        );

        assertEquals(categoryNotFoundMsg, actual.getMessage());
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenSavingWithExistsSKU() {
        when(brandRepository.findById(validId)).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(validCategoryIds)).thenReturn(List.of(category));
        when(productRepository.existsBySku(requestDTO.getSku())).thenReturn(true);

        AlreadyExistsException actual = assertThrows(AlreadyExistsException.class, () ->
                service.save(requestDTO)
        );

        assertEquals(existsSkuMsg, actual.getMessage());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSavingWithInvalidSpecificationId() {
        when(brandRepository.findById(validId)).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(validCategoryIds)).thenReturn(List.of(category));
        when(productRepository.existsBySku(requestDTO.getSku())).thenReturn(false);
        when(specificationRepository.getSpecificationsByIds(List.of(validId))).thenReturn(List.of());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.save(requestDTO)
        );

        assertEquals(specificationNotFoundMsg, actual.getMessage());
    }

    @Test
    void shouldSetValueNumberWhenSpecificationWithStringType() {
        when(brandRepository.findById(validId)).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(validCategoryIds)).thenReturn(List.of(category));
        when(productRepository.existsBySku(requestDTO.getSku())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(specificationRepository.getSpecificationsByIds(validSpecificationIds)).thenReturn(List.of(specification));
        when(prodSpecRepository.saveAll(any(List.class))).thenReturn(List.of(productSpecification));
        when(assembler.toModel(any(Product.class))).thenReturn(dto);

        service.save(requestDTO);

        ArgumentCaptor<List> captor =
                ArgumentCaptor.forClass(List.class);

        verify(prodSpecRepository).saveAll(captor.capture());

        List<ProductSpecification> captured = captor.getValue();
        ProductSpecification actual = captured.getFirst();

        assertEquals("1920x1080", actual.getValueString());
        assertNull(actual.getValueNumber());
        assertNull(actual.getValueBoolean());
    }

    @Test
    void shouldSetValueNumberWhenSpecificationWithNumberType() {
        Specification spec = SpecificationBuilder
                .builder()
                .withName("Frequência")
                .withSpecificationType(SpecificationType.NUMBER)
                .build();

        ProductSpecificationSaveDTO prodSpec = new ProductSpecificationSaveDTO(
                spec.getId(),
                null,
                60.0,
                null
        );

        requestDTO.setSpecifications(List.of(prodSpec));

        when(brandRepository.findById(validId)).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(validCategoryIds)).thenReturn(List.of(category));
        when(productRepository.existsBySku(requestDTO.getSku())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(specificationRepository.getSpecificationsByIds(validSpecificationIds)).thenReturn(List.of(spec));
        when(prodSpecRepository.saveAll(any(List.class))).thenReturn(List.of(productSpecification));
        when(assembler.toModel(any(Product.class))).thenReturn(dto);

        service.save(requestDTO);

        ArgumentCaptor<List> captor =
                ArgumentCaptor.forClass(List.class);

        verify(prodSpecRepository).saveAll(captor.capture());

        List<ProductSpecification> captured = captor.getValue();
        ProductSpecification actual = captured.getFirst();

        assertEquals(prodSpec.valueNumber(), actual.getValueNumber());
        assertNull(actual.getValueString());
        assertNull(actual.getValueBoolean());
    }

    @Test
    void shouldSetValueNumberWhenSpecificationWithBooleanType() {
        Specification spec = SpecificationBuilder
                .builder()
                .withName("Gamer")
                .withSpecificationType(SpecificationType.BOOLEAN)
                .build();

        ProductSpecificationSaveDTO prodSpec = new ProductSpecificationSaveDTO(
                spec.getId(),
                null,
                null,
                false
        );

        requestDTO.setSpecifications(List.of(prodSpec));

        when(brandRepository.findById(validId)).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(validCategoryIds)).thenReturn(List.of(category));
        when(productRepository.existsBySku(requestDTO.getSku())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(specificationRepository.getSpecificationsByIds(validSpecificationIds)).thenReturn(List.of(spec));
        when(prodSpecRepository.saveAll(any(List.class))).thenReturn(List.of(productSpecification));
        when(assembler.toModel(any(Product.class))).thenReturn(dto);

        service.save(requestDTO);

        ArgumentCaptor<List> captor =
                ArgumentCaptor.forClass(List.class);

        verify(prodSpecRepository).saveAll(captor.capture());

        List<ProductSpecification> captured = captor.getValue();
        ProductSpecification actual = captured.getFirst();

        assertEquals(prodSpec.valueBoolean(), actual.getValueBoolean());
        assertNull(actual.getValueString());
        assertNull(actual.getValueNumber());
    }

    @Test
    void shouldUpdateProductWhenUpdatingWithValidData() {
        requestDTO.setName("Monitor Gamer Samsung 24 polegadas");
        requestDTO.setSalePrice(BigDecimal.valueOf(2000.1));
        requestDTO.setMinimumStock(2);

        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot(requestDTO.getSku(), validId)).thenReturn(false);
        when(brandRepository.findById(requestDTO.getBrandId())).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(requestDTO.getCategoryIds())).thenReturn(List.of(category));
        when(productRepository.saveAndFlush(any(Product.class))).thenReturn(product);
        when(specificationRepository.getSpecificationsByIds(validSpecificationIds)).thenReturn(List.of(specification));
        when(prodSpecRepository.saveAll(any(List.class))).thenReturn(List.of(productSpecification));
        when(assembler.toModel(any(Product.class))).thenReturn(dto);

        ProductDTO actual = service.update(validId, requestDTO);

        assertNotNull(actual);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).saveAndFlush(captor.capture());
        Product captured = captor.getValue();

        assertEquals(captured.getName(), requestDTO.getName());
        assertTrue(captured.getSalePrice().compareTo(requestDTO.getSalePrice()) == 0);
        assertEquals(captured.getMinimumStock(), requestDTO.getMinimumStock());
    }

    @Test
    void shouldCreateSpecificationWhenUpdatingWithValidData() {
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot(requestDTO.getSku(), validId)).thenReturn(false);
        when(brandRepository.findById(requestDTO.getBrandId())).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(requestDTO.getCategoryIds())).thenReturn(List.of(category));
        when(productRepository.saveAndFlush(any(Product.class))).thenReturn(product);
        when(specificationRepository.getSpecificationsByIds(validSpecificationIds)).thenReturn(List.of(specification));
        when(prodSpecRepository.saveAll(any(List.class))).thenReturn(List.of(productSpecification));
        when(assembler.toModel(any(Product.class))).thenReturn(dto);

        ProductDTO actual = service.update(validId, requestDTO);

        assertNotNull(actual);

        ArgumentCaptor<List> captor =
                ArgumentCaptor.forClass(List.class);

        verify(prodSpecRepository).saveAll(captor.capture());

        List<ProductSpecification> captured = captor.getValue();

        assertTrue(captured.stream()
                .anyMatch(ps ->
                        ps.getSpecification().getName().equals(specification.getName())
                                && ps.getSpecification().getId().equals(specification.getId())
                )
        );

        assertTrue(captured.stream()
                .anyMatch(ps ->
                        ps.getProduct().getId().equals(product.getId())
                                && ps.getProduct().getName().equals(product.getName())
                )
        );

        assertTrue(captured.stream()
                .anyMatch(ps ->
                        ps.getValueString().equals("1920x1080")
                )
        );
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingWithInvalidId() {
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.update(invalidId, requestDTO));

        assertEquals(prodNotFoundMsg, actual.getMessage());
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenUpdatingWithExistsSku() {
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot(requestDTO.getSku(), validId)).thenReturn(true);

        AlreadyExistsException actual = assertThrows(AlreadyExistsException.class, () ->
                service.update(validId, requestDTO));

        assertEquals(existsSkuMsg, actual.getMessage());
    }

    @Test
    void shouldCallBrandFindByIdWhenUpdatingWithValidBrandId() {
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot(requestDTO.getSku(), validId)).thenReturn(false);
        when(brandRepository.findById(requestDTO.getBrandId())).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(requestDTO.getCategoryIds())).thenReturn(List.of(category));
        when(productRepository.saveAndFlush(any(Product.class))).thenReturn(product);
        when(specificationRepository.getSpecificationsByIds(validSpecificationIds)).thenReturn(List.of(specification));
        when(prodSpecRepository.saveAll(any(List.class))).thenReturn(List.of(productSpecification));
        when(assembler.toModel(any(Product.class))).thenReturn(dto);

        service.update(validId, requestDTO);

        verify(brandRepository).findById(requestDTO.getBrandId());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingWithInvalidBrandId() {
        requestDTO.setBrandId(3L);

        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot(requestDTO.getSku(), validId)).thenReturn(false);
        when(brandRepository.findById(3L)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.update(validId, requestDTO));

        assertEquals(brandNotFoundMsg, actual.getMessage());
    }

    @Test
    void shouldNotCallCategoryFindAllByIdWhenUpdatingWithSameCategories() {
        product.setCategories(Set.of(category));
        requestDTO.setCategoryIds(Set.of(category.getId()));

        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot(requestDTO.getSku(), validId)).thenReturn(false);
        when(brandRepository.findById(requestDTO.getBrandId())).thenReturn(Optional.of(brand));
        when(productRepository.saveAndFlush(any(Product.class))).thenReturn(product);
        when(specificationRepository.getSpecificationsByIds(validSpecificationIds)).thenReturn(List.of(specification));
        when(prodSpecRepository.saveAll(any(List.class))).thenReturn(List.of(productSpecification));
        when(assembler.toModel(any(Product.class))).thenReturn(dto);

        service.update(validId, requestDTO);

        verify(categoryRepository, never()).findAllById(any(Set.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingWithInvalidCategoryIds() {
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot(requestDTO.getSku(), validId)).thenReturn(false);
        when(brandRepository.findById(requestDTO.getBrandId())).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(requestDTO.getCategoryIds())).thenReturn(List.of());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.update(validId, requestDTO));

        assertEquals(categoryNotFoundMsg, actual.getMessage());
    }

    @Test
    void shouldChangeActiveToFalseWhenDeletingWithValidId() {
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        service.delete(validId);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product captured = captor.getValue();

        assertEquals(false, captured.getActive());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingWithInvalidId() {
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.delete(invalidId));

        assertEquals(prodNotFoundMsg, actual.getMessage());
    }
}
