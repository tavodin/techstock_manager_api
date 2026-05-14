package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.SpecificationAssembler;
import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.dto.SpecificationDTO;
import io.github.tavodin.techstock_manager.dto.SpecificationRequestDTO;
import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.entities.Unit;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.SpecificationRepository;
import io.github.tavodin.techstock_manager.repositories.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpecificationServiceTest {

    @Mock
    private SpecificationRepository repository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private SpecificationAssembler assembler;

    @Mock
    private PagedResourcesAssembler<SpecificationDTO> pagedAssembler;

    @InjectMocks
    private SpecificationService service;

    private Long validId;
    private Long invalidID;

    private Specification specification;
    private SpecificationDTO specificationDTO;
    private SpecificationRequestDTO request;
    private Unit unit;

    @BeforeEach
    void setUp() {
        specification = SpecificationBuilder.builder().build();

        specificationDTO = new SpecificationDTO(
                specification.getId(),
                specification.getName(),
                specification.getDataType(),
                specification.getFilterable(),
                specification.getUnit().getSymbol(),
                specification.getCreatedAt(),
                specification.getUpdatedAt()
        );

        request = new SpecificationRequestDTO(
                specification.getName(),
                specification.getDataType(),
                specification.getFilterable(),
                1L);

        unit = new Unit("Hertz", "Hz");

        validId = 1L;
        invalidID = 2L;
    }

    @Test
    void shouldReturnSpecificationDTOWhenIdExists() {
        when(repository.getSpecificationById(validId)).thenReturn(Optional.of(specificationDTO));
        when(assembler.toModel(any(SpecificationDTO.class))).thenReturn(specificationDTO);

        SpecificationDTO findUnit = service.findById(validId);

        assertNotNull(findUnit.getId());
        assertEquals(specificationDTO.getName(), findUnit.getName());
        assertEquals(specificationDTO.getDataType(), findUnit.getDataType());
        assertEquals(specificationDTO.getFilterable(), findUnit.getFilterable());
        assertEquals(specificationDTO.getCreatedAt(), findUnit.getCreatedAt());
        assertEquals(specificationDTO.getUpdatedAt(), findUnit.getUpdatedAt());

        verify(repository).getSpecificationById(validId);
        verify(assembler).toModel(specificationDTO);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
        when(repository.getSpecificationById(invalidID)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.findById(invalidID)
        );

        assertEquals("Specification not found!", actual.getMessage());
    }

    @Test
    void shouldReturnPagedModelWhenFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SpecificationDTO> page = new PageImpl<>(List.of(specificationDTO));
        PagedModel<SpecificationDTO> pagedModel = mock(PagedModel.class);

        when(repository.findAllProjected(pageable)).thenReturn(page);
        when(pagedAssembler.toModel(page, assembler)).thenReturn(pagedModel);

        PagedModel<SpecificationDTO> actual = service.findAll(pageable);

        assertNotNull(actual);
        assertEquals(pagedModel, actual);

        verify(repository).findAllProjected(pageable);
        verify(pagedAssembler).toModel(page, assembler);
    }

    @Test
    void shouldSaveSpecificationWhenSave() {
        Specification saveSpecification = SpecificationBuilder.builder().withId(null).build();

        when(unitRepository.findById(validId)).thenReturn(Optional.of(unit));
        when(repository.save(saveSpecification)).thenReturn(specification);
        when(assembler.toModel(specificationDTO)).thenReturn(specificationDTO);

        SpecificationDTO actual = service.save(request);

        assertEquals(specification.getId(), actual.getId());
        assertEquals(specification.getName(), actual.getName());
        assertEquals(specification.getDataType(), actual.getDataType());
        assertEquals(specification.getFilterable(), actual.getFilterable());
        assertEquals(specification.getCreatedAt(), actual.getCreatedAt());
        assertEquals(specification.getUpdatedAt(), actual.getUpdatedAt());
        assertEquals(specification.getUnit().getSymbol(), actual.getUnitSymbol());

        verify(unitRepository).findById(saveSpecification.getUnit().getId());
    }

    @Test
    void shouldThrownResourceNotFoundExceptionWhenUnitIdDoesNotExistInSave() {
        SpecificationRequestDTO invalidRequest = new SpecificationRequestDTO(
                specification.getName(),
                specification.getDataType(),
                specification.getFilterable(),
                invalidID);

        when(unitRepository.findById(invalidID)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.save(invalidRequest));

        assertEquals("Unit not found!", actual.getMessage());
    }

    @Test
    void shouldNotCallUnitFindByIdWhenSavingWithNullUnitId() {
        Specification saveSpec = SpecificationBuilder.builder()
                .withId(null).withUnit(null).build();

        SpecificationRequestDTO requestNullUnitId =
                new SpecificationRequestDTO(saveSpec.getName(), saveSpec.getDataType(), saveSpec.getFilterable(), null);

        when(repository.save(saveSpec)).thenReturn(specification);
        when(assembler.toModel(specificationDTO)).thenReturn(specificationDTO);

        service.save(requestNullUnitId);

        verify(unitRepository, never()).findById(anyLong());
    }

    @Test
    void shouldUpdateSpecificationWhenUpdate() {
        Specification updateEntity = SpecificationBuilder.builder()
                .withId(validId)
                .withName("Terabyte")
                .withSpecificationType(specification.getDataType())
                .withFilterable(specification.getFilterable())
                .build();

        SpecificationRequestDTO updateRequest = new SpecificationRequestDTO(
                updateEntity.getName(),
                updateEntity.getDataType(),
                updateEntity.getFilterable(),
                updateEntity.getUnit().getId());

        SpecificationDTO dtoUpdate = new SpecificationDTO(updateEntity);

        when(repository.findById(validId)).thenReturn(Optional.of(specification));
        when(repository.save(any(Specification.class))).thenReturn(updateEntity);
        when(assembler.toModel(dtoUpdate)).thenReturn(dtoUpdate);

        SpecificationDTO actual = service.update(validId, updateRequest);

        assertEquals(updateEntity.getId(), actual.getId());
        assertEquals(updateEntity.getName(), actual.getName());
        assertEquals(updateEntity.getDataType(), actual.getDataType());
        assertEquals(updateEntity.getFilterable(), actual.getFilterable());

        verify(unitRepository, never()).findById(anyLong());
    }

    @Test
    void shouldCallUnitFindByIdWhenSpecificationUnitIdAndRequestUnitIdIsDifferent() {
        Specification updateEntity = SpecificationBuilder.builder()
                .withId(validId)
                .withName("Terabyte")
                .withSpecificationType(specification.getDataType())
                .withFilterable(specification.getFilterable())
                .build();

        SpecificationRequestDTO updateRequest = new SpecificationRequestDTO(
                updateEntity.getName(),
                updateEntity.getDataType(),
                updateEntity.getFilterable(),
                2L);

        when(repository.findById(validId)).thenReturn(Optional.of(specification));
        when(unitRepository.findById(anyLong())).thenReturn(Optional.of(unit));
        when(repository.save(any(Specification.class))).thenReturn(updateEntity);
        when(assembler.toModel(any(SpecificationDTO.class))).thenReturn(any(SpecificationDTO.class));

        SpecificationDTO actual = service.update(validId, updateRequest);

        verify(unitRepository).findById(anyLong());
    }

    @Test
    void shouldNotCallUnitFindByIdWhenUpdatingWithNullId() {
        Specification updateEntity = SpecificationBuilder.builder()
                .withId(validId)
                .withName("Terabyte")
                .withSpecificationType(specification.getDataType())
                .withFilterable(specification.getFilterable())
                .build();

        SpecificationRequestDTO requestNullUnitId = new SpecificationRequestDTO(
                updateEntity.getName(),
                updateEntity.getDataType(),
                updateEntity.getFilterable(),
                null);

        when(repository.findById(anyLong())).thenReturn(Optional.of(specification));
        when(repository.save(any(Specification.class))).thenReturn(updateEntity);
        when(assembler.toModel(any(SpecificationDTO.class))).thenReturn(any(SpecificationDTO.class));

        service.update(validId, requestNullUnitId);

        verify(unitRepository, never()).findById(anyLong());
    }

    @Test
    void shouldThrownResourceNotFoundWhenUnitIdNotFound() {
        SpecificationRequestDTO updateRequest = new SpecificationRequestDTO(
                "Terabyte",
                specification.getDataType(),
                specification.getFilterable(),
                2L);

        when(repository.findById(validId)).thenReturn(Optional.of(specification));
        when(unitRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.update(validId, updateRequest));

        assertEquals("Unit not found!", actual.getMessage());
        verify(unitRepository).findById(anyLong());
    }

    @Test
    void shouldDeleteSpecificationWhenIdExists() {
        when(repository.findById(validId)).thenReturn(Optional.of(specification));
        doNothing().when(repository).delete(specification);

        service.delete(validId);

        verify(repository).findById(validId);
        verify(repository).delete(specification);
    }

    @Test
    void shouldThrownExceptionWhenIdDoesNotExistInDelete() {
        when(repository.findById(invalidID)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.delete(invalidID));

        assertEquals("Specification not found!", actual.getMessage());

        verify(repository, never()).delete(any(Specification.class));
    }

    @Test
    void shouldThrownEntityInUseWhenDeletingEntityWithRelationships() {
        when(repository.findById(validId)).thenReturn(Optional.of(specification));
        doNothing().when(repository).delete(specification);
        doThrow(new DataIntegrityViolationException("FK constraint"))
                .when(repository)
                .flush();

        EntityInUseException actual = assertThrows(EntityInUseException.class, () ->
                service.delete(validId));

        assertEquals("Specification is in use and cannot be deleted", actual.getMessage());

        verify(repository).delete(specification);
    }
}
