package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.SaleAssembler;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final SaleAssembler assembler;
    private final PagedResourcesAssembler<Sale> pagedAssembler;

    public SaleService(SaleRepository saleRepository, SaleItemRepository saleItemRepository, ProductRepository productRepository, StockMovementRepository stockMovementRepository, SaleAssembler assembler, PagedResourcesAssembler<Sale> pagedAssembler) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @Transactional(readOnly = true)
    public SaleDTO findById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));

        return assembler.toModel(sale);
    }

    @Transactional(readOnly = true)
    public PagedModel<SaleDTO> findAll(Pageable pageable) {
        Page<Sale> page = saleRepository.findAll(pageable);
        return pagedAssembler.toModel(page, assembler);
    }

    @Transactional
    public SaleDTO save(SaleRequestDTO request) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        Sale sale = new Sale();
        sale.setSaleDate(LocalDateTime.now());
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setStatus(SaleStatus.COMPLETED);
        sale.setTotalAmount(totalAmount);

        Set<Long> productIds = request.getItems()
                .stream()
                .map(SaleItemRequestDTO::productId)
                .collect(Collectors.toSet());

        List<Product> products = getProductsOrThrowException(productIds);

        sale = saleRepository.save(sale);

        List<StockMovement> stockMovements = new ArrayList<>();

        for(SaleItemRequestDTO item : request.getItems()) {
            Product findProduct = products
                    .stream()
                    .filter(product -> item.productId().equals(product.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if((findProduct.getQuantityInStock() - item.quantity()) < 0) {
                throw new BusinessException("Insufficient stock quantity");
            }

            SaleItem saleItem = new SaleItem();
            saleItem.setQuantity(item.quantity());
            saleItem.setProduct(findProduct);
            saleItem.setSale(sale);
            saleItem.setUnitPrice(findProduct.getSalePrice());

            BigDecimal subTotal = findProduct.getSalePrice()
                    .multiply(BigDecimal.valueOf(saleItem.getQuantity()));
            saleItem.setSubtotal(subTotal);

            saleItemRepository.save(saleItem);

            totalAmount = totalAmount.add(subTotal);

            findProduct.setQuantityInStock(findProduct.getQuantityInStock() - item.quantity());
            productRepository.save(findProduct);

            StockMovement stockMovement = new StockMovement();
            stockMovement.setMovementDate(LocalDateTime.now());
            stockMovement.setType(MovementType.SALE);
            stockMovement.setReason("Sale Product");
            stockMovement.setQuantity(item.quantity());
            stockMovement.setProduct(findProduct);

            stockMovements.add(stockMovement);
        }

        stockMovementRepository.saveAll(stockMovements);

        sale.setTotalAmount(totalAmount);
        sale = saleRepository.save(sale);

        return assembler.toModel(sale);
    }

    @Transactional
    public SaleDTO canceled(Long id) {
        Sale sale = saleRepository.getSaleAndItemsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));

        sale.setStatus(SaleStatus.CANCELED);
        saleRepository.save(sale);

        Set<SaleItem> saleItems = sale.getSaleItems();

        for (SaleItem item : saleItems) {
            Product product = item.getProduct();
            int quantity = product.getQuantityInStock() + item.getQuantity();
            product.setQuantityInStock(quantity);

            productRepository.save(product);

            StockMovement stockMovement = new StockMovement();
            stockMovement.setMovementDate(LocalDateTime.now());
            stockMovement.setType(MovementType.SALE_RETURN);
            stockMovement.setReason("Product return");
            stockMovement.setQuantity(item.getQuantity());
            stockMovement.setProduct(product);

            stockMovementRepository.save(stockMovement);
        }

        return assembler.toModel(sale);
    }

    private List<Product> getProductsOrThrowException(Set<Long> productIds) {
        List<Product> products = productRepository.findAllById(productIds);

        if(productIds.size() != products.size()) {
            throw new ResourceNotFoundException("One or more products were not found");
        }

        return products;
    }
}
