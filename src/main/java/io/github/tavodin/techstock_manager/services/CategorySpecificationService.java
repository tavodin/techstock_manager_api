package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.CategorySpecificationAssembler;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationDTO;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationRequestDTO;
import io.github.tavodin.techstock_manager.entities.Category;
import io.github.tavodin.techstock_manager.entities.CategorySpecification;
import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.CategoryRepository;
import io.github.tavodin.techstock_manager.repositories.CategorySpecificationRepository;
import io.github.tavodin.techstock_manager.repositories.SpecificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategorySpecificationService {

    private final CategorySpecificationRepository repository;
    private final CategoryRepository categoryRepository;
    private final SpecificationRepository specificationRepository;
    private final CategorySpecificationAssembler assembler;

    public CategorySpecificationService(CategorySpecificationRepository repository, CategoryRepository categoryRepository, SpecificationRepository specificationRepository, CategorySpecificationAssembler assembler) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.specificationRepository = specificationRepository;
        this.assembler = assembler;
    }

    @Transactional
    public CategorySpecificationDTO save(CategorySpecificationRequestDTO request) {
        CategorySpecification entity = new CategorySpecification();

        Category categoryEntity = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));

        Specification specificationEntity = specificationRepository.findById(request.specificationId())
                .orElseThrow(() -> new ResourceNotFoundException("Specification not found!"));

        entity.setCategory(categoryEntity);
        entity.setSpecification(specificationEntity);
        entity.setRequired(request.required());

        entity = repository.save(entity);

        return assembler.toModel(entity);
    }

    @Transactional
    public CategorySpecificationDTO update(Long id, CategorySpecificationRequestDTO request) {
        CategorySpecification entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category Specification not found!"));

        if(!entity.getCategory().getId().equals(request.categoryId())) {
            Category categoryEntity = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));

            entity.setCategory(categoryEntity);
        }

        if(!entity.getSpecification().getId().equals(request.specificationId())) {
            Specification specificationEntity = specificationRepository.findById(request.specificationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specification not found!"));
            entity.setSpecification(specificationEntity);
        }

        entity.setRequired(request.required());

        entity = repository.save(entity);

        return assembler.toModel(entity);
    }

    @Transactional
    public void delete(Long id) {
        CategorySpecification entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category Specification not found!"));

        repository.delete(entity);
    }
}
