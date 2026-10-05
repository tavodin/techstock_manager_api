package io.github.tavodin.techstock_manager.services;

import io.github.tavodin.techstock_manager.assemblers.CategoryAssembler;
import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationsInputDTO;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationsListDTO;
import io.github.tavodin.techstock_manager.dto.category.CategoryAutocompleteDTO;
import io.github.tavodin.techstock_manager.dto.category.CategoryDTO;
import io.github.tavodin.techstock_manager.dto.category.CategoryLoadDTO;
import io.github.tavodin.techstock_manager.dto.category.CategoryRequestDTO;
import io.github.tavodin.techstock_manager.entities.Category;
import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.exceptions.EntityInUseException;
import io.github.tavodin.techstock_manager.exceptions.ResourceNotFoundException;
import io.github.tavodin.techstock_manager.repositories.CategoryRepository;
import io.github.tavodin.techstock_manager.repositories.SpecificationRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @Mock
    private SpecificationRepository specRepository;

    @Mock
    private CategoryAssembler assembler;

    @Mock
    private PagedResourcesAssembler<Category> pagedAssembler;

    @InjectMocks
    private CategoryService service;

    private Long validId = 1L;
    private Long invalidId = 2L;
    private String notFoundMsg = "Category not found!";
    private String specificationsNotFound = "One or more specifications were not found";
    private String entityInUseMsg = "Category is in use and cannot be deleted";

    private Category category = new Category();
    private CategoryDTO categoryDTO;
    private CategoryRequestDTO request;
    private Specification specification;

    @BeforeEach
    void setUp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 30, 10, 0);
        LocalDateTime updatedAt = createdAt.plusHours(1L);

        category.setId(validId);
        category.setName("Monitor");
        category.setCreatedAt(createdAt);
        category.setUpdatedAt(updatedAt);

        categoryDTO = new CategoryDTO(category);
        request = new CategoryRequestDTO(category.getName(), Set.of(validId));

        specification = SpecificationBuilder.builder().build();
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

        when(repository.getAll(category.getName(), pageable)).thenReturn(page);
        when(pagedAssembler.toModel(page, assembler)).thenReturn(pagedModel);

        PagedModel<CategoryDTO> actual = service.findAll(category.getName(), pageable);

        assertNotNull(actual);
        assertEquals(pagedModel, actual);

        verify(repository).getAll(category.getName(), pageable);
        verify(pagedAssembler).toModel(page, assembler);
    }

    @Test
    void shouldTrimStringWhenFindAllWithNameWithSpaces() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> page = new PageImpl<>(List.of(category));
        PagedModel<CategoryDTO> pagedModel = mock(PagedModel.class);
        String nameWithSpaces = "  " + category.getName() + "  ";

        when(repository.getAll(category.getName(), pageable)).thenReturn(page);
        when(pagedAssembler.toModel(page, assembler)).thenReturn(pagedModel);

        service.findAll(nameWithSpaces, pageable);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(repository).getAll(captor.capture(), eq(pageable));

        assertEquals(category.getName(), captor.getValue());
    }

    @Test
    void shouldReturnNullWhenFindAllWithBlankString() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> page = new PageImpl<>(List.of(category));
        PagedModel<CategoryDTO> pagedModel = mock(PagedModel.class);

        when(repository.getAll(null, pageable)).thenReturn(page);
        when(pagedAssembler.toModel(page, assembler)).thenReturn(pagedModel);

        service.findAll("", pageable);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(repository).getAll(captor.capture(), eq(pageable));

        assertNull(captor.getValue());
    }

    @Test
    void shouldReturnCategoryAutocompleteDTOListWhenGetCategoriesToAutocomplete() {
        List<CategoryAutocompleteDTO> dto = List.of(new CategoryAutocompleteDTO(category.getId(), categoryDTO.getName()));
        when(repository.getCategoriesByName(eq(category.getName()), any(Pageable.class)))
                .thenReturn(dto);

        List<CategoryAutocompleteDTO> result = service.getCategoriesToAutocomplete(categoryDTO.getName());

        assertEquals(category.getId(), result.getFirst().id());
        assertEquals(category.getName(), result.getFirst().name());
    }

    @Test
    void shouldTrimStringWhenGetCategoriesToAutocompleteWithNameWithSpaces() {
        List<CategoryAutocompleteDTO> dto = List.of(
                new CategoryAutocompleteDTO(
                        category.getId(),
                        categoryDTO.getName()
                )
        );

        String nameWithSpaces = "   " + category.getName() + "   ";

        when(repository.getCategoriesByName(eq(category.getName()), any(Pageable.class)))
                .thenReturn(dto);

        service.getCategoriesToAutocomplete(nameWithSpaces);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(repository).getCategoriesByName(captor.capture(), any(Pageable.class));

        assertEquals(category.getName(), captor.getValue());
    }

    @Test
    void shouldReturnNullWhenGetCategoriesToAutocompleteWithBlankName() {
        List<CategoryAutocompleteDTO> dto = List.of(
                new CategoryAutocompleteDTO(
                        category.getId(),
                        categoryDTO.getName()
                )
        );

        when(repository.getCategoriesByName(eq(null), any(Pageable.class)))
                .thenReturn(dto);

        service.getCategoriesToAutocomplete("");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(repository).getCategoriesByName(captor.capture(), any(Pageable.class));

        assertNull(captor.getValue());
    }

    @Test
    void shouldReturnCategorySpecificationsListDTOWhenFindAllSpecificationByCategoryIdWithValidId() {
        when(repository.findById(validId)).thenReturn(Optional.of(category));
        when(repository.findAllSpecificationsByCategoryId(validId)).thenReturn(List.of(specification));

        List<CategorySpecificationsListDTO> result = service.findAllSpecificationByCategoryId(validId);

        assertNotNull(result);
        assertEquals(specification.getId(), result.getFirst().getSpecificationId());
        assertEquals(specification.getName(), result.getFirst().getSpecificationName());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenFindAllSpecificationByCategoryIdWithInvalidId() {
        when(repository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException result = assertThrows(ResourceNotFoundException.class, () ->
                service.findAllSpecificationByCategoryId(invalidId)
        );

        assertEquals(notFoundMsg, result.getMessage());
    }

    @Test
    void shouldReturnSpecificationListWhenFindingSpecificationsWithValidCategoryId() {
        Specification spec = SpecificationBuilder.builder().build();
        List<Specification> specList = List.of(spec);

        when(repository.findById(validId)).thenReturn(Optional.of(category));
        when(repository.findAllSpecificationsByCategoryId(validId)).thenReturn(specList);

        List<CategorySpecificationsListDTO> actual = service.findAllSpecificationByCategoryId(validId);
        CategorySpecificationsListDTO dto = actual.getFirst();

        assertTrue(actual.size() > 0);
        assertEquals(spec.getId(), dto.getSpecificationId());
        assertEquals(spec.getName(), dto.getSpecificationName());
    }

    @Test
    void shouldThrownResourceNotFoundExceptionWhenFindingSpecificationsWithInvalidId() {
        when(repository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException actual = assertThrows(ResourceNotFoundException.class,
                () -> service.findAllSpecificationByCategoryId(invalidId));

        assertEquals(notFoundMsg, actual.getMessage());
    }

    @Test
    void shouldReturnCategoryLoadDTOWhenLoadCategoryByIdWithValidId() {
        when(repository.loadCategoryById(validId)).thenReturn(Optional.of(category));

        CategoryLoadDTO result = service.loadCategoryById(validId);

        assertEquals(category.getId(), result.getId());
        assertEquals(category.getName(), result.getName());
        assertTrue(result.getSpecifications().isEmpty());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenLoadCategoryByIdWithInvalidId() {
        when(repository.loadCategoryById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException result = assertThrows(ResourceNotFoundException.class, () ->
                service.loadCategoryById(invalidId));

        assertEquals(notFoundMsg, result.getMessage());
    }

    @Test
    void shouldReturnCategoryDTOWhenSaving() {
        when(specRepository.getSpecificationByIds(Set.of(validId))).thenReturn(Set.of(specification));
        when(repository.save(any(Category.class))).thenReturn(category);
        when(assembler.toModel(category)).thenReturn(categoryDTO);

        service.save(request);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(repository).save(captor.capture());

        assertEquals(request.name(), captor.getValue().getName());
        assertTrue(captor.getValue().getSpecifications()
                .stream().anyMatch(s -> s.getName().equals(specification.getName())));
    }

    @Test
    void shouldNotCallGetSpecificationByIdsWhenSpecificationIdsIsNull() {
        CategoryRequestDTO requestDTO = new CategoryRequestDTO(category.getName(), null);

        when(repository.save(any(Category.class))).thenReturn(category);
        when(assembler.toModel(category)).thenReturn(categoryDTO);

        service.save(requestDTO);

        verify(specRepository, never()).getSpecificationByIds(anySet());
    }

    @Test
    void shouldNotCallGetSpecificationByIdsWhenSpecificationIdsIsEmpty() {
        CategoryRequestDTO requestDTO = new CategoryRequestDTO(category.getName(), Set.of());

        when(repository.save(any(Category.class))).thenReturn(category);
        when(assembler.toModel(category)).thenReturn(categoryDTO);

        service.save(requestDTO);

        verify(specRepository, never()).getSpecificationByIds(anySet());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSavingWithNullSpecificationIds() {
        when(specRepository.getSpecificationByIds(request.specificationIds()))
                .thenReturn(Set.of());

        ResourceNotFoundException result = assertThrows(ResourceNotFoundException.class, () ->
                service.save(request)
        );

        assertEquals(specificationsNotFound, result.getMessage());
    }

    @Test
    void shouldReturnCategoryDTOWhenUpdatingWithValidId() {
        CategoryRequestDTO updateRequest = new CategoryRequestDTO("Mouse", null);

        when(repository.findById(validId)).thenReturn(Optional.of(category));
        when(repository.save(any(Category.class))).thenReturn(category);
        when(assembler.toModel(category)).thenReturn(categoryDTO);

        service.update(validId, updateRequest);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(repository).save(captor.capture());

        assertEquals(category.getId(), captor.getValue().getId());
        assertEquals(updateRequest.name(), captor.getValue().getName());
        assertTrue(captor.getValue().getSpecifications().isEmpty());
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
    void shouldNotCallGetSpecificationByIdsWhenUpdatingWithNullSpecificationIds() {
        CategoryRequestDTO requestDTO = new CategoryRequestDTO(category.getName(), null);

        when(repository.findById(validId)).thenReturn(Optional.of(category));
        when(repository.save(any(Category.class))).thenReturn(category);
        when(assembler.toModel(category)).thenReturn(categoryDTO);

        service.update(validId, requestDTO);

        verify(specRepository, never()).getSpecificationByIds(anySet());
    }

    @Test
    void shouldNotCallGetSpecificationByIdsWhenUpdatingWithBlankSpecificationIds() {
        CategoryRequestDTO requestDTO = new CategoryRequestDTO(category.getName(), Set.of());

        when(repository.findById(validId)).thenReturn(Optional.of(category));
        when(repository.save(any(Category.class))).thenReturn(category);
        when(assembler.toModel(category)).thenReturn(categoryDTO);

        service.update(validId, requestDTO);

        verify(specRepository, never()).getSpecificationByIds(anySet());
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

    @Test
    void shouldReturnCategorySpecificationsInputDTOWhenFindAllSpecificationByCategoryIdsWithValidIds() {
        CategorySpecificationsInputDTO dto = new CategorySpecificationsInputDTO(
                specification.getId(), specification.getName(), specification.getDataType(), null
        );

        when(repository.findAllSpecificationsByCategoryIds(new ArrayList<>(request.specificationIds())))
                .thenReturn(List.of(dto));

        List<CategorySpecificationsInputDTO> result = service
                .findAllSpecificationByCategoryIds(new ArrayList<>(request.specificationIds()));

        assertNotNull(result);
        assertEquals(dto.specificationId(), result.getFirst().specificationId());
        assertEquals(dto.specificationName(), result.getFirst().specificationName());
        assertEquals(dto.specificationType(), result.getFirst().specificationType());
        assertEquals(dto.unitSymbol(), result.getFirst().unitSymbol());
    }
}
