package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.SpecificationAssembler;
import io.github.tavodin.techstock_manager.dto.SpecificationAutocompleteDTO;
import io.github.tavodin.techstock_manager.dto.SpecificationDTO;
import io.github.tavodin.techstock_manager.dto.SpecificationRequestDTO;
import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.entities.Unit;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.SpecificationRepository;
import io.github.tavodin.techstock_manager.repositories.UnitRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SpecificationService {

    private final SpecificationRepository specificationRepository;
    private final UnitRepository unitRepository;
    private final SpecificationAssembler specificationAssembler;
    private final PagedResourcesAssembler<SpecificationDTO> pagedAssembler;

    public SpecificationService(SpecificationRepository specificationRepository, UnitRepository unitRepository, SpecificationAssembler specificationAssembler, PagedResourcesAssembler<SpecificationDTO> pagedAssembler) {
        this.specificationRepository = specificationRepository;
        this.unitRepository = unitRepository;
        this.specificationAssembler = specificationAssembler;
        this.pagedAssembler = pagedAssembler;
    }

    @Transactional(readOnly = true)
    public SpecificationDTO findById(Long id) {
        SpecificationDTO entity = getDTOOrThrownException(id);
        return specificationAssembler.toModel(entity);
    }

    @Transactional(readOnly = true)
    public PagedModel<SpecificationDTO> findAll(String name, Pageable pageable) {
        Page<SpecificationDTO> page = specificationRepository.findAllProjected(name, pageable);

        return pagedAssembler.toModel(page, specificationAssembler);
    }

    @Transactional(readOnly = true)
    public List<SpecificationAutocompleteDTO> getSpecificationsByName(String name) {
        Pageable pageable = PageRequest.of(0, 5);
        Page<SpecificationAutocompleteDTO> page = specificationRepository.getSpecificationsByName(name, pageable);
        return page.getContent();
    }

    @Transactional
    public SpecificationDTO save(SpecificationRequestDTO request) {
        Specification specificationEntity = new Specification();

        if(request.unitId() != null) {
            Unit unitEntity = unitRepository.findById(request.unitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found!"));
            specificationEntity.setUnit(unitEntity);
        }

        specificationEntity.setName(request.name());
        specificationEntity.setDataType(request.dataType());
        specificationEntity.setFilterable(request.filterable());

        specificationEntity = specificationRepository.save(specificationEntity);

        return specificationAssembler.toModel(new SpecificationDTO(specificationEntity));
    }

    @Transactional
    public SpecificationDTO update(Long id, SpecificationRequestDTO request) {
        Specification entity = getEntityOrThrownException(id);

        if(request.unitId() != null) {
            if(entity.getUnit() != null) {
                if(entity.getUnit().getId() != request.unitId()) {
                    Unit unit = unitRepository.findById(request.unitId())
                            .orElseThrow(() -> new ResourceNotFoundException("Unit not found!"));
                    entity.setUnit(unit);
                }
            } else {
                Unit unit = unitRepository.findById(request.unitId())
                        .orElseThrow(() -> new ResourceNotFoundException("Unit not found!"));
                entity.setUnit(unit);
            }
        } else {
            entity.setUnit(null);
        }

        entity.setName(request.name());
        entity.setFilterable(request.filterable());
        entity.setDataType(request.dataType());

        entity = specificationRepository.save(entity);

        return specificationAssembler.toModel(new SpecificationDTO(entity));
    }

    @Transactional
    public void delete(Long id) {
        try {
            Specification entity = getEntityOrThrownException(id);
            specificationRepository.delete(entity);
            specificationRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new EntityInUseException("Specification is in use and cannot be deleted");
        }
    }

    private SpecificationDTO getDTOOrThrownException(Long id) {
        return specificationRepository.getSpecificationById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specification not found!"));
    }

    private Specification getEntityOrThrownException(Long id) {
        return specificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specification not found!"));
    }
}
