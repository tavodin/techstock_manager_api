package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
