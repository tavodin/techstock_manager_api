package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.configurations.AbstractJpaTest;
import io.github.tavodin.techstock_manager.dto.SpecificationDTO;
import io.github.tavodin.techstock_manager.entities.Specification;
import io.github.tavodin.techstock_manager.entities.Unit;
import io.github.tavodin.techstock_manager.enums.SpecificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SpecificationRepositoryTest extends AbstractJpaTest {

    @Autowired
    private SpecificationRepository specificationRepository;

    @Autowired
    private UnitRepository unitRepository;

    private Unit unit;
    private Specification entity;

    @BeforeEach
    void clean() {
        specificationRepository.deleteAll();
        unitRepository.deleteAll();
    }

    @Test
    void shouldReturnSpecificationDTOWhenGetSpecificationById() {
        unit = unitRepository.save(new Unit("Gigabyte", "GB"));
        entity = new Specification("RAM", SpecificationType.NUMBER, true, unit);
        entity = specificationRepository.save(entity);

        SpecificationDTO actual = specificationRepository.getSpecificationById(entity.getId()).get();

        assertNotNull(actual.getId());
        assertEquals(entity.getName(), entity.getName());
        assertEquals(entity.getDataType(), actual.getDataType());
        assertEquals(entity.getFilterable(), actual.getFilterable());
        assertEquals(entity.getUnit().getSymbol(), actual.getUnitSymbol());

        assertNotNull(actual.getCreatedAt());
        assertNotNull(actual.getUpdatedAt());
    }

    @Test
    void shouldReturnPageSpecificationWhenFindAllProjected() {
        unit = unitRepository.save(new Unit("Gigabyte", "GB"));
        entity = new Specification("RAM", SpecificationType.NUMBER, true, unit);
        entity = specificationRepository.save(entity);

        Page<SpecificationDTO> actual =
                specificationRepository.findAllProjected(PageRequest.of(0, 10));

        assertNotNull(actual);

        assertEquals(entity.getName(), actual.getContent().getFirst().getName());
        assertEquals(entity.getDataType(), actual.getContent().getFirst().getDataType());
        assertEquals(entity.getFilterable(), actual.getContent().getFirst().getFilterable());

        assertEquals(10, actual.getSize());
        assertEquals(1, actual.getTotalPages());
        assertEquals(0, actual.getNumber());
    }
}
