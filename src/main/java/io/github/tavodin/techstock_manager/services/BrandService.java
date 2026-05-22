package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.BrandAssembler;
import io.github.tavodin.techstock_manager.dto.BrandDTO;
import io.github.tavodin.techstock_manager.dto.BrandRequestDTO;
import io.github.tavodin.techstock_manager.entities.Brand;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.BrandRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandService {

    private final BrandRepository repository;
    private final BrandAssembler assembler;
    private final PagedResourcesAssembler<Brand> pagedAssembler;

    public BrandService(BrandRepository repository, BrandAssembler assembler, PagedResourcesAssembler<Brand> pagedAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @Transactional(readOnly = true)
    public BrandDTO findById(Long id) {
        Brand entity = getEntityOrThrowException(id);
        return assembler.toModel(entity);
    }

    @Transactional(readOnly = true)
    public PagedModel<BrandDTO> findAll(Pageable pageable) {
        Page<Brand> page = repository.findAll(pageable);
        return pagedAssembler.toModel(page, assembler);
    }

    @Transactional
    public BrandDTO save(BrandRequestDTO request) {
        Brand entity = new Brand();
        entity.setName(request.name());
        entity = repository.save(entity);
        return assembler.toModel(entity);
    }

    @Transactional
    public BrandDTO update(Long id, BrandRequestDTO request) {
        Brand entity = getEntityOrThrowException(id);
        entity.setName(request.name());
        entity = repository.save(entity);
        return assembler.toModel(entity);
    }

    @Transactional
    public void delete(Long id) {
        try {
            Brand entity = getEntityOrThrowException(id);
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new EntityInUseException("Brand is in use and cannot be deleted");
        }
    }

    private Brand getEntityOrThrowException(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
    }
}
