package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.SupplierAssembler;
import io.github.tavodin.techstock_manager.dto.SupplierDTO;
import io.github.tavodin.techstock_manager.dto.SupplierRequestDTO;
import io.github.tavodin.techstock_manager.entities.Supplier;
import io.github.tavodin.techstock_manager.exceptions.AlreadyExistsException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SupplierServiceTest {

    @Mock
    private SupplierRepository repository;

    @Mock
    private SupplierAssembler assembler;

    @Mock
    private PagedResourcesAssembler<Supplier> pagedAssembler;

    @InjectMocks
    private SupplierService service;

    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 2L;
    private static final String SUPPLIER_NOT_FOUND_MSG = "Supplier not found";
    private static final String EXIST_DOCUMENT_MSG = "A record with the same document value already exists";
    private static final String EXIST_EMAIL_MSG = "Email already registered";

    private Supplier supplier;
    private SupplierDTO dto;
    private SupplierRequestDTO request;

    @BeforeEach
    void setUp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 07, 20, 14, 02);
        LocalDateTime updatedAt = createdAt.plusHours(1L);

        supplier = new Supplier(
            "TechParts",
                "00000000000191",
                "techparts@gmail.com",
                "6137644711",
                true
        );
        supplier.setId(VALID_ID);
        supplier.setCreatedAt(createdAt);
        supplier.setUpdatedAt(updatedAt);

        dto = new SupplierDTO(supplier);

        request = new SupplierRequestDTO(
                supplier.getName(),
                supplier.getDocument(),
                supplier.getEmail(),
                supplier.getPhone()
        );
    }

    @Test
    void shouldReturnSupplierDTOWhenFindingWithValidId() {
        when(repository.findById(VALID_ID)).thenReturn(Optional.of(supplier));
        when(assembler.toModel(supplier)).thenReturn(dto);

        SupplierDTO actual = service.findById(VALID_ID);

        assertNotNull(actual);

        verify(repository).findById(VALID_ID);
        verify(assembler).toModel(supplier);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenFindingWithInvalidId() {
        when(repository.findById(INVALID_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.findById(INVALID_ID));

        assertEquals(SUPPLIER_NOT_FOUND_MSG, actual.getMessage());

        verify(assembler, never()).toModel(any(Supplier.class));
    }

    @Test
    void shouldReturnPagedModelWhenFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> page = new PageImpl<>(List.of(supplier));
        PagedModel<SupplierDTO> pagedModel = mock(PagedModel.class);

        when(repository.findAll(pageable)).thenReturn(page);
        when(pagedAssembler.toModel(page, assembler)).thenReturn(pagedModel);

        PagedModel<SupplierDTO> actual = service.findAll(pageable);

        assertNotNull(actual);

        verify(repository).findAll(pageable);
        verify(pagedAssembler).toModel(page, assembler);
    }

    @Test
    void shouldReturnSupplierDTOWhenSavingWithValidData() {
        when(repository.existsByDocument(request.getDocument())).thenReturn(false);
        when(repository.existsByEmail(request.getEmail())).thenReturn(false);
        when(repository.save(any(Supplier.class))).thenReturn(supplier);
        when(assembler.toModel(supplier)).thenReturn(dto);

        SupplierDTO actual = service.save(request);

        assertNotNull(actual);

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(repository).save(captor.capture());
        Supplier captured = captor.getValue();

        assertEquals(request.getName(), captured.getName());
        assertEquals(request.getDocument(), captured.getDocument());
        assertEquals(request.getEmail(), captured.getEmail());
        assertEquals(request.getPhone(), captured.getPhone());
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenSavingWithExistDocument() {
        when(repository.existsByDocument(request.getDocument()))
                .thenReturn(true);

        AlreadyExistsException actual = assertThrows(AlreadyExistsException.class, () ->
                service.save(request));

        assertEquals(EXIST_DOCUMENT_MSG, actual.getMessage());
        verify(repository, never()).existsByEmail(anyString());
        verify(assembler, never()).toModel(any(Supplier.class));
    }

    @Test
    void shouldThrowAlreadyExistsExceptionSavingWithWithExistEmail() {
        when(repository.existsByDocument(request.getDocument()))
                .thenReturn(false);
        when(repository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        AlreadyExistsException actual = assertThrows(AlreadyExistsException.class, () ->
                service.save(request));

        assertEquals(EXIST_EMAIL_MSG, actual.getMessage());
        verify(repository).existsByDocument(anyString());
        verify(assembler, never()).toModel(any(Supplier.class));
    }

    @Test
    void shouldReturnSupplierDTOWhenUpdatingWithValidData() {
        request.setName("Distribuidora A");

        when(repository.existsByDocumentAndIdNot(request.getDocument(), VALID_ID)).thenReturn(false);
        when(repository.existsByEmailAndIdNot(request.getEmail(), VALID_ID)).thenReturn(false);
        when(repository.findById(VALID_ID)).thenReturn(Optional.of(supplier));
        when(repository.save(any(Supplier.class))).thenReturn(supplier);
        when(assembler.toModel(supplier)).thenReturn(dto);

        SupplierDTO actual = service.update(VALID_ID, request);

        assertNotNull(actual);

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(repository).save(captor.capture());
        Supplier captured = captor.getValue();

        assertEquals(request.getName(), captured.getName());
        assertEquals(request.getDocument(), captured.getDocument());
        assertEquals(request.getEmail(), captured.getEmail());
        assertEquals(request.getPhone(), captured.getPhone());
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenUpdatingWithExistDocument() {
        when(repository.existsByDocumentAndIdNot(request.getDocument(), VALID_ID))
                .thenReturn(true);

        AlreadyExistsException actual = assertThrows(AlreadyExistsException.class, () ->
                service.update(VALID_ID, request));

        assertEquals(EXIST_DOCUMENT_MSG, actual.getMessage());
        verify(repository, never()).existsByEmailAndIdNot(anyString(), anyLong());
        verify(repository, never()).findById(anyLong());
        verify(assembler, never()).toModel(any(Supplier.class));
    }

    @Test
    void shouldThrowAlreadyExistsExceptionWhenUpdatingWithExistEmail() {
        when(repository.existsByDocumentAndIdNot(request.getDocument(), VALID_ID))
                .thenReturn(false);
        when(repository.existsByEmailAndIdNot(request.getEmail(), VALID_ID))
                .thenReturn(true);

        AlreadyExistsException actual = assertThrows(AlreadyExistsException.class, () ->
                service.update(VALID_ID, request));

        assertEquals(EXIST_EMAIL_MSG, actual.getMessage());
        verify(repository).existsByDocumentAndIdNot(anyString(), anyLong());
        verify(repository, never()).findById(anyLong());
        verify(assembler, never()).toModel(any(Supplier.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingInvalidId() {
        when(repository.existsByDocumentAndIdNot(request.getDocument(), INVALID_ID))
                .thenReturn(false);
        when(repository.existsByEmailAndIdNot(request.getEmail(), INVALID_ID))
                .thenReturn(false);
        when(repository.findById(INVALID_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.update(INVALID_ID, request));

        assertEquals(SUPPLIER_NOT_FOUND_MSG, actual.getMessage());
        verify(repository).existsByDocumentAndIdNot(anyString(), anyLong());
        verify(repository).existsByEmailAndIdNot(anyString(), anyLong());
        verify(assembler, never()).toModel(any(Supplier.class));
    }

    @Test
    void shouldSetActiveToFalseWhenDeletingWithValidId() {
        when(repository.findById(VALID_ID)).thenReturn(Optional.of(supplier));

        service.delete(VALID_ID);

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(repository).save(captor.capture());
        Supplier captured = captor.getValue();

        assertEquals(false, captured.getActive());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingWithInvalidId() {
        when(repository.findById(INVALID_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.delete(INVALID_ID));

        assertEquals(SUPPLIER_NOT_FOUND_MSG, actual.getMessage());
    }
}
