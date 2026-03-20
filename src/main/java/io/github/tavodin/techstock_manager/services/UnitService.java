package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.UnitAssembler;
import io.github.tavodin.techstock_manager.dto.UnitDTO;
import io.github.tavodin.techstock_manager.entities.Unit;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repository.UnitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnitService {

    private final UnitRepository unitRepository;
    private final UnitAssembler unitAssembler;
    private final PagedResourcesAssembler<Unit> pagedAssembler;

    public UnitService(UnitRepository unitRepository, UnitAssembler unitAssembler, PagedResourcesAssembler<Unit> pagedAssembler) {
        this.unitRepository = unitRepository;
        this.unitAssembler = unitAssembler;
        this.pagedAssembler = pagedAssembler;
    }

    @Transactional(readOnly = true)
    public UnitDTO findById(Long id) {
        Unit entity = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found!"));

        return unitAssembler.toModel(entity);
    }

    @Transactional(readOnly = true)
    public PagedModel<UnitDTO> findAll(Pageable pageable) {
        Page<Unit> page = unitRepository.findAll(pageable);

        return pagedAssembler.toModel(page, unitAssembler);
    }
}
