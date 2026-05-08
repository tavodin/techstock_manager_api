package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.UnitAssembler;
import io.github.tavodin.techstock_manager.dto.UnitDTO;
import io.github.tavodin.techstock_manager.dto.UnitRequestDTO;
import io.github.tavodin.techstock_manager.entities.Unit;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private UnitRepository repository;

    @Mock
    private UnitAssembler assembler;

    @Mock
    private PagedResourcesAssembler<Unit> pagedAssembler;

    @InjectMocks
    private UnitService service;

    Long existId;
    Long nonExistId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    Unit unit;
    UnitDTO unitDTO;
    UnitRequestDTO request;

    @BeforeEach
    void setUp() {
        existId = 1L;
        nonExistId = 100L;
        createdAt = LocalDateTime.of(2026, 3, 30, 10, 0);
        updatedAt = createdAt.plusHours(1L);

        unit = new Unit(
                existId,
                createdAt,
                updatedAt,
                "Hertz",
                "Hz");

        unitDTO = new UnitDTO(
                unit.getId(),
                unit.getName(),
                unit.getSymbol(),
                unit.getCreatedAt(),
                unit.getUpdatedAt());

        request = new UnitRequestDTO("Gigahertz", "GHz");
    }

    @Test
    void shouldReturnUnitDTOWhenIdExist() {
        when(repository.findById(existId)).thenReturn(Optional.of(unit));
        when(assembler.toModel(unit)).thenReturn(unitDTO);

        UnitDTO findUnit = service.findById(existId);

        assertNotNull(findUnit);
        assertEquals(existId, findUnit.getId());
        assertEquals("Hertz", findUnit.getName());
        assertEquals("Hz", findUnit.getSymbol());
        assertEquals(createdAt, findUnit.getCreatedAt());
        assertEquals(updatedAt, findUnit.getUpdatedAt());
        verify(repository).findById(existId);
        verify(assembler).toModel(unit);
    }

    @Test
    void shouldThrowExceptionWhenFindingUnitWithInvalidId() {
        when(repository.findById(nonExistId)).thenReturn(Optional.empty());

        String expectedMessage = "Unit not found!";

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.findById(nonExistId)
        );

        assertEquals(expectedMessage, actual.getMessage());
    }

    @Test
    void shouldReturnPagedModelWhenFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Unit> page = new PageImpl<>(List.of(unit));
        PagedModel<UnitDTO> pagedModel = mock(PagedModel.class);

        when(repository.findAll(pageable)).thenReturn(page);
        when(pagedAssembler.toModel(page, assembler)).thenReturn(pagedModel);

        PagedModel<UnitDTO> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(pagedModel, result);

        verify(repository).findAll(pageable);
        verify(pagedAssembler).toModel(page, assembler);
    }

    @Test
    void shouldSaveUnitWhenSave() {
        Unit savedUnit = new Unit("Hertz", "Hz");
        savedUnit.setId(1L);

        when(repository.save(any(Unit.class))).thenReturn(savedUnit);
        when(assembler.toModel(savedUnit)).thenReturn(unitDTO);

        UnitDTO result = service.save(request);

        assertNotNull(result);
        assertEquals("Hertz", result.getName());
        assertEquals("Hz", result.getSymbol());

        verify(repository).save(any(Unit.class));
        verify(assembler).toModel(savedUnit);
    }

    @Test
    void shouldUpdateUnitWhenUpdate() {
        unit.setName(request.name());
        unit.setSymbol(request.symbol());

        unitDTO.setName(request.name());
        unitDTO.setSymbol(request.symbol());

        when(repository.findById(existId)).thenReturn(Optional.of(unit));
        when(repository.saveAndFlush(any(Unit.class))).thenReturn(unit);
        when(assembler.toModel(unit)).thenReturn(unitDTO);

        UnitDTO updatedUnit = service.update(existId, request);

        assertNotNull(updatedUnit);
        assertEquals("Gigahertz", updatedUnit.getName());
        assertEquals("GHz", updatedUnit.getSymbol());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithInvalidId() {
        when(repository.findById(nonExistId)).thenReturn(Optional.empty());

        String expectedMessage = "Unit not found!";

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.update(nonExistId, request)
        );

        assertEquals(expectedMessage, actual.getMessage());
    }

    @Test
    void shouldDeleteUnitWhenIdExist() {
        when(repository.findById(existId)).thenReturn(Optional.of(unit));
        doNothing().when(repository).delete(unit);

        service.delete(existId);

        verify(repository).findById(existId);
        verify(repository).delete(unit);
    }

    @Test
    void shouldThrowExceptionWhenDeletingWithInvalidId() {
        when(repository.findById(nonExistId)).thenReturn(Optional.empty());

        String expectedMessage = "Unit not found!";

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.delete(nonExistId)
        );

        assertEquals(expectedMessage, actual.getMessage());
    }

    @Test
    void shouldThrownEntityInUseWhenDeletingEntityWithRelationships() {
        when(repository.findById(existId)).thenReturn(Optional.of(unit));
        doNothing().when(repository).delete(unit);
        doThrow(new DataIntegrityViolationException("FK constraint"))
                .when(repository)
                .flush();

        EntityInUseException actual = assertThrows(EntityInUseException.class, () ->
                service.delete(existId));

        assertEquals("Unit is in use and cannot be deleted", actual.getMessage());

        verify(repository).delete(unit);
    }
}