package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.configurations.AbstractJpaTest;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationsInputDTO;
import io.github.tavodin.techstock_manager.dto.category.CategoryAutocompleteDTO;
import io.github.tavodin.techstock_manager.entities.Category;
import io.github.tavodin.techstock_manager.entities.Specification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryRepositoryTest extends AbstractJpaTest {

    @Autowired
    private CategoryRepository repository;

    @Autowired
    private CategorySpecificationRepository categorySpecRepository;

    @Autowired
    private SpecificationRepository specificationRepository;

    private Specification specification;
    private Category category;

    @BeforeEach
    void setUp() {
        specification = SpecificationBuilder
                .builder()
                .withId(null)
                .withUnit(null)
                .build();

        category = new Category();
        category.setName("Memória RAM");

        repository.deleteAll();
        categorySpecRepository.deleteAll();
        specificationRepository.deleteAll();
    }

    @Test
    void shouldReturnCategoriesWhenGetAllByName() {
        Category firstCategory = new Category(null, "Monitor", null, null);
        Category secondCategory = new Category(null, "Mouse", null, null);

        repository.saveAll(List.of(firstCategory, secondCategory));

        Page<Category> categories = repository.getAll("Mo", PageRequest.of(0, 5));

        Optional<Category> firstCategoryResult = categories.getContent()
                .stream()
                .filter(c -> c.getName().equals("Monitor"))
                .findFirst();

        Optional<Category> secondCategoryResult = categories.getContent()
                .stream()
                .filter(c -> c.getName().equals("Mouse"))
                .findFirst();

        assertTrue(firstCategoryResult.isPresent());
        assertTrue(secondCategoryResult.isPresent());
    }

    @Test
    void shouldReturnSpecificationListWhenFindAllSpecificationsByCategoryId() {
        Category category = new Category(null, "Memória", null, null);
        Specification specification = SpecificationBuilder
                .builder()
                .withId(null)
                .withUnit(null)
                .build();

        specification = specificationRepository.save(specification);
        category.setSpecifications(Set.of(specification));
        category = repository.save(category);

        List<Specification> specifications = repository.findAllSpecificationsByCategoryId(category.getId());

        assertTrue(!specifications.isEmpty());
        assertEquals("RAM", specifications.getFirst().getName());
        assertEquals(specification.getId(), specifications.getFirst().getId());
    }

    @Test
    void shouldReturnCategoryAutocompleteDTOListWhenGetCategoriesByName() {
        Category category = new Category(null, "Memória", null, null);
        category = repository.save(category);

        List<CategoryAutocompleteDTO> result = repository
                .getCategoriesByName("Me", PageRequest.of(0, 5));

        assertNotNull(result);
        assertEquals(category.getId(), result.getFirst().id());
        assertEquals(category.getName(), result.getFirst().name());
    }

    @Test
    void shouldReturnCategorySpecificationsInputDTOWhenFindAllSpecificationsByCategoryIds() {
        Category memoryCategory = new Category(null, "Memória", null, null);
        Category mouseCategory = new Category(null, "Mouse", null, null);

        Specification ramSpecification = SpecificationBuilder
                .builder()
                .withId(null)
                .withUnit(null)
                .build();

        Specification clockSpecification = SpecificationBuilder
                .builder()
                .withId(null)
                .withName("Clock")
                .withUnit(null)
                .build();

        Specification dpiSpecification = SpecificationBuilder
                .builder()
                .withId(null)
                .withName("DPI")
                .withUnit(null)
                .build();

        Specification pesoSpecification = SpecificationBuilder
                .builder()
                .withId(null)
                .withName("Peso")
                .withUnit(null)
                .build();

        List<Specification> memorySpecification = specificationRepository.saveAll(
                List.of(ramSpecification, clockSpecification)
        );

        List<Specification> mouseSpecifications = specificationRepository.saveAll(
                List.of(dpiSpecification, pesoSpecification)
        );

        memoryCategory.setSpecifications(new HashSet<>(memorySpecification));
        mouseCategory.setSpecifications(new HashSet<>(mouseSpecifications));

        List<Category> categories = repository.saveAll(List.of(memoryCategory, mouseCategory));
        List<Long> ids = categories.stream().map(Category::getId).toList();

        List<CategorySpecificationsInputDTO> findSpecs = repository.findAllSpecificationsByCategoryIds(ids);

        assertTrue(findSpecs.size() == 4);
        assertTrue(findSpecs.stream().anyMatch(s -> s.specificationName().equals("RAM")));
        assertTrue(findSpecs.stream().anyMatch(s -> s.specificationName().equals("Clock")));
        assertTrue(findSpecs.stream().anyMatch(s -> s.specificationName().equals("DPI")));
        assertTrue(findSpecs.stream().anyMatch(s -> s.specificationName().equals("Peso")));
    }
}
