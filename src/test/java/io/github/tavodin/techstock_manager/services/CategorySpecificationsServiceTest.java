package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.CategorySpecificationAssembler;
import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationDTO;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationRequestDTO;
import io.github.tavodin.techstock_manager.entities.Category;
import io.github.tavodin.techstock_manager.entities.CategorySpecification;
import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.CategoryRepository;
import io.github.tavodin.techstock_manager.repositories.CategorySpecificationRepository;
import io.github.tavodin.techstock_manager.repositories.SpecificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategorySpecificationsServiceTest {

    @Mock
    private CategorySpecificationRepository repository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SpecificationRepository specificationRepository;

    @Mock
    private CategorySpecificationAssembler assembler;

    @InjectMocks
    private CategorySpecificationService service;

    private CategorySpecification categorySpecification;
    private CategorySpecificationDTO catSpecDTO;
    private CategorySpecificationRequestDTO request;
    private Category category;
    private Specification specification;
    private Long validId = 1L;
    private Long invalidId = 2L;
    private String notFoundMsg = " not found!";

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(validId);
        category.setName("Monitor");

        specification = SpecificationBuilder.builder().build();

        categorySpecification = new CategorySpecification();
        categorySpecification.setCategory(category);
        categorySpecification.setSpecification(specification);
        categorySpecification.setRequired(true);

        catSpecDTO = new CategorySpecificationDTO(categorySpecification);

        request = new CategorySpecificationRequestDTO(category.getId(), specification.getId(), true);
    }

    @Test
    void shouldReturnCategorySpecificationDTOWhenSavingWithValidData() {
        CategorySpecification savedEntity =
                new CategorySpecification(validId, categorySpecification.getRequired(), category, specification);

        when(categoryRepository.findById(request.categoryId())).thenReturn(Optional.of(category));
        when(specificationRepository.findById(request.specificationId())).thenReturn(Optional.of(specification));

        when(repository.save(any(CategorySpecification.class))).thenReturn(savedEntity);
        when(assembler.toModel(savedEntity)).thenReturn(catSpecDTO);

        CategorySpecificationDTO actual = service.save(request);

        assertNotNull(actual);

        ArgumentCaptor<CategorySpecification> captor =
                ArgumentCaptor.forClass(CategorySpecification.class);

        verify(repository).save(captor.capture());

        CategorySpecification captured = captor.getValue();

        assertEquals(category, captured.getCategory());
        assertEquals(specification, captured.getSpecification());
        assertEquals(request.required(), captured.getRequired());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSavingWithInvalidCategoryId() {
        CategorySpecificationRequestDTO invalidRequest =
                new CategorySpecificationRequestDTO(invalidId, validId, true);

        String errorMsg = "Category" + notFoundMsg;

        when(categoryRepository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class,
                () -> service.save(invalidRequest));

        assertEquals(errorMsg, actual.getMessage());

        verify(specificationRepository, never()).findById(anyLong());
        verify(repository, never()).save(any());
        verify(assembler, never()).toModel(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSavingWithInvalidSpecificationId() {
        CategorySpecificationRequestDTO invalidRequest =
                new CategorySpecificationRequestDTO(validId, invalidId, true);

        String errorMsg = "Specification" + notFoundMsg;

        when(categoryRepository.findById(validId)).thenReturn(Optional.of(category));
        when(specificationRepository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class,
                () -> service.save(invalidRequest));

        assertEquals(errorMsg, actual.getMessage());

        verify(repository, never()).save(any());
        verify(assembler, never()).toModel(any());
    }

    @Test
    void shouldReturnCategorySpecificationDTOWhenUpdatingWithValidData() {
        CategorySpecification findEntity =
                new CategorySpecification(validId, categorySpecification.getRequired(), category, specification);

        Category newCat = new Category();
        newCat.setId(2L);
        newCat.setName("Teclado");

        Specification newSpec = SpecificationBuilder.builder().withId(2L).withName("RGB").build();

        CategorySpecificationRequestDTO updateRequest =
                new CategorySpecificationRequestDTO(newCat.getId(), newSpec.getId(), true);

        when(repository.findById(validId)).thenReturn(Optional.of(findEntity));
        when(categoryRepository.findById(updateRequest.categoryId())).thenReturn(Optional.of(newCat));
        when(specificationRepository.findById(updateRequest.specificationId())).thenReturn(Optional.of(newSpec));
        when(repository.save(any(CategorySpecification.class))).thenReturn(findEntity);
        when(assembler.toModel(findEntity)).thenReturn(catSpecDTO);

        CategorySpecificationDTO actual = service.update(validId, updateRequest);

        assertNotNull(actual);

        ArgumentCaptor<CategorySpecification> captor =
                ArgumentCaptor.forClass(CategorySpecification.class);

        verify(repository).save(captor.capture());

        CategorySpecification captured = captor.getValue();

        assertEquals(newCat, captured.getCategory());
        assertEquals(newSpec, captured.getSpecification());
        assertEquals(request.required(), captured.getRequired());
    }

    @Test
    void shouldNotSearchCategoryAgainWhenCategoryIdIsTheSame() {
        CategorySpecificationRequestDTO updateRequest =
                new CategorySpecificationRequestDTO(
                        category.getId(),
                        2L,
                        true
                );

        Specification anotherSpecification = new Specification();
        anotherSpecification.setId(2L);

        when(repository.findById(validId)).thenReturn(Optional.of(categorySpecification));
        when(specificationRepository.findById(2L)).thenReturn(Optional.of(anotherSpecification));
        when(repository.save(any(CategorySpecification.class))).thenReturn(categorySpecification);
        when(assembler.toModel(any(CategorySpecification.class))).thenReturn(catSpecDTO);

        service.update(validId, updateRequest);

        verify(categoryRepository, never()).findById(any());
        verify(specificationRepository).findById(2L);
    }

    @Test
    void shouldNotSearchSpecificationAgainWhenSpecificationIdIsTheSame() {
        Category anotherCategory = new Category();
        anotherCategory.setId(2L);

        CategorySpecificationRequestDTO updateRequest =
                new CategorySpecificationRequestDTO(
                        2L,
                        specification.getId(),
                        true
                );

        when(repository.findById(validId)).thenReturn(Optional.of(categorySpecification));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(anotherCategory));
        when(repository.save(any(CategorySpecification.class))).thenReturn(categorySpecification);
        when(assembler.toModel(any(CategorySpecification.class))).thenReturn(catSpecDTO);

        service.update(validId, updateRequest);

        verify(specificationRepository, never()).findById(any());
        verify(categoryRepository).findById(2L);
    }

    @Test
    void shouldThrownResourceNotFoundExceptionWhenUpdatingWithInvalidId() {
        String errorMsg = "Category Specification" + notFoundMsg;

        when(repository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class,
                () -> service.update(invalidId, request));

        assertEquals(errorMsg, actual.getMessage());
    }

    @Test
    void shouldThrownResourceNotFoundExceptionWhenUpdatingWithInvalidCategoryId() {
        CategorySpecificationRequestDTO invalidRequest =
                new CategorySpecificationRequestDTO(invalidId, validId, true);

        String errorMsg = "Category" + notFoundMsg;

        when(repository.findById(validId)).thenReturn(Optional.of(categorySpecification));
        when(categoryRepository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class,
                () -> service.update(validId, invalidRequest));

        assertEquals(errorMsg, actual.getMessage());
    }

    @Test
    void shouldThrownResourceNotFoundExceptionWhenUpdatingWithInvalidSpecificationId() {
        CategorySpecificationRequestDTO invalidRequest =
                new CategorySpecificationRequestDTO(3L, invalidId, true);

        String errorMsg = "Specification" + notFoundMsg;

        when(repository.findById(validId)).thenReturn(Optional.of(categorySpecification));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(specificationRepository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class,
                () -> service.update(validId, invalidRequest));

        assertEquals(errorMsg, actual.getMessage());
    }

    @Test
    void shouldDeleteWhenDeletingWithValidId() {
        when(repository.findById(validId)).thenReturn(Optional.of(categorySpecification));
        doNothing().when(repository).delete(categorySpecification);

        service.delete(validId);

        verify(repository).findById(validId);
        verify(repository).delete(categorySpecification);
    }

    @Test
    void shouldThrownResourceNotFoundExceptionWhenDeletingWithInvalidId() {
        when(repository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class,
                () -> service.delete(invalidId));

        assertEquals("Category Specification" + notFoundMsg, actual.getMessage());

        verify(repository).findById(invalidId);
        verify(repository, never()).delete(any(CategorySpecification.class));
    }
}
