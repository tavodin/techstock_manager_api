package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.PurchaseAssembler;
import io.github.tavodin.techstock_manager.builder.ProductBuilder;
import io.github.tavodin.techstock_manager.dto.PurchaseDTO;
import io.github.tavodin.techstock_manager.dto.PurchaseItemRequestDTO;
import io.github.tavodin.techstock_manager.dto.PurchaseRequestDTO;
import io.github.tavodin.techstock_manager.entities.*;
import io.github.tavodin.techstock_manager.enums.MovementType;
import io.github.tavodin.techstock_manager.enums.PurchaseStatus;
import io.github.tavodin.techstock_manager.exceptions.BusinessException;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private PurchaseItemRepository itemRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private PurchaseAssembler assembler;

    @Mock
    private PagedResourcesAssembler<Purchase> pagedAssembler;

    @InjectMocks
    private PurchaseService service;

    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 2L;
    private static final String PURCHASE_NOT_FOUND_MSG = "Purchase not found";
    private static final String SUPPLIER_NOT_FOUND_MSG = "Supplier not found";
    private static final String PRODUCT_NOT_FOUND_MSG = "Product not found";

    private Purchase purchase;
    private PurchaseDTO dto;
    private Supplier supplier;
    private PurchaseRequestDTO request;
    private Product product;
    private PurchaseItem item;
    private StockMovement stockMovement;

    @BeforeEach
    void setUp() {
        supplier = new Supplier(
                "TechParts",
                "00000000000191",
                "techparts@gmail.com",
                "6137644711",
                true
        );
        supplier.setId(VALID_ID);

        product = ProductBuilder
                .builder()
                .withCostPrice(BigDecimal.ZERO)
                .withQuantityInStock(0)
                .build();

        purchase = new Purchase();
        purchase.setId(VALID_ID);
        purchase.setPurchaseDate(LocalDate.of(2026, 07, 25));
        purchase.setStatus(PurchaseStatus.OPEN);
        purchase.setTotalAmount(BigDecimal.valueOf(235.22));
        purchase.setSupplier(supplier);

        request = new PurchaseRequestDTO();
        request.setPurchaseDate(purchase.getPurchaseDate());
        request.setSupplierId(supplier.getId());
        request.setItems(List.of(new PurchaseItemRequestDTO(
                product.getId(),
                2,
                BigDecimal.valueOf(799.99)
        )));

        item = new PurchaseItem(
                VALID_ID,
                2,
                BigDecimal.valueOf(799.99),
                BigDecimal.valueOf(1599.98),
                product,
                purchase
        );
        purchase.setPurchaseItems(Set.of(item));

        dto = new PurchaseDTO(
                purchase.getId(),
                purchase.getPurchaseDate(),
                purchase.getStatus(),
                purchase.getTotalAmount()
        );

        stockMovement = new StockMovement();
        stockMovement.setQuantity(item.getQuantity());
        stockMovement.setProduct(product);
        stockMovement.setMovementDate(LocalDateTime.now());
        stockMovement.setType(MovementType.PURCHASE);
        stockMovement.setReason("Purchase product");
    }

    @Test
    void shouldReturnProductDTOWhenFindingWithValidId() {
        when(purchaseRepository.findById(VALID_ID)).thenReturn(Optional.of(purchase));
        when(assembler.toModel(purchase)).thenReturn(dto);

        PurchaseDTO actual = service.findById(VALID_ID);

        assertEquals(dto.getId(), actual.getId());
        assertEquals(dto.getStatus(), actual.getStatus());
        assertEquals(dto.getPurchaseDate(), actual.getPurchaseDate());
        assertTrue(actual.getTotalAmount().compareTo(dto.getTotalAmount()) == 0);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenFindingWithInvalidId() {
        when(purchaseRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.findById(INVALID_ID));

        assertEquals(PURCHASE_NOT_FOUND_MSG, actual.getMessage());

        verify(assembler, never()).toModel(any(Purchase.class));
    }

    @Test
    void shouldReturnPagedModelWhenFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Purchase> page = new PageImpl<>(List.of(purchase));
        PagedModel<PurchaseDTO> pagedModel = mock(PagedModel.class);

        when(purchaseRepository.findAll(pageable)).thenReturn(page);
        when(pagedAssembler.toModel(page, assembler)).thenReturn(pagedModel);

        PagedModel<PurchaseDTO> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(pagedModel, result);

        verify(purchaseRepository).findAll(pageable);
        verify(pagedAssembler).toModel(page, assembler);
    }

    @Test
    void shouldCreatePurchaseWhenSavingWithValidData() {
        when(supplierRepository.findById(VALID_ID)).thenReturn(Optional.of(supplier));
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(itemRepository.save(any(PurchaseItem.class))).thenReturn(item);
        when(assembler.toModel(purchase)).thenReturn(dto);

        PurchaseDTO actual = service.save(request);

        assertNotNull(actual);

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(assembler).toModel(captor.capture());
        Purchase captured = captor.getValue();

        assertEquals(VALID_ID, captured.getId());
        assertEquals(request.getPurchaseDate(), captured.getPurchaseDate());
        assertEquals(PurchaseStatus.OPEN, captured.getStatus());

        BigDecimal total = BigDecimal.valueOf(799.99);
        total = total.multiply(BigDecimal.valueOf(2));
        assertTrue(captured.getTotalAmount().compareTo(total) == 0);

        assertEquals(supplier, captured.getSupplier());
    }

    @Test
    void shouldCreatePurchaseItemWhenSavingWithValidData() {
        when(supplierRepository.findById(VALID_ID)).thenReturn(Optional.of(supplier));
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(itemRepository.save(any(PurchaseItem.class))).thenReturn(item);
        when(assembler.toModel(purchase)).thenReturn(dto);

        PurchaseDTO actual = service.save(request);

        assertNotNull(actual);

        ArgumentCaptor<PurchaseItem> captor = ArgumentCaptor.forClass(PurchaseItem.class);
        verify(itemRepository).save(captor.capture());
        PurchaseItem captured = captor.getValue();

        PurchaseItemRequestDTO requestItem = request.getItems().getFirst();
        assertEquals(requestItem.quantity(), captured.getQuantity());
        assertTrue(captured.getUnitCost().compareTo(requestItem.unitCost()) == 0);
        assertEquals(product, captured.getProduct());

        BigDecimal total = BigDecimal.valueOf(799.99);
        total = total.multiply(BigDecimal.valueOf(2));
        assertTrue(captured.getSubtotal().compareTo(total) == 0);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSavingWithInvalidSupplierId() {
        request.setSupplierId(INVALID_ID);
        when(supplierRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.save(request));

        assertEquals(SUPPLIER_NOT_FOUND_MSG, exception.getMessage());
        verify(purchaseRepository, never()).save(any(Purchase.class));
        verify(productRepository, never()).findById(anyLong());
        verify(itemRepository, never()).save(any(PurchaseItem.class));
        verify(assembler, never()).toModel(any(Purchase.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSavingWithInvalidProductId() {
        request.setItems(List.of(new PurchaseItemRequestDTO(
                INVALID_ID,
                2,
                BigDecimal.valueOf(799.99)
        )));
        when(supplierRepository.findById(VALID_ID)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.save(request));

        assertEquals(PRODUCT_NOT_FOUND_MSG, exception.getMessage());
        verify(itemRepository, never()).save(any(PurchaseItem.class));
        verify(assembler, never()).toModel(any(Purchase.class));
    }

    @Test
    void shouldChangeProductWhenCompletePurchaseWithValidId() {
        when(purchaseRepository.getPurchaseAndItems(VALID_ID)).thenReturn(Optional.of(purchase));
        when(productRepository.findAllById(List.of(product.getId()))).thenReturn(List.of(product));

        product.setCostPrice(item.getUnitCost());
        product.setQuantityInStock(item.getQuantity());
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(stockMovement);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        when(assembler.toModel(any(Purchase.class))).thenReturn(dto);

        service.completedPurchase(VALID_ID);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product captured = captor.getValue();

        assertTrue(captured.getCostPrice().compareTo(item.getUnitCost()) == 0);
    }

    @Test
    void shouldChangePurchaseStatusToCompleteWhenCompletePurchaseWithValidId() {
        when(purchaseRepository.getPurchaseAndItems(VALID_ID)).thenReturn(Optional.of(purchase));
        when(productRepository.findAllById(List.of(product.getId()))).thenReturn(List.of(product));

        product.setCostPrice(item.getUnitCost());
        product.setQuantityInStock(item.getQuantity());
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(stockMovement);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        when(assembler.toModel(any(Purchase.class))).thenReturn(dto);

        service.completedPurchase(VALID_ID);

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(captor.capture());
        Purchase captured = captor.getValue();

        assertEquals(PurchaseStatus.COMPLETED, captured.getStatus());
    }

    @Test
    void shouldCreateStockMovementWhenCompletePurchaseWithValidId() {
        when(purchaseRepository.getPurchaseAndItems(VALID_ID)).thenReturn(Optional.of(purchase));
        when(productRepository.findAllById(List.of(product.getId()))).thenReturn(List.of(product));

        product.setCostPrice(item.getUnitCost());
        product.setQuantityInStock(item.getQuantity());
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(stockMovement);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        when(assembler.toModel(any(Purchase.class))).thenReturn(dto);

        service.completedPurchase(VALID_ID);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovement captured = captor.getValue();

        assertEquals(item.getQuantity(), captured.getQuantity());
        assertEquals(product, captured.getProduct());
        assertNotNull(captured.getMovementDate());
        assertEquals(MovementType.PURCHASE, captured.getType());
        assertEquals("Purchase product", captured.getReason());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenCompletingPurchaseWithInvalidPurchaseId() {
        when(purchaseRepository.getPurchaseAndItems(INVALID_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.completedPurchase(INVALID_ID));

        assertEquals(PURCHASE_NOT_FOUND_MSG, actual.getMessage());

        verify(productRepository, never()).findAllById(anyCollection());
        verify(productRepository, never()).save(any(Product.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
        verify(purchaseRepository, never()).save(any(Purchase.class));
        verify(assembler, never()).toModel(any(Purchase.class));
    }

    @Test
    void shouldThrowBusinessExceptionWhenCompletingPurchaseWithCompletedPurchaseStatus() {
        purchase.setStatus(PurchaseStatus.COMPLETED);
        when(purchaseRepository.getPurchaseAndItems(VALID_ID)).thenReturn(Optional.of(purchase));

        BusinessException actual = assertThrows(BusinessException.class, () ->
                service.completedPurchase(VALID_ID));

        assertEquals("The purchase status must be 'OPEN'", actual.getMessage());

        verify(productRepository, never()).findAllById(anyCollection());
        verify(productRepository, never()).save(any(Product.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
        verify(purchaseRepository, never()).save(any(Purchase.class));
        verify(assembler, never()).toModel(any(Purchase.class));
    }

    @Test
    void shouldThrowBusinessExceptionWhenCompletingPurchaseWithCanceledPurchaseStatus() {
        purchase.setStatus(PurchaseStatus.CANCELED);
        when(purchaseRepository.getPurchaseAndItems(VALID_ID)).thenReturn(Optional.of(purchase));

        BusinessException actual = assertThrows(BusinessException.class, () ->
                service.completedPurchase(VALID_ID));

        assertEquals("The purchase status must be 'OPEN'", actual.getMessage());

        verify(productRepository, never()).findAllById(anyCollection());
        verify(productRepository, never()).save(any(Product.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
        verify(purchaseRepository, never()).save(any(Purchase.class));
        verify(assembler, never()).toModel(any(Purchase.class));
    }

    @Test
    void shouldChangeStatusToCanceledWhenCancelingWithValidId() {
        when(purchaseRepository.findById(VALID_ID)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        when(assembler.toModel(purchase)).thenReturn(dto);

        PurchaseDTO actual = service.canceledPurchase(VALID_ID);

        assertNotNull(actual);

        ArgumentCaptor<Purchase> argumentCaptor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(argumentCaptor.capture());
        Purchase captured = argumentCaptor.getValue();

        assertEquals(PurchaseStatus.CANCELED, captured.getStatus());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenCancelingWithInvalidId() {
        when(purchaseRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.canceledPurchase(INVALID_ID));

        assertEquals(PURCHASE_NOT_FOUND_MSG, actual.getMessage());
    }

    @Test
    void shouldThrowBusinessExceptionWhenCancelingWithCanceledStatus() {
        purchase.setStatus(PurchaseStatus.CANCELED);
        when(purchaseRepository.findById(INVALID_ID)).thenReturn(Optional.of(purchase));

        BusinessException actual = assertThrows(BusinessException.class, () ->
                service.canceledPurchase(INVALID_ID));

        assertEquals("The purchase status must be 'OPEN'", actual.getMessage());
    }

    @Test
    void shouldThrowBusinessExceptionWhenCancelingWithCompletedStatus() {
        purchase.setStatus(PurchaseStatus.COMPLETED);
        when(purchaseRepository.findById(INVALID_ID)).thenReturn(Optional.of(purchase));

        BusinessException actual = assertThrows(BusinessException.class, () ->
                service.canceledPurchase(INVALID_ID));

        assertEquals("The purchase status must be 'OPEN'", actual.getMessage());
    }
}
