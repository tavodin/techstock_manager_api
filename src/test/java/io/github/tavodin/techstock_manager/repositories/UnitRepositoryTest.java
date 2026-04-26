package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.Unit;
import io.github.tavodin.techstock_manager.integrationtests.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UnitRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UnitRepository repository;

    private Unit unit;

    @BeforeEach
    void setUp() {
        unit = new Unit("Hertz", "Hz");
    }

    @Test
    void shouldThrowExceptionWhenSavingUnitWithNullSymbol() {
        unit.setSymbol(null);

        assertThrows(DataIntegrityViolationException.class, () -> {
            repository.saveAndFlush(unit);
        });
    }

    @Test
    void shouldAutomaticallySetCreatedAndUpdatedAtOnSave() {
        Unit savedUnit = repository.save(unit);

        assertNotNull(savedUnit.getCreatedAt());
        assertNotNull(savedUnit.getUpdatedAt());
    }

    @Test
    void shouldUpdateUpdatedAtWhenEntityIsModified() throws InterruptedException {
        Unit saved = repository.saveAndFlush(unit);

        LocalDateTime createdAt = saved.getCreatedAt();
        LocalDateTime updatedAt = saved.getUpdatedAt();

        Thread.sleep(10);

        saved.setName("Grama");
        saved.setSymbol("g");

        Unit updated = repository.saveAndFlush(saved);

        assertEquals(createdAt, updated.getCreatedAt());
        assertTrue(updated.getUpdatedAt().isAfter(updatedAt));
    }
}
