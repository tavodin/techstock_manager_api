package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.CategoryAssembler;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationsInputDTO;
import io.github.tavodin.techstock_manager.dto.category.CategoryAutocompleteDTO;
import io.github.tavodin.techstock_manager.dto.category.CategoryDTO;
import io.github.tavodin.techstock_manager.dto.category.CategoryLoadDTO;
import io.github.tavodin.techstock_manager.dto.category.CategoryRequestDTO;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationsListDTO;
import io.github.tavodin.techstock_manager.entities.Category;
import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.CategoryRepository;
import io.github.tavodin.techstock_manager.repositories.SpecificationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final SpecificationRepository specificationRepository;
    private final CategoryAssembler assembler;
    private final PagedResourcesAssembler<Category> pagedAssembler;

    public CategoryService(
            PagedResourcesAssembler<Category> pagedAssembler, CategoryAssembler assembler,
            SpecificationRepository specificationRepository, CategoryRepository repository
    ) {
        this.pagedAssembler = pagedAssembler;
        this.assembler = assembler;
        this.specificationRepository = specificationRepository;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        Category entity = getEntityOrThrowException(id);
        return assembler.toModel(entity);
    }

    @Transactional(readOnly = true)
    public PagedModel<CategoryDTO> findAll(String name, Pageable pageable) {
        Page<Category> page = repository.getAll(name, pageable);
        return pagedAssembler.toModel(page, assembler);
    }

    @Transactional(readOnly = true)
    public List<CategoryAutocompleteDTO> getCategoriesToAutocomplete(String name) {
        return repository.getCategoriesByName(name, PageRequest.of(0, 5));
    }

    @Transactional(readOnly = true)
    public List<CategorySpecificationsListDTO> findAllSpecificationByCategoryId(Long id) {
        getEntityOrThrowException(id);
        List<CategorySpecificationsListDTO> entity = repository.findAllSpecificationsByCategoryId(id)
                .stream()
                .map(CategorySpecificationsListDTO::new)
                .toList();
        return entity;
    }

    @Transactional(readOnly = true)
    public CategoryLoadDTO loadCategoryById(Long id) {
        Category category = repository.loadCategoryById(id);
        return new CategoryLoadDTO(category);
    }

    @Transactional
    public CategoryDTO save(CategoryRequestDTO request) {
        Set<Specification> specs = getSpecificationOrThrowException(request.specificationIds());

        Category entity = new Category();
        entity.setName(request.name());
        entity.setSpecifications(specs);

        entity = repository.save(entity);
        return assembler.toModel(entity);
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryRequestDTO request) {
        Category entity = getEntityOrThrowException(id);
        Set<Specification> specs = getSpecificationOrThrowException(request.specificationIds());

        entity.setName(request.name());
        entity.setSpecifications(specs);
        entity = repository.save(entity);

        return assembler.toModel(entity);
    }

    @Transactional
    public void delete(Long id) {
        Category entity = getEntityOrThrowException(id);
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new EntityInUseException("Category is in use and cannot be deleted");
        }
    }

    @Transactional(readOnly = true)
    public List<CategorySpecificationsInputDTO> findAllSpecificationByCategoryIds(List<Long> ids) {
        return repository.findAllSpecificationsByCategoryIds(ids);
    }

    private Category getEntityOrThrowException(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));
    }

    private Set<Specification> getSpecificationOrThrowException(Set<Long> ids) {
        Set<Specification> specifications = specificationRepository.getSpecificationByIds(ids);

        if(specifications.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more specifications were not found");
        }

        return specifications;
    }


}
