package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.PurchaseAssembler;
import io.github.tavodin.techstock_manager.dto.PurchaseDTO;
import io.github.tavodin.techstock_manager.dto.PurchaseItemRequestDTO;
import io.github.tavodin.techstock_manager.dto.PurchaseRequestDTO;
import io.github.tavodin.techstock_manager.entities.*;
import io.github.tavodin.techstock_manager.enums.MovementType;
import io.github.tavodin.techstock_manager.enums.PurchaseStatus;
import io.github.tavodin.techstock_manager.exceptions.BusinessException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository itemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PurchaseAssembler assembler;
    private final PagedResourcesAssembler<Purchase> pagedAssembler;

    public PurchaseService(PurchaseRepository purchaseRepository, PurchaseItemRepository itemRepository, SupplierRepository supplierRepository, ProductRepository productRepository, StockMovementRepository stockMovementRepository, PurchaseAssembler assembler, PagedResourcesAssembler<Purchase> pagedAssembler) {
        this.purchaseRepository = purchaseRepository;
        this.itemRepository = itemRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @Transactional(readOnly = true)
    public PurchaseDTO findById(Long id) {
        Purchase purchase = getPurchaseOrThrowException(id);
        return assembler.toModel(purchase);
    }

    @Transactional(readOnly = true)
    public PagedModel<PurchaseDTO> findAll(Pageable pageable) {
        Page<Purchase> page = purchaseRepository.findAll(pageable);
        return pagedAssembler.toModel(page, assembler);
    }

    @Transactional
    public PurchaseDTO save(PurchaseRequestDTO request) {
        Supplier supplier = getSupplierOrThrowException(request.getSupplierId());

        BigDecimal totalAmount = BigDecimal.ZERO;

        Purchase purchase = new Purchase();
        purchase.setPurchaseDate(request.getPurchaseDate());
        purchase.setStatus(PurchaseStatus.OPEN);
        purchase.setSupplier(supplier);
        purchase.setTotalAmount(totalAmount);

        purchase = purchaseRepository.save(purchase);

        for(PurchaseItemRequestDTO item : request.getItems()) {
            Product product = getProductOrThrowException(item.productId());
            BigDecimal subTotal = item.unitCost().multiply(BigDecimal.valueOf(item.quantity()));

            PurchaseItem purchaseItem = new PurchaseItem();
            purchaseItem.setQuantity(item.quantity());
            purchaseItem.setUnitCost(item.unitCost());
            purchaseItem.setSubtotal(subTotal);
            purchaseItem.setProduct(product);
            purchaseItem.setPurchase(purchase);

            totalAmount = totalAmount.add(subTotal);

            itemRepository.save(purchaseItem);
        }

        purchase.setTotalAmount(totalAmount);
        return assembler.toModel(purchase);
    }

    @Transactional
    public PurchaseDTO completedPurchase(Long id) {
        Purchase purchase = getPurchaseAndItemsOrThrowException(id);

        if(purchase.getStatus() != PurchaseStatus.OPEN) {
            throw new BusinessException("The purchase status must be 'OPEN'");
        }

        purchase.setStatus(PurchaseStatus.COMPLETED);

        List<Long> productId =  purchase.getPurchaseItems()
                .stream()
                .map(PurchaseItem::getProduct)
                .map(Product::getId)
                .toList();

        List<Product> products = productRepository.findAllById(productId);

        for(Product product : products) {
            PurchaseItem purchaseItem = purchase.getPurchaseItems()
                    .stream()
                    .filter(item -> item.getProduct().equals(product))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase Item not found"));

            product.setQuantityInStock(product.getQuantityInStock() + purchaseItem.getQuantity());
            product.setCostPrice(purchaseItem.getUnitCost());

            StockMovement stockMovement = new StockMovement();
            stockMovement.setQuantity(purchaseItem.getQuantity());
            stockMovement.setProduct(product);
            stockMovement.setMovementDate(LocalDateTime.now());
            stockMovement.setType(MovementType.PURCHASE);
            stockMovement.setReason("Purchase product");

            productRepository.save(product);
            stockMovementRepository.save(stockMovement);
        }

        purchaseRepository.save(purchase);

        return assembler.toModel(purchase);
    }

    @Transactional
    public PurchaseDTO canceledPurchase(Long id) {
        Purchase purchase = getPurchaseOrThrowException(id);

        if(purchase.getStatus() != PurchaseStatus.OPEN) {
            throw new BusinessException("The purchase status must be 'OPEN'");
        }

        purchase.setStatus(PurchaseStatus.CANCELED);

        purchase = purchaseRepository.save(purchase);

        return assembler.toModel(purchase);
    }

    private Purchase getPurchaseAndItemsOrThrowException(Long id) {
        return purchaseRepository.getPurchaseAndItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
    }

    private Purchase getPurchaseOrThrowException(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
    }

    private Supplier getSupplierOrThrowException(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    private Product getProductOrThrowException(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }
}
