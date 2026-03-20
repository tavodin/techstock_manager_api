package io.github.tavodin.techstock_manager.repository;

import io.github.tavodin.techstock_manager.entities.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository extends JpaRepository<Unit, Long> {
}
