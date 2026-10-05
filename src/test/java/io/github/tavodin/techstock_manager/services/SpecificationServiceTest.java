package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.SpecificationAssembler;
import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.dto.SpecificationAutocompleteDTO;
import io.github.tavodin.techstock_manager.dto.SpecificationDTO;
import io.github.tavodin.techstock_manager.dto.SpecificationLoadUpdateDTO;
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
import org.mockito.ArgumentCaptor;
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

    private final static Long VALID_ID = 1L;
    private final static Long INVALID_ID = 2L;
    private final static String NOT_FOUND_MSG = "Specification not found!";

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

    }

    @Test
    void shouldReturnSpecificationDTOWhenIdExists() {
        when(repository.getSpecificationById(VALID_ID)).thenReturn(Optional.of(specificationDTO));
        when(assembler.toModel(any(SpecificationDTO.class))).thenReturn(specificationDTO);

        SpecificationDTO findUnit = service.findById(VALID_ID);

        assertNotNull(findUnit.getId());
        assertEquals(specificationDTO.getName(), findUnit.getName());
        assertEquals(specificationDTO.getDataType(), findUnit.getDataType());
        assertEquals(specificationDTO.getFilterable(), findUnit.getFilterable());
        assertEquals(specificationDTO.getCreatedAt(), findUnit.getCreatedAt());
        assertEquals(specificationDTO.getUpdatedAt(), findUnit.getUpdatedAt());

        verify(repository).getSpecificationById(VALID_ID);
        verify(assembler).toModel(specificationDTO);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
        when(repository.getSpecificationById(INVALID_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.findById(INVALID_ID)
        );

        assertEquals("Specification not found!", actual.getMessage());
    }

    @Test
    void shouldReturnSpecificationLoadUpdateDTOWhenGetSpecificationForUpdateWithValidId() {
        SpecificationLoadUpdateDTO spec = new SpecificationLoadUpdateDTO(
                specification.getName(),
                specification.getDataType(),
                specification.getFilterable(),
                null,
                null
        );

        when(repository.loadSpecificationToUpdateFields(VALID_ID))
                .thenReturn(Optional.of(spec));

        SpecificationLoadUpdateDTO result = service.getSpecificationForUpdate(VALID_ID);

        assertEquals(specification.getName(), result.getName());
        assertEquals(specification.getDataType(), result.getDataType());
        assertEquals(specification.getFilterable(), result.getFilterable());
        assertNull(result.getUnit());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenGetSpecificationForUpdateWithInvalidId() {
        when(repository.loadSpecificationToUpdateFields(INVALID_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException result = assertThrows(ResourceNotFoundException.class, () ->
            service.getSpecificationForUpdate(INVALID_ID)
        );

        assertEquals(NOT_FOUND_MSG, result.getMessage());
    }

    @Test
    void shouldReturnSpecificationAutocompleteDTOListWhenGetSpecificationsByName() {
        Pageable pageable = PageRequest.of(0, 5);

        SpecificationAutocompleteDTO spec = new SpecificationAutocompleteDTO(
                specification.getId(),
                specification.getName()
        );

        Page<SpecificationAutocompleteDTO> page = new PageImpl<>(
                List.of(spec),
                pageable,
                1
        );

        when(repository.getSpecificationsByName(specification.getName(), pageable)).thenReturn(page);

        List<SpecificationAutocompleteDTO> result = service.getSpecificationsByName(specification.getName());

        assertTrue(!result.isEmpty());
    }

    @Test
    void shouldTrimStringWhenGetSpecificationsByNameWithNameWithSpaces() {
        Pageable pageable = PageRequest.of(0, 5);

        SpecificationAutocompleteDTO spec = new SpecificationAutocompleteDTO(
                specification.getId(),
                specification.getName()
        );

        Page<SpecificationAutocompleteDTO> page = new PageImpl<>(
                List.of(spec),
                pageable,
                1
        );

        String nameWithSpaces = "  " + specification.getName() + "  ";

        when(repository.getSpecificationsByName(eq(specification.getName()), any(Pageable.class)))
                .thenReturn(page);

        service.getSpecificationsByName(nameWithSpaces);

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

        verify(repository).getSpecificationsByName(nameCaptor.capture(), eq(pageable));

        assertEquals(specification.getName(), nameCaptor.getValue());
    }

    @Test
    void shouldReturnNullWhenGetSpecificationsByNameWithNullName() {
        Pageable pageable = PageRequest.of(0, 5);

        SpecificationAutocompleteDTO spec = new SpecificationAutocompleteDTO(
                specification.getId(),
                specification.getName()
        );

        Page<SpecificationAutocompleteDTO> page = new PageImpl<>(
                List.of(spec),
                pageable,
                1
        );

        when(repository.getSpecificationsByName(eq(null), any(Pageable.class)))
                .thenReturn(page);

        service.getSpecificationsByName("");

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

        verify(repository).getSpecificationsByName(nameCaptor.capture(), eq(pageable));

        assertNull(nameCaptor.getValue());
    }

    @Test
    void shouldReturnPagedModelWhenFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SpecificationDTO> page = new PageImpl<>(List.of(specificationDTO));
        PagedModel<SpecificationDTO> pagedModel = mock(PagedModel.class);

        when(repository.findAllProjected(null, pageable)).thenReturn(page);
        when(pagedAssembler.toModel(page, assembler)).thenReturn(pagedModel);

        PagedModel<SpecificationDTO> actual = service.findAll(null, pageable);

        assertNotNull(actual);
        assertEquals(pagedModel, actual);

        verify(repository).findAllProjected(null, pageable);
        verify(pagedAssembler).toModel(page, assembler);
    }

    @Test
    void shouldTrimStringWhenWhenFindAllWithNameWithSpaces() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SpecificationDTO> page = new PageImpl<>(List.of(specificationDTO));
        PagedModel<SpecificationDTO> pagedModel = mock(PagedModel.class);

        String nameWithSpaces = "  " + specification.getName() + "  ";

        when(repository.findAllProjected(specification.getName(), pageable))
                .thenReturn(page);
        when(pagedAssembler.toModel(page, assembler)).thenReturn(pagedModel);

        service.findAll(nameWithSpaces, pageable);

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(repository).findAllProjected(nameCaptor.capture(), eq(pageable));

        assertEquals(specification.getName(), nameCaptor.getValue());
    }

    @Test
    void shouldReturnNullWhenWhenFindAllWithNullName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SpecificationDTO> page = new PageImpl<>(List.of(specificationDTO));
        PagedModel<SpecificationDTO> pagedModel = mock(PagedModel.class);

        when(repository.findAllProjected(null, pageable))
                .thenReturn(page);
        when(pagedAssembler.toModel(page, assembler)).thenReturn(pagedModel);

        service.findAll("", pageable);

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(repository).findAllProjected(nameCaptor.capture(), eq(pageable));

        assertNull(nameCaptor.getValue());
    }

    @Test
    void shouldSaveSpecificationWhenSave() {
        Specification saveSpecification = SpecificationBuilder.builder().withId(null).build();

        when(unitRepository.findById(VALID_ID)).thenReturn(Optional.of(unit));
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
                INVALID_ID);

        when(unitRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

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
                .withId(VALID_ID)
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

        when(repository.findById(VALID_ID)).thenReturn(Optional.of(specification));
        when(repository.save(any(Specification.class))).thenReturn(updateEntity);
        when(assembler.toModel(dtoUpdate)).thenReturn(dtoUpdate);

        SpecificationDTO actual = service.update(VALID_ID, updateRequest);

        assertEquals(updateEntity.getId(), actual.getId());
        assertEquals(updateEntity.getName(), actual.getName());
        assertEquals(updateEntity.getDataType(), actual.getDataType());
        assertEquals(updateEntity.getFilterable(), actual.getFilterable());

        verify(unitRepository, never()).findById(anyLong());
    }

    @Test
    void shouldCallUnitFindByIdWhenUpdatingWithDifferentSpecificationUnitIdAndRequestUnitId() {
        Specification updateEntity = SpecificationBuilder.builder()
                .withId(VALID_ID)
                .withName("Terabyte")
                .withSpecificationType(specification.getDataType())
                .withFilterable(specification.getFilterable())
                .build();

        SpecificationRequestDTO updateRequest = new SpecificationRequestDTO(
                updateEntity.getName(),
                updateEntity.getDataType(),
                updateEntity.getFilterable(),
                2L);

        when(repository.findById(VALID_ID)).thenReturn(Optional.of(specification));
        when(unitRepository.findById(anyLong())).thenReturn(Optional.of(unit));
        when(repository.save(any(Specification.class))).thenReturn(updateEntity);
        when(assembler.toModel(any(SpecificationDTO.class))).thenReturn(any(SpecificationDTO.class));

        service.update(VALID_ID, updateRequest);

        verify(unitRepository).findById(anyLong());
    }

    @Test
    void shouldCallFindByIdWhenUpdatingWithValidRequestUnitIdAndNullSpecificationUnit() {
        specification.setUnit(null);
        when(repository.findById(VALID_ID)).thenReturn(Optional.of(specification));
        when(unitRepository.findById(anyLong())).thenReturn(Optional.of(unit));
        when(repository.save(any(Specification.class))).thenReturn(specification);

        SpecificationRequestDTO updateRequest = new SpecificationRequestDTO(
                "Terabyte",
                specification.getDataType(),
                specification.getFilterable(),
                2L
        );

        service.update(VALID_ID, updateRequest);

        verify(unitRepository).findById(updateRequest.unitId());
    }

    @Test
    void shouldNotCallUnitFindByIdWhenUpdatingWithNullId() {
        Specification updateEntity = SpecificationBuilder.builder()
                .withId(VALID_ID)
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

        service.update(VALID_ID, requestNullUnitId);

        verify(unitRepository, never()).findById(anyLong());
    }

    @Test
    void shouldThrownResourceNotFoundWhenUnitIdNotFound() {
        SpecificationRequestDTO updateRequest = new SpecificationRequestDTO(
                "Terabyte",
                specification.getDataType(),
                specification.getFilterable(),
                2L);

        when(repository.findById(VALID_ID)).thenReturn(Optional.of(specification));
        when(unitRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.update(VALID_ID, updateRequest));

        assertEquals("Unit not found!", actual.getMessage());
        verify(unitRepository).findById(anyLong());
    }

    @Test
    void shouldDeleteSpecificationWhenIdExists() {
        when(repository.findById(VALID_ID)).thenReturn(Optional.of(specification));
        doNothing().when(repository).delete(specification);

        service.delete(VALID_ID);

        verify(repository).findById(VALID_ID);
        verify(repository).delete(specification);
    }

    @Test
    void shouldThrownExceptionWhenDeletingWithInvalidId() {
        when(repository.findById(INVALID_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.delete(INVALID_ID));

        assertEquals("Specification not found!", actual.getMessage());

        verify(repository, never()).delete(any(Specification.class));
    }

    @Test
    void shouldThrownEntityInUseWhenDeletingEntityWithRelationships() {
        when(repository.findById(VALID_ID)).thenReturn(Optional.of(specification));
        doNothing().when(repository).delete(specification);
        doThrow(new DataIntegrityViolationException("FK constraint"))
                .when(repository)
                .flush();

        EntityInUseException actual = assertThrows(EntityInUseException.class, () ->
                service.delete(VALID_ID));

        assertEquals("Specification is in use and cannot be deleted", actual.getMessage());

        verify(repository).delete(specification);
    }
}
