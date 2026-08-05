package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.builder.SpecificationBuilder;
import io.github.tavodin.techstock_manager.configurations.AbstractJpaTest;
import io.github.tavodin.techstock_manager.dto.CategorySpecificationsListDTO;
import io.github.tavodin.techstock_manager.entities.Category;
import io.github.tavodin.techstock_manager.entities.CategorySpecification;
import io.github.tavodin.techstock_manager.entities.Specification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
    void shouldReturnSpecificationListWhenFindAllSpecificationsByCategoryId() {
        CategorySpecification categorySpecification = new CategorySpecification();
        Specification savedSpec = specificationRepository.save(specification);
        Category savedCategory = repository.save(category);

        categorySpecification.setCategory(savedCategory);
        categorySpecification.setSpecification(savedSpec);
        categorySpecification.setRequired(true);

        CategorySpecification savedCatSpec = categorySpecRepository.save(categorySpecification);

        List<CategorySpecificationsListDTO> list = repository.findAllSpecificationsByCategoryId(savedCategory.getId());
        CategorySpecificationsListDTO dto = list.getFirst();

        assertNotNull(list);
        assertTrue(list.size() > 0);

        assertEquals(savedCatSpec.getId(), dto.getCategorySpecificationId());
        assertEquals(savedSpec.getName(), dto.getSpecificationName());
        assertEquals(savedCatSpec.getRequired(), dto.getIsRequired());
    }

    @Test
    void shouldReturnRequiredSpecificationWhenFindRequiredSpecificationsIdsByCategoryIds() {
        Specification newSpecification = SpecificationBuilder
                .builder()
                .withId(null)
                .withName("Frequência")
                .withUnit(null)
                .build();

        List<Specification> savedSpec = specificationRepository.saveAll(List.of(specification, newSpecification));
        Category savedCategory = repository.save(category);

        CategorySpecification ramSpec = new CategorySpecification();
        ramSpec.setCategory(savedCategory);
        ramSpec.setSpecification(savedSpec.get(0));
        ramSpec.setRequired(false);

        CategorySpecification frequencySpec = new CategorySpecification();
        frequencySpec.setCategory(savedCategory);
        frequencySpec.setSpecification(savedSpec.get(1));
        frequencySpec.setRequired(true);

        List<CategorySpecification> catSpecs = categorySpecRepository.saveAll(List.of(ramSpec, frequencySpec));

        List<Long> requiredSpecs = repository
                .findRequiredSpecificationsIdsByCategoryIds(List.of(savedCategory.getId()));

        assertEquals(1, requiredSpecs.size());
        assertEquals(catSpecs.get(1).getId(), requiredSpecs.getFirst());
    }
}
