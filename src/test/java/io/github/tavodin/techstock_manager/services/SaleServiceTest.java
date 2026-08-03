package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.SaleAssembler;
import io.github.tavodin.techstock_manager.builder.ProductBuilder;
import io.github.tavodin.techstock_manager.dto.SaleDTO;
import io.github.tavodin.techstock_manager.dto.SaleItemRequestDTO;
import io.github.tavodin.techstock_manager.dto.SaleRequestDTO;
import io.github.tavodin.techstock_manager.entities.Product;
import io.github.tavodin.techstock_manager.entities.Sale;
import io.github.tavodin.techstock_manager.entities.SaleItem;
import io.github.tavodin.techstock_manager.entities.StockMovement;
import io.github.tavodin.techstock_manager.enums.MovementType;
import io.github.tavodin.techstock_manager.enums.SaleStatus;
import io.github.tavodin.techstock_manager.exceptions.BusinessException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.ProductRepository;
import io.github.tavodin.techstock_manager.repositories.SaleItemRepository;
import io.github.tavodin.techstock_manager.repositories.SaleRepository;
import io.github.tavodin.techstock_manager.repositories.StockMovementRepository;
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
public class SaleServiceTest {

    @Mock
    private SaleRepository repository;

    @Mock
    private SaleItemRepository saleItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private SaleAssembler assembler;

    @Mock
    private PagedResourcesAssembler<Sale> pagedAssembler;

    @InjectMocks
    private SaleService service;

    private final Long VALID_ID = 1L;
    private final Long INVALID_ID = 2L;
    private final String SALE_NOT_FOUND_MSG = "Sale not found";
    private final String PRODUCTS_NOT_FOUND_MSG = "One or more products were not found";
    private final String EXCEEDING_STOCK_MSG = "Insufficient stock quantity";
    private final String SALE_MSG = "Sale Product";
    private final String CANCELED_MSG = "Product return";

    private Sale sale;
    private SaleDTO saleDTO;
    private Product product;
    private SaleRequestDTO request;
    private StockMovement stockMovement;
    private SaleItemRequestDTO saleItemRequest;
    private SaleItem saleItem;

    @BeforeEach
    void setUp() {
        product = ProductBuilder.builder().build();

        saleItem = new SaleItem();
        saleItem.setId(VALID_ID);
        saleItem.setProduct(product);
        saleItem.setSale(sale);
        saleItem.setQuantity(2);
        saleItem.setUnitPrice(product.getSalePrice());
        saleItem.setSubtotal(BigDecimal.valueOf(2600.0));

        sale = new Sale();
        sale.setId(VALID_ID);
        sale.setSaleDate(LocalDateTime.of(2026, 8, 3, 13, 00));
        sale.setStatus(SaleStatus.COMPLETED);
        sale.setPaymentMethod("Débito");
        sale.setTotalAmount(BigDecimal.valueOf(250.5));
        sale.setSaleItems(Set.of(saleItem));

        saleDTO = new SaleDTO();
        saleDTO.setId(sale.getId());
        saleDTO.setSaleDate(sale.getSaleDate());
        saleDTO.setStatus(sale.getStatus());
        saleDTO.setTotalAmount(sale.getTotalAmount());
        saleDTO.setPaymentMethod(sale.getPaymentMethod());

        saleItemRequest = new SaleItemRequestDTO(
                saleItem.getQuantity(),
                VALID_ID
        );

        request = new SaleRequestDTO();
        request.setPaymentMethod(sale.getPaymentMethod());
        request.setItems(Set.of(saleItemRequest));

        stockMovement = new StockMovement();
        stockMovement.setProduct(product);
        stockMovement.setReason(SALE_MSG);
        stockMovement.setQuantity(2);
        stockMovement.setType(MovementType.SALE);
        stockMovement.setMovementDate(LocalDateTime.of(2026, 8, 03, 13, 42));
    }

    @Test
    void shouldReturnSaleDTOWhenFindingWithValid() {
        when(repository.findById(VALID_ID)).thenReturn(Optional.of(sale));
        when(assembler.toModel(sale)).thenReturn(saleDTO);

        SaleDTO actual = service.findById(VALID_ID);

        assertEquals(VALID_ID, actual.getId());
        assertEquals(saleDTO.getSaleDate(), actual.getSaleDate());
        assertEquals(saleDTO.getStatus(), actual.getStatus());
        assertEquals(saleDTO.getPaymentMethod(), actual.getPaymentMethod());
        assertEquals(saleDTO.getTotalAmount(), actual.getTotalAmount());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenFindingWithInvalidId() {
        when(repository.findById(INVALID_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.findById(INVALID_ID));

        assertEquals(SALE_NOT_FOUND_MSG, actual.getMessage());
        verify(assembler, never()).toModel(any(Sale.class));
    }

    @Test
    void shouldReturnPagedModelWhenFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Sale> page = new PageImpl<>(List.of(sale));
        PagedModel<SaleDTO> pagedModel = mock(PagedModel.class);

        when(repository.findAll(pageable)).thenReturn(page);
        when(pagedAssembler.toModel(page, assembler)).thenReturn(pagedModel);

        PagedModel<SaleDTO> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(pagedModel, result);

        verify(repository).findAll(pageable);
        verify(pagedAssembler).toModel(page, assembler);
    }

    @Test
    void shouldCreateSaleWhenSavingWithValidData() {
        when(productRepository.findAllById(Set.of(VALID_ID))).thenReturn(List.of(product));
        when(repository.save(any(Sale.class))).thenReturn(sale);
        when(saleItemRepository.save(any(SaleItem.class))).thenReturn(saleItem);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(stockMovementRepository.saveAll(any(List.class))).thenReturn(List.of(stockMovement));
        when(assembler.toModel(sale)).thenReturn(saleDTO);

        SaleDTO actual = service.save(request);

        ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
        verify(repository, times(2)).save(captor.capture());
        List<Sale> captured = captor.getAllValues();
        Sale first = captured.get(1);

        assertNotNull(actual);
        assertNotNull(first.getSaleDate());
        assertEquals(sale.getPaymentMethod(), first.getPaymentMethod());
        assertEquals(SaleStatus.COMPLETED, first.getStatus());
        assertTrue(first.getTotalAmount().compareTo(BigDecimal.valueOf(2600)) == 0);
    }

    @Test
    void shouldCreateSaleItemWhenSavingWithValidData() {
        when(productRepository.findAllById(Set.of(VALID_ID))).thenReturn(List.of(product));
        when(repository.save(any(Sale.class))).thenReturn(sale);
        when(saleItemRepository.save(any(SaleItem.class))).thenReturn(saleItem);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(stockMovementRepository.saveAll(any(List.class))).thenReturn(List.of(stockMovement));
        when(assembler.toModel(sale)).thenReturn(saleDTO);

        service.save(request);

        ArgumentCaptor<SaleItem> captor = ArgumentCaptor.forClass(SaleItem.class);
        verify(saleItemRepository).save(captor.capture());
        SaleItem captured = captor.getValue();

        assertEquals(product, captured.getProduct());
        assertEquals(saleItemRequest.quantity(), captured.getQuantity());
        assertEquals(sale, captured.getSale());
        assertTrue(captured.getUnitPrice().compareTo(product.getSalePrice()) == 0);

        BigDecimal subTotal = product.getSalePrice()
                .multiply(BigDecimal.valueOf(saleItemRequest.quantity()));
        assertTrue(captured.getSubtotal().compareTo(subTotal) == 0);
    }

    @Test
    void shouldChangeProductQuantityWhenSavingWithValidData() {
        Integer quantity = product.getQuantityInStock() - saleItem.getQuantity();

        when(productRepository.findAllById(Set.of(VALID_ID))).thenReturn(List.of(product));
        when(repository.save(any(Sale.class))).thenReturn(sale);
        when(saleItemRepository.save(any(SaleItem.class))).thenReturn(saleItem);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(stockMovementRepository.saveAll(any(List.class))).thenReturn(List.of(stockMovement));
        when(assembler.toModel(sale)).thenReturn(saleDTO);

        service.save(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product captured = captor.getValue();

        assertEquals(quantity, captured.getQuantityInStock());
    }

    @Test
    void shouldCreateStockMovementWhenSavingWithValidData() {
        when(productRepository.findAllById(Set.of(VALID_ID))).thenReturn(List.of(product));
        when(repository.save(any(Sale.class))).thenReturn(sale);
        when(saleItemRepository.save(any(SaleItem.class))).thenReturn(saleItem);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(stockMovementRepository.saveAll(any(List.class))).thenReturn(List.of(stockMovement));
        when(assembler.toModel(sale)).thenReturn(saleDTO);

        service.save(request);

        ArgumentCaptor<List<StockMovement>> captor = ArgumentCaptor.forClass(List.class);
        verify(stockMovementRepository).saveAll(captor.capture());
        List<StockMovement> captured = captor.getValue();

        StockMovement movement = captured.get(0);

        assertEquals(1, captured.size());

        assertNotNull(movement.getMovementDate());
        assertEquals(MovementType.SALE, movement.getType());
        assertEquals(SALE_MSG, movement.getReason());
        assertEquals(saleItemRequest.quantity(), movement.getQuantity());
        assertEquals(product, movement.getProduct());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSavingWithInvalidProductId() {
        SaleItemRequestDTO firstItem = new SaleItemRequestDTO(1, 1L);
        SaleItemRequestDTO secondItem = new SaleItemRequestDTO(2, 2L);

        request.setItems(Set.of(firstItem, secondItem));

        when(productRepository.findAllById(Set.of(firstItem.productId(), secondItem.productId())))
                .thenReturn(List.of(product));

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.save(request));

        assertEquals(PRODUCTS_NOT_FOUND_MSG, actual.getMessage());
        verify(repository, never()).save(any(Sale.class));
        verify(saleItemRepository, never()).save(any(SaleItem.class));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldThrowBusinessExceptionWhenSavingWithQuantityExceedingStockCapacity() {
        SaleItemRequestDTO item = new SaleItemRequestDTO(3, 1L);
        request.setItems(Set.of(item));
        product.setQuantityInStock(1);

        when(productRepository.findAllById(Set.of(item.productId())))
                .thenReturn(List.of(product));
        when(repository.save(any(Sale.class))).thenReturn(sale);

        BusinessException actual = assertThrows(BusinessException.class, () ->
                service.save(request));

        assertEquals(EXCEEDING_STOCK_MSG, actual.getMessage());
        verify(saleItemRepository, never()).save(any(SaleItem.class));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldChangeSaleStatusToCanceledWhenCancelingWithValidId() {
        when(repository.getSaleAndItemsById(VALID_ID)).thenReturn(Optional.of(sale));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(stockMovement);
        when(assembler.toModel(any(Sale.class))).thenReturn(saleDTO);

        SaleDTO actual = service.canceled(VALID_ID);

        ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
        verify(repository).save(captor.capture());
        Sale captured = captor.getValue();

        assertEquals(SaleStatus.CANCELED, captured.getStatus());
    }

    @Test
    void shouldChangeProductQuantityWhenCancelingWithValidId() {
        Integer quantity = product.getQuantityInStock() + saleItem.getQuantity();

        when(repository.getSaleAndItemsById(VALID_ID)).thenReturn(Optional.of(sale));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(stockMovement);
        when(assembler.toModel(any(Sale.class))).thenReturn(saleDTO);

        SaleDTO actual = service.canceled(VALID_ID);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product captured = captor.getValue();

        assertEquals(quantity, captured.getQuantityInStock());
    }

    @Test
    void shouldCreateStockMovementWhenCancelingWithValidId() {
        when(repository.getSaleAndItemsById(VALID_ID)).thenReturn(Optional.of(sale));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(stockMovement);
        when(assembler.toModel(any(Sale.class))).thenReturn(saleDTO);

        service.canceled(VALID_ID);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovement captured = captor.getValue();

        assertNotNull(captured.getMovementDate());
        assertEquals(MovementType.SALE_RETURN, captured.getType());
        assertEquals(CANCELED_MSG, captured.getReason());
        assertEquals(saleItem.getQuantity(), captured.getQuantity());
        assertEquals(product, captured.getProduct());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenCancelingWithInvalidId() {
        when(repository.getSaleAndItemsById(INVALID_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.canceled(INVALID_ID));

        assertEquals(SALE_NOT_FOUND_MSG, actual.getMessage());
        verify(repository, never()).save(any(Sale.class));
        verify(productRepository, never()).save(any(Product.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }
}
