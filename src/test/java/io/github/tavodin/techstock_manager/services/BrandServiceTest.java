package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.BrandAssembler;
import io.github.tavodin.techstock_manager.dto.BrandDTO;
import io.github.tavodin.techstock_manager.dto.BrandRequestDTO;
import io.github.tavodin.techstock_manager.entities.Brand;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.BrandRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository repository;

    @Mock
    private BrandAssembler assembler;

    @Mock
    private PagedResourcesAssembler<Brand> pagedAssembler;

    @InjectMocks
    private BrandService service;

    private Long validId = 1L;
    private Long invalidId = 2L;
    private String notFoundMsg = "Brand not found";
    private String entityInUseMsg = "Brand is in use and cannot be deleted";

    private Brand brand;
    private BrandDTO dto;
    private BrandRequestDTO request;

    @BeforeEach
    void setUp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 05, 25, 17, 30);
        LocalDateTime updatedAt = createdAt.plusHours(1);

        brand = new Brand();
        brand.setId(validId);
        brand.setName("DELL");
        brand.setCreatedAt(createdAt);
        brand.setUpdatedAt(updatedAt);

        dto = new BrandDTO(brand);

        request = new BrandRequestDTO(brand.getName());
    }

    @Test
    void shouldReturnBrandDTOWhenFindingWithValidId() {
        when(repository.findById(validId)).thenReturn(Optional.of(brand));
        when(assembler.toModel(brand)).thenReturn(dto);

        BrandDTO actual = service.findById(validId);

        assertEquals(validId, actual.getId());
        assertEquals(dto.getName(), actual.getName());
        assertEquals(dto.getCreatedAt(), actual.getCreatedAt());
        assertEquals(dto.getUpdatedAt(), actual.getUpdatedAt());
    }

    @Test
    void shouldThrownResourceNotFoundExceptionWhenFindingWithInvalidId() {
        when(repository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class,
                () -> service.findById(invalidId));

        assertEquals(notFoundMsg, actual.getMessage());

        verify(assembler, never()).toModel(any(Brand.class));
    }

    @Test
    void shouldReturnPagedModelWhenFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Brand> page = new PageImpl<>(List.of(brand));
        PagedModel<BrandDTO> pagedModel = mock(PagedModel.class);

        when(repository.findAll(pageable)).thenReturn(page);
        when(pagedAssembler.toModel(page, assembler)).thenReturn(pagedModel);

        PagedModel<BrandDTO> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(pagedModel, result);

        verify(repository).findAll(pageable);
        verify(pagedAssembler).toModel(page, assembler);
    }

    @Test
    void shouldReturnBrandDTOWhenSavingWithValidData() {
        when(repository.save(any(Brand.class))).thenReturn(brand);
        when(assembler.toModel(brand)).thenReturn(dto);

        BrandDTO actual = service.save(request);

        assertNotNull(actual);

        ArgumentCaptor<Brand> captor =
                ArgumentCaptor.forClass(Brand.class);

        verify(repository).save(captor.capture());

        Brand captured = captor.getValue();

        assertEquals(request.name(), captured.getName());
    }

    @Test
    void shouldReturnBrandDTOWhenUpdatingWithValidData() {
        BrandRequestDTO updateRequest = new BrandRequestDTO("HP");

        when(repository.findById(validId)).thenReturn(Optional.of(brand));
        when(repository.save(any(Brand.class))).thenReturn(brand);
        when(assembler.toModel(brand)).thenReturn(dto);

        BrandDTO actual = service.update(validId, updateRequest);

        assertNotNull(actual);

        ArgumentCaptor<Brand> captor =
                ArgumentCaptor.forClass(Brand.class);

        verify(repository).save(captor.capture());

        Brand captured = captor.getValue();

        assertEquals(updateRequest.name(), captured.getName());
    }

    @Test
    void shouldThrownResourceNotFoundExceptionWhenUpdatingWithInvalidId() {
        when(repository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class,
                () -> service.findById(invalidId));

        assertEquals(notFoundMsg, actual.getMessage());

        verify(repository, never()).save(any(Brand.class));
        verify(assembler, never()).toModel(any(Brand.class));
    }

    @Test
    void shouldDoNothingWhenDeletingWithValidId() {
        when(repository.findById(validId)).thenReturn(Optional.of(brand));
        doNothing().when(repository).delete(brand);

        service.delete(validId);

        verify(repository).delete(brand);
        verify(repository).flush();
    }

    @Test
    void shouldThrownResourceNotFoundExceptionWhenDeletingWithInvalidId() {
        when(repository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class,
                () -> service.delete(invalidId));

        assertEquals(notFoundMsg, actual.getMessage());

        verify(repository, never()).delete(brand);
        verify(repository, never()).flush();
    }

    @Test
    void shouldThrownEntityInUseWhenDeletingEntityWithRelationships() {
        when(repository.findById(validId)).thenReturn(Optional.of(brand));
        doNothing().when(repository).delete(brand);
        doThrow(new DataIntegrityViolationException("FK constraint"))
                .when(repository)
                .flush();

        EntityInUseException actual = assertThrows(EntityInUseException.class,
                () -> service.delete(validId));

        assertEquals(entityInUseMsg, actual.getMessage());

        verify(repository).delete(brand);
    }
}
