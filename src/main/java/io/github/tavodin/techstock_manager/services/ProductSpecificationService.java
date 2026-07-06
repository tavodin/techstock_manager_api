package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.dto.ProductSpecificationDTO;
import io.github.tavodin.techstock_manager.dto.ProductSpecificationListDTO;
import io.github.tavodin.techstock_manager.dto.ProductSpecificationSaveDTO;
import io.github.tavodin.techstock_manager.dto.ProductSpecificationUpdateDTO;
import io.github.tavodin.techstock_manager.entities.Category;
import io.github.tavodin.techstock_manager.entities.Product;
import io.github.tavodin.techstock_manager.entities.ProductSpecification;
import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.enums.SpecificationType;
import io.github.tavodin.techstock_manager.exceptions.BusinessException;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.CategoryRepository;
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
    private final CategoryRepository categoryRepository;

    public ProductSpecificationService(ProductSpecificationRepository repository, ProductRepository productRepository, SpecificationRepository specificationRepository, CategoryRepository categoryRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.specificationRepository = specificationRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductSpecificationListDTO> findAll(Long prodId) {
        return repository.getAllByProductIdAndSpecificationId(prodId);
    }

    @Transactional
    public ProductSpecificationDTO save(Long prodId, ProductSpecificationSaveDTO request) {
        ProductSpecification entity = new ProductSpecification();

        if(repository.existsByProduct_IdAndSpecification_Id(prodId, request.specificationId())) {
            throw new EntityInUseException("The product cannot have the same specification");
        }

        Product findProduct = productRepository.findById(prodId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Specification findSpecification = getSpecificationOrThrownException(request.specificationId());

        entity.setProduct(findProduct);
        entity.setSpecification(findSpecification);

        if(findSpecification.getDataType() == SpecificationType.STRING) {
            if(request.valueString() == null) {
                throw new BusinessException("Value String cannot be null");
            }
            entity.setValueString(request.valueString());
        } else if (findSpecification.getDataType() == SpecificationType.NUMBER) {
            if(request.valueNumber() == null) {
                throw new BusinessException("Value Number cannot be null");
            }
            entity.setValueNumber(request.valueNumber());
        } else {
            if(request.valueBoolean() == null) {
                throw new BusinessException("Value Boolean cannot be null");
            }
            entity.setValueBoolean(request.valueBoolean());
        }

        entity = repository.save(entity);

        return new ProductSpecificationDTO(entity);
    }

    @Transactional
    public ProductSpecificationDTO update(Long prodId, Long specId, ProductSpecificationUpdateDTO request) {
        ProductSpecification entity = getProdSpecOrThrowException(prodId, specId);
        Specification findSpecification = getSpecificationOrThrownException(specId);

        if(findSpecification.getDataType() == SpecificationType.STRING) {
            if(request.valueString() == null) {
                throw new BusinessException("Value String cannot be null");
            }
            entity.setValueString(request.valueString());
        } else if (findSpecification.getDataType() == SpecificationType.NUMBER) {
            if(request.valueNumber() == null) {
                throw new BusinessException("Value Number cannot be null");
            }
            entity.setValueNumber(request.valueNumber());
        } else {
            if(request.valueBoolean() == null) {
                throw new BusinessException("Value Boolean cannot be null");
            }
            entity.setValueBoolean(request.valueBoolean());
        }

        entity = repository.save(entity);

        return new ProductSpecificationDTO(entity);
    }

    @Transactional
    public void delete(Long prodId, Long specId) {
        Product findProduct = productRepository.findById(prodId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        List<Long> catIds = findProduct.getCategories().stream().map(Category::getId).toList();
        List<Long> requiredSpecIds = categoryRepository.findRequiredSpecificationsIdsByCategoryIds(catIds);

        if(requiredSpecIds.contains(specId)) {
            throw new BusinessException("The specification is required and cannot be excluded");
        }

        ProductSpecification findProdSpec = getProdSpecOrThrowException(prodId, specId);

        repository.delete(findProdSpec);
    }

    private ProductSpecification getProdSpecOrThrowException(Long prodId, Long specId) {
        return repository.getByProductIdAndSpecificationId(prodId, specId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Specification not found"));
    }

    private Specification getSpecificationOrThrownException(Long specId) {
        return specificationRepository.findById(specId)
                .orElseThrow(() -> new ResourceNotFoundException("Specification not found"));
    }
}
