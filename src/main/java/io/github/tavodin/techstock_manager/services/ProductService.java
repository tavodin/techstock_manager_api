package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.ProductAssembler;
import io.github.tavodin.techstock_manager.dto.ProductDTO;
import io.github.tavodin.techstock_manager.dto.ProductRequestDTO;
import io.github.tavodin.techstock_manager.dto.ProductSpecificationRequestDTO;
import io.github.tavodin.techstock_manager.entities.*;
import io.github.tavodin.techstock_manager.enums.SpecificationType;
import io.github.tavodin.techstock_manager.exceptions.AlreadyExistsException;
import io.github.tavodin.techstock_manager.exceptions.BusinessException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SpecificationRepository specRepository;
    private final ProductSpecificationRepository prodSpecRepository;
    private final ProductAssembler assembler;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, BrandRepository brandRepository, SpecificationRepository specRepository, ProductSpecificationRepository prodSpecRepository, ProductAssembler assembler) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.specRepository = specRepository;
        this.prodSpecRepository = prodSpecRepository;
        this.assembler = assembler;
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product entity = getProductOrThrowException(id);
        return assembler.toModel(entity);
    }

    @Transactional
    public ProductDTO save(ProductRequestDTO request) {
        Product product = new Product();

        Brand brand = getBrandOrThrowException(request.getBrandId());

        Set<Category> categories = new HashSet<>(getCategoriesOrThrowException(request.getCategoryIds()));

        if(productRepository.existsBySku(request.getSku())) {
            throw new AlreadyExistsException("SKU already exists");
        }

        product.setName(request.getName());
        product.setCostPrice(BigDecimal.valueOf(0));
        product.setSalePrice(request.getSalePrice());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setQuantityInStock(0);
        product.setMinimumStock(request.getMinimumStock());
        product.setActive(true);

        product.setBrand(brand);
        product.setCategories(categories);

        product = productRepository.save(product);

        List<ProductSpecification> specifications = createSpecifications(request, product, request.getCategoryIds());
        prodSpecRepository.saveAll(specifications);

        return assembler.toModel(product);
    }

    private List<ProductSpecification> createSpecifications(
            ProductRequestDTO request,  Product product, Set<Long> categoriesId) {

        List<ProductSpecificationRequestDTO> productSpecRequest = request.getSpecifications();
        List<Long> specIds = productSpecRequest.stream().map(ProductSpecificationRequestDTO::specificationId).toList();
        List<Specification> specifications = getSpecificationsOrThrowException(specIds);

        requiredSpecificationsValidation(specifications, categoriesId);

        List<ProductSpecification> productSpecifications = new ArrayList<>();

        for(Specification specification : specifications) {
            ProductSpecificationRequestDTO findProdSpec = productSpecRequest.stream()
                    .filter(ps -> ps.specificationId().equals(specification.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Product Specification not found"));

            ProductSpecification productSpec = new ProductSpecification();
            productSpec.setProduct(product);
            productSpec.setSpecification(specification);

            if(specification.getDataType() == SpecificationType.NUMBER) {
                productSpec.setValueNumber(findProdSpec.valueNumber());
            } else if(specification.getDataType() == SpecificationType.STRING) {
                productSpec.setValueString(findProdSpec.valueString());
            } else {
                productSpec.setValueBoolean(findProdSpec.valueBoolean());
            }

            productSpecifications.add(productSpec);
        }

        return productSpecifications;
    }

    private void requiredSpecificationsValidation(List<Specification> specifications, Set<Long> categoriesId) {
        Set<Long> requiredIds = new HashSet<>(categoryRepository
                .findRequiredSpecificationsIdsByCategoryIds(new ArrayList<>(categoriesId)));

        Set<Long> requestIds = specifications
                .stream()
                .map(Specification::getId)
                .collect(Collectors.toSet());

        if(!requestIds.containsAll(requiredIds)) {
            throw new BusinessException("Missing required specifications");
        }
    }

    private Brand getBrandOrThrowException(Long brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
    }

    private List<Category> getCategoriesOrThrowException(Set<Long> categoriesId) {
        List<Category> categories = categoryRepository.findAllById(categoriesId);

        if(categoriesId.size() != categories.size()) {
            throw new ResourceNotFoundException("One or more categories were not found");
        }

        return categories;
    }

    private List<Specification> getSpecificationsOrThrowException(List<Long> specificationsId) {
        List<Specification> specifications = specRepository.getSpecificationsByIds(specificationsId);

        if(specificationsId.size() != specifications.size()) {
            throw new ResourceNotFoundException("One or more specifications were not found");
        }

        return specifications;
    }

    private Product getProductOrThrowException(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return product;
    }
}
