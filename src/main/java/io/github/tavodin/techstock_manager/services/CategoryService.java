package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.CategoryAssembler;
import io.github.tavodin.techstock_manager.dto.CategoryDTO;
import io.github.tavodin.techstock_manager.dto.error.CategoryRequestDTO;
import io.github.tavodin.techstock_manager.repositories.CategoryRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryAssembler assembler;

    public CategoryService(CategoryRepository repository, CategoryAssembler assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        return null;
    }

    public PagedModel<CategoryDTO> findAll(Pageable pageable) {
        return null;
    }

    public CategoryDTO save(CategoryRequestDTO request) {
        return null;
    }

    public CategoryDTO update(Long id, CategoryRequestDTO request) {
        return null;
    }

    public void delete(Long id) {

    }
}
