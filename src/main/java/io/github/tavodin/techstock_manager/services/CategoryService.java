package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.CategoryAssembler;
import io.github.tavodin.techstock_manager.dto.CategoryDTO;
import io.github.tavodin.techstock_manager.dto.CategoryRequestDTO;
import io.github.tavodin.techstock_manager.entities.Category;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryAssembler assembler;
    private final PagedResourcesAssembler<Category> pagedAssembler;

    public CategoryService(CategoryRepository repository, CategoryAssembler assembler, PagedResourcesAssembler<Category> pagedAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        Category entity = getEntityOrThrowException(id);
        return assembler.toModel(entity);
    }

    @Transactional(readOnly = true)
    public PagedModel<CategoryDTO> findAll(Pageable pageable) {
        Page<Category> page = repository.findAll(pageable);
        return pagedAssembler.toModel(page, assembler);
    }

    @Transactional
    public CategoryDTO save(CategoryRequestDTO request) {
        Category entity = new Category();
        entity.setName(request.name());
        entity = repository.save(entity);
        return assembler.toModel(entity);
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryRequestDTO request) {
        Category entity = getEntityOrThrowException(id);
        entity.setName(request.name());
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

    private Category getEntityOrThrowException(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));
    }
}
