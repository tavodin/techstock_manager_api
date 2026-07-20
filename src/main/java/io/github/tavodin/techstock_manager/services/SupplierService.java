package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.SupplierAssembler;
import io.github.tavodin.techstock_manager.dto.SupplierDTO;
import io.github.tavodin.techstock_manager.dto.SupplierRequestDTO;
import io.github.tavodin.techstock_manager.entities.Supplier;
import io.github.tavodin.techstock_manager.exceptions.AlreadyExistsException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService {

    private final SupplierRepository repository;
    private final SupplierAssembler assembler;
    private final PagedResourcesAssembler<Supplier> pagedAssembler;

    public SupplierService(SupplierRepository repository, SupplierAssembler assembler, PagedResourcesAssembler<Supplier> pagedAssembler) {
        this.repository = repository;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @Transactional(readOnly = true)
    public SupplierDTO findById(Long id) {
        Supplier supplier = getSupplierOrThrowException(id);
        return assembler.toModel(supplier);
    }

    @Transactional(readOnly = true)
    public PagedModel<SupplierDTO> findAll(Pageable pageable) {
        Page<Supplier> page = repository.findAll(pageable);
        return pagedAssembler.toModel(page, assembler);
    }

    @Transactional
    public SupplierDTO save(SupplierRequestDTO request) {
        if(repository.existsByDocument(request.getDocument())) {
            throw new AlreadyExistsException("A record with the same document value already exists");
        }

        if(repository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already registered");
        }

        Supplier entity = new Supplier(
                request.getName(),
                request.getDocument(),
                request.getEmail(),
                request.getPhone(),
                true
        );

        entity = repository.save(entity);

        return assembler.toModel(entity);
    }

    @Transactional
    public SupplierDTO update(Long id, SupplierRequestDTO request) {
        if(repository.existsByDocumentAndIdNot(request.getDocument(), id)) {
            throw new AlreadyExistsException("A record with the same document value already exists");
        }

        if(repository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new AlreadyExistsException("Email already registered");
        }

        Supplier entity = getSupplierOrThrowException(id);

        entity.setName(request.getName());
        entity.setDocument(request.getDocument());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setActive(true);

        entity = repository.save(entity);

        return assembler.toModel(entity);
    }

    @Transactional
    public void delete(Long id) {
        Supplier entity = getSupplierOrThrowException(id);
        entity.setActive(false);
        repository.save(entity);
    }

    private Supplier getSupplierOrThrowException(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }
}
