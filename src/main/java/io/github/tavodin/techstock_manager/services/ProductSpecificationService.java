package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.dto.product.ProductSpecificationListDTO;
import io.github.tavodin.techstock_manager.entities.ProductSpecification;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.ProductRepository;
import io.github.tavodin.techstock_manager.repositories.ProductSpecificationRepository;
import io.github.tavodin.techstock_manager.repositories.SpecificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductSpecificationService {

    private final ProductSpecificationRepository repository;
    private final ProductRepository productRepository;
    private final SpecificationRepository specificationRepository;

    public ProductSpecificationService(ProductSpecificationRepository repository, ProductRepository productRepository, SpecificationRepository specificationRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.specificationRepository = specificationRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductSpecificationListDTO> findAll(Long prodId) {
        productRepository.findById(prodId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return repository.getAllByProductId(prodId);
    }

    @Transactional
    public void delete(Long prodId, Long specId) {
        ProductSpecification findProdSpec = repository.getByProductIdAndSpecificationId(prodId, specId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Specification not found"));

        repository.delete(findProdSpec);
    }
}
