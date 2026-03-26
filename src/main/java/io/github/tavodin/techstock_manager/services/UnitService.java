package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.UnitAssembler;
import io.github.tavodin.techstock_manager.dto.UnitDTO;
import io.github.tavodin.techstock_manager.dto.UnitResponseDTO;
import io.github.tavodin.techstock_manager.entities.Unit;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.mappers.UnitMapper;
import io.github.tavodin.techstock_manager.repository.UnitRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final UnitMapper unitMapper;

    public UnitService(UnitRepository unitRepository, UnitAssembler unitAssembler, PagedResourcesAssembler<Unit> pagedAssembler, UnitMapper unitMapper) {
        this.unitRepository = unitRepository;
        this.unitAssembler = unitAssembler;
        this.pagedAssembler = pagedAssembler;
        this.unitMapper = unitMapper;
    }

    @Transactional(readOnly = true)
    public UnitDTO findById(Long id) {
        Unit entity = getUnitOrElseThrow(id);

        return unitAssembler.toModel(entity);
    }

    @Transactional(readOnly = true)
    public PagedModel<UnitDTO> findAll(Pageable pageable) {
        Page<Unit> page = unitRepository.findAll(pageable);

        return pagedAssembler.toModel(page, unitAssembler);
    }

    @Transactional
    public UnitDTO save(UnitResponseDTO responseDTO) {
        Unit entity = unitMapper.toEntity(responseDTO);
        entity = unitRepository.save(entity);
        return unitAssembler.toModel(entity);
    }

    @Transactional
    public UnitDTO update(Long id, UnitResponseDTO responseDTO) {
        Unit entity = getUnitOrElseThrow(id);

        entity.setName(responseDTO.name());
        entity.setSymbol(responseDTO.symbol());

        entity = unitRepository.saveAndFlush(entity);

        return unitAssembler.toModel(entity);
    }

    @Transactional
    public void delete(Long id) {
        Unit entity = getUnitOrElseThrow(id);
        try {
            unitRepository.delete(entity);
            unitRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new EntityInUseException("Unit is in use and cannot be deleted");
        }
    }

    private Unit getUnitOrElseThrow(Long id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found!"));
    }
}
