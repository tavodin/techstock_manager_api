package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.CategoryAssembler;
import io.github.tavodin.techstock_manager.dto.CategoryDTO;
import io.github.tavodin.techstock_manager.dto.CategoryRequestDTO;
import io.github.tavodin.techstock_manager.entities.Category;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.CategoryRepository;
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
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @Mock
    private CategoryAssembler assembler;

    @Mock
    private PagedResourcesAssembler<Category> pagedAssembler;

    @InjectMocks
    private CategoryService service;

    private Long validId = 1L;
    private Long invalidId = 2L;
    private String notFoundMsg = "Category not found!";
    private String entityInUseMsg = "Category is in use and cannot be deleted";

    private Category category = new Category();
    private CategoryDTO categoryDTO;
    private CategoryRequestDTO request;

    @BeforeEach
    void setUp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 30, 10, 0);
        LocalDateTime updatedAt = createdAt.plusHours(1L);

        category.setId(validId);
        category.setName("Monitor");
        category.setCreatedAt(createdAt);
        category.setUpdatedAt(updatedAt);

        categoryDTO = new CategoryDTO(category);
        request = new CategoryRequestDTO(category.getName());
    }

    @Test
    void shouldReturnCategoryDTOWhenFindingWithValidId() {
        when(repository.findById(validId)).thenReturn(Optional.of(category));
        when(assembler.toModel(category)).thenReturn(categoryDTO);

        CategoryDTO findCategory = service.findById(validId);

        assertEquals(categoryDTO.getId(), findCategory.getId());
        assertEquals(categoryDTO.getName(), findCategory.getName());
        assertEquals(categoryDTO.getCreatedAt(), findCategory.getCreatedAt());
        assertEquals(categoryDTO.getUpdatedAt(), findCategory.getUpdatedAt());

        verify(repository).findById(validId);
        verify(assembler).toModel(category);
    }

    @Test
    void shouldThrowResourceNotFoundWhenFindingWithInvalidId() {
        when(repository.findById(validId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                service.findById(validId));

        assertEquals(notFoundMsg, exception.getMessage());

        verify(repository).findById(validId);
        verify(assembler, never()).toModel(any());
    }

    @Test
    void shouldReturnPagedModelWhenFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> page = new PageImpl<>(List.of(category));
        PagedModel<CategoryDTO> pagedModel = mock(PagedModel.class);

        when(repository.findAll(pageable)).thenReturn(page);
        when(pagedAssembler.toModel(page, assembler)).thenReturn(pagedModel);

        PagedModel<CategoryDTO> actual = service.findAll(pageable);

        assertNotNull(actual);
        assertEquals(pagedModel, actual);

        verify(repository).findAll(pageable);
        verify(pagedAssembler).toModel(page, assembler);
    }

    @Test
    void shouldReturnCategoryDTOWhenSaving() {
        Category savedCategory = new Category();
        savedCategory.setName(categoryDTO.getName());

        when(repository.save(savedCategory)).thenReturn(category);
        when(assembler.toModel(category)).thenReturn(categoryDTO);

        CategoryDTO actual = service.save(request);

        assertEquals(categoryDTO.getId(), actual.getId());
        assertEquals(categoryDTO.getName(), actual.getName());
        assertEquals(categoryDTO.getCreatedAt(), actual.getCreatedAt());
        assertEquals(categoryDTO.getUpdatedAt(), actual.getUpdatedAt());
    }

    @Test
    void shouldReturnCategoryDTOWhenUpdatingWithValidId() {
        CategoryRequestDTO updateRequest = new CategoryRequestDTO("Mouse");

        Category updateCategory = new Category();
        updateCategory.setId(category.getId());
        updateCategory.setName(updateRequest.name());

        category.setName(updateCategory.getName());

        when(repository.findById(validId)).thenReturn(Optional.of(category));
        when(repository.save(updateCategory)).thenReturn(category);
        when(assembler.toModel(category)).thenReturn(categoryDTO);

        CategoryDTO actual = service.update(validId, updateRequest);

        assertEquals(categoryDTO.getId(), actual.getId());
        assertEquals(categoryDTO.getName(), actual.getName());
        assertEquals(categoryDTO.getCreatedAt(), actual.getCreatedAt());
        assertEquals(categoryDTO.getUpdatedAt(), actual.getUpdatedAt());
    }

    @Test
    void shouldThrowResourceNotFoundWhenUpdatingWithInvalidId() {
        when(repository.findById(invalidId))
                .thenThrow(new ResourceNotFoundException(notFoundMsg));

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.update(invalidId, request));

        assertEquals(notFoundMsg, actual.getMessage());
        verify(repository, never()).save(any(Category.class));
        verify(assembler, never()).toModel(any(Category.class));
    }

    @Test
    void shouldDoNothingWhenDeletingWithValidId() {
        when(repository.findById(validId)).thenReturn(Optional.of(category));
        doNothing().when(repository).delete(category);

        service.delete(validId);

        verify(repository).delete(category);
        verify(repository).flush();
    }

    @Test
    void shouldThrowResourceNotFoundWhenDeletingWithInvalidId() {
        when(repository.findById(invalidId))
                .thenThrow(new ResourceNotFoundException(notFoundMsg));

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class, () ->
                service.update(invalidId, request));

        assertEquals(notFoundMsg, actual.getMessage());
        verify(repository, never()).delete(any(Category.class));
        verify(repository, never()).flush();
    }

    @Test
    void shouldThrownEntityInUseWhenDeletingEntityWithRelationships() {
        when(repository.findById(validId)).thenReturn(Optional.of(category));
        doNothing().when(repository).delete(category);
        doThrow(new DataIntegrityViolationException("FK constraint"))
                .when(repository)
                .flush();

        EntityInUseException actual = assertThrows(EntityInUseException.class, () ->
                service.delete(validId));

        assertEquals(entityInUseMsg, actual.getMessage());

        verify(repository).delete(category);
    }
}
