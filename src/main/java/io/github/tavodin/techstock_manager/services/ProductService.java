package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.ProductAssembler;
import io.github.tavodin.techstock_manager.dto.product.*;
import io.github.tavodin.techstock_manager.entities.*;
import io.github.tavodin.techstock_manager.enums.SpecificationType;
import io.github.tavodin.techstock_manager.exceptions.AlreadyExistsException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.*;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SpecificationRepository specRepository;
    private final ProductSpecificationRepository prodSpecRepository;
    private final ProductAssembler assembler;
    private final PagedResourcesAssembler<Product> pagedAssembler;
    private final EntityManager entityManager;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, BrandRepository brandRepository, SpecificationRepository specRepository, ProductSpecificationRepository prodSpecRepository, ProductAssembler assembler, PagedResourcesAssembler<Product> pagedAssembler, EntityManager entityManager) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.specRepository = specRepository;
        this.prodSpecRepository = prodSpecRepository;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product entity = getProductOrThrowException(id);
        return assembler.toModel(entity);
    }

    @Transactional(readOnly = true)
    public ProductLoadDTO loadProductById(Long id) {
        Product entity = productRepository.getProductToLoad(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product no found"));

        return new ProductLoadDTO(entity);
    }

    @Transactional(readOnly = true)
    public PagedModel<ProductDTO> findAll(
            String name,
            BigDecimal maxSale,
            BigDecimal minSale,
            BigDecimal maxPurchase,
            BigDecimal minPurchase,
            Set<Long> categoryIds,
            Long brandId,
            Boolean active,
            Boolean belowMinimumStock,
            Pageable pageable
    ) {
        String nameFilter = name == null || name.isBlank()
                ? null
                : name.trim();

        Page<Product> page = productRepository.getAll(
                nameFilter,
                maxSale,
                minSale,
                maxPurchase,
                minPurchase,
                categoryIds,
                brandId,
                active,
                belowMinimumStock,
                pageable
        );

        return pagedAssembler.toModel(page, assembler);
    }

    @Transactional
    public ProductDTO save(ProductRequestDTO request) {
        Product product = new Product();

        Brand brand = getBrandOrThrowException(request.getBrandId());
        Set<Category> categories = new HashSet<>(getCategoriesOrThrowException(request.getCategoryIds()));

        if (productRepository.existsBySku(request.getSku())) {
            throw new AlreadyExistsException("SKU already exists");
        }

        product.setName(request.getName());
        product.setCostPrice(BigDecimal.valueOf(0));
        product.setSalePrice(request.getSalePrice());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setQuantityInStock(0);
        product.setMinimumStock(request.getMinimumStock());
        product.setActive(request.getActive());

        product.setBrand(brand);
        product.setCategories(categories);

        product = productRepository.save(product);

        List<ProductSpecification> specifications = new ArrayList<>();
        createSpecifications(request, product, specifications);

        prodSpecRepository.saveAll(specifications);

        return assembler.toModel(product);
    }

    @Transactional
    public ProductDTO update(Long id, ProductRequestDTO request) {
        Product findProduct = getProductOrThrowException(id);

        if (productRepository.existsBySkuAndIdNot(request.getSku(), id)) {
            throw new AlreadyExistsException("SKU already exists");
        }

        if (findProduct.getBrand().getId() != request.getBrandId()) {
            Brand findBrand = getBrandOrThrowException(request.getBrandId());
            findProduct.setBrand(findBrand);
        }

        List<Long> findCategoryIds = findProduct.getCategories()
                .stream()
                .map(Category::getId)
                .toList();

        if (!findCategoryIds.equals(request.getCategoryIds())) {
            Set<Category> findCategories = new HashSet<>(getCategoriesOrThrowException(request.getCategoryIds()));
            findProduct.setCategories(findCategories);
        }

        findProduct.setName(request.getName());
        findProduct.setSalePrice(request.getSalePrice());
        findProduct.setDescription(request.getDescription());
        findProduct.setSku(request.getSku());
        findProduct.setMinimumStock(request.getMinimumStock());
        findProduct.setActive(request.getActive());

        findProduct.getSpecifications().clear();
        findProduct = productRepository.saveAndFlush(findProduct);

        List<ProductSpecification> specifications = new ArrayList<>();

        Product finalFindProduct = findProduct;

        createSpecifications(request, finalFindProduct, specifications);

        prodSpecRepository.saveAll(specifications);
        return assembler.toModel(findProduct);
    }

    @Transactional
    public void delete(Long id) {
        Product product = getProductOrThrowException(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private void createSpecifications(
            ProductRequestDTO request, Product product, List<ProductSpecification> productSpecifications) {

        List<ProductSpecificationSaveDTO> productSpecRequest = request.getSpecifications();
        List<Long> specIds = productSpecRequest.stream().map(ProductSpecificationSaveDTO::specificationId).toList();

        List<Specification> specifications = getSpecificationsOrThrowException(specIds);

        for (Specification specification : specifications) {
            ProductSpecificationSaveDTO findProdSpec = productSpecRequest.stream()
                    .filter(ps -> ps.specificationId().equals(specification.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Product Specification not found"));

            ProductSpecification productSpec = new ProductSpecification();
            productSpec.setProduct(product);
            productSpec.setSpecification(specification);

            if (specification.getDataType() == SpecificationType.NUMBER) {
                productSpec.setValueNumber(findProdSpec.valueNumber());
            } else if (specification.getDataType() == SpecificationType.STRING) {
                productSpec.setValueString(findProdSpec.valueString());
            } else {
                productSpec.setValueBoolean(findProdSpec.valueBoolean());
            }

            productSpecifications.add(productSpec);
        }
    }

    private ProductSpecification getProdSpecOrThrowException(Long prodId, Long specId) {
        return prodSpecRepository.getByProductIdAndSpecificationId(prodId, specId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Specification not found"));
    }

    private Brand getBrandOrThrowException(Long brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
    }

    private List<Category> getCategoriesOrThrowException(Set<Long> categoriesId) {
        List<Category> categories = categoryRepository.findAllById(categoriesId);

        if (categoriesId.size() != categories.size()) {
            throw new ResourceNotFoundException("One or more categories were not found");
        }

        return categories;
    }

    private List<Specification> getSpecificationsOrThrowException(List<Long> specificationsId) {
        List<Specification> specifications = specRepository.getSpecificationsByIds(specificationsId);

        if (specificationsId.size() != specifications.size()) {
            throw new ResourceNotFoundException("One or more specifications were not found");
        }

        return specifications;
    }

    private Product getProductOrThrowException(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return product;
    }


    public List<ProductAutocompleteDTO> getAllAutocomplete(String name) {
        Pageable pageable = PageRequest.of(0, 5);
        return productRepository.getAllAutocomplete(name, pageable);
    }
}
